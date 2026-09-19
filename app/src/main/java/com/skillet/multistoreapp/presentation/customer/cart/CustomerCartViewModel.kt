package com.skillet.multistoreapp.presentation.customer.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillet.multistoreapp.core.model.CartItem
import com.skillet.multistoreapp.domain.repository.AuthRepository
import com.skillet.multistoreapp.domain.repository.CartRepository
import com.skillet.multistoreapp.domain.repository.OrderRepository
import com.skillet.multistoreapp.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerCartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val isCreatingOrder: Boolean = false,
    val totalPrice: Double = 0.0,
    val errorMessage: String? = null,
)

sealed interface CustomerCartEffect {
    data class ShowMessage(
        val message: String,
    ) : CustomerCartEffect

    data class openWhatsApp(
        val phoneNumber: String,
        val message: String,
    ) : CustomerCartEffect
}

sealed interface CustomerCartEvent {
    object ClearCart : CustomerCartEvent
    data class RemoveProduct(
        val productId: String,
    ) : CustomerCartEvent

    data object CreateOrder : CustomerCartEvent
}

@HiltViewModel
class CustomerCartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerCartUiState())
    val uiState: StateFlow<CustomerCartUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CustomerCartEffect>()
    val effect: SharedFlow<CustomerCartEffect> = _effect.asSharedFlow()


    fun onEvent(event: CustomerCartEvent) {
        when (event) {
            is CustomerCartEvent.RemoveProduct -> removeProduct(
                productId = event.productId,
            )

            is CustomerCartEvent.ClearCart -> clearCart()

            is CustomerCartEvent.CreateOrder -> createOrder()

        }
    }

    init {
        loadCart()
    }

    private fun createOrder() {
        viewModelScope.launch {
            if (_uiState.value.isCreatingOrder) return@launch

            _uiState.update { current ->
                current.copy(
                    isCreatingOrder = true,
                    errorMessage = null
                )
            }
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _uiState.update { current ->
                    current.copy(
                        isCreatingOrder = false,
                        errorMessage = "No hay usuario logueado"
                    )
                }
                _effect.emit(
                    CustomerCartEffect.ShowMessage(
                        message = "No hay usuario logueado"
                    )
                )
                return@launch
            }
            val cartItems = _uiState.value.cartItems

            if (cartItems.isEmpty()) {
                _uiState.update { current ->
                    current.copy(
                        isCreatingOrder = false
                    )
                }
                _effect.emit(
                    CustomerCartEffect.ShowMessage(
                        message = "El carrito está vacío"
                    )
                )
                return@launch
            }

            val storeId = cartItems.first().product.storeId

            if (storeId.isEmpty()) {
                _uiState.update { current ->
                    current.copy(
                        isCreatingOrder = false,
                        errorMessage = "No se pudo identificar la tienda"
                    )
                }
                _effect.emit(
                    CustomerCartEffect.ShowMessage(
                        message = "No se pudo identificar la tienda"
                    )
                )
                return@launch
            }

            val hasProductFromAnotheStore = cartItems.any { cartItem ->
                cartItem.product.storeId != storeId
            }

            if (hasProductFromAnotheStore) {
                _uiState.update { current ->
                    current.copy(
                        isCreatingOrder = false,
                        errorMessage = "El carrito solo puede contener productos de la misma tienda"
                    )
                }
                _effect.emit(
                    CustomerCartEffect.ShowMessage(
                        message = "El carrito solo puede contener productos de la misma tienda"
                    )
                )
                return@launch
            }

            val store = storeRepository.getStoreById(storeId).getOrElse { error ->
                _uiState.update { current ->
                    current.copy(
                        isCreatingOrder = false,
                        errorMessage = error.message
                            ?: "Error desconocido al obtener la tienda"
                    )
                }
                _effect.emit(
                    CustomerCartEffect.ShowMessage(
                        message = error.message
                            ?: "Error desconocido al obtener la tienda"
                    )
                )
                return@launch
            }

            if (store!!.sellerId.isBlank()) {
                _uiState.update { current ->
                    current.copy(
                        isCreatingOrder = false,
                        errorMessage = "No se pudo identificar al vendedor de la tienda"
                    )
                }
                _effect.emit(
                    CustomerCartEffect.ShowMessage(
                        message = "No se pudo identificar la tienda"
                    )
                )
            }

            val seller = authRepository.getUserById(store.sellerId).getOrElse { error ->
                _uiState.update { current ->
                    current.copy(
                        isCreatingOrder = false,
                        errorMessage = error.message
                            ?: "No se pudo obtener la informacion del vendedor"
                    )
                }
                _effect.emit(
                    CustomerCartEffect.ShowMessage(
                        message = error.message
                            ?: "No se pudo obtener la informacion del vendedor"
                    )
                )
                return@launch
            }

            val sellerPhone = seller.phone

            if (sellerPhone.isBlank()) {
                _uiState.update { current ->
                    current.copy(
                        isCreatingOrder = false,
                        errorMessage = "El vendedor no tiene un número de teléfono registrado"
                    )
                }
                _effect.emit(
                    CustomerCartEffect.ShowMessage(
                        message = "El vendedor no tiene un número de teléfono registrado"
                    )
                )
                return@launch
            }

            val orderTotal = cartItems.sumOf { cartItem ->
                cartItem.product.price * cartItem.quantity
            }
            val productDetail = cartItems.joinToString(
                separator = "\n"
            ) { cartItem ->
                val subtotal = cartItem.product.price * cartItem.quantity
                "• ${cartItem.product.name} (x${cartItem.quantity}) - ${"%.2f".format(subtotal)} USD"
            }

            val whatsappMessage = """
                |Hola, acabo de realizar un pedido en *${store.name}*
                |
                |*Cliente:* ${currentUser.firstName} ${currentUser.lastName}
                |
                |*Productos:*
                |$productDetail
                |
                |*Total:* *${"%.2f".format(orderTotal)} USD*
                |
                |Quedo atent@ a la confirmación del pedido.
            """.trimMargin()


            orderRepository
                .createOrder(cartItems)
                .onSuccess {
                    cartRepository.clearCart()
                        .onSuccess {
                            _uiState.update { current ->
                                current.copy(
                                    isCreatingOrder = false,
                                    errorMessage = null
                                )
                            }
                            _effect.emit(
                                CustomerCartEffect.ShowMessage(
                                    message = "Pedido creado correctamente"
                                )
                            )

                            _effect.emit(
                                CustomerCartEffect.openWhatsApp(
                                    phoneNumber = sellerPhone,
                                    message = whatsappMessage
                                )
                            )
                        }.onFailure { error ->
                            _uiState.update { current ->
                                current.copy(
                                    isCreatingOrder = false,
                                    errorMessage = error.message
                                        ?: "El pedido se creó correctamente pero no se pudo vaciar el carrito"
                                )
                            }
                            _effect.emit(
                                CustomerCartEffect.ShowMessage(
                                    message = error.message
                                        ?: "El pedido se creó correctamente pero no se pudo vaciar el carrito"
                                )
                            )

                        }
                }.onFailure { error ->
                    _uiState.update { current ->
                        current.copy(
                            isCreatingOrder = false,
                            errorMessage = error.message
                                ?: "Error desconocido al crear el pedido"
                        )
                    }
                    _effect.emit(
                        CustomerCartEffect.ShowMessage(
                            message = error.message ?: "Error desconocido al crear el pedido"
                        )
                    )
                }

        }

    }

    private fun loadCart() {
        viewModelScope.launch {
            cartRepository
                .getCartItemsFlorw()
                .onStart {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                }.catch { error ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = error.message
                                ?: "Error desconocido al obtener el carrito"
                        )
                    }
                }.collect { items ->

                    val totalAmount = items.sumOf { cartItem ->
                        cartItem.product.price * cartItem.quantity
                    }
                    _uiState.update { current ->
                        current.copy(
                            cartItems = items,
                            totalPrice = totalAmount,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun removeProduct(productId: String) {
        viewModelScope.launch {
            cartRepository
                .removeProduct(productId)
                .onSuccess {
                    _effect.emit(
                        CustomerCartEffect.ShowMessage(
                            message = "Producto eliminado del carrito"
                        )
                    )
                }.onFailure { error ->
                    _effect.emit(
                        CustomerCartEffect.ShowMessage(
                            message = error.message
                                ?: "Error desconocido al eliminar el producto del carrito"
                        )
                    )
                }
        }
    }

    private fun clearCart() {
        viewModelScope.launch {
            cartRepository
                .clearCart()
                .onSuccess {
                    _effect.emit(
                        CustomerCartEffect.ShowMessage(
                            message = "Carrito vaciado correctamente"
                        )
                    )
                }.onFailure { error ->
                    _effect.emit(
                        CustomerCartEffect.ShowMessage(
                            message = error.message ?: "Error desconocido al eliminar el carrito"
                        )
                    )
                }
        }
    }


}