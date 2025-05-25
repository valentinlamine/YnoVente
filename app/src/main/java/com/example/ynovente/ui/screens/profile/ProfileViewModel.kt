package com.example.ynovente.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val _user = MutableStateFlow<FirebaseUser?>(FirebaseAuth.getInstance().currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Pour la reconnexion
    private val _needsReauth = MutableStateFlow(false)
    val needsReauth: StateFlow<Boolean> = _needsReauth.asStateFlow()
    private var pendingPassword: String? = null
    private var pendingAction: (() -> Unit)? = null

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        _user.value = null
    }

    fun updateDisplayName(newName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onError("Utilisateur introuvable")
            return
        }
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(newName).build()).addOnCompleteListener {
                    _isLoading.value = false
                    if (it.isSuccessful) {
                        _user.value = FirebaseAuth.getInstance().currentUser
                        onSuccess()
                    } else {
                        onError(it.exception?.localizedMessage ?: "Erreur lors de la modification du nom")
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.localizedMessage ?: "Erreur lors de la modification du nom")
            }
        }
    }

    fun updatePassword(newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onError("Utilisateur introuvable")
            return
        }
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                user.updatePassword(newPassword).addOnCompleteListener {
                    _isLoading.value = false
                    if (it.isSuccessful) {
                        onSuccess()
                    } else {
                        val message = it.exception?.localizedMessage ?: "Erreur lors de la modification du mot de passe"
                        // Vérifie si reconnexion nécessaire
                        if (message.contains("recent authentication", ignoreCase = true)) {
                            pendingPassword = newPassword
                            pendingAction = { updatePassword(newPassword, onSuccess, onError) }
                            _needsReauth.value = true
                        } else {
                            onError(message)
                        }
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.localizedMessage ?: "Erreur lors de la modification du mot de passe")
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onError("Utilisateur introuvable")
            return
        }
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                user.delete().addOnCompleteListener {
                    _isLoading.value = false
                    if (it.isSuccessful) {
                        _user.value = null
                        onSuccess()
                    } else {
                        val message = it.exception?.localizedMessage ?: "Erreur lors de la suppression du compte"
                        if (message.contains("recent authentication", ignoreCase = true)) {
                            pendingAction = { deleteAccount(onSuccess, onError) }
                            _needsReauth.value = true
                        } else {
                            onError(message)
                        }
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.localizedMessage ?: "Erreur lors de la suppression du compte")
            }
        }
    }

    fun reauthenticate(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onError("Utilisateur introuvable")
            return
        }
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val credential = EmailAuthProvider.getCredential(email, password)
                user.reauthenticate(credential).addOnCompleteListener { task ->
                    _isLoading.value = false
                    if (task.isSuccessful) {
                        _needsReauth.value = false
                        // Relance l'action en attente si besoin
                        pendingAction?.invoke()
                        pendingAction = null
                        pendingPassword = null
                        onSuccess()
                    } else {
                        onError(task.exception?.localizedMessage ?: "Erreur lors de la ré-authentification")
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                onError(e.localizedMessage ?: "Erreur lors de la ré-authentification")
            }
        }
    }
}