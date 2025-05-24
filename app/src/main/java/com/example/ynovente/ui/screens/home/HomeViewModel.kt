package com.example.ynovente.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ynovente.data.model.Offer
import com.example.ynovente.data.repository.FakeOfferRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: FakeOfferRepository = FakeOfferRepository()
) : ViewModel() {
    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers

    init {
        viewModelScope.launch {
            repository.getOffers().collect { _offers.value = it }
        }
    }
}