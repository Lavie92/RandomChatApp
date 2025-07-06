package com.lavie.randochat.repository

import com.google.firebase.database.*
import com.lavie.randochat.R
import com.lavie.randochat.model.ChatRoom
import com.lavie.randochat.utils.ChatType
import com.lavie.randochat.utils.Constants
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MatchRepositoryImpl(
    private val database: DatabaseReference
) : MatchRepository {

    override suspend fun findUserForMatch(
        userId: String,
        chatType: ChatType
    ): MatchRepository.MatchResult =
        suspendCancellableCoroutine { cont ->
            val waitingRef = database.child(Constants.WAITING_USERS)
            val chatRoomsRef = database.child(Constants.CHAT_ROOMS)

            var matchedUserId: String? = null

            waitingRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val waitingList = mutableData.children.toList()

                    val availableUsers = when (chatType) {
                        ChatType.RANDOM -> {
                            waitingList.filter {
                                it.key != userId &&
                                        it.child(Constants.STATUS).value == Constants.STATUS_WAITING &&
                                        it.child(Constants.MATCHING).value != true &&
                                        (it.child(Constants.CHAT_TYPE).value?.toString() == ChatType.RANDOM.name)
                            }
                        }

                        ChatType.AGE -> TODO()

                        ChatType.LOCATION -> TODO()
                    }

                    if (availableUsers.isNotEmpty()) {
                        val otherUser = availableUsers.minByOrNull {
                            it.child(Constants.TIMESTAMP).getValue(Long::class.java) ?: 0L
                        }!!

                        val otherUserId = otherUser.key!!
                        matchedUserId = otherUserId

                        mutableData.child(userId).child(Constants.MATCHING).value = true
                        mutableData.child(otherUserId).child(Constants.MATCHING).value = true

                        return Transaction.success(mutableData)
                    } else {
                        val currentUser = mutableData.child(userId)
                        if (currentUser.value == null || currentUser.child(Constants.STATUS).value != Constants.STATUS_WAITING) {
                            mutableData.child(userId).child(Constants.STATUS).value =
                                Constants.STATUS_WAITING
                            mutableData.child(userId).child(Constants.TIMESTAMP).value =
                                System.currentTimeMillis()
                            mutableData.child(userId).child(Constants.MATCHING).value = false
                            mutableData.child(userId).child(Constants.CHAT_TYPE).value =
                                chatType.name
                        }
                        return Transaction.success(mutableData)
                    }
                }

                override fun onComplete(
                    error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?
                ) {
                    if (error != null) {
                        cont.resume(MatchRepository.MatchResult.Error(R.string.network_error))
                        return
                    }

                    if (!committed) {
                        cont.resume(MatchRepository.MatchResult.Error(R.string.network_error))
                        return
                    }

                    matchedUserId?.let { otherUserId ->
                        createMatchAndCleanup(
                            userId,
                            otherUserId,
                            chatType,
                            waitingRef,
                            chatRoomsRef,
                        ) { result ->
                            cont.resume(result)
                        }
                    } ?: run {
                        cont.resume(MatchRepository.MatchResult.Waiting)
                    }
                }
            })
        }

    private fun createMatchAndCleanup(
        userId: String,
        otherUserId: String,
        chatType: ChatType,
        chatRoomsRef: DatabaseReference,
        matchNotificationsRef: DatabaseReference,
        callback: (MatchRepository.MatchResult) -> Unit
    ) {
        val newRoomId = chatRoomsRef.push().key
        if (newRoomId == null) {
            callback(MatchRepository.MatchResult.Error(R.string.match_failed))
            return
        }

        val chatRoom = ChatRoom(
            id = newRoomId,
            participantIds = listOf(userId, otherUserId),
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis(),
            isActive = true,
            chatType = chatType
        )

        val matchDataForUser = mapOf(
            Constants.ROOM_ID to newRoomId,
            Constants.PARTNER_ID to otherUserId,
            Constants.TIMESTAMP to System.currentTimeMillis(),
            Constants.STATUS to Constants.STATUS_MATCHED
        )

        val matchDataForOther = mapOf(
            Constants.ROOM_ID to newRoomId,
            Constants.PARTNER_ID to userId,
            Constants.TIMESTAMP to System.currentTimeMillis(),
            Constants.STATUS to Constants.STATUS_MATCHED
        )

        val updates = hashMapOf(
            "${Constants.CHAT_ROOMS}/$newRoomId" to chatRoom,
            "${Constants.MATCH_NOTIFICATIONS}/$userId" to matchDataForUser,
            "${Constants.MATCH_NOTIFICATIONS}/$otherUserId" to matchDataForOther,
            "${Constants.WAITING_USERS}/$userId" to null,
            "${Constants.WAITING_USERS}/$otherUserId" to null
        )

        database.updateChildren(updates)
            .addOnSuccessListener {
                callback(MatchRepository.MatchResult.Matched(newRoomId, otherUserId))
            }
            .addOnFailureListener {
                matchNotificationsRef.child(userId).removeValue()
                matchNotificationsRef.child(otherUserId).removeValue()

                val rollbackUpdates = hashMapOf<String, Any>(
                    "${Constants.WAITING_USERS}/$userId/${Constants.STATUS}" to Constants.STATUS_WAITING,
                    "${Constants.WAITING_USERS}/$userId/${Constants.TIMESTAMP}" to System.currentTimeMillis(),
                    "${Constants.WAITING_USERS}/$userId/${Constants.MATCHING}" to false,
                    "${Constants.WAITING_USERS}/$otherUserId/${Constants.STATUS}" to Constants.STATUS_WAITING,
                    "${Constants.WAITING_USERS}/$otherUserId/${Constants.TIMESTAMP}" to System.currentTimeMillis(),
                    "${Constants.WAITING_USERS}/$otherUserId/${Constants.MATCHING}" to false
                )
                database.updateChildren(rollbackUpdates)

                callback(MatchRepository.MatchResult.Error(R.string.match_failed))
            }
    }

    override suspend fun createChatRoom(userA: String, userB: String): MatchRepository.MatchResult {
        val chatRoomsRef = database.child(Constants.CHAT_ROOMS)
        return try {
            val roomId = chatRoomsRef.push().key
                ?: return MatchRepository.MatchResult.Error(R.string.match_failed)
            val chatRoom = ChatRoom(
                id = roomId,
                participantIds = listOf(userA, userB),
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis(),
                isActive = true
            )

            chatRoomsRef.child(roomId).setValue(chatRoom).await()
            MatchRepository.MatchResult.Matched(roomId, userB)
        } catch (e: Exception) {
            MatchRepository.MatchResult.Error(R.string.match_failed)
        }
    }

    override suspend fun cancelWaiting(userId: String) {
        database.child(Constants.WAITING_USERS).child(userId).removeValue().await()
    }

    override fun observeMatchedUser(
        userId: String,
        onMatched: (matched: Boolean, matchedWith: String?, roomId: String?) -> Unit
    ): ValueEventListener {
        val ref = database.child(Constants.MATCH_NOTIFICATIONS).child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val status = snapshot.child(Constants.STATUS).getValue(String::class.java)
                    if (status == Constants.STATUS_MATCHED) {
                        val partnerId =
                            snapshot.child(Constants.PARTNER_ID).getValue(String::class.java)
                        val roomId = snapshot.child(Constants.ROOM_ID).getValue(String::class.java)
                        onMatched(true, partnerId, roomId)

                        snapshot.ref.removeValue()
                    }
                } else {
                    onMatched(false, null, null)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        return listener
    }

    override fun removeMatchedListener(userId: String, listener: ValueEventListener) {
        database.child(Constants.MATCH_NOTIFICATIONS).child(userId).removeEventListener(listener)
    }
}
