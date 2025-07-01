package com.lavie.randochat.model

enum class MessageType {
    TEXT, IMAGE, AUDIO
}

enum class MessageStatus {
    SENDING, SENT, RECEIVED, READ, FAILED
}

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val type: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.SENT
)
