package com.lavie.randochat.repository

import android.content.Context
import com.google.firebase.database.ValueEventListener
import com.lavie.randochat.model.Message
import java.io.File

interface ChatRepository {
    suspend fun sendMessage(roomId: String, message: Message): Result<Unit>

    fun listenForMessages(roomId: String, onNewMessages: (List<Message>) -> Unit): ValueEventListener

    fun removeMessageListener(roomId: String, listener: ValueEventListener)

    suspend fun uploadAudioFile(context: Context, file: File): Result<String>
}