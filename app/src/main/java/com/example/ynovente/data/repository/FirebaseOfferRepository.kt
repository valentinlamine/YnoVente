package com.example.ynovente.data.repository

import com.example.ynovente.data.model.Offer
import com.example.ynovente.data.model.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseOfferRepository {

    private val database = FirebaseDatabase.getInstance()
    private val offersRef = database.getReference("offers")

    suspend fun addOffer(offer: Offer) {
        // Génère une nouvelle clé pour l'offre
        val key = offersRef.push().key ?: throw Exception("Erreur génération clé Firebase")
        val offerWithId = offer.copy(id = key)
        offersRef.child(key).setValue(offerWithId).await()
    }
}