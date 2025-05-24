package com.example.ynovente.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.ynovente.data.model.Offer
import com.example.ynovente.data.model.Bid
import com.example.ynovente.data.repository.FakeOfferRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow

class HomeViewModel(
    private val repository: FakeOfferRepository = FakeOfferRepository()
) : ViewModel() {
    // Expose le StateFlow directement, sans recopier dans _offers
    val offers: StateFlow<List<Offer>> = repository.offers

    fun getBidsForOfferFlow(offerId: String): Flow<List<Bid>> {
        return repository.getBidsForOfferFlow(offerId)
    }
}