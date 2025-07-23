package com.lavie.randochat.repository

import com.google.firebase.database.ValueEventListener
import com.lavie.randochat.utils.ChatType

interface MatchRepository {
    suspend fun findUserForMatch(userId: String, chatType: ChatType): MatchResult

    suspend fun cancelWaiting(userId: String)

    fun observeMatchedUser(
        userId: String,
        onMatched: (matched: Boolean, matchedWith: String?, roomId: String?) -> Unit
    ): ValueEventListener

    fun removeMatchedListener(userId: String, listener: ValueEventListener)

    sealed class MatchResult {
        data class Matched(val roomId: String, val otherUserId: String) : MatchResult()
        object Waiting : MatchResult()
        data class Error(val messageId: Int) : MatchResult()
        object Cancelled : MatchResult()
    }
}