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
import com.google.firebase.database.FirebaseDatabase

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
        // Synchronisation de l'utilisateur à chaque connexion
        auth.currentUser?.let { user ->
            saveUserToDatabase(user.uid, user.displayName ?: "", user.email ?: "")
        }
        true
    } catch (e: Exception) {
        false
    }

    suspend fun register(email: String, password: String): Boolean = try {
        auth.createUserWithEmailAndPassword(email, password).await()
        _isLoggedIn.value = auth.currentUser != null
        // Synchronisation de l'utilisateur à chaque inscription
        auth.currentUser?.let { user ->
            saveUserToDatabase(user.uid, user.displayName ?: "", user.email ?: "")
        }
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
        // Synchronisation du nom dans la base
        user?.let {
            saveUserToDatabase(it.uid, name, it.email ?: "")
        }
    }

    suspend fun updateProfileEmail(newEmail: String) {
        val user = auth.currentUser
        user?.updateEmail(newEmail)?.await()
        // Synchronisation de l'email dans la base
        user?.let {
            saveUserToDatabase(it.uid, it.displayName ?: "", newEmail)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Boolean = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
        _isLoggedIn.value = auth.currentUser != null
        // Synchronisation de l'utilisateur à chaque connexion Google
        auth.currentUser?.let { user ->
            saveUserToDatabase(user.uid, user.displayName ?: "", user.email ?: "")
        }
        true
    } catch (e: Exception) {
        false
    }

    fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
    }

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

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun saveUserToDatabase(uid: String, name: String, email: String) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        val userMap = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email
        )
        usersRef.child(uid).setValue(userMap).await()
    }
}