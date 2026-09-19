package com.skillet.multistoreapp.domain.repository

interface ProductStorageRepository {
    suspend fun uploadProductImage(
        sellerId: String?,
        storeId: String,
        byteArray: ByteArray,
        extension: String
    ): Result<Pair<String, String>>

    suspend fun updateProductImage(
        sellerId: String,
        oldStoragePath: String,
        newByteArray: ByteArray,
        extension: String
    ): Result<Pair<String, String>>

    suspend fun deleteProductImage(
        storagePath: String
    ): Result<Unit>
}