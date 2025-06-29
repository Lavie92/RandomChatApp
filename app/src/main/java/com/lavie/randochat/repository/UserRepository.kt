package com.lavie.randochat.repository

import com.lavie.randochat.model.User
import com.lavie.randochat.repository.UserRepositoryImpl.SaveUserResult

interface UserRepository {
    suspend fun signInWithGoogle(idToken: String): User?

    suspend fun saveUserToDb(user: User): SaveUserResult

    fun getCurrentUser(): User?

    suspend fun checkUserValid(): User?
}