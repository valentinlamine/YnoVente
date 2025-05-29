package com.example.ynovente.data.repository

import com.example.ynovente.data.model.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository {
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")

    suspend fun getUserById(userId: String): User? {
        val snapshot = usersRef.child(userId).get().await()
        return snapshot.getValue(User::class.java)
    }
}