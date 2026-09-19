package com.skillet.multistoreapp.domain.repository

import com.skillet.multistoreapp.core.model.CartItem
import com.skillet.multistoreapp.core.model.Order
import com.skillet.multistoreapp.core.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {

    suspend fun createOrder(
        items: List<CartItem>,
    ): Result<Unit>

    fun getCustomerOrders(): Flow<List<Order>>

    fun getStoreOrders(
        storeId: String
    ): Flow<List<Order>>

    suspend fun getOrderById(
        orderId: String
    ): Result<Order>

    suspend fun updateOrderStatus(
        orderId: String,
        status: OrderStatus
    ): Result<Unit>
}