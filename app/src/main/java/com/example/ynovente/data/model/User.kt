package com.example.ynovente.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val fcmToken: String? = null,
    val admin: Boolean = false
)