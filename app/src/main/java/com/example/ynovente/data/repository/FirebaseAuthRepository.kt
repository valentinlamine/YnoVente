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
import com.google.firebase.messaging.FirebaseMessaging

class FirebaseAuthRepository(
    private val activity: Activity
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1070335553426-kcm84kc363b2ll3kvsoi56g1p7p9u5kd.apps.googleusercontent.com")
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    suspend fun login(email: String, password: String): Boolean = try {
        auth.signInWithEmailAndPassword(email, password).await()
        _isLoggedIn.value = auth.currentUser != null
        // Synchronisation de l'utilisateur à chaque connexion avec le token FCM
        auth.currentUser?.let { user ->
            val token = FirebaseMessaging.getInstance().token.await()
            saveUserToDatabase(user.uid, user.displayName ?: "", user.email ?: "", token)
        }
        true
    } catch (e: Exception) {
        false
    }

    suspend fun register(email: String, password: String): Boolean = try {
        auth.createUserWithEmailAndPassword(email, password).await()
        _isLoggedIn.value = auth.currentUser != null
        // Synchronisation de l'utilisateur à chaque inscription avec le token FCM
        auth.currentUser?.let { user ->
            val token = FirebaseMessaging.getInstance().token.await()
            saveUserToDatabase(user.uid, user.displayName ?: "", user.email ?: "", token)
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
        // Synchronisation du nom dans la base avec le token FCM
        user?.let {
            val token = FirebaseMessaging.getInstance().token.await()
            saveUserToDatabase(it.uid, name, it.email ?: "", token)
        }
    }

    suspend fun updateProfileEmail(newEmail: String) {
        val user = auth.currentUser
        user?.updateEmail(newEmail)?.await()
        // Synchronisation de l'email dans la base avec le token FCM
        user?.let {
            val token = FirebaseMessaging.getInstance().token.await()
            saveUserToDatabase(it.uid, it.displayName ?: "", newEmail, token)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Boolean = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
        _isLoggedIn.value = auth.currentUser != null
        // Synchronisation de l'utilisateur à chaque connexion Google avec le token FCM
        auth.currentUser?.let { user ->
            val token = FirebaseMessaging.getInstance().token.await()
            saveUserToDatabase(user.uid, user.displayName ?: "", user.email ?: "", token)
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

    // Ajout de la gestion du champ admin, qui reste inchangé sauf si dashboard web modifie ce champ
    suspend fun saveUserToDatabase(uid: String, name: String, email: String, fcmToken: String? = null) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        val currentSnapshot = usersRef.child(uid).get().await()
        val currentAdmin = currentSnapshot.child("admin").getValue(Boolean::class.java) ?: false
        val userMap = mutableMapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "admin" to currentAdmin // garde la valeur si déjà admin, sinon false
        )
        fcmToken?.let { userMap["fcmToken"] = it }
        usersRef.child(uid).setValue(userMap).await()
    }
}