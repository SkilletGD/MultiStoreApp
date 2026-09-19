package com.skillet.multistoreapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.skillet.multistoreapp.core.model.CartItem
import com.skillet.multistoreapp.core.model.Order
import com.skillet.multistoreapp.core.model.OrderItem
import com.skillet.multistoreapp.core.model.OrderStatus
import com.skillet.multistoreapp.core.model.Store
import com.skillet.multistoreapp.domain.repository.AuthRepository
import com.skillet.multistoreapp.domain.repository.OrderRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await


class OrderRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
): OrderRepository {

    private val collectionOrders = "orders"

    private fun ordersRef() = firestore.collection(collectionOrders)

    private val collectionStores = "stores"

    private fun storeRef() = firestore.collection(collectionStores)

    override suspend fun createOrder(items: List<CartItem>): Result<Unit> =
        runCatching {
            val currentUser = authRepository.getCurrentUser() ?: throw Exception("No hay usuario logueado")

            if(items.isEmpty()) throw IllegalArgumentException("El carrito está vacío")

            val firstItem = items.first()
            val storeId = firstItem.product.storeId

            val store = getStoreById(storeId)

            val storeName = store.name
            val sellerId = store.sellerId

            val customerName = "${currentUser.firstName} ${currentUser.lastName}"

            val orderItems = items.map { cartItem ->
                OrderItem(
                    productId = cartItem.product.id,
                    productName = cartItem.product.name,
                    productDescription = cartItem.product.description,
                    productImageUrl = cartItem.product.imageUrl,
                    productPrice = cartItem.product.price,
                    quantity = cartItem.quantity,
                    subtotal = cartItem.product.price * cartItem.quantity,
                )
            }

            val total = orderItems.sumOf { it.subtotal }

            val orderId = ordersRef().document().id

            val finalOrder = Order(
                id = orderId,
                customerId = currentUser.uid,
                customerName = customerName,
                storeId = storeId,
                storeName = storeName,
                sellerId = sellerId,
                items = orderItems,
                total = total,
                status = OrderStatus.PENDIENTE,
                created = System.currentTimeMillis()
            )

            ordersRef().document(orderId).set(finalOrder).await()

            Unit
        }
    private suspend fun getStoreById(storeId: String): Store{
        val id = storeId.trim()

        if(id.isEmpty()) throw IllegalArgumentException("El id de la tienda está vacío")

        val document = storeRef().document(id).get().await()

        return document.toObject(Store::class.java) ?: throw Exception("La tienda no existe")
    }

    override fun getCustomerOrders(): Flow<List<Order>> =
        callbackFlow {
            val customerId = authRepository.getCurrentUser() ?: throw Exception("No hay usuario logueado")

            if (customerId == null){
                trySend(emptyList())
                return@callbackFlow
            }

            val registration = ordersRef()
                .whereEqualTo("customerId", customerId.uid)
                .addSnapshotListener { snapshots, error ->
                    if(error != null){
                        return@addSnapshotListener
                    }
                    if(snapshots == null){
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val orders = snapshots.toObjects(Order::class.java)
                    trySend(orders)
                }
            awaitClose {
                registration.remove()
            }
        }

    override fun getStoreOrders(storeId: String): Flow<List<Order>> =
        callbackFlow {
            val storeIdTrimmed = storeId.trim()

            if(storeIdTrimmed.isEmpty()) {
                trySend(emptyList())
                return@callbackFlow
            }

            val registration = ordersRef()
                .whereEqualTo("storeId", storeIdTrimmed)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshots == null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val orders = snapshots.toObjects(Order::class.java)
                    trySend(orders)
                }
            awaitClose {
                registration.remove()
            }

        }

    override suspend fun getOrderById(orderId: String): Result<Order> =
        runCatching {
            val id = orderId.trim()
            if(id.isBlank()) throw IllegalArgumentException("El id del pedido está vacío")

            val document = ordersRef().document(id).get().await()

            document.toObject(Order::class.java) ?: throw Exception("El pedido no existe")

        }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> =
        runCatching {
            val id = orderId.trim()
            if(id.isBlank()) throw IllegalArgumentException("El id del pedido está vacío")

            ordersRef().document(id).update("status", status).await()

            Unit
        }
}