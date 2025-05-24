package com.example.ynovente.ui.screens.login

import androidx.lifecycle.ViewModel
import com.example.ynovente.data.repository.FakeAuthRepository
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel(
    private val authRepository: FakeAuthRepository = FakeAuthRepository()
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        authRepository.login(email, password, onResult)
    }

    fun logout() {
        authRepository.logout()
    }
}