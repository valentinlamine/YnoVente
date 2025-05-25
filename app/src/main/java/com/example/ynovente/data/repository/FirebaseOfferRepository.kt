package com.example.ynovente.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.ynovente.data.model.Bid
import com.example.ynovente.data.model.Offer
import com.example.ynovente.data.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant

class FirebaseOfferRepository {

    private val database = FirebaseDatabase.getInstance()
    private val offersRef = database.getReference("offers")

    suspend fun addOffer(offer: Offer) {
        // Génère une nouvelle clé pour l'offre
        val key = offersRef.push().key ?: throw Exception("Erreur génération clé Firebase")
        val offerWithId = offer.copy(id = key)
        offersRef.child(key).setValue(offerWithId).await()
    }

    fun getOffersFlow(): Flow<List<Offer>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offers = snapshot.children.mapNotNull { it.getValue(Offer::class.java) }
                trySend(offers)
            }
            override fun onCancelled(error: DatabaseError) { }
        }
        offersRef.addValueEventListener(listener)
        awaitClose { offersRef.removeEventListener(listener) }
    }

    fun getOfferByIdFlow(offerId: String): Flow<Offer?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offer = snapshot.getValue(Offer::class.java)
                trySend(offer)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        offersRef.child(offerId).addValueEventListener(listener)
        awaitClose { offersRef.child(offerId).removeEventListener(listener) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun placeBid(offerId: String, userId: String, userName: String, amount: Double) {
        val database = FirebaseDatabase.getInstance()
        val bidsRef = database.getReference("bids").child(offerId)
        val bidId = bidsRef.push().key ?: throw Exception("Erreur génération clé bid")
        val bid = Bid(
            id = bidId,
            offerId = offerId,
            userId = userId,
            userName = userName,
            amount = amount,
            date = Instant.now().toString()
        )
        // Ajoute l'enchère
        bidsRef.child(bidId).setValue(bid).await()
        // Met à jour le prix de l'offre
        database.getReference("offers").child(offerId).child("price").setValue(amount).await()
    }

    fun getBidsForOfferFlow(offerId: String): Flow<List<Bid>> = callbackFlow {
        val ref = FirebaseDatabase.getInstance().getReference("bids/$offerId")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bids = snapshot.children.mapNotNull { it.getValue(Bid::class.java) }
                trySend(bids.sortedByDescending { it.amount })
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // AJOUT : Mise à jour d'une offre (titre, description, date de fin)
    suspend fun updateOffer(
        offerId: String,
        title: String? = null,
        description: String? = null,
        endDate: String? = null
    ) {
        val updates = mutableMapOf<String, Any>()
        title?.let { updates["title"] = it }
        description?.let { updates["description"] = it }
        endDate?.let { updates["endDate"] = it }
        if (updates.isNotEmpty()) {
            offersRef.child(offerId).updateChildren(updates).await()
        }
    }

    // AJOUT : Suppression d'une offre, de ses bids et de son image
    suspend fun deleteOffer(offerId: String) {
        // Récupère l'offre pour obtenir l'URL de l'image (avant suppression)
        val offerSnapshot = offersRef.child(offerId).get().await()
        val offer = offerSnapshot.getValue(Offer::class.java)
        val imageUrl = offer?.imageUrl

        // Supprime l'offre
        offersRef.child(offerId).removeValue().await()
        // Supprime les bids associés
        database.getReference("bids").child(offerId).removeValue().await()
        // Supprime l'image si elle existe
        imageUrl?.let {
            try {
                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(it)
                storageRef.delete().await()
            } catch (e: Exception) {
                // Si l'image n'existe pas ou déjà supprimée, ignorer l'erreur
            }
        }
    }
}