package com.example.ynovente.data.model

data class Offer(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var price: Double = 0.0,
    var endDate: String = "",
    var imageUrl: String? = null,
    var userId: String = ""
)