package com.lavie.randochat.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.lavie.randochat.R
import com.lavie.randochat.model.ChatRoom
import kotlinx.coroutines.suspendCancellableCoroutine
import com.lavie.randochat.repository.MatchRepository.MatchResult
import com.lavie.randochat.utils.Constants
import kotlin.coroutines.resume

class MatchRepositoryImpl(
    private val database: DatabaseReference
) : MatchRepository {

    override suspend fun findUserForMatch(
        userId: String
    ): MatchResult = suspendCancellableCoroutine { cont ->
        val waitingRef = database.child(Constants.WAITING_USERS)
        val chatRoomsRef = database.child(Constants.CHAT_ROOMS)

        waitingRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val waitingList = mutableData.children.toList()
                if (waitingList.isNotEmpty()) {
                    val otherUser = waitingList.first()
                    val otherUserId = otherUser.key
                    if (otherUserId != null && otherUserId != userId) {
                        mutableData.child(otherUserId).value = null
                        // Để tạo room, tốt nhất chỉ đánh dấu ở đây, sau đó xử lý ở onComplete
                        mutableData.child(Constants.MATCHED_WITH).value = otherUserId
                        return Transaction.success(mutableData)
                    }
                }
                mutableData.child(userId).value = true
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?
            ) {
                if (error != null || !committed) {
                    cont.resume(MatchResult.Error(R.string.network_error))
                    return
                }
                val matchedWith =
                    currentData?.child(Constants.MATCHED_WITH)?.getValue(String::class.java)
                if (matchedWith != null) {
                    // Đã match thành công, tạo phòng chat
                    val roomId = chatRoomsRef.push().key
                    if (roomId != null) {

                        val chatRoom = ChatRoom(
                            id = roomId,
                            participantIds = listOf(userId, matchedWith),
                            createdAt = System.currentTimeMillis(),
                            lastUpdated = System.currentTimeMillis(),
                            isActive = true
                        )

                        chatRoomsRef.child(roomId).setValue(chatRoom).addOnSuccessListener {
                            cont.resume(MatchResult.Matched(roomId, matchedWith))
                        }.addOnFailureListener {
                            cont.resume(MatchResult.Error(R.string.match_failed))
                        }
                    } else {
                        cont.resume(MatchResult.Error(R.string.match_failed))
                    }
                } else {
                    // Đang chờ
                    cont.resume(MatchResult.Waiting)
                }
            }
        })
    }

    override suspend fun cancelWaiting(userId: String) {
        TODO("Not yet implemented")
    }
}