package com.lavie.randochat.repository

import com.google.firebase.database.*
import com.lavie.randochat.model.Message
import com.lavie.randochat.utils.CommonUtils
import com.lavie.randochat.utils.Constants
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class ChatRepositoryImpl(
    private val database: DatabaseReference
) : ChatRepository {

    private val listeners = mutableMapOf<String, ValueEventListener>()

    override suspend fun sendMessage(roomId: String, message: Message): Result<Unit> {
        return try {
            Timber.d("message: ${message}")
            val key = CommonUtils.generateMessageKey(roomId, message.senderId)

            val encryptedContent = CommonUtils.encryptMessage(message.content, key)

            val encryptedMessage = message.copy(content = encryptedContent)

            val msgRef = database
                .child(Constants.CHAT_ROOMS)
                .child(roomId)
                .child(Constants.MESSAGES)
                .child(message.id)
            msgRef.setValue(encryptedMessage).await()

            val roomRef = database.child(Constants.CHAT_ROOMS).child(roomId)
            roomRef.updateChildren(
                mapOf(
                    Constants.LAST_MESSAGE to encryptedContent,
                    Constants.LAST_UPDATED to message.timestamp
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun listenForMessages(
        roomId: String,
        onNewMessages: (List<Message>) -> Unit
    ): ValueEventListener {
        val msgRef = database.child(Constants.CHAT_ROOMS).child(roomId).child(Constants.MESSAGES)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull {val msg = it.getValue(Message::class.java)
                    msg?.let {
                        val key = CommonUtils.generateMessageKey(roomId, msg.senderId)
                        val decryptedContent = try {
                            CommonUtils.decryptMessage(msg.content, key)
                        } catch (e: Exception) {
                            msg.content
                        }
                        msg.copy(content = decryptedContent)
                    }
                }

                onNewMessages(messages)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        msgRef.addValueEventListener(listener)
        listeners[roomId] = listener
        return listener
    }

    override fun removeMessageListener(roomId: String, listener: ValueEventListener) {
        database.child(Constants.CHAT_ROOMS).child(roomId).child(Constants.MESSAGES).removeEventListener(listener)
        listeners.remove(roomId)
    }
}
