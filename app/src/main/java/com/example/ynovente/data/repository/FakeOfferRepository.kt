package com.example.ynovente.data.repository

import com.example.ynovente.data.model.Bid
import com.example.ynovente.data.model.Offer
import com.example.ynovente.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

class FakeOfferRepository {
    private val users = listOf(
        User("1", "Alice", "alice@mail.com", "password"),
        User("2", "Bob", "bob@mail.com", "password"),
    )

    private val bids = listOf(
        Bid(
            id = "b1",
            offerId = "1",
            user = users[0],
            amount = 110.0,
            date = "2025-05-23T12:00:00"
        ),
        Bid(
            id = "b2",
            offerId = "1",
            user = users[1],
            amount = 120.0,
            date = "2025-05-23T13:00:00"
        ),
        Bid(
            id = "b3",
            offerId = "2",
            user = users[0],
            amount = 600.0,
            date = "2025-05-23T14:00:00"
        ),
        Bid(
            id = "b4",
            offerId = "2",
            user = users[1],
            amount = 650.0,
            date = "2025-05-23T15:00:00"
        ),
        Bid(
            id = "b5",
            offerId = "3",
            user = users[0],
            amount = 10.0,
            date = "2025-05-23T16:00:00"
        ),
        Bid(
            id = "b6",
            offerId = "3",
            user = users[1],
            amount = 15.0,
            date = "2025-05-23T17:00:00"
        ),
        Bid(
            id = "b7",
            offerId = "4",
            user = users[0],
            amount = 280.0,
            date = "2025-05-23T18:00:00"
        ),
        Bid(
            id = "b8",
            offerId = "4",
            user = users[1],
            amount = 300.0,
            date = "2025-05-23T19:00:00"
        ),
        Bid(
            id = "b9",
            offerId = "5",
            user = users[0],
            amount = 180.0,
            date = "2025-05-23T20:00:00"
        ),
        Bid(
            id = "b10",
            offerId = "5",
            user = users[1],
            amount = 200.0,
            date = "2025-05-23T21:00:00"
        )
    )

    private val _offers = MutableStateFlow<List<Offer>>(
        listOf(
            Offer(id = "1", title = "Vélo de course", description = "Super vélo", price = 120.0, endDate = "2023-12-31", user = users[0]),
            Offer(id = "2", title = "PC Portable", description = "Gamer", price = 650.0, endDate = "2023-11-30", user = users[1]),
            Offer(id = "3", title = "Livre Kotlin", description = "Pour apprendre", price = 15.0, endDate = "2023-10-15", user = users[0]),
            Offer(id = "4", title = "Console de jeux", description = "Dernier modèle", price = 300.0, endDate = "2023-12-15", user = users[1]),
            Offer(id = "5", title = "Table de jardin", description = "En bois massif", price = 200.0, endDate = "2024-01-01", user = users[0]),
            Offer(id = "6", title = "Smartphone", description = "Dernier modèle", price = 800.0, endDate = "2023-11-20", user = users[1]),
            Offer(id = "7", title = "Appareil photo", description = "Reflex numérique", price = 450.0, endDate = "2023-12-05", user = users[0]),
            Offer(id = "8", title = "Chaise de bureau", description = "Ergonomique", price = 75.0, endDate = "2023-10-30", user = users[1]),
            Offer(id = "9", title = "Montre connectée", description = "Pour le sport", price = 250.0, endDate = "2023-11-15", user = users[0]),
            Offer(id = "10", title = "Sac à dos", description = "Pour l'école", price = 40.0, endDate = "2023-10-20", user = users[1]),
            Offer(id = "11", title = "Tente de camping", description = "4 personnes", price = 150.0, endDate = "2024-02-01", user = users[0]),
            Offer(id = "12", title = "Grill électrique", description = "Pour barbecue", price = 100.0, endDate = "2023-12-10", user = users[1]),
            Offer(id = "13", title = "Casque audio", description = "Sans fil", price = 120.0, endDate = "2023-11-25", user = users[0]),
            Offer(id = "14", title = "Batterie externe", description = "Pour smartphone", price = 30.0, endDate = "2023-10-25", user = users[1]),
            Offer(id = "15", title = "Jeu vidéo", description = "Action RPG", price = 60.0, endDate = "2023-11-05", user = users[0])
        )
    )

    val offers: StateFlow<List<Offer>> = _offers.asStateFlow()

    fun getBidsForOffer(offerId: String): List<Bid> =
        bids.filter { it.offerId == offerId }.sortedByDescending { it.date }

    fun getBidsForOfferFlow(offerId: String): Flow<List<Bid>> = flow {
        emit(getBidsForOffer(offerId))
    }
}