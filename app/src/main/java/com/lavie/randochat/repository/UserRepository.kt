package com.lavie.randochat.repository

import com.lavie.randochat.model.User

interface UserRepository {
    suspend fun signInWithGoogle(idToken: String): User?

    suspend fun saveUserToDb(user: User)

    fun getCurrentUser(): User?

    suspend fun checkUserValid(): User?
}