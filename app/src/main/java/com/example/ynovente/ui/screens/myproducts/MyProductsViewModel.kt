package com.example.ynovente.ui.screens.myproducts

import androidx.lifecycle.ViewModel
import com.example.ynovente.data.model.Offer
import com.example.ynovente.data.model.User
import com.example.ynovente.data.repository.FakeOfferRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MyProductsViewModel(
    private val offerRepository: FakeOfferRepository,
    private val currentUser: User
) : ViewModel() {
    // Les offres de l'utilisateur courant (filtrage par userId, plus par user)
    val myOffers: StateFlow<List<Offer>> =
        offerRepository.offers
            .map { offers -> offers.filter { it.userId == currentUser.id } }
            .stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Lazily, emptyList())
}