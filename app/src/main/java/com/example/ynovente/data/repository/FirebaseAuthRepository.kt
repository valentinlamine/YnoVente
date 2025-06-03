package com.example.ynovente.data.repository

import android.app.Activity
import android.util.Log
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

    // Modifie ici ton client ID pour le debug et vérifie qu'il est bien celui avec SHA-1 debug
    private val GOOGLE_CLIENT_ID_DEBUG = "1070335553426-gmrnbafqo4l8tc7ngsukprr5lh5184se.apps.googleusercontent.com"

    fun getGoogleSignInClient(): GoogleSignInClient {
        Log.d("DEBUG_AUTH", "Création GoogleSignInClient avec clientId: $GOOGLE_CLIENT_ID_DEBUG")

        if (GOOGLE_CLIENT_ID_DEBUG.isBlank() || !GOOGLE_CLIENT_ID_DEBUG.contains("apps.googleusercontent.com")) {
            Log.e("DEBUG_AUTH", "Client ID Google mal configuré dans FirebaseAuthRepository !")
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(GOOGLE_CLIENT_ID_DEBUG)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    suspend fun login(email: String, password: String): AuthResult = try {
        Log.d("DEBUG_AUTH", "Tentative login email: $email")
        auth.signInWithEmailAndPassword(email, password).await()
        _isLoggedIn.value = auth.currentUser != null
        auth.currentUser?.let { user ->
            val token = FirebaseMessaging.getInstance().token.await()
            saveUserToDatabase(user.uid, user.displayName ?: "", user.email ?: "", token)
        }
        AuthResult(success = true)
    } catch (e: Exception) {
        Log.e("DEBUG_AUTH", "Erreur login email/password", e)
        AuthResult(success = false, errorMessage = e.localizedMessage ?: "Erreur inconnue")
    }

    suspend fun register(email: String, password: String): AuthResult = try {
        Log.d("DEBUG_AUTH", "Tentative register email: $email")
        auth.createUserWithEmailAndPassword(email, password).await()
        _isLoggedIn.value = auth.currentUser != null
        auth.currentUser?.let { user ->
            val token = FirebaseMessaging.getInstance().token.await()
            saveUserToDatabase(user.uid, user.displayName ?: "", user.email ?: "", token)
        }
        AuthResult(success = true)
    } catch (e: Exception) {
        Log.e("DEBUG_AUTH", "Erreur register", e)
        AuthResult(success = false, errorMessage = e.localizedMessage ?: "Erreur inconnue")
    }

    suspend fun updateProfileName(name: String) {
        val user = auth.currentUser
        Log.d("DEBUG_AUTH", "Mise à jour du nom: $name pour user: ${user?.uid}")
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
        Log.d("DEBUG_AUTH", "Mise à jour de l'email: $newEmail pour user: ${user?.uid}")
        user?.updateEmail(newEmail)?.await()
        user?.let {
            val token = FirebaseMessaging.getInstance().token.await()
            saveUserToDatabase(it.uid, it.displayName ?: "", newEmail, token)
        }
    }

    suspend fun loginWithGoogle(idToken: String): AuthResult {
        Log.d("DEBUG_AUTH", "Début loginWithGoogle avec idToken: $idToken")
        if (idToken.isBlank()) {
            Log.e("DEBUG_AUTH", "idToken est vide ou null !")
            return AuthResult(success = false, errorMessage = "idToken vide ou null")
        }
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            Log.d("DEBUG_AUTH", "Création credential OK")
            auth.signInWithCredential(credential).await()
            Log.d("DEBUG_AUTH", "SignInWithCredential OK, user: ${auth.currentUser?.uid}")
            _isLoggedIn.value = auth.currentUser != null
            auth.currentUser?.let { user ->
                val token = FirebaseMessaging.getInstance().token.await()
                saveUserToDatabase(user.uid, user.displayName ?: "", user.email ?: "", token)
            }
            AuthResult(success = true)
        } catch (e: Exception) {
            Log.e("DEBUG_AUTH", "Erreur loginWithGoogle", e)
            AuthResult(success = false, errorMessage = e.localizedMessage ?: "Erreur inconnue")
        }
    }


    fun logout() {
        Log.d("DEBUG_AUTH", "Déconnexion utilisateur")
        auth.signOut()
        _isLoggedIn.value = false
    }

    suspend fun reauthenticate(email: String, password: String): Boolean = try {
        val user = auth.currentUser
        Log.d("DEBUG_AUTH", "Re-authentification pour user: ${user?.uid} avec email: $email")
        if (user != null) {
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            true
        } else {
            false
        }
    } catch (e: Exception) {
        Log.e("DEBUG_AUTH", "Erreur reauthenticate", e)
        false
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun saveUserToDatabase(uid: String, name: String, email: String, fcmToken: String? = null) {
        Log.d("DEBUG_AUTH", "Sauvegarde user en base: uid=$uid, name=$name, email=$email, fcmToken=$fcmToken")
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
