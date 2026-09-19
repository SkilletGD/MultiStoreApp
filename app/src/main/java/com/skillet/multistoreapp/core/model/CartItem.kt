package com.skillet.multistoreapp.core.model

data class CartItem (
    val product: Product = Product(),
    val quantity: Int = 0,
    val totalPrice: Double = 0.0
)