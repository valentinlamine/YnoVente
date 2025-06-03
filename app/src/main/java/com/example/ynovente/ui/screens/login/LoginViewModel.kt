package com.example.ynovente.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ynovente.data.repository.AuthResult
import com.example.ynovente.data.repository.FirebaseAuthRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class LoginViewModel(
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn

    fun login(email: String, password: String, onResult: (AuthResult) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            if (result.success) {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val token = FirebaseMessaging.getInstance().token.await()
                    authRepository.saveUserToDatabase(user.uid, user.displayName ?: "", user.email ?: email, token)
                }
            }
            onResult(result)
        }
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        return authRepository.getGoogleSignInClient()
    }

    fun firebaseAuthWithGoogle(idToken: String, onResult: (AuthResult) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.loginWithGoogle(idToken)
            onResult(result)
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
