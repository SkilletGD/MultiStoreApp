package com.skillet.multistoreapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.skillet.multistoreapp.core.model.Product
import com.skillet.multistoreapp.domain.repository.ProductRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepositoryImpl (
    private val firestore: FirebaseFirestore
): ProductRepository {

    private val collectionProducts: String = "products"

    private fun productsRef() = firestore.collection(collectionProducts)

    override suspend fun createProduct(product: Product): Result<Unit> {
        return runCatching {
            val nameTrimed: String = product.name.trim()
            val descriptionTrimed: String = product.description.trim()
            val categoryIdTrimed: String = product.categoryId.trim()
            val storeIdTrimed: String = product.storeId.trim()
            val imageUrlTrimed: String = product.imageUrl.trim()
            val storagePathTrimed: String = product.storagePath.trim()

            if (nameTrimed.isBlank()) {
                throw IllegalArgumentException("El nombre del producto esta vacio")
            }
            if (descriptionTrimed.isBlank()) {
                throw IllegalArgumentException("La descripcion del producto esta vacia")
            }
            if (product.price <= 0) {
                throw IllegalArgumentException("El precio del producto debe ser mayor a 0")
            }
            if (product.stock < 0) {
                throw IllegalArgumentException("El stock del producto no puede ser negativo")
            }
            if (categoryIdTrimed.isBlank()) {
                throw IllegalArgumentException("El id de la categoria del producto esta vacio")
            }
            if (storeIdTrimed.isBlank()) {
                throw IllegalArgumentException("El StoreId del producto esta vacio")
            }
            if (imageUrlTrimed.isBlank()) {
                throw IllegalArgumentException("La url de la imagen del producto esta vacia")
            }
            if (storagePathTrimed.isBlank()) {
                throw IllegalArgumentException("El storePath del producto esta vacio")
            }

            val finalId = productsRef().document().id

            val now = System.currentTimeMillis()

            val finalProduct = product.copy(
                id = finalId,
                name = nameTrimed,
                description = descriptionTrimed,
                categoryId = categoryIdTrimed,
                storeId = storeIdTrimed,
                imageUrl = imageUrlTrimed,
                storagePath = storagePathTrimed,
                uploadedAt = now
            )

            val data = mapOf(
                "id" to finalProduct.id,
                "name" to finalProduct.name,
                "description" to finalProduct.description,
                "price" to finalProduct.price,
                "currency" to finalProduct.currency,
                "stock" to finalProduct.stock,
                "imageUrl" to finalProduct.imageUrl,
                "categoryId" to finalProduct.categoryId,
                "storeId" to finalProduct.storeId,
                "storagePath" to finalProduct.storagePath,
                "uploadedAt" to finalProduct.uploadedAt,
                "attributes" to finalProduct.attributes,
                "nameLower" to nameTrimed.lowercase()
            )
            productsRef().document(finalId)
                .set(data)
                .await()

            Unit
        }
    }

    override fun getProductsByStoreFlow(storeId: String): Flow<List<Product>> =
        callbackFlow {
            val query = productsRef()
                .whereEqualTo("storeId", storeId)
            val registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.map { doc ->
                    Product(
                        id = doc.id,
                        name = doc.getString("name").orEmpty(),
                        description = doc.getString("description").orEmpty(),
                        price = doc.getDouble("price") ?: 0.0,
                        currency = doc.getString("currency").orEmpty(),
                        stock = doc.getLong("stock")?.toInt() ?: 0,
                        imageUrl = doc.getString("imageUrl").orEmpty(),
                        categoryId = doc.getString("categoryId").orEmpty(),
                        storeId = doc.getString("storeId").orEmpty(),
                        attributes =
                            doc.get("attributes")
                                    as? Map<String, String> ?: emptyMap(),
                        storagePath = doc.getString("storagePath").orEmpty(),
                        uploadedAt = doc.getLong("uploadedAt") ?: 0L,
                    )
                }
                trySend(list)
            }
            awaitClose {
                registration.remove()
            }
        }

    override fun getProductsByCategoryFlow(categoryId: String): Flow<List<Product>> =
        callbackFlow {
            val query = productsRef().whereEqualTo("categoryId", categoryId)

            val registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot.documents.map { doc ->
                    Product(
                        id = doc.id,
                        name = doc.getString("name").orEmpty(),
                        description = doc.getString("description").orEmpty(),
                        price = doc.getDouble("price") ?: 0.0,
                        currency = doc.getString("currency").orEmpty(),
                        stock = doc.getLong("stock")?.toInt() ?: 0,
                        imageUrl = doc.getString("imageUrl").orEmpty(),
                        categoryId = doc.getString("categoryId").orEmpty(),
                        storeId = doc.getString("storeId").orEmpty(),
                        attributes =
                            doc.get("attributes")
                                as? Map<String, String> ?: emptyMap(),
                        storagePath = doc.getString("storagePath").orEmpty(),
                        uploadedAt = doc.getLong("uploadedAt") ?: 0L
                    )
                }
                trySend(list)
            }
            awaitClose {
                registration.remove()
            }
        }

    override suspend fun getProductById(productId: String): Result<Product?> =
        runCatching {

            if (productId.isBlank()) {
                throw IllegalArgumentException("El id del producto esta vacio")
            }
            val snapshot = productsRef()
                .document(productId)
                .get()
                .await()

            if (!snapshot.exists()){
                return@runCatching null
            }

            Product(
                id = snapshot.id,
                name = snapshot.getString("name").orEmpty(),
                description = snapshot.getString("description").orEmpty(),
                price = snapshot.getDouble("price") ?: 0.0,
                currency = snapshot.getString("currency").orEmpty(),
                stock = snapshot.getLong("stock")?.toInt() ?: 0,
                imageUrl = snapshot.getString("imageUrl").orEmpty(),
                categoryId = snapshot.getString("categoryId").orEmpty(),
                storeId = snapshot.getString("storeId").orEmpty(),
                attributes =
                    snapshot.get("attributes")
                        as? Map<String, String> ?: emptyMap(),
                storagePath = snapshot.getString("storagePath").orEmpty(),
                uploadedAt = snapshot.getLong("uploadedAt") ?: 0L
            )
        }

    override suspend fun updateProduct(product: Product): Result<Unit> =
        runCatching {
            if(product.id.isBlank()){
                throw IllegalArgumentException("El id del producto esta vacio")
            }

            val nameTrimed: String = product.name.trim()
            val descriptionTrimed: String = product.description.trim()

            if(nameTrimed.isBlank()){
                throw IllegalArgumentException("El nombre del producto esta vacio")
            }
            if(descriptionTrimed.isBlank()){
                throw IllegalArgumentException("La descripcion del producto esta vacia")
            }

            val updates = mapOf(
                "name" to nameTrimed,
                "description" to descriptionTrimed,
                "price" to product.price,
                "currency" to product.currency,
                "stock" to product.stock,
                "imageUrl" to product.imageUrl,
                "categoryId" to product.categoryId,
                "storagePath" to product.storagePath,
                "attributes" to product.attributes,
                "nameLower" to nameTrimed.lowercase()
            )

            productsRef().document(product.id)
                .update(updates)
                .await()

            Unit
        }

    override suspend fun deleteProduct(productId: String): Result<Unit> =
        runCatching {
            if(productId.isBlank()){
                throw IllegalArgumentException("El id del producto esta vacia")
            }

            productsRef().document(productId).delete().await()

            Unit

        }

    override suspend fun updateProductsStock(productId: String, newStock: Int): Result<Unit> =
        runCatching {
            if(productId.isBlank()){
                throw IllegalArgumentException("El productId esta vacio")
            }
            if(newStock == 0){
                throw IllegalArgumentException("El stock no puede ser negativo")
            }

            productsRef().document(productId).update(
                "stock",
                newStock
            ).await()
        }
}