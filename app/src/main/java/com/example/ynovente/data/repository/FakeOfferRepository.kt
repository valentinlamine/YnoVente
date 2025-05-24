package com.example.ynovente.data.repository

import com.example.ynovente.data.model.Offer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeOfferRepository {
    private val offers = listOf(
        Offer(id = "1", title = "Vélo de course", description = "Super vélo", price = 120.0, endDate = "2023-12-31"),
        Offer(id = "2", title = "PC Portable", description = "Gamer", price = 650.0, endDate = "2023-11-30"),
        Offer(id = "3", title = "Livre Kotlin", description = "Pour apprendre", price = 15.0, endDate = "2023-10-15"),
        Offer(id = "4", title = "Console de jeux", description = "Dernier modèle", price = 300.0, endDate = "2023-12-15"),
        Offer(id = "5", title = "Table de jardin", description = "En bois massif", price = 200.0, endDate = "2024-01-01"),
        Offer(id = "6", title = "Smartphone", description = "Dernier modèle", price = 800.0, endDate = "2023-11-20"),
        Offer(id = "7", title = "Appareil photo", description = "Reflex numérique", price = 450.0, endDate = "2023-12-05"),
        Offer(id = "8", title = "Chaise de bureau", description = "Ergonomique", price = 75.0, endDate = "2023-10-30"),
        Offer(id = "9", title = "Montre connectée", description = "Pour le sport", price = 250.0, endDate = "2023-11-15"),
        Offer(id = "10", title = "Sac à dos", description = "Pour l'école", price = 40.0, endDate = "2023-10-20"),
        Offer(id = "11", title = "Tente de camping", description = "4 personnes", price = 150.0, endDate = "2024-02-01"),
        Offer(id = "12", title = "Grill électrique", description = "Pour barbecue", price = 100.0, endDate = "2023-12-10"),
        Offer(id = "13", title = "Casque audio", description = "Sans fil", price = 120.0, endDate = "2023-11-25"),
        Offer(id = "14", title = "Batterie externe", description = "Pour smartphone", price = 30.0, endDate = "2023-10-25"),
        Offer(id = "15", title = "Jeu vidéo", description = "Action RPG", price = 60.0, endDate = "2023-11-05")
    )

    fun getOffers(): Flow<List<Offer>> = flowOf(offers)
}