package com.skillet.multistoreapp.core.model

data class Order (
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val storeId: String = "",
    val storeName: String = "",
    val sellerId: String = "",
    val items: List<OrderItem> = emptyList(),
    val total: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDIENTE,
    val created: Long = System.currentTimeMillis()
)

enum class OrderStatus{
    PENDIENTE,
    CONFIRMADO,
    EN_CAMINO,
    ENTREGADO,
    CANCELADO
}