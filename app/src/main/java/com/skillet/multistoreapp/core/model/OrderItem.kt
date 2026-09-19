package com.skillet.multistoreapp.core.model

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val productDescription: String = "",
    val productImageUrl: String = "",
    val productPrice: Double = 0.0,
    val quantity: Int = 0,
    val subtotal: Double = 0.0
)