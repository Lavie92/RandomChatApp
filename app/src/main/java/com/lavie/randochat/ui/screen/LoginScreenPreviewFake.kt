package com.lavie.randochat.ui.screen


import androidx.lifecycle.MutableLiveData
import com.lavie.randochat.R
import com.lavie.randochat.viewmodel.AuthViewModel
import com.lavie.randochat.repository.UserRepository
import com.lavie.randochat.model.User
import com.lavie.randochat.repository.UserRepository.UserResult


class FakeUserRepository : UserRepository {
    override suspend fun signInWithGoogle(idToken: String): User? = null

    override suspend fun saveUserToDb(user: User): UserRepository.UserResult {
        return UserRepository.UserResult.Success(user)
    }

     fun getCurrentUser(): User? = null

    override suspend fun checkUserValid(): UserRepository.UserResult {
        return UserRepository.UserResult.Error(messageId = R.string.login_error)
    }
}

val fakeViewModel = AuthViewModel(FakeUserRepository())
