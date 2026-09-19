package com.skillet.multistoreapp.core.model

data class Product (
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val currency: String = "MXN",
    val stock: Int = 0,
    val imageUrl: String = "",
    val categoryId: String = "",
    val storeId: String = "",
    val attributes: Map<String, String> = emptyMap(),
    val storagePath: String = "",
    val uploadedAt: Long = 0L
)