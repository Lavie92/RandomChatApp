package com.lavie.randochat.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage
import com.lavie.randochat.model.Message
import com.lavie.randochat.model.MessageStatus
import com.lavie.randochat.repository.ChatRepository
import com.lavie.randochat.repository.ImageFileRepository
import com.lavie.randochat.utils.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.system.measureTimeMillis

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val imageFileRepository: ImageFileRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

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

    fun sendVoiceMessage(roomId: String, senderId: String, audioUrl: String) {
        sendMessage(roomId, senderId, audioUrl, MessageType.VOICE)
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

    private fun decodeAndCompressImage(uri: Uri, context: Context): ByteArray {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
        options.inSampleSize = calculateInSampleSize(options, 1024, 1024)
        options.inJustDecodeBounds = false

        Timber.d("Original size: ${options.outWidth}x${options.outHeight}, target: 1024x1024, chosen inSampleSize: ${options.inSampleSize}")

        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        return baos.toByteArray()
    }

//    private fun uploadImageToFirebase(
//        data: ByteArray,
//        roomId: String,
//        senderId: String,
//        localId: String,
//        onComplete: () -> Unit
//    ) {
//        val storageRef = Firebase.storage.reference.child("chat_images/${System.currentTimeMillis()}.jpg")
//        val startUpload = System.currentTimeMillis()
//
//        storageRef.putBytes(data)
//            .addOnSuccessListener {
//                val elapsedUpload = System.currentTimeMillis() - startUpload
//                Timber.d("Upload completed in $elapsedUpload ms")
//
//                val startDownloadUrl = System.currentTimeMillis()
//                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
//                    val elapsedDownloadUrl = System.currentTimeMillis() - startDownloadUrl
//                    Timber.d("Download URL retrieval took $elapsedDownloadUrl ms")
//
//                    sendImageMessage(roomId, senderId, downloadUri.toString())
//                    updateMessageStatus(localId, MessageStatus.SENT)
//                    onComplete()
//                }
//            }
//            .addOnFailureListener {
//                Timber.d("Upload failed after ${System.currentTimeMillis() - startUpload} ms")
//                updateMessageStatus(localId, MessageStatus.FAILED)
//                onComplete()
//            }
//    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        Timber.d("Original size: ${width}x${height}, target: ${reqWidth}x${reqHeight}, chosen inSampleSize: $inSampleSize")
        return inSampleSize
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
}