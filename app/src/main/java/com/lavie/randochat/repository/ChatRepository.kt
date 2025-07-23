package com.lavie.randochat.repository

import android.content.Context
import com.google.firebase.database.ValueEventListener
import com.lavie.randochat.model.Message
import com.lavie.randochat.utils.MessageStatus
import java.io.File

interface ChatRepository {
    suspend fun sendMessage(roomId: String, message: Message): Result<Unit>

    fun listenForMessages(
        roomId: String, limit: Int,
        startAfter: Long? = null, onNewMessages: (List<Message>) -> Unit
    ): ValueEventListener

    fun removeMessageListener(roomId: String, listener: ValueEventListener)

    suspend fun getPreviousMessages(
        roomId: String,
        limit: Int,
        startAfter: Long
    ): List<Message>

    suspend fun updateMessageStatus(roomId: String, messageId: String, status: MessageStatus): Result<Unit>

    suspend fun updateTypingStatus(roomId: String, userId: String, isTyping: Boolean): Result<Unit>

    fun listenForTyping(
        roomId: String,
        myUserId: String,
        onTyping: (Boolean) -> Unit
    ): ValueEventListener

    fun removeTypingListener(roomId: String, listener: ValueEventListener)

    suspend fun getChatType(roomId: String): String?

    suspend fun uploadAudioToCloudinary(context: Context, file: File): Result<String>

}