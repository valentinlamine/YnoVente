package com.example.ynovente.ui.screens.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.example.ynovente.data.repository.FirebaseAuthRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            onResult(result)
        }
    }

    /**
     * Fournit un GoogleSignInClient prêt à l'emploi pour la Google Sign-In Intent.
     * Remplace "YOUR_WEB_CLIENT_ID" par ton web client ID (console Firebase, Auth > Google).
     */
    fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1070335553426-9ng0lbpm288d92ar26v8kf5ck1krr78n.apps.googleusercontent.com")
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    fun firebaseAuthWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.firebaseAuthWithGoogle(idToken)
            onResult(result)
        }
    }

    fun logout() {
        authRepository.logout()
    }
}