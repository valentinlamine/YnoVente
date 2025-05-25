package com.example.ynovente.data.repository

import com.example.ynovente.data.model.Bid
import com.example.ynovente.data.model.Offer
import com.example.ynovente.data.model.User
import kotlinx.coroutines.flow.*
import java.time.Instant

class FakeOfferRepository {
    private val users = listOf(
        User("1", "Alice", "alice@mail.com", "password"),
        User("2", "Bob", "bob@mail.com", "password"),
    )

    private val _bids = MutableStateFlow(
        listOf(
            Bid(
                id = "b1",
                offerId = "1",
                userId = users[0].id,
                userName = users[0].name,
                amount = 110.0,
                date = "2025-05-23T12:00:00"
            ),
            Bid(
                id = "b2",
                offerId = "1",
                userId = users[1].id,
                userName = users[1].name,
                amount = 120.0,
                date = "2025-05-23T13:00:00"
            ),
            Bid(
                id = "b3",
                offerId = "2",
                userId = users[0].id,
                userName = users[0].name,
                amount = 600.0,
                date = "2025-05-23T14:00:00"
            ),
            Bid(
                id = "b4",
                offerId = "2",
                userId = users[1].id,
                userName = users[1].name,
                amount = 650.0,
                date = "2025-05-23T15:00:00"
            ),
            Bid(
                id = "b5",
                offerId = "3",
                userId = users[0].id,
                userName = users[0].name,
                amount = 10.0,
                date = "2025-05-23T16:00:00"
            ),
            Bid(
                id = "b6",
                offerId = "3",
                userId = users[1].id,
                userName = users[1].name,
                amount = 15.0,
                date = "2025-05-23T17:00:00"
            ),
            Bid(
                id = "b7",
                offerId = "4",
                userId = users[0].id,
                userName = users[0].name,
                amount = 280.0,
                date = "2025-05-23T18:00:00"
            ),
            Bid(
                id = "b8",
                offerId = "4",
                userId = users[1].id,
                userName = users[1].name,
                amount = 300.0,
                date = "2025-05-23T19:00:00"
            ),
            Bid(
                id = "b9",
                offerId = "5",
                userId = users[0].id,
                userName = users[0].name,
                amount = 180.0,
                date = "2025-05-23T20:00:00"
            ),
            Bid(
                id = "b10",
                offerId = "5",
                userId = users[1].id,
                userName = users[1].name,
                amount = 200.0,
                date = "2025-05-23T21:00:00"
            )
        )
    )
    val bids: StateFlow<List<Bid>> = _bids.asStateFlow()

    private val _offers = MutableStateFlow<List<Offer>>(
        listOf(
            Offer(id = "1", title = "Vélo de course", description = "Super vélo", price = 120.0, endDate = "2023-12-31", userId = users[0].id),
            Offer(id = "2", title = "PC Portable", description = "Gamer", price = 650.0, endDate = "2023-11-30", userId = users[1].id),
            Offer(id = "3", title = "Livre Kotlin", description = "Pour apprendre", price = 15.0, endDate = "2023-10-15", userId = users[0].id),
            Offer(id = "4", title = "Console de jeux", description = "Dernier modèle", price = 300.0, endDate = "2023-12-15", userId = users[1].id),
            Offer(id = "5", title = "Table de jardin", description = "En bois massif", price = 200.0, endDate = "2024-01-01", userId = users[0].id),
            Offer(id = "6", title = "Smartphone", description = "Dernier modèle", price = 800.0, endDate = "2023-11-20", userId = users[1].id),
            Offer(id = "7", title = "Appareil photo", description = "Reflex numérique", price = 450.0, endDate = "2023-12-05", userId = users[0].id),
            Offer(id = "8", title = "Chaise de bureau", description = "Ergonomique", price = 75.0, endDate = "2023-10-30", userId = users[1].id),
            Offer(id = "9", title = "Montre connectée", description = "Pour le sport", price = 250.0, endDate = "2023-11-15", userId = users[0].id),
            Offer(id = "10", title = "Sac à dos", description = "Pour l'école", price = 40.0, endDate = "2023-10-20", userId = users[1].id),
            Offer(id = "11", title = "Tente de camping", description = "4 personnes", price = 150.0, endDate = "2024-02-01", userId = users[0].id),
            Offer(id = "12", title = "Grill électrique", description = "Pour barbecue", price = 100.0, endDate = "2023-12-10", userId = users[1].id),
            Offer(id = "13", title = "Casque audio", description = "Sans fil", price = 120.0, endDate = "2023-11-25", userId = users[0].id),
            Offer(id = "14", title = "Batterie externe", description = "Pour smartphone", price = 30.0, endDate = "2023-10-25", userId = users[1].id),
            Offer(id = "15", title = "Jeu vidéo", description = "Action RPG", price = 60.0, endDate = "2023-11-05", userId = users[0].id)
        )
    )
    val offers: StateFlow<List<Offer>> = _offers.asStateFlow()

    fun addOffer(offer: Offer) {
        _offers.value = _offers.value + offer
    }

    fun getBidsForOffer(offerId: String): List<Bid> =
        _bids.value.filter { it.offerId == offerId }.sortedByDescending { it.date }

    fun getBidsForOfferFlow(offerId: String): Flow<List<Bid>> =
        _bids.map { bidsList ->
            bidsList.filter { it.offerId == offerId }.sortedByDescending { it.date }
        }

    fun getOfferByIdFlow(offerId: String): Flow<Offer?> =
        offers.map { offerList -> offerList.find { it.id == offerId } }

    /**
     * Fonction de surenchère (ajoute une enchère si le montant est supérieur à l'actuel)
     */
    fun placeBid(offerId: String, userId: String, userName: String, amount: Double) {
        val offer = _offers.value.find { it.id == offerId } ?: return
        if (amount > offer.price) {
            // Mettre à jour le prix de l'offre
            val newOffer = offer.copy(price = amount)
            _offers.value = _offers.value.map { if (it.id == offerId) newOffer else it }
            // Ajouter la nouvelle enchère
            val newBid = Bid(
                id = "fake_bid_${System.currentTimeMillis()}",
                offerId = offerId,
                userId = userId,
                userName = userName,
                amount = amount,
                date = Instant.now().toString()
            )
            _bids.value = _bids.value + newBid
        }
    }
}