package com.skillet.multistoreapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.skillet.multistoreapp.core.model.CartItem
import com.skillet.multistoreapp.core.model.Product
import com.skillet.multistoreapp.domain.repository.AuthRepository
import com.skillet.multistoreapp.domain.repository.CartRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CartRepositoryImpl (
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
): CartRepository {

    private val cartCollection = "carts"
    private fun cartsRef() = firestore.collection(cartCollection)

    private suspend fun currentUserId(): String {
        val user = authRepository.getCurrentUser()
            ?: throw IllegalArgumentException("No existe un usuario autenticado")
        return user.uid
    }

    override suspend fun addProduct(product: Product): Result<Unit> =
        runCatching {
            val userId = currentUserId()
            // carts
            //  |----userId
            //          |---- items
            //                  |---- product.id

            val itemRef = cartsRef()
                .document(userId)
                .collection("items")
                .document(product.id)

            val snapshot = itemRef.get().await()

            if(!snapshot.exists()){
                val cartItem = CartItem(
                    product = product,
                    quantity = 1
                )
                itemRef.set(cartItem).await()
            }else{
                val currentQuantity = snapshot.getLong("quantity") ?.toInt() ?: 0

                itemRef.update(
                    "quantity", currentQuantity + 1
                ).await()
            }
        }

    override suspend fun removeProduct(productId: String): Result<Unit> =
        runCatching {
            val userId = currentUserId()
            cartsRef()
                .document(userId)
                .collection("items")
                .document(productId)
                .delete()
                .await()
        }

    override suspend fun clearCart(): Result<Unit> =
        runCatching {
            val userId = currentUserId()
            val snapshot =
                cartsRef()
                    .document(userId)
                    .collection("items")
                    .get()
                    .await()

            snapshot.documents.forEach { documentSnapshot ->
                documentSnapshot.reference.delete().await()
            }
        }

    override fun getCartItemsFlorw(): Flow<List<CartItem>> =
        callbackFlow {
            val userId = currentUserId()

            if(userId == null){
                trySend(
                    emptyList()
                )
                close()
                return@callbackFlow
            }

            val listener =
                cartsRef()
                    .document(userId)
                    .collection("items")
                    .addSnapshotListener { snapshot, error ->
                        if(error != null) {
                            trySend(
                                emptyList()
                            )
                            return@addSnapshotListener
                        }

                        val items =
                            snapshot
                                ?.documents
                                ?.mapNotNull { documentSnapshot ->
                                documentSnapshot.toObject(CartItem::class.java)
                            } ?: emptyList()

                        trySend(items)
                    }
            awaitClose {
                listener.remove()
            }
        }



}