package com.lavie.randochat.repository

import android.content.Context
import com.google.firebase.database.*
import com.lavie.randochat.model.ChatRoom
import com.lavie.randochat.model.Message
import com.lavie.randochat.utils.CommonUtils
import com.lavie.randochat.utils.Constants
import kotlinx.coroutines.tasks.await
import java.io.File
import com.lavie.randochat.utils.MessageStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import timber.log.Timber

class ChatRepositoryImpl(
    private val database: DatabaseReference,
    private val httpClient: OkHttpClient
) : ChatRepository {

    private val listeners = mutableMapOf<String, ValueEventListener>()
    private val typingListeners = mutableMapOf<String, ValueEventListener>()

    override suspend fun sendMessage(roomId: String, message: Message): Result<Unit> {
        return try {
            Timber.d("message: ${message}")
            val key = CommonUtils.generateMessageKey(roomId, message.senderId)

            val encryptedContent = CommonUtils.encryptMessage(message.content, key)

            val msgRef = database
                .child(Constants.CHAT_ROOMS)
                .child(roomId)
                .child(Constants.MESSAGES)
                .child(message.id)

            val messageMap = mapOf(
                Constants.ID to message.id,
                Constants.SENDER_ID to message.senderId,
                Constants.CONTENT_RES_ID to message.contentResId,
                Constants.CONTENT to encryptedContent,
                Constants.TIMESTAMP to ServerValue.TIMESTAMP,
                Constants.TYPE to message.type.name,
                Constants.STATUS to message.status.name
            )

            msgRef.setValue(messageMap).await()

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
        val msgRef = database.child(Constants.CHAT_ROOMS)
            .child(roomId)
            .child(Constants.MESSAGES)

        val query = msgRef
            .orderByChild(Constants.TIMESTAMP)
            .limitToLast(Constants.PAGE_SIZE_MESSAGES)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                    .sortedBy { it.timestamp }
                    .map { msg ->
                        val decrypted = decryptedMessage(msg, roomId)
                        msg.copy(content = decrypted)
                    }
                onNewMessages(messages)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        query.addValueEventListener(listener)
        listeners[roomId] = listener
        return listener
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

    override fun listenToRoomStatus(
        roomId: String,
        onStatusChanged: (Boolean) -> Unit
    ): ValueEventListener {
        val ref = database.child(Constants.CHAT_ROOMS).child(roomId).child(Constants.ACTIVE)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isActive = snapshot.getValue(Boolean::class.java) ?: true
                onStatusChanged(isActive)
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.message)
            }
        }
        ref.addValueEventListener(listener)

        return listener
    }

    override fun removeRoomStatusListener(roomId: String, listener: ValueEventListener) {
        database.child(Constants.CHAT_ROOMS).child(roomId).child(Constants.ACTIVE).removeEventListener(listener)
    }

    override suspend fun getChatType(roomId: String): String? {
        return try {
            val snapshot = database
                .child(Constants.CHAT_ROOMS)
                .child(roomId)
                .child(Constants.CHAT_TYPE)
                .get()
                .await()
            snapshot.getValue(String::class.java)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    override suspend fun endChat(roomId: String, userId: String): Result<Unit> {
        return try {
            val chatRoomSnap = database.child(Constants.CHAT_ROOMS).child(roomId).get().await()
            val participantIds = chatRoomSnap.child(Constants.PARTICIPANTS_ID).children.mapNotNull { it.getValue(String::class.java) }

            val updates = mutableMapOf<String, Any?>(
                "${Constants.CHAT_ROOMS}/$roomId/${Constants.ACTIVE}" to false
            )

            participantIds.forEach { uid ->
                updates["${Constants.USERS}/$uid/${Constants.ACTIVE_ROOM_ID}"] = null
                updates["${Constants.USERS}/$uid/${Constants.LAST_ROOM_ID}"] = roomId
            }

            database.updateChildren(updates).await()
            Result.success(Unit)
        } catch (ex: Exception) {
            Timber.e(ex, "Failed to end chat for $roomId")
            Result.failure(ex)
    override suspend fun uploadAudioToCloudinary(context: Context, file: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val uploadPreset = Constants.CLOUDINARY_AUDIO_UPLOAD_PRESET
            val url = Constants.CLOUDINARY_AUDIO_UPLOAD_URL

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(Constants.CLOUDINARY_FORM_KEY_FILE, file.name, file.asRequestBody(Constants.AUDIO_MIME_TYPE.toMediaTypeOrNull()))
                .addFormDataPart(Constants.CLOUDINARY_FORM_KEY_UPLOAD_PRESET, uploadPreset)
                .addFormDataPart(Constants.CLOUDINARY_FORM_KEY_FOLDER, Constants.CLOUDINARY_AUDIO_FOLDER)
                .build()

            val request = Request.Builder().url(url).post(requestBody).build()
            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string()
                val json = JSONObject(body!!)
                val secureUrl = json.getString(Constants.CLOUDINARY_RESPONSE_KEY_SECURE_URL)
                Result.success(secureUrl)
            } else {
                Result.failure(Exception("${Constants.CLOUDINARY_ERROR_PREFIX}${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
