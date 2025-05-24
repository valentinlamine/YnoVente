package com.example.ynovente.data.model

data class Bid(
    val id: String,
    val offerId: String,          // l'offre associée
    val user: User,               // l'utilisateur ayant surenchéri
    val amount: Double,
    val date: String              // ISO String, pour compatibilité Firebase
)