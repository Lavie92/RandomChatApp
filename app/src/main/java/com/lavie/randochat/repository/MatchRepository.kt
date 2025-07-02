package com.lavie.randochat.repository

import com.google.firebase.database.DatabaseReference

interface MatchRepository {
    suspend fun findUserForMatch(userId: String): MatchResult
    suspend fun cancelWaiting(userId: String)

    sealed class MatchResult {
        data class Matched(val roomId: String, val otherUserId: String) : MatchResult()
        object Waiting : MatchResult()
        data class Error(val messageId: Int) : MatchResult()
        object Cancelled : MatchResult()
    }

}