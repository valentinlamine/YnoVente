package com.example.ynovente.data.model

data class FinishedOfferDisplay(
    val offer: Offer,
    val bestBid: Bid?,
    val winnerEmail: String?
)