package com.lavie.randochat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lavie.randochat.model.Message
import com.lavie.randochat.repository.ChatRepository
import com.lavie.randochat.service.PreferencesService
import com.lavie.randochat.utils.CacheUtils
import com.google.firebase.database.ValueEventListener
import com.lavie.randochat.utils.MessageStatus
import com.lavie.randochat.utils.MessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.lavie.randochat.R
import com.lavie.randochat.utils.Constants
import timber.log.Timber

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val prefs: PreferencesService
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

    fun loadInitialMessages(roomId: String) {
        removeMessageListener()

        currentRoomId = roomId
        isEndReached = false
        oldestTimestamp = null
        val cachedJson = prefs.getString(Constants.CACHED_MESSAGES_PREFIX + roomId, null)
        val cachedMessages = CacheUtils.jsonToMessages(cachedJson)
        _messages.value = cachedMessages.sortedBy { it.timestamp }
        oldestTimestamp = _messages.value.minByOrNull { it.timestamp }?.timestamp
        isEndReached = cachedMessages.size < pageSize

        messagesListener = chatRepository.listenForMessages(
            roomId, limit = pageSize, startAfter = null
        ) { newMessages ->
            val sorted = newMessages.sortedBy { it.timestamp }
            _messages.value = sorted
            oldestTimestamp = sorted.minByOrNull { it.timestamp }?.timestamp
            isEndReached = sorted.size < pageSize
            cacheMessages(roomId, sorted)
        }
    }

    fun loadMoreMessages(onLoaded: (addedCount: Int) -> Unit = {}) {
        if (_isLoadingMore.value || isEndReached || currentRoomId == null || oldestTimestamp == null) return

        _isLoadingMore.value = true
        viewModelScope.launch {
            try {
                val result = chatRepository.getPreviousMessages(
                    currentRoomId!!, pageSize, oldestTimestamp!!
                )
                val sorted = result.sortedBy { it.timestamp }
                if (result.size < pageSize) isEndReached = true

                val added = sorted.size

                _messages.value = (sorted + _messages.value).associateBy { it.id }.values.sortedBy { it.timestamp }
                oldestTimestamp = _messages.value.minByOrNull { it.timestamp }?.timestamp
                cacheMessages(currentRoomId!!, _messages.value)

                onLoaded(added)
            }
            catch (e: Exception) {
                Timber.d(e)
            }
            finally {
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

    fun sendVoiceMessage(roomId: String, senderId: String, audioUrl: String) {
        sendMessage(roomId, senderId, audioUrl, MessageType.VOICE)
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
            timestamp = System.currentTimeMillis(),
            type = type,
            status = status
        )
        _messages.value = _messages.value + message
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

    fun sendWelcomeMessage(roomId: String) {
        if (roomId !in sentWelcomeMessages) {
            val welcomeMsg = Message(
                id = UUID.randomUUID().toString(),
                senderId = Constants.SYSTEM,
                contentResId = R.string.welcome_notice,
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
        prefs.putString(
            Constants.CACHED_MESSAGES_PREFIX + roomId,
            CacheUtils.messagesToJson(messages)
        )
    }

    fun removeMessageListener() {
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
}
