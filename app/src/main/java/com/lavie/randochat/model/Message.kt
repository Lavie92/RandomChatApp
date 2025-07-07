package com.lavie.randochat.model

import com.lavie.randochat.utils.MessageType

data class Message(
    val id: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val type: MessageType = MessageType.TEXT
)

