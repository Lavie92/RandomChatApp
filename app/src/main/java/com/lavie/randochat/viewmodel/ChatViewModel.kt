package com.lavie.randochat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lavie.randochat.model.Message
import com.lavie.randochat.repository.ChatRepository
import com.google.firebase.database.ValueEventListener
import com.lavie.randochat.utils.MessageStatus
import com.lavie.randochat.utils.MessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.lavie.randochat.R
import com.lavie.randochat.utils.Constants

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages
    private val sentWelcomeMessages = mutableSetOf<String>()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    fun startListening(roomId: String): ValueEventListener {
        return chatRepository.listenForMessages(roomId) { newMessages ->
            _messages.value = newMessages.sortedBy { it.timestamp }
        }
    }

    fun removeMessageListener(roomId: String, listener: ValueEventListener) {
        chatRepository.removeMessageListener(roomId, listener)
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
        viewModelScope.launch {
            val result = chatRepository.sendMessage(roomId, message)
            if (result.isSuccess) {
                chatRepository.updateMessageStatus(roomId, message.id, MessageStatus.SENT)
                _messages.value = _messages.value.map {
                    if (it.id == message.id) it.copy(status = MessageStatus.SENT) else it
                }
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

}
