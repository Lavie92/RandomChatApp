package com.lavie.randochat.repository

import com.google.firebase.database.ValueEventListener
import com.lavie.randochat.model.Message
import com.lavie.randochat.utils.MessageStatus

interface ChatRepository {
    suspend fun sendMessage(roomId: String, message: Message): Result<Unit>

    fun listenForMessages(
        roomId: String, limit: Int,
        startAfter: Long? = null, onNewMessages: (List<Message>) -> Unit
    ): ValueEventListener

    fun removeMessageListener(roomId: String, listener: ValueEventListener)

    suspend fun updateMessageStatus(
        roomId: String,
        messageId: String,
        status: MessageStatus
    ): Result<Unit>

    suspend fun getPreviousMessages(
        roomId: String,
        limit: Int,
        startAfter: Long
    ): List<Message>

}