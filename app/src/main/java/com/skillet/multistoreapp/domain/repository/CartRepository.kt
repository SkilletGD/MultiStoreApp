package com.skillet.multistoreapp.domain.repository

import com.skillet.multistoreapp.core.model.CartItem
import com.skillet.multistoreapp.core.model.Product
import kotlinx.coroutines.flow.Flow

interface CartRepository {

    suspend fun addProduct(product: Product, quantity: Int = 1): Result<Unit>

    suspend fun removeProduct(productId: String): Result<Unit>

    suspend fun clearCart(): Result<Unit>

    fun getCartItemsFlorw(): Flow<List<CartItem>>

}