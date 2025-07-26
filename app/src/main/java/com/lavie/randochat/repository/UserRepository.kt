package com.lavie.randochat.repository

import com.lavie.randochat.model.ChatRoom
import com.lavie.randochat.model.User
import com.google.firebase.auth.FirebaseAuth

interface UserRepository {
    suspend fun signInWithGoogle(idToken: String): UserResult?

    suspend fun saveUserToDb(user: User): UserResult?

    suspend fun checkUserValid(): UserResult?

    suspend fun getActiveRoomForUser(userId: String): ChatRoom?
    
    suspend fun registerWithEmail(email: String, password: String): UserResult?

    suspend fun loginWithEmail(email: String, password: String): UserResult?

    suspend fun addFcmToken(userId: String, token: String)

    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener)

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener)

    suspend fun getChatRoomStatus(roomId: String): Boolean?

    suspend fun getNavigableActiveRoomForUser(userId: String): ChatRoom?

    suspend fun getActiveOrLastRoom(userId: String): String?

    sealed class UserResult {
        data class Success(val user: User) : UserResult()
        data class Error(val messageId: Int?) : UserResult()
    }
}