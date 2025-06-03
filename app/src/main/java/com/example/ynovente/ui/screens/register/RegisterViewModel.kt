package com.example.ynovente.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ynovente.data.repository.AuthResult
import com.example.ynovente.data.repository.FirebaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class RegisterViewModel(
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun register(
        name: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.register(email, password)
            if (result.success) {
                authRepository.updateProfileName(name)
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val token = FirebaseMessaging.getInstance().token.await()
                    authRepository.saveUserToDatabase(user.uid, name, user.email ?: email, token)
                }
                onSuccess()
            } else {
                onError(result.errorMessage ?: "Erreur lors de la création du compte")
            }
            _isLoading.value = false
        }
    }


    fun getGoogleSignInClient(): GoogleSignInClient {
        return authRepository.getGoogleSignInClient()
    }

    fun firebaseAuthWithGoogle(idToken: String, onResult: (AuthResult) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.loginWithGoogle(idToken)
            if (result.success) {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    authRepository.saveUserToDatabase(
                        user.uid,
                        user.displayName ?: "",
                        user.email ?: ""
                    )
                }
            }
            onResult(result)
        }
    }
}