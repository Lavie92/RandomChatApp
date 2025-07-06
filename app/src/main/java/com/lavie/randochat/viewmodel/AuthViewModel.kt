package com.lavie.randochat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lavie.randochat.R
import com.lavie.randochat.model.ChatRoom
import com.lavie.randochat.model.User
import com.lavie.randochat.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<User?>(null)
    val loginState: StateFlow<User?> = _loginState

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessageId = MutableStateFlow<Int?>(null)
    val errorMessageId: StateFlow<Int?> = _errorMessageId

    private val _signInRequest = MutableSharedFlow<Unit>()
    val signInRequest = _signInRequest.asSharedFlow()

    private val _progressMessageId = MutableStateFlow<Int?>(null)
    val progressMessageId: StateFlow<Int?> = _progressMessageId

    private val _activeRoom = MutableStateFlow<ChatRoom?>(null)
    val activeRoom: StateFlow<ChatRoom?> = _activeRoom

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

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

                when (val result = userRepository.signInWithGoogle(idToken)) {
                    is UserRepository.UserResult.Success -> {
                        handleSuccessfulSignIn(result.user)
                    }

                    is UserRepository.UserResult.Error -> {
                        result.messageId?.let { handleLoginError(it) }
                    }

                    else -> {
                        handleLoginError(R.string.login_error)
                    }
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

                    val activeRoom = userRepository.getActiveRoomForUser(result.user.id)
                    _activeRoom.value = activeRoom

                    if (activeRoom != null) {
                        val partnerId = activeRoom.participantIds.first { it != result.user.id }
                        _navigationEvent.emit(NavigationEvent.NavigateToChat(partnerId))
                    } else {
                        _navigationEvent.emit(NavigationEvent.NavigateToStartChat)
                    }
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

                val activeRoom = userRepository.getActiveRoomForUser(saveResult.user.id)
                _activeRoom.value = activeRoom

                if (activeRoom != null) {
                    val partnerId = activeRoom.participantIds.first { it != saveResult.user.id }
                    _navigationEvent.emit(NavigationEvent.NavigateToChat(partnerId))
                } else {
                    _navigationEvent.emit(NavigationEvent.NavigateToStartChat)
                }
            }

            is UserRepository.UserResult.Error -> {
                Timber.e("Save user failed: messageId=${saveResult.messageId}")
                saveResult.messageId?.let { handleLoginError(it) }
            }

            else -> {
                handleLoginError(R.string.login_error)
            }
        }
    }

    private fun handleLoginError(messageId: Int) {
        _loginState.value = null
        _errorMessageId.value = messageId
        _progressMessageId.value = null
    }

    sealed class NavigationEvent {
        data object NavigateToStartChat : NavigationEvent()
        data class NavigateToChat(val partnerId: String) : NavigationEvent()
    }
}