package com.example.ynovente.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAuthRepository {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        // Simule un login réussi si email et password non vides
        _isLoggedIn.value = email.isNotBlank() && password.isNotBlank()
        onResult(_isLoggedIn.value)
    }

    fun logout() {
        _isLoggedIn.value = false
    }
}