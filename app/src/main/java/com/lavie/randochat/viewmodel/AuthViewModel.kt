package com.lavie.randochat.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lavie.randochat.model.User
import com.lavie.randochat.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loginState = MutableLiveData<User?>()
    val loginState: LiveData<User?> = _loginState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _signInRequest = MutableSharedFlow<Unit>()
    val signInRequest = _signInRequest.asSharedFlow()

    init {
        viewModelScope.launch {
            val user = userRepository.checkUserValid()
            _loginState.value = user
        }
    }

    fun onGoogleLoginClick() {
        Log.d("AuthViewModel", "onGoogleLoginClick called!")
        viewModelScope.launch { _signInRequest.emit(Unit) }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val user = userRepository.signInWithGoogle(idToken)
                if (user != null) {
                    userRepository.saveUserToDb(user)
                    _loginState.value = user
                } else {
                    _errorMessage.value = "Đăng nhập thất bại"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi đăng nhập: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}