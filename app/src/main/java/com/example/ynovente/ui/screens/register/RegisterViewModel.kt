package com.example.ynovente.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ynovente.data.repository.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class RegisterViewModel(
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun register(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.register(email, password)
            if (result) {
                onSuccess()
            } else {
                onError("Erreur lors de la création du compte")
            }
            _isLoading.value = false
        }
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        return authRepository.getGoogleSignInClient()
    }

    fun firebaseAuthWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.loginWithGoogle(idToken)
            onResult(result)
        }
    }
}