package com.skillet.multistoreapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.skillet.multistoreapp.core.model.Category
import com.skillet.multistoreapp.domain.repository.CategoryRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CategoryRepositoryImpl(
    private val firestore: FirebaseFirestore
) : CategoryRepository {


    private val collectionCategories: String = "categories"

    private fun categoriesRef() =
        firestore.collection(collectionCategories)


    override suspend fun createCategory(category: Category): Result<Unit> {
        return runCatching {
            val nameTrimed: String = category.name.trim()
            val storeIdTrimed: String = category.storeId.trim()

            if (nameTrimed.isBlank()) {
                throw IllegalArgumentException("El nombre esta vacío")
            }
            if (storeIdTrimed.isBlank()) {
                throw IllegalArgumentException("El id de la tienda esta vacío")
            }

            val finalId = categoriesRef().document().id

            val finalCategory = Category(
                id = finalId,
                name = nameTrimed,
                storeId = storeIdTrimed
            )

            val data = mapOf(
                "id" to finalCategory.id,
                "name" to finalCategory.name,
                "storeId" to finalCategory.storeId
            )

            categoriesRef()
                .document(finalId)
                .set(data)
                .await()
        }
    }

    override fun getCategoriesByStoreFlow(storeId: String): Flow<List<Category>> =
        callbackFlow {
            val storeIdTrimed: String = storeId.trim()
            if (storeIdTrimed.isBlank()) {
                trySend(emptyList())
                return@callbackFlow
            }

            val registration = categoriesRef()
                .whereEqualTo(
                    "storeId", storeIdTrimed
                )
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot == null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val categories = snapshot.documents.map { doc->
                        Category(
                            id = doc.id,
                            name = doc.getString("name").orEmpty(),
                            storeId = doc.getString("storeId").orEmpty()
                        )
                    }

                    trySend(categories)
                }
            awaitClose {
                registration.remove()
            }
        }


    override suspend fun updateCategory(category: Category): Result<Unit> {
        return runCatching {
            val id: String = category.id
            val nameTrimed: String = category.name.trim()

            if (id.isBlank()) {
                throw IllegalArgumentException("El id de la categoria esta vacío")
            }
            if (nameTrimed.isBlank()) {
                throw IllegalArgumentException("El nombre de la categoria esta vacío")
            }

            val data = mapOf(
                "name" to nameTrimed
            )

            categoriesRef()
                .document(id)
                .update(data)
                .await()
        }
    }

    override suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return runCatching {
            val id: String = categoryId.trim()

            if (id.isBlank()) {
                throw IllegalArgumentException("El id de la categoria esta vacío")
            }

            categoriesRef()
                .document(categoryId)
                .delete()
                .await()
        }
    }

    override suspend fun getCategoriesById(categoryId: String): Result<Category?>{
        return runCatching {
            val id: String = categoryId.trim()

            if (id.isBlank()) {
                throw IllegalArgumentException("El id de la categoria esta vacío")
            }

            val snapshot = categoriesRef()
                .document(categoryId)
                .get()
                .await()

            snapshot.toObject(Category::class.java)
        }
    }
}