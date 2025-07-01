package com.lavie.randochat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.identity.util.UUID
import com.lavie.randochat.model.Message
import com.lavie.randochat.model.MessageType
import com.lavie.randochat.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun startListening(conversationId: String) {
        chatRepository.listenForMessages(conversationId) { newMessages ->
            _messages.value = newMessages.sortedBy { it.timestamp }
        }
    }

    fun sendTextMessage(conversationId: String, senderId: String, receiverId: String, content: String) {
        sendMessage(conversationId, senderId, receiverId, content, MessageType.TEXT)
    }

    fun sendImageMessage(conversationId: String, senderId: String, receiverId: String, imageUrl: String) {
        sendMessage(conversationId, senderId, receiverId, imageUrl, MessageType.IMAGE)
    }

    fun sendVoiceMessage(conversationId: String, senderId: String, receiverId: String, audioUrl: String) {
        sendMessage(conversationId, senderId, receiverId, audioUrl, MessageType.VOICE)
    }

    private fun sendMessage(
        conversationId: String,
        senderId: String,
        receiverId: String,
        content: String,
        type: MessageType
    ) {
        val message = Message(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            timestamp = System.currentTimeMillis(),
            type = type
        )
        viewModelScope.launch { chatRepository.sendMessage(conversationId, message) }
    }
}
