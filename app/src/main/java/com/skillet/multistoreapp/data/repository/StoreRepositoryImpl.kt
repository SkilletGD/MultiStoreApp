package com.skillet.multistoreapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.skillet.multistoreapp.core.model.Store
import com.skillet.multistoreapp.domain.repository.StoreRepository
import kotlinx.coroutines.tasks.await

class StoreRepositoryImpl (
    private val firestore: FirebaseFirestore
): StoreRepository {

    val collectionStores: String = "stores"

    private fun storesRef() =
        firestore.collection(collectionStores)

    override suspend fun updateStore(store: Store): Result<Unit> =
        runCatching {
            val storeId: String = store.id.trim()
            val sellerIdTrimed: String = store.sellerId.trim()
            val nameTrimed: String = store.name.trim()
            val descriptionTrimed: String = store.description.trim()
            val categoryTrimed: String = store.category.trim()

            if (storeId.isEmpty()) {
                throw IllegalArgumentException("El id de la tienda no puede estar vacío")
            }
            if (sellerIdTrimed.isEmpty()) {
                throw IllegalArgumentException("El id del vendedor no puede estar vacío")
            }
            if (nameTrimed.isEmpty()) {
                throw IllegalArgumentException("El nombre de la tienda no puede estar vacío")
            }
            if (descriptionTrimed.isEmpty()) {
                throw IllegalArgumentException("La descripción de la tienda no puede estar vacía")
            }
            if (categoryTrimed.isEmpty()) {
                throw IllegalArgumentException("Debes de seleccionar una categoría")
            }

            val finalStore: Store = store.copy(
                id = storeId,
                sellerId = sellerIdTrimed,
                name = nameTrimed,
                description = descriptionTrimed,
                category = categoryTrimed
            )

            val data = mapOf(
                "id" to finalStore.id,
                "sellerId" to finalStore.sellerId,
                "name" to finalStore.name,
                "description" to finalStore.description,
                "category" to finalStore.category,
                "isActive" to finalStore.isActive,
                "createdAt" to finalStore.createdAt
            )

            storesRef()
                .document(storeId)
                .update(data)
                .await()

        }

    override suspend fun getStoreBySeller(sellerId: String): Result<Store?> {
        return runCatching {
            val sellerIdTrimed: String = sellerId.trim()
            if (sellerIdTrimed.isEmpty()) {
                throw IllegalArgumentException("El sellerId esta vacío")
            }

            val snapshot = storesRef()
                .whereEqualTo("sellerId", sellerIdTrimed)
                .limit(1)
                .get()
                .await()

            val document = snapshot.documents.firstOrNull()

            document?.toObject(Store::class.java)
        }
    }

    override suspend fun createStore(store: Store): Result<Unit> =
        runCatching {

            val sellerIdTrimed: String = store.sellerId.trim()
            val nameTrimed: String = store.name.trim()
            val descriptionTrimed: String = store.description.trim()
            val categoryTrimed: String = store.category.trim()

            if (sellerIdTrimed.isEmpty()) {
                throw IllegalArgumentException("El id del vendedor no puede estar vacío")
            }
            if (nameTrimed.isEmpty()) {
                throw IllegalArgumentException("El nombre de la tienda no puede estar vacío")
            }
            if (descriptionTrimed.isEmpty()) {
                throw IllegalArgumentException("La descripción de la tienda no puede estar vacía")
            }
            if (categoryTrimed.isEmpty()) {
                throw IllegalArgumentException("Debes de seleccionar una categoría")
            }

            val finalStoreId: String = storesRef().document().id

            val createdAt: Long = System.currentTimeMillis()

            val finalStore: Store = store.copy(
                id = finalStoreId,
                sellerId = sellerIdTrimed,
                name = nameTrimed,
                description = descriptionTrimed,
                category = categoryTrimed,
                createdAt = createdAt
            )

            val data = mapOf(
                "id" to finalStore.id,
                "sellerId" to finalStore.sellerId,
                "name" to finalStore.name,
                "description" to finalStore.description,
                "category" to finalStore.category,
                "imageUrl" to finalStore.imageUrl,
                "isActive" to finalStore.isActive,
                "createdAt" to finalStore.createdAt
            )

            storesRef()
                .document(finalStore.id)
                .set(data)
                .await()
        }

    override suspend fun getAllStores(): Result<List<Store>> =
        runCatching {
            storesRef().get().await().documents.mapNotNull { document ->
                document.toObject(Store::class.java)
            }
        }

    override suspend fun getStoreById(storeId: String): Result<Store?> =
        runCatching{
            val storeIdTrimmed = storeId.trim()
            if(storeIdTrimmed.isEmpty()) throw IllegalArgumentException("El id de la tienda esta vacío")

            val document = storesRef().document(storeIdTrimmed).get().await()

            if(!document.exists()) throw Exception("No se encontro la informacion de la tienda")

            document.toObject(Store::class.java) ?: throw Exception("No se pudo convertir la informacion de la tienda")

        }
}