package com.lavie.randochat.repository

import com.lavie.randochat.model.User

interface UserRepository {
    suspend fun signInWithGoogle(idToken: String): User?

    suspend fun saveUserToDb(user: User): UserResult?

    suspend fun checkUserValid(): UserResult?

    sealed class UserResult {
        data class Success(val user: User) : UserResult()
        data class Error(val messageId: Int) : UserResult()
    }
}