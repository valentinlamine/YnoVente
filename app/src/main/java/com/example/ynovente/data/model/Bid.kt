package com.example.ynovente.data.model

data class Bid(
    var id: String = "",
    var offerId: String = "",
    var userId: String = "",
    var userName: String = "",
    var amount: Double = 0.0,
    var date: String = "" // ISO instant ou format court
)