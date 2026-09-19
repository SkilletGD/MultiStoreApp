package com.skillet.multistoreapp.core.model

data class Store(
    val id: String = "",
    val sellerId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = 0L
)