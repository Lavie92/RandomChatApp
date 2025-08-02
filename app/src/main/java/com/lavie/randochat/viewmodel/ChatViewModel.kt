package com.lavie.randochat.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ValueEventListener
import com.lavie.randochat.R
import com.lavie.randochat.localdata.datasource.MessageCacheDataSource
import com.lavie.randochat.model.Message
import com.lavie.randochat.repository.ChatRepository
import com.lavie.randochat.repository.ImageFileRepository
import com.lavie.randochat.ui.component.VoiceRecordState
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.utils.MessageStatus
import com.lavie.randochat.utils.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.UUID

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val imageFileRepository: ImageFileRepository,
    private val messageCacheDataSource: MessageCacheDataSource
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages
    private val sentWelcomeMessages = mutableSetOf<String>()
    private var isEndReached = false
    private var oldestTimestamp: Long? = null
    private var currentRoomId: String? = null
    private val pageSize = Constants.PAGE_SIZE_MESSAGES
    private var messagesListener: ValueEventListener? = null
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping
    private var _chatType = MutableStateFlow("")
    val chatType: StateFlow<String> = _chatType
    
 	private val _voiceRecordState = MutableStateFlow<VoiceRecordState>(VoiceRecordState.Idle)
    val voiceRecordState: StateFlow<VoiceRecordState> = _voiceRecordState

    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    private val _isSendingImage = MutableStateFlow(false)
    private var _isChatRoomEnded = MutableStateFlow(false)
    val isChatRoomEnded = _isChatRoomEnded
    private var roomStatusListener: ValueEventListener? = null

    fun loadInitialMessages(roomId: String) {
        currentRoomId = roomId
        isEndReached = false
        oldestTimestamp = null

        viewModelScope.launch {
            val cachedMessages = messageCacheDataSource.getCachedMessages(roomId)
            _messages.value = cachedMessages
            oldestTimestamp = _messages.value.minByOrNull { it.timestamp }?.timestamp
            isEndReached = cachedMessages.size < pageSize
        }

        startRealtimeMessageListener(roomId)
    }

    fun startRealtimeMessageListener(roomId: String) {
        removeMessageListener()

        messagesListener = chatRepository.listenForMessages(
            roomId = roomId
        ) { newMessages ->
            val combined = (_messages.value + newMessages)
                .associateBy { it.id }
                .values
                .sortedBy { it.timestamp }

            _messages.value = combined

            oldestTimestamp = _messages.value.minByOrNull { it.timestamp }?.timestamp
            cacheMessages(roomId, _messages.value)
        }
    }

    fun loadMoreMessages(onLoaded: (addedCount: Int) -> Unit = {}) {
        if (_isLoadingMore.value || isEndReached || currentRoomId == null || oldestTimestamp == null) return

        _isLoadingMore.value = true

        viewModelScope.launch {
            try {
                val olderMessages = chatRepository.getPreviousMessages(
                    roomId = currentRoomId!!,
                    limit = pageSize,
                    startAfter = oldestTimestamp!!
                )

                val sorted = olderMessages.sortedBy { it.timestamp }

                if (sorted.isEmpty()) {
                    isEndReached = true
                    return@launch
                }

                val combined = (sorted + _messages.value)
                    .associateBy { it.id }
                    .values
                    .sortedBy { it.timestamp }

                _messages.value = combined
                oldestTimestamp = _messages.value.minByOrNull { it.timestamp }?.timestamp

                cacheMessages(currentRoomId!!, _messages.value)
                onLoaded(sorted.size)
            } catch (e: Exception) {
                Timber.e(e, "Failed to load more messages")
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun updateTypingStatus(roomId: String, userId: String, isTyping: Boolean) {
        viewModelScope.launch {
            chatRepository.updateTypingStatus(roomId, userId, isTyping)
        }
    }

    fun startTypingListener(roomId: String, myUserId: String): ValueEventListener {
        return chatRepository.listenForTyping(roomId, myUserId) { typing ->
            _isTyping.value = typing
        }
    }

    fun removeTypingListener(roomId: String, listener: ValueEventListener) {
        chatRepository.removeTypingListener(roomId, listener)
    }

    fun sendTextMessage(roomId: String, senderId: String, content: String) {
        sendMessage(roomId, senderId, content, MessageType.TEXT)
    }

    fun sendImageMessage(roomId: String, senderId: String, imageUrl: String) {
        sendMessage(roomId, senderId, imageUrl, MessageType.IMAGE)
    }

    private fun sendMessage(
        roomId: String,
        senderId: String,
        content: String,
        type: MessageType,
        status: MessageStatus = MessageStatus.SENDING
    ) {
        val messageId = UUID.randomUUID().toString()
        val message = Message(
            id = messageId,
            senderId = senderId,
            content = content,
            type = type,
            status = status
        )
        _messages.value += message

        currentRoomId?.let { cacheMessages(it, _messages.value) }
        viewModelScope.launch {
            val result = chatRepository.sendMessage(roomId, message)
            if (result.isSuccess) {
                chatRepository.updateMessageStatus(roomId, message.id, MessageStatus.SENT)
                _messages.value = _messages.value.map {
                    if (it.id == message.id) it.copy(status = MessageStatus.SENT) else it
                }
                currentRoomId?.let { cacheMessages(it, _messages.value) }
            }
        }
    }

    fun markMessagesAsSeen(roomId: String, myUserId: String, messages: List<Message>) {
        viewModelScope.launch {
            messages
                .filter { it.senderId != myUserId && it.status == MessageStatus.SENT }
                .forEach { msg ->
                    chatRepository.updateMessageStatus(roomId, msg.id, MessageStatus.SEEN)
                }
        }
    }

    fun sendSystemMessage(roomId: String, messageId: Int) {
        if (roomId !in sentWelcomeMessages) {
            val welcomeMsg = Message(
                id = UUID.randomUUID().toString(),
                senderId = Constants.SYSTEM,
                content = "",
                contentResId = messageId,
                timestamp = System.currentTimeMillis(),
                type = MessageType.TEXT,
                status = MessageStatus.SENT
            )
            viewModelScope.launch {
                chatRepository.sendMessage(roomId, welcomeMsg)
                sentWelcomeMessages.add(roomId)
            }
        }
    }

    private fun cacheMessages(roomId: String, messages: List<Message>) {
        viewModelScope.launch {
            messageCacheDataSource.cacheMessages(roomId, messages)
        }
    }

    private fun removeMessageListener() {
        currentRoomId?.let { roomId ->
            messagesListener?.let { chatRepository.removeMessageListener(roomId, it) }
        }
        messagesListener = null
    }

    fun loadChatType(roomId: String) {
        viewModelScope.launch {
            val type = chatRepository.getChatType(roomId)
            _chatType.value = type.toString()
        }
    }

    fun resetChatState() {
        _isChatRoomEnded.value = false
    }

    fun endChat(roomId: String, userId: String) {
        viewModelScope.launch {
            try {
                sendSystemMessage(roomId, R.string.chat_ended)

                val result = chatRepository.endChat(roomId, userId)
                if (result.isSuccess) {
                    _isChatRoomEnded.value = true
                    Timber.d("Chat Ended. activeRoomId cleared. lastRoomId set to $roomId")
                }
            } catch (ex: Exception) {
                Timber.e(ex, "Error when ending chat")
     		}
        }
    }
    
    fun sendImage(roomId: String, senderId: String, uri: Uri, context: Context) {
        val localId = UUID.randomUUID().toString()
        val localMessage = Message(
            id = localId,
            senderId = senderId,
            content = uri.toString(),
            type = MessageType.IMAGE,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENDING
        )
        addLocalMessage(localMessage)

        _isSendingImage.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val compressedFile = imageFileRepository.compressImage(context, uri)
                val result = imageFileRepository.uploadImageToCloudinary(context, Uri.fromFile(compressedFile))

                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        val downloadUrl = result.getOrNull()!!
                        val sentMessage = localMessage.copy(content = downloadUrl, status = MessageStatus.SENT)
                        chatRepository.sendMessage(roomId, sentMessage)
                    } else {
                        updateMessageStatus(localId, MessageStatus.FAILED)
                    }
                    _isSendingImage.value = false
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    updateMessageStatus(localId, MessageStatus.FAILED)
                    _isSendingImage.value = false
                }
            }
        }
    }

    fun listenToRoomStatus(roomId: String) {
        roomStatusListener?.let {
            chatRepository.removeRoomStatusListener(roomId, it)
        }

        roomStatusListener = chatRepository.listenToRoomStatus(roomId) { isActive ->
            val ended = !isActive
            _isChatRoomEnded.value = ended

            if (ended) {
                Timber.d("Detected chat end. Waiting for user action.")
            }
        }
    }

    fun clearListeners() {
        messagesListener?.let {
            currentRoomId?.let { roomId ->
                chatRepository.removeMessageListener(roomId, it)
            }
        }
        roomStatusListener?.let {
            currentRoomId?.let { roomId ->
                chatRepository.removeRoomStatusListener(roomId, it)
            }
        }
        messagesListener = null
        roomStatusListener = null
    }

    private fun addLocalMessage(message: Message) {
        _messages.value = _messages.value + message
    }

    private fun updateMessageStatus(messageId: String, status: MessageStatus) {
        _messages.value = _messages.value.map {
            if (it.id == messageId) it.copy(status = status) else it
        }
    }

    fun downloadImage(context: Context, imageUrl: String, onResult: (Boolean) -> Unit) {
        imageFileRepository.saveImageToGallery(context, imageUrl, onResult)
    }

    fun startRecording(context: Context, onPermissionDenied: () -> Unit) {
        if (_voiceRecordState.value != VoiceRecordState.Idle) return

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            onPermissionDenied()
            return
        }

        try {
            val file = File(
                context.cacheDir,
                "${Constants.AUDIO_FILE_PREFIX}${System.currentTimeMillis()}${Constants.AUDIO_FILE_EXTENSION}"
            )
            audioFile = file
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            _voiceRecordState.value = VoiceRecordState.Recording
        } catch (_: Exception) {
            _voiceRecordState.value = VoiceRecordState.Locked
        }
    }

    fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            audioFile?.let { file ->
                _voiceRecordState.value = VoiceRecordState.Recorded(file)
            }
        } catch (_: Exception) {
            _voiceRecordState.value = VoiceRecordState.Locked
        }
    }

    fun cancelRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            audioFile?.delete()
            audioFile = null
        } catch (e: Exception) {
            Timber.e(e)
        }
        _voiceRecordState.value = VoiceRecordState.Idle
    }

    fun sendVoiceMessageOptimistic(context: Context, roomId: String, userId: String) {
        val file = (voiceRecordState.value as? VoiceRecordState.Recorded)?.file ?: return

        val localId = UUID.randomUUID().toString()
        val localMessage = Message(
            id = localId,
            senderId = userId,
            content = file.absolutePath,
            type = MessageType.VOICE,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENDING
        )
        addLocalMessage(localMessage)

        viewModelScope.launch(Dispatchers.IO) {
            val uploadResult = chatRepository.uploadAudioToCloudinary(context, file)
            if (uploadResult.isSuccess) {
                val url = uploadResult.getOrNull()!!
                val sentMessage = localMessage.copy(content = url, status = MessageStatus.SENT)
                chatRepository.sendMessage(roomId, sentMessage)
                updateMessageStatus(localId, MessageStatus.SENT, url)
            } else {
                updateMessageStatus(localId, MessageStatus.FAILED)
            }

        }
        _voiceRecordState.value = VoiceRecordState.Idle
    }

    private fun updateMessageStatus(localId: String, status: MessageStatus, newContent: String? = null) {
        _messages.update { msgs ->
            msgs.map {
                if (it.id == localId)
                    it.copy(status = status, content = newContent ?: it.content)
                else it
            }
        }
    }
}
