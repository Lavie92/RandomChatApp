package com.lavie.randochat.model

import com.lavie.randochat.utils.Constants
import com.lavie.randochat.utils.MessageStatus
import com.lavie.randochat.utils.MessageType

data class Message(
    val id: String = "",
    val senderId: String = "",
    val content: String = "",
    val contentResId: Int? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT,
    val status: MessageStatus =  MessageStatus.SENDING
) {
    fun isSystemMessage(): Boolean = senderId == Constants.SYSTEM
}
