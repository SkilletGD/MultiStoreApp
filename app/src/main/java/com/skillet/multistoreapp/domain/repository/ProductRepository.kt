package com.skillet.multistoreapp.domain.repository

import com.skillet.multistoreapp.core.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun createProduct(
        product: Product
    ): Result<Unit>

    fun getProductsByStoreFlow(
        storeId: String
    ): Flow<List<Product>>

    fun getProductsByCategoryFlow(
        categoryId: String
    ): Flow<List<Product>>
    suspend fun getProductById(
        productId: String
    ): Result<Product?>

    suspend fun updateProduct(
        product: Product
    ): Result<Unit>

    suspend fun deleteProduct(
        productId: String
    ): Result<Unit>

    suspend fun updateProductsStock(
        productId: String,
        newStock: Int
    ): Result<Unit>
}