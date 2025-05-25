package com.example.ynovente.ui.screens.myproducts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ynovente.data.model.Offer
import com.example.ynovente.data.repository.FirebaseOfferRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MyProductsViewModel(
    private val offerRepository: FirebaseOfferRepository
) : ViewModel() {
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    val myOffers: StateFlow<List<Offer>> = offerRepository
        .getOffersFlow()
        .map { offers -> offers.filter { it.userId == currentUserId } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}