package com.lavie.randochat.repository

import com.lavie.randochat.model.Message

interface ChatRepository {
    suspend fun sendMessage(roomId: String, message: Message): Result<Unit>

    fun listenForMessages(roomId: String, onNewMessages: (List<Message>) -> Unit)
}