package com.example.ynovente.data.repository

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn

class FirebaseAuthRepository(
    private val activity: Activity
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1070335553426-9ng0lbpm288d92ar26v8kf5ck1krr78n.apps.googleusercontent.com") // Remplace par le bon client ID de la console Firebase
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    suspend fun login(email: String, password: String): Boolean = try {
        auth.signInWithEmailAndPassword(email, password).await()
        _isLoggedIn.value = true
        true
    } catch (e: Exception) {
        false
    }

    suspend fun firebaseAuthWithGoogle(idToken: String): Boolean = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
        _isLoggedIn.value = true
        true
    } catch (e: Exception) {
        false
    }

    fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
    }
}