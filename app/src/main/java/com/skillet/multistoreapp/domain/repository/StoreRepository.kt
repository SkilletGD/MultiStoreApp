package com.skillet.multistoreapp.domain.repository

import com.skillet.multistoreapp.core.model.Store

interface StoreRepository {
    suspend fun createStore(
        store: Store
    ): Result<Unit>

    suspend fun getStoreBySeller(
        sellerId: String
    ): Result<Store?>

    suspend fun updateStore(
        store: Store
    ): Result<Unit>

    suspend fun getAllStores(): Result<List<Store>>

    suspend fun getStoreById(
        storeId: String
    ): Result<Store?>
}