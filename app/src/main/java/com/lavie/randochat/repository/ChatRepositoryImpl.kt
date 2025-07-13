package com.lavie.randochat.repository

import com.google.firebase.database.*
import com.lavie.randochat.model.Message
import com.lavie.randochat.utils.CommonUtils
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.utils.MessageStatus
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class ChatRepositoryImpl(
    private val database: DatabaseReference
) : ChatRepository {

    private val listeners = mutableMapOf<String, ValueEventListener>()
    private val typingListeners = mutableMapOf<String, ValueEventListener>()

    override suspend fun sendMessage(roomId: String, message: Message): Result<Unit> {
        return try {

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
        limit: Int,
        startAfter: Long?,
        onNewMessages: (List<Message>) -> Unit
    ): ValueEventListener {
        val msgRef = database.child(Constants.CHAT_ROOMS)
            .child(roomId)
            .child(Constants.MESSAGES)

        val query = if (startAfter == null) {
            msgRef.orderByChild(Constants.TIMESTAMP)
                .limitToLast(limit)
        } else {
            msgRef.orderByChild(Constants.TIMESTAMP)
                .endAt(startAfter.toDouble() - 1)
                .limitToLast(limit)
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                    .sortedBy { it.timestamp }
                    .map { msg ->
                        val decryptedContent = decryptedMessage(msg, roomId)
                        msg.copy(content = decryptedContent)
                    }

                onNewMessages(messages)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        query.addValueEventListener(listener)
        listeners[roomId] = listener
        return listener
    }

    override fun removeMessageListener(roomId: String, listener: ValueEventListener) {
        database.child(Constants.CHAT_ROOMS).child(roomId).child(Constants.MESSAGES).removeEventListener(listener)
        listeners.remove(roomId)
    }

    override suspend fun updateMessageStatus(roomId: String, messageId: String, status: MessageStatus): Result<Unit> {
        return try {
            val statusRef = database.child(Constants.CHAT_ROOMS)
                .child(roomId)
                .child(Constants.MESSAGES)
                .child(messageId)
                .child(Constants.STATUS)
            statusRef.setValue(status.name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPreviousMessages(
        roomId: String,
        limit: Int,
        startAfter: Long
    ): List<Message> {
        val msgRef = database.child(Constants.CHAT_ROOMS)
            .child(roomId)
            .child(Constants.MESSAGES)

        return try {
            val snapshot = msgRef.orderByChild(Constants.TIMESTAMP)
                .endAt(startAfter.toDouble() - 1)
                .limitToLast(limit)
                .get()
                .await()

            snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                .sortedBy { it.timestamp }
                .map { msg ->
                    val decryptedContent = decryptedMessage(msg, roomId)
                    msg.copy(content = decryptedContent)
                }
        } catch (e: Exception) {
            Timber.d(e)
            emptyList()
        }
    }

    private fun decryptedMessage(message: Message, roomId: String) : String {
        val key = CommonUtils.generateMessageKey(roomId, message.senderId)

        return try {
            CommonUtils.decryptMessage(message.content, key)
        } catch (e: Exception) {
            message.content
        }
    }
        
    override suspend fun updateTypingStatus(roomId: String, userId: String, isTyping: Boolean): Result<Unit> {
        return try {
            val typingRef = database.child(Constants.CHAT_ROOMS)
                .child(roomId)
                .child(Constants.TYPING)
                .child(userId)
            typingRef.setValue(isTyping).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun listenForTyping(
        roomId: String,
        myUserId: String,
        onTyping: (Boolean) -> Unit
    ): ValueEventListener {
        val typingRef = database.child(Constants.CHAT_ROOMS).child(roomId).child(Constants.TYPING)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val otherTyping = snapshot.children.any { child ->
                    child.key != myUserId && child.getValue(Boolean::class.java) == true
                }
                onTyping(otherTyping)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        typingRef.addValueEventListener(listener)
        typingListeners[roomId] = listener
        return listener
    }

    override fun removeTypingListener(roomId: String, listener: ValueEventListener) {
        database.child(Constants.CHAT_ROOMS).child(roomId).child(Constants.TYPING).removeEventListener(listener)
        typingListeners.remove(roomId)
    }
}
