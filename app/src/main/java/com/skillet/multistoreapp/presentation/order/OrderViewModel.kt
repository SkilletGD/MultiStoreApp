package com.skillet.multistoreapp.presentation.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillet.multistoreapp.core.model.Order
import com.skillet.multistoreapp.domain.repository.AuthRepository
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

data class OrderUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface OrderEffect {
    data class ShowMessage(
        val message: String,
    ) : OrderEffect
}

sealed interface OrderEvent {
    data object LoadCustomerOrdes : OrderEvent
    data object LoadSellerOrders : OrderEvent
}

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<OrderEffect>()
    val effect: SharedFlow<OrderEffect> = _effect.asSharedFlow()

    fun onEvent(event: OrderEvent) {
        when (event) {
            is OrderEvent.LoadCustomerOrdes -> loadCustomerOrders()
            is OrderEvent.LoadSellerOrders -> loadSellerStoreOrders()
        }
    }

    private fun loadCustomerOrders() {
        viewModelScope.launch {
            orderRepository
                .getCustomerOrders()
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
                                ?: "No se pudo obtener los pedidos"
                        )
                    }
                    _effect.emit(
                        OrderEffect.ShowMessage(
                            message = error.message ?: "No se pudo obtener los pedidos"
                        )
                    )
                }.collect { orders ->
                    val sortedOrders = orders.sortedByDescending { order ->
                        order.created
                    }
                    _uiState.update { current ->
                        current.copy(
                            orders = orders,
                            isLoading = false,
                            errorMessage = null
                        )
                    }

                }
        }
    }

    private fun loadSellerStoreOrders() {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val currentUser = authRepository.getCurrentUser()

            if (currentUser == null) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = "No existe un vendedor autenticado"
                    )
                }
                _effect.emit(
                    OrderEffect.ShowMessage(
                        message = "No existe un vendedor autenticado"
                    )
                )
                return@launch
            }

            val sellerId = currentUser.uid

            if (sellerId.isEmpty()) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = "No pudo identificar al vendedor"
                    )
                }
                _effect.emit(
                    OrderEffect.ShowMessage(
                        message = "No pudo identificar al vendedor"
                    )
                )
            }

            val storeResult = storeRepository.getStoreBySeller(sellerId)

            storeResult.onSuccess { store ->
                if (store == null) {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = "No se encontro la tienda del vendedor"
                        )
                    }
                    _effect.emit(
                        OrderEffect.ShowMessage(
                            message = "No se encontro la tienda del vendedor"
                        )
                    )
                    return@launch
                }
                val storeId = store.id

                if (storeId.isEmpty()) {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = "No se encontro la tienda del vendedor"
                        )
                    }
                    _effect.emit(
                        OrderEffect.ShowMessage(
                            message = "No se encontro la tienda del vendedor"
                        )
                    )
                    return@launch
                }

                loadStoreOrder(
                    storeId = storeId
                )

            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = error.message
                            ?: "No se pudo obtener la tienda del vendedor"
                    )
                }
                _effect.emit(
                    OrderEffect.ShowMessage(
                        message = error.message
                            ?: "No se pudo obtener la tienda del vendedor"
                    )
                )
            }
        }
    }

    private fun loadStoreOrder(
        storeId: String,
    ) {
        val id = storeId.trim()
        if (id.isEmpty()) {
            viewModelScope.launch {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = "No se pudo identificar la tienda"
                    )
                }
                _effect.emit(
                    OrderEffect.ShowMessage(
                        message = "No se pudo identificar la tienda"
                    )
                )
            }
        }

        viewModelScope.launch {
            orderRepository.getStoreOrders(storeId)
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
                                ?: "No se pudo obtener los pedidos de la tienda"
                        )
                    }
                    _effect.emit(
                        OrderEffect.ShowMessage(
                            message = error.message
                                ?: "No se pudo obtener los pedidos de la tienda"
                        )
                    )

                }.collect { orders ->
                    val sortedOrders = orders.sortedByDescending { order ->
                        order.created
                    }
                    _uiState.update { current ->
                        current.copy(
                            orders = sortedOrders,
                            isLoading = false,
                            errorMessage = null
                        )

                    }
                }
        }

    }
}
