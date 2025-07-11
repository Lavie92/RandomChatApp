package com.lavie.randochat.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.lavie.randochat.model.Message
import com.lavie.randochat.repository.ChatRepository
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.storage
import com.lavie.randochat.utils.MessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

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

    fun sendImage(roomId: String, senderId: String, uri: Uri) {
        val fileName = UUID.randomUUID().toString()
        val storageRef = Firebase.storage.reference.child("chat_images/$fileName")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    sendImageMessage(roomId, senderId, downloadUri.toString())
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Upload image failed: ${e.message}")
            }
    }




}
