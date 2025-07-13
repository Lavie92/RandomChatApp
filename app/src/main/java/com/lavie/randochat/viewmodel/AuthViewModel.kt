package com.lavie.randochat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.auth.FirebaseAuth
import com.lavie.randochat.R
import com.lavie.randochat.model.ChatRoom
import com.lavie.randochat.model.User
import com.lavie.randochat.repository.UserRepository
import com.lavie.randochat.service.PreferencesService
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.utils.CacheUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class AuthViewModel(
    private val userRepository: UserRepository,
    private val firebaseMessaging: FirebaseMessaging,
    private val prefs: PreferencesService
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

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        if (user != null && _loginState.value == null) {
            viewModelScope.launch {
                checkInitialUserState(navigateOnResult = true)
            }
        }
    }

    init {
        val hasCached = restoreCachedUser()
        userRepository.addAuthStateListener(authStateListener)
        if (!hasCached) {
            checkInitialUserState(navigateOnResult = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        userRepository.removeAuthStateListener(authStateListener)
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

    private fun checkInitialUserState(navigateOnResult: Boolean) {
        viewModelScope.launch {
            when (val result = userRepository.checkUserValid()) {
                is UserRepository.UserResult.Success -> {
                    _loginState.value = result.user
                    cacheUser(result.user)
                    _errorMessageId.value = null

                    val activeRoom = userRepository.getActiveRoomForUser(result.user.id)
                    _activeRoom.value = activeRoom
                    cacheActiveRoom(activeRoom)

                    if (navigateOnResult) {
                        if (activeRoom != null) {
                            val roomId = activeRoom.id
                            _navigationEvent.emit(NavigationEvent.NavigateToChat(roomId))
                        } else {
                            _navigationEvent.emit(NavigationEvent.NavigateToStartChat)
                        }
                    }
                }

                is UserRepository.UserResult.Error -> {
                    if (result.messageId == R.string.network_error) {
                        if (_loginState.value != null) {
                            _errorMessageId.value = null
                        } else {
                            _loginState.value = null
                            _errorMessageId.value = result.messageId
                        }
                    } else {
                        _loginState.value = null
                        _errorMessageId.value = result.messageId
                    }
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
                cacheUser(saveResult.user)
                _progressMessageId.value = null
                _errorMessageId.value = null

                val activeRoom = userRepository.getActiveRoomForUser(saveResult.user.id)
                _activeRoom.value = activeRoom
                cacheActiveRoom(activeRoom)

                if (activeRoom != null) {
                    val roomId = activeRoom.id
                    _navigationEvent.emit(NavigationEvent.NavigateToChat(roomId))
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

        updateFcmTokenForCurrentUser()
    }

    private fun handleLoginError(messageId: Int) {
        _loginState.value = null
        _errorMessageId.value = messageId
        _progressMessageId.value = null
    }

    fun registerWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessageId.value = null
            _progressMessageId.value = R.string.signing_in

            when (val result = userRepository.registerWithEmail(email, password)) {
                is UserRepository.UserResult.Success -> handleSuccessfulSignIn(result.user)
                is UserRepository.UserResult.Error -> result.messageId?.let { handleLoginError(it) }
                else -> handleLoginError(R.string.login_error)
            }

            _isLoading.value = false
        }
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessageId.value = null
            _progressMessageId.value = R.string.signing_in

            when (val result = userRepository.loginWithEmail(email, password)) {
                is UserRepository.UserResult.Success -> {
                    handleSuccessfulSignIn(result.user)
                }
                is UserRepository.UserResult.Error -> {
                    result.messageId?.let { handleLoginError(it) }
                }
                else -> handleLoginError(R.string.login_error)
            }

            _isLoading.value = false
        }
    }

    private fun updateFcmTokenForCurrentUser() {
        val userId = loginState.value!!.id
        firebaseMessaging.token.addOnSuccessListener { token ->
            viewModelScope.launch {
                userRepository.updateFcmToken(userId, token)
            }
        }
    }

    private fun cacheUser(user: User) {
        prefs.putString(Constants.CACHED_USER_ID, user.id)
        prefs.putString(Constants.CACHED_USER_EMAIL, user.email)
        prefs.putString(Constants.CACHED_USER_NICKNAME, user.nickname)
    }

    private fun restoreCachedUser(): Boolean {
        val cachedUser = getCachedUser()
        if (cachedUser != null) {
            _loginState.value = cachedUser
            _errorMessageId.value = null
            val cachedRoom = getCachedActiveRoom()
            _activeRoom.value = cachedRoom
            viewModelScope.launch {
                if (cachedRoom != null) {
                    _navigationEvent.emit(NavigationEvent.NavigateToChat(cachedRoom.id))
                } else {
                    _navigationEvent.emit(NavigationEvent.NavigateToStartChat)
                }
            }
            return true
        }
        return false
    }

    private fun getCachedUser(): User? {
        val id = prefs.getString(Constants.CACHED_USER_ID, null) ?: return null
        val email = prefs.getString(Constants.CACHED_USER_EMAIL, "") ?: ""
        val nickname = prefs.getString(Constants.CACHED_USER_NICKNAME, "") ?: ""
        return User(id = id, email = email, nickname = nickname, isOnline = false)
    }

    private fun cacheActiveRoom(room: ChatRoom?) {
        if (room != null) {
            val json = CacheUtils.chatRoomToJson(room)
            prefs.putString(Constants.CACHED_ACTIVE_ROOM, json)
        } else {
            prefs.remove(Constants.CACHED_ACTIVE_ROOM)
        }
    }

    private fun getCachedActiveRoom(): ChatRoom? {
        val json = prefs.getString(Constants.CACHED_ACTIVE_ROOM, null)
        return CacheUtils.jsonToChatRoom(json)
    }

    sealed class NavigationEvent {
        data object NavigateToStartChat : NavigationEvent()
        data class NavigateToChat(val roomId: String) : NavigationEvent()
    }
}