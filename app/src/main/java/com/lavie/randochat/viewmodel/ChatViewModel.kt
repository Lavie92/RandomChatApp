package com.lavie.randochat.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage
import com.lavie.randochat.model.Message
import com.lavie.randochat.model.MessageStatus
import com.lavie.randochat.repository.ChatRepository
import com.lavie.randochat.repository.ImageFileRepository
import com.lavie.randochat.ui.component.VoiceRecordState
import com.lavie.randochat.utils.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.UUID

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val imageFileRepository: ImageFileRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _voiceRecordState = MutableStateFlow<VoiceRecordState>(VoiceRecordState.Idle)
    val voiceRecordState: StateFlow<VoiceRecordState> = _voiceRecordState

    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    private val _isSendingImage = MutableStateFlow(false)
    val isSendingImage: StateFlow<Boolean> = _isSendingImage

    fun startListening(roomId: String): ValueEventListener {
        return chatRepository.listenForMessages(roomId) { newMessages ->
            _messages.value = newMessages.sortedBy { it.timestamp }
        }
    }

    fun removeMessageListener(roomId: String, listener: ValueEventListener) {
        chatRepository.removeMessageListener(roomId, listener)
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
        type: MessageType
    ) {
        val message = Message(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            content = content,
            timestamp = System.currentTimeMillis(),
            type = type
        )
        viewModelScope.launch { chatRepository.sendMessage(roomId, message) }
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
        val totalStart = System.currentTimeMillis()

        viewModelScope.launch(Dispatchers.IO) {
            uploadImageFileToFirebase(uri, roomId, senderId, localId) {
                val totalElapsed = System.currentTimeMillis() - totalStart
                Timber.d("✅ Total end-to-end time: $totalElapsed ms")
                _isSendingImage.value = false
            }
        }
    }

    private fun uploadImageFileToFirebase(
        uri: Uri,
        roomId: String,
        senderId: String,
        localId: String,
        onComplete: () -> Unit
    ) {
        val storageRef = Firebase.storage.reference.child("chat_images/${System.currentTimeMillis()}.jpg")
        val startUpload = System.currentTimeMillis()

        storageRef.putFile(uri)
            .addOnSuccessListener {
                val elapsedUpload = System.currentTimeMillis() - startUpload
                Timber.d("Upload completed in $elapsedUpload ms")

                val startDownloadUrl = System.currentTimeMillis()
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val elapsedDownloadUrl = System.currentTimeMillis() - startDownloadUrl
                    Timber.d("Download URL retrieval took $elapsedDownloadUrl ms")

                    sendImageMessage(roomId, senderId, downloadUri.toString())
                    updateMessageStatus(localId, MessageStatus.SENT)
                    onComplete()
                }
            }
            .addOnFailureListener {
                Timber.d("Upload failed after ${System.currentTimeMillis() - startUpload} ms")
                updateMessageStatus(localId, MessageStatus.FAILED)
                onComplete()
            }
    }

    private fun addLocalMessage(message: Message) {
        _messages.value = _messages.value + message
    }

    private fun updateMessageStatus(messageId: String, status: MessageStatus) {
        _messages.value = _messages.value.map {
            if (it.id == messageId) it.copy(status = status) else it
        }
    }

    fun downloadImage(context: Context, imageUrl: String) {
        imageFileRepository.saveImageToGallery(context, imageUrl)
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
            val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
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
        } catch (e: Exception) {
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
        } catch (e: Exception) {
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
            // ignore
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
            val uploadResult = chatRepository.uploadAudioFile(context, file)
            if (uploadResult.isSuccess) {
                val url = uploadResult.getOrNull()!!
                val sentMessage = localMessage.copy(
                    content = url,
                    status = MessageStatus.SENT,
                    id = UUID.randomUUID().toString()
                )
                chatRepository.sendMessage(roomId, sentMessage)
                updateMessageStatus(localId, MessageStatus.SENT, url)
            } else {
                updateMessageStatus(localId, MessageStatus.FAILED)
            }
            _voiceRecordState.value = VoiceRecordState.Idle
        }
        _voiceRecordState.value = VoiceRecordState.Idle
    }

    fun updateMessageStatus(localId: String, status: MessageStatus, newContent: String? = null) {
        _messages.update { msgs ->
            msgs.map {
                if (it.id == localId)
                    it.copy(status = status, content = newContent ?: it.content)
                else it
            }
        }
    }


}