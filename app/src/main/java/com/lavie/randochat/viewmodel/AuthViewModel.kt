package com.lavie.randochat.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lavie.randochat.R
import com.lavie.randochat.model.User
import com.lavie.randochat.repository.UserRepository
import com.lavie.randochat.utils.isNetworkError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

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
        viewModelScope.launch {
            val user = userRepository.checkUserValid()
            _loginState.value = user
        }
    }

    fun onGoogleLoginClick() {
        viewModelScope.launch { _signInRequest.emit(Unit) }
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
                    Timber.d("User signed in successfully, saving to database...")
                    _progressMessageId.value = R.string.saving_user_data

                    try {
                        userRepository.saveUserToDb(user)
                        Timber.d("User saved to database successfully")
                        _loginState.value = user
                        _progressMessageId.value = null

                    } catch (dbException: Exception) {
                        Timber.e(dbException, "Failed to save user to database")
                        _loginState.value = user
                        _progressMessageId.value = null
                        _errorMessageId.value = R.string.network_error
                    }

                } else {
                    Timber.e("User is null after sign in")
                    _errorMessageId.value = R.string.account_not_exist
                    _progressMessageId.value = null
                }

            } catch (e: Exception) {
                Timber.e(e, "Login failed")
                _errorMessageId.value = if (isNetworkError(e)) {
                    R.string.network_error
                } else {
                    R.string.login_error
                }
                _progressMessageId.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}