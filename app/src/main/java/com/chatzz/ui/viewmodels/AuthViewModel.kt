package com.chatzz.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatzz.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _isSignUp = MutableStateFlow(false)
    val isSignUp: StateFlow<Boolean> = _isSignUp

    fun toggleSignUpMode() { 
        _isSignUp.value = !_isSignUp.value 
        _error.value = null
    }

    fun onEmailChange(newEmail: String) { _email.value = newEmail }

    fun sendOtp() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.signInWithOtp(_email.value)
                _isOtpSent.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyOtp(token: String, signUpPassword: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (_isSignUp.value) {
                    repository.verifySignUpOtp(_email.value, token, signUpPassword)
                } else {
                    repository.verifyLoginOtp(_email.value, token)
                }
                _isLoggedIn.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to verify OTP"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                repository.signOut()
            } finally {
                _email.value = ""
                _isOtpSent.value = false
                _isSignUp.value = false
                _isLoggedIn.value = false
                _error.value = null
            }
        }
    }
}
