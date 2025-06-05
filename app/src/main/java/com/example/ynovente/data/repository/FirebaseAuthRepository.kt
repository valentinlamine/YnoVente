package com.example.ynovente.data.repository

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
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
import com.google.firebase.messaging.FirebaseMessaging

data class AuthResult(
    val success: Boolean,
    val errorMessage: String? = null
)

class FirebaseAuthRepository(
    private val activity: Activity
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val GOOGLE_CLIENT_ID = "1070335553426-9ng0lbpm288d92ar26v8kf5ck1krr78n.apps.googleusercontent.com"

    @RequiresApi(Build.VERSION_CODES.P)
    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(GOOGLE_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()

        return GoogleSignIn.getClient(activity, gso)
    }

    suspend fun login(email: String, password: String): AuthResult = try {
        auth.signInWithEmailAndPassword(email, password).await()
        _isLoggedIn.value = auth.currentUser != null
        auth.currentUser?.let { user ->
            val token = FirebaseMessaging.getInstance().token.await()
            saveUserToDatabase(user.uid, user.displayName ?: "", user.email ?: "", token)
        }
        AuthResult(success = true)
    } catch (e: Exception) {
        AuthResult(success = false, errorMessage = e.localizedMessage ?: "Erreur inconnue")
    }

    suspend fun register(email: String, password: String): AuthResult = try {
        auth.createUserWithEmailAndPassword(email, password).await()
        _isLoggedIn.value = auth.currentUser != null
        auth.currentUser?.let { user ->
            val token = FirebaseMessaging.getInstance().token.await()
            saveUserToDatabase(user.uid, user.displayName ?: "", user.email ?: "", token)
        }
        AuthResult(success = true)
    } catch (e: Exception) {
        AuthResult(success = false, errorMessage = e.localizedMessage ?: "Erreur inconnue")
    }

    suspend fun updateProfileName(name: String) {
        val user = auth.currentUser
        user?.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
        )?.await()
        user?.let {
            val token = FirebaseMessaging.getInstance().token.await()
            saveUserToDatabase(it.uid, name, it.email ?: "", token)
        }
    }

    suspend fun updateProfileEmail(newEmail: String) {
        val user = auth.currentUser
        user?.updateEmail(newEmail)?.await()
        user?.let {
            val token = FirebaseMessaging.getInstance().token.await()
            saveUserToDatabase(it.uid, it.displayName ?: "", newEmail, token)
        }
    }

    suspend fun loginWithGoogle(idToken: String): AuthResult {
        if (idToken.isBlank()) {
            return AuthResult(success = false, errorMessage = "Token Google vide")
        }

        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()

            authResult.user?.let { user ->
                val token = try {
                    FirebaseMessaging.getInstance().token.await()
                } catch (e: Exception) {
                    null
                }
                saveUserToDatabase(user.uid, user.displayName ?: "", user.email ?: "", token)
                _isLoggedIn.value = true
                AuthResult(success = true)
            } ?: AuthResult(success = false, errorMessage = "Échec de la connexion Google")
        } catch (e: Exception) {
            AuthResult(success = false, errorMessage = e.localizedMessage ?: "Erreur inconnue")
        }
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

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun saveUserToDatabase(uid: String, name: String, email: String, fcmToken: String? = null) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        val userMap = mutableMapOf(
            "uid" to uid,
            "name" to name,
            "email" to email
        )
        fcmToken?.let { userMap["fcmToken"] = it }
        usersRef.child(uid).setValue(userMap).await()
    }
}