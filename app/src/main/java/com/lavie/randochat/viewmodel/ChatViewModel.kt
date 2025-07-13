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
import timber.log.Timber

class ChatViewModel(
    private val chatRepository: ChatRepository
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


    fun loadInitialMessages(roomId: String) {
        removeMessageListener()

        currentRoomId = roomId
        isEndReached = false
        oldestTimestamp = null
        _messages.value = emptyList()

        messagesListener = chatRepository.listenForMessages(
            roomId, limit = pageSize, startAfter = null
        ) { newMessages ->
            val sorted = newMessages.sortedBy { it.timestamp }
            _messages.value = sorted
            oldestTimestamp = sorted.minByOrNull { it.timestamp }?.timestamp
            isEndReached = sorted.size < pageSize
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

    fun removeMessageListener() {
        currentRoomId?.let { roomId ->
            messagesListener?.let { chatRepository.removeMessageListener(roomId, it) }
        }
        messagesListener = null
    }
}
