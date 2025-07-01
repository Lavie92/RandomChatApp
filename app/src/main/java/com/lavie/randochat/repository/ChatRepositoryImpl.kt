package com.lavie.randochat.repository

import com.google.firebase.database.*
import com.lavie.randochat.model.Message
import com.lavie.randochat.utils.Constants
import kotlinx.coroutines.tasks.await

class ChatRepositoryImpl(
    private val database: DatabaseReference
) : ChatRepository {

    override suspend fun sendMessage(roomId: String, message: Message): Result<Unit> {
        return try {
            val msgRef = database
                .child(Constants.CHATS)
                .child(roomId)
                .child(Constants.MESSAGES)
                .child(message.id)
            msgRef.setValue(message).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun listenForMessages(roomId: String, onNewMessages: (List<Message>) -> Unit) {
        val msgRef = database.child(Constants.CHATS).child(roomId).child(Constants.MESSAGES)
        msgRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                onNewMessages(messages)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
