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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.EmailAuthProvider

class FirebaseAuthRepository(
    private val activity: Activity
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1070335553426-9ng0lbpm288d92ar26v8kf5ck1krr78n.apps.googleusercontent.com")
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    suspend fun login(email: String, password: String): Boolean = try {
        auth.signInWithEmailAndPassword(email, password).await()
        _isLoggedIn.value = auth.currentUser != null
        true
    } catch (e: Exception) {
        false
    }

    suspend fun register(email: String, password: String): Boolean = try {
        auth.createUserWithEmailAndPassword(email, password).await()
        _isLoggedIn.value = auth.currentUser != null
        true
    } catch (e: Exception) {
        false
    }

    suspend fun updateProfileName(name: String) {
        val user = auth.currentUser
        user?.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
        )?.await()
    }

    suspend fun loginWithGoogle(idToken: String): Boolean = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
        _isLoggedIn.value = auth.currentUser != null
        true
    } catch (e: Exception) {
        false
    }

    fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
    }

    // Ajout : fonction de ré-authentification (peut servir ailleurs)
    suspend fun reauthenticate(email: String, password: String): Boolean = try {
        val user = auth.currentUser
        if (user != null) {
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            true
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}