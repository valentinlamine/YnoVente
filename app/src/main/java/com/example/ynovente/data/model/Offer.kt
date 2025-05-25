package com.example.ynovente.data.model

data class Offer(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val endDate: String,
    val imageUrl: String? = null,
    val userId: String
)