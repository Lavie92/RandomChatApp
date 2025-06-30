package com.lavie.randochat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lavie.randochat.R
import com.lavie.randochat.model.User
import com.lavie.randochat.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
open class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loginState = MutableLiveData<User?>()
    val loginState: LiveData<User?> = _loginState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessageId = MutableLiveData<Int?>()
    val errorMessageId: LiveData<Int?> = _errorMessageId

    private val _signInRequest = MutableSharedFlow<Unit>()
    val signInRequest = _signInRequest.asSharedFlow()

    private val _progressMessageId = MutableLiveData<Int?>()
    val progressMessageId: LiveData<Int?> = _progressMessageId

    init {
        checkInitialUserState()
    }

    fun onGoogleLoginClick() {
        viewModelScope.launch {
            _signInRequest.emit(Unit)
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessageId.value = null
            _progressMessageId.value = R.string.signing_in

            try {
                Timber.d("Starting Google login...")

                val user = userRepository.signInWithGoogle(idToken)
                if (user != null) {
                    handleSuccessfulSignIn(user)
                } else {
                    handleSignInFailure()
                }
            } catch (e: Exception) {
                Timber.e(e, "Login failed")
                handleLoginError(R.string.login_error)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun checkInitialUserState() {
        viewModelScope.launch {
            when (val result = userRepository.checkUserValid()) {
                is UserRepository.UserResult.Success -> {
                    _loginState.value = result.user
                    _errorMessageId.value = null
                }

                is UserRepository.UserResult.Error -> {
                    _loginState.value = null
                    _errorMessageId.value = result.messageId
                }

                else -> {
                    _loginState.value = null
                    _errorMessageId.value = R.string.login_error
                }
            }
        }
    }

    private suspend fun handleSuccessfulSignIn(user: User) {
        Timber.d("User signed in successfully, saving to database...")
        _progressMessageId.value = R.string.saving_user_data

        when (val saveResult = userRepository.saveUserToDb(user)) {
            is UserRepository.UserResult.Success -> {
                Timber.d("User saved to database successfully")
                _loginState.value = saveResult.user
                _progressMessageId.value = null
                _errorMessageId.value = null
            }

            is UserRepository.UserResult.Error -> {
                Timber.e("Save user failed: messageId=${saveResult.messageId}")
                handleLoginError(saveResult.messageId)
            }

            else -> {
                handleLoginError(R.string.login_error)
            }
        }
    }

    private fun handleSignInFailure() {
        Timber.e("User is null after sign in")
        handleLoginError(R.string.account_not_exist)
    }

    private fun handleLoginError(messageId: Int) {
        _loginState.value = null
        _errorMessageId.value = messageId
        _progressMessageId.value = null
    }
}