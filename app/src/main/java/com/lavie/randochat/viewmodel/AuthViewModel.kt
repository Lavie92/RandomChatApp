package com.lavie.randochat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.lavie.randochat.R
import com.lavie.randochat.model.User
import com.lavie.randochat.repository.UserRepository
import com.lavie.randochat.service.PreferencesService
import com.lavie.randochat.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class AuthViewModel(
    private val userRepository: UserRepository,
    private val firebaseMessaging: FirebaseMessaging,
    private val prefs: PreferencesService
) : ViewModel() {

    private var hasCheckedInitialState = false

    private val _loginState = MutableStateFlow<User?>(null)
    val loginState: StateFlow<User?> = _loginState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessageId = MutableStateFlow<Int?>(null)
    val errorMessageId: StateFlow<Int?> = _errorMessageId

    private val _signInRequest = MutableSharedFlow<Unit>()
    val signInRequest = _signInRequest.asSharedFlow()

    private val _progressMessageId = MutableStateFlow<Int?>(null)
    val progressMessageId: StateFlow<Int?> = _progressMessageId

    private val _activeRoomId = MutableStateFlow<String?>(null)
    val activeRoomId: StateFlow<String?> = _activeRoomId

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        if (user != null && _loginState.value == null && !hasCheckedInitialState) {
            viewModelScope.launch {
                hasCheckedInitialState = true
                checkInitialUserState()
                Timber.d("🧪 AuthStateListener fired: loginState=${_loginState.value}, hasChecked=$hasCheckedInitialState")
            }
        }
    }

    init {
        userRepository.addAuthStateListener(authStateListener)
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
                when (val result = userRepository.signInWithGoogle(idToken)) {
                    is UserRepository.UserResult.Success -> handleSuccessfulSignIn(result.user)
                    is UserRepository.UserResult.Error -> result.messageId?.let { handleLoginError(it) }
                    else -> handleLoginError(R.string.login_error)
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
            val cachedUser = getCachedUser()
            val cachedRoom = getCachedRoomId()

            when (val result = userRepository.checkUserValid()) {
                is UserRepository.UserResult.Success -> {
                    val user = result.user
                    _loginState.value = user
                    cacheUser(user)

                    val roomId = userRepository.getActiveOrLastRoom(user.id)
                    if (roomId != null) {
                        _activeRoomId.value = roomId
                        cacheRoomId(roomId)
                        _navigationEvent.emit(NavigationEvent.NavigateToChat(roomId))
                    } else {
                        _navigationEvent.emit(NavigationEvent.NavigateToStartChat)
                    }
                }

                is UserRepository.UserResult.Error -> {
                    if (cachedUser != null) {
                        _loginState.value = cachedUser

                        if (cachedRoom != null) {
                            _activeRoomId.value = cachedRoom
                            _navigationEvent.emit(NavigationEvent.NavigateToChat(cachedRoom))
                        } else {
                            _navigationEvent.emit(NavigationEvent.NavigateToStartChat)
                            Timber.d("➡️ LoginCheckSplash Failed")
                        }
                    } else {
                        _errorMessageId.value = result.messageId ?: R.string.login_error
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
        _progressMessageId.value = R.string.saving_user_data

        when (val saveResult = userRepository.saveUserToDb(user)) {
            is UserRepository.UserResult.Success -> {
                _loginState.value = saveResult.user
                cacheUser(saveResult.user)
                _progressMessageId.value = null
                _errorMessageId.value = null

                val activeRoom = userRepository.getActiveOrLastRoom(saveResult.user.id)
                _activeRoomId.value = activeRoom
                cacheRoomId(activeRoom)

                if (activeRoom != null) {
                    _navigationEvent.emit(NavigationEvent.NavigateToChat(activeRoom))
                } else {
                    _navigationEvent.emit(NavigationEvent.NavigateToStartChat)
                }
            }

            is UserRepository.UserResult.Error -> {
                saveResult.messageId?.let { handleLoginError(it) }
            }

            else -> handleLoginError(R.string.login_error)
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
                is UserRepository.UserResult.Success -> handleSuccessfulSignIn(result.user)
                is UserRepository.UserResult.Error -> result.messageId?.let { handleLoginError(it) }
                else -> handleLoginError(R.string.login_error)
            }

            _isLoading.value = false
        }
    }

    private fun updateFcmTokenForCurrentUser() {
        val userId = loginState.value?.id ?: return
        firebaseMessaging.token.addOnSuccessListener { token ->
            viewModelScope.launch {
                userRepository.addFcmToken(userId, token)
            }
        }
    }

    private fun cacheUser(user: User) {
        prefs.putString(Constants.CACHED_USER_ID, user.id)
        prefs.putString(Constants.CACHED_USER_EMAIL, user.email)
        prefs.putString(Constants.CACHED_USER_NICKNAME, user.nickname)
    }

    suspend fun restoreCachedUser(): Boolean = withContext(Dispatchers.IO) {
        Timber.d("🔁 [restoreCachedUser] Called")

        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return@withContext false
        val userId = firebaseUser.uid
        val user = userRepository.getUserById(userId) ?: return@withContext false
        val roomId = userRepository.getActiveOrLastRoom(userId) ?: return@withContext false

        Timber.d("📦 [restoreCachedUser] roomId = $roomId")

        withContext(Dispatchers.Main) {
            _loginState.value = user
            _activeRoomId.value = roomId
            _navigationEvent.emit(NavigationEvent.NavigateToChat(roomId))
            hasCheckedInitialState = true
        }

        return@withContext true
    }


    fun hasCachedUser(): Boolean {
        return getCachedUser() != null
    }

    private fun getCachedUser(): User? {
        val id = prefs.getString(Constants.CACHED_USER_ID, null) ?: return null
        val email = prefs.getString(Constants.CACHED_USER_EMAIL, "") ?: ""
        val nickname = prefs.getString(Constants.CACHED_USER_NICKNAME, "") ?: ""
        return User(id = id, email = email, nickname = nickname, isOnline = false)
    }

    private fun cacheRoomId(roomId: String?) {
        if (roomId != null) {
            prefs.putString(Constants.CACHED_ROOM_ID, roomId)
        } else {
            prefs.remove(Constants.CACHED_ROOM_ID)
        }
    }

    fun clearActiveRoom() {
        _activeRoomId.value = null
        cacheRoomId(null)
    }

    private fun getCachedRoomId(): String? {
        return prefs.getString(Constants.CACHED_ROOM_ID, null)
    }

    suspend fun getActiveRoomIdOnly(): String? {
        val userId = loginState.value?.id ?: return null
        return userRepository.getActiveRoomId(userId)
    }

    sealed class NavigationEvent {
        data object NavigateToStartChat : NavigationEvent()
        data class NavigateToChat(val roomId: String) : NavigationEvent()
    }
}
