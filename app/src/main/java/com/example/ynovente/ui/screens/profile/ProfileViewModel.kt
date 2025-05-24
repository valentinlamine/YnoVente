package com.example.ynovente.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {
    private val _user = MutableStateFlow<FirebaseUser?>(FirebaseAuth.getInstance().currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        _user.value = null
    }
}