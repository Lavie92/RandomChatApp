package com.lavie.randochat.repository

import com.lavie.randochat.model.ChatRoom
import com.lavie.randochat.model.User

interface UserRepository {
    suspend fun signInWithGoogle(idToken: String): UserResult?

    suspend fun saveUserToDb(user: User): UserResult?

    suspend fun checkUserValid(): UserResult?

    suspend fun getActiveRoomForUser(userId: String): ChatRoom?
    
    suspend fun registerWithEmail(email: String, password: String): UserResult?

    suspend fun loginWithEmail(email: String, password: String): UserResult?

    sealed class UserResult {
        data class Success(val user: User) : UserResult()
        data class Error(val messageId: Int?) : UserResult()
    }
}