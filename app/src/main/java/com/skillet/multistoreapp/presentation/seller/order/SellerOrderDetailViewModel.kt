package com.skillet.multistoreapp.presentation.seller.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillet.multistoreapp.core.model.Order
import com.skillet.multistoreapp.core.model.OrderStatus
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SellerOrderDetailUiState(
    val orderId: String = "",
    val order: Order? = null,
    val isLoading: Boolean = false,
    val isUpdatingStatus: Boolean = false,
    val errorMessage: String? = null,

    )

sealed interface SellerOrderDetailEffect {
    data class ShowMessage(
        val message: String,
    ) : SellerOrderDetailEffect

    data object OrderStatusUpdated : SellerOrderDetailEffect
}

sealed interface SellerOrderDetailEvent {
    data class UpdateOrderStatus(
        val status: OrderStatus
    ) : SellerOrderDetailEvent
}

@HiltViewModel
class SellerOrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val orderId: String = savedStateHandle.get<String>("orderId") ?: ""
    private val _uiState = MutableStateFlow(SellerOrderDetailUiState())
    val uiState: StateFlow<SellerOrderDetailUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SellerOrderDetailEffect>()
    val effect: SharedFlow<SellerOrderDetailEffect> = _effect.asSharedFlow()

    fun onEvent(event: SellerOrderDetailEvent) {
        when (event) {
            is SellerOrderDetailEvent.UpdateOrderStatus -> updateOrderStatus(event.status)
        }
    }

    init {
        if(
            orderId.isNotBlank()
        ){
            loadOrder()
        }else{
            viewModelScope.launch {
                _effect.emit(
                    SellerOrderDetailEffect.ShowMessage(
                        message = "Pedido no valido"
                    )
                )
            }
        }
    }
    private fun loadOrder() {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            orderRepository
                .getOrderById(orderId)
                .onSuccess { order ->
                    _uiState.update { current ->
                        current.copy(
                            order = order,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = error.message
                                ?: "No se pudo obtener el pedido"
                        )
                    }
                    _effect.emit(
                        SellerOrderDetailEffect.ShowMessage(
                            message = error.message
                                ?: "No se pudo obtener el pedido"
                        )
                    )
                }
        }
    }

    fun updateOrderStatus(
        status: OrderStatus
    ) {
        val currentOrder = uiState.value.order ?: return

        if(currentOrder.status == status){
            viewModelScope.launch {
                _effect.emit(
                    SellerOrderDetailEffect.ShowMessage(
                        message = "El pedido ya esta en este estado"
                    )
                )
            }
            return
        }

        if(_uiState.value.isUpdatingStatus) return

        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isUpdatingStatus = true
                )
            }
            orderRepository
                .updateOrderStatus(
                    orderId = orderId,
                    status = status
                )
                .onSuccess {
                    _uiState.update { current ->
                        current.copy(
                            order = current.order?.copy(
                                status = status
                            ),
                            isUpdatingStatus = false,
                            errorMessage = null
                        )
                    }
                    _effect.emit(
                        SellerOrderDetailEffect.OrderStatusUpdated
                    )
                }
                .onFailure { error ->
                    _uiState.update { current ->
                        current.copy(
                            isUpdatingStatus = false,
                            errorMessage = error.message
                                ?: "No se pudo actualizar el estado del pedido"
                        )
                    }
                    _effect.emit(
                        SellerOrderDetailEffect.ShowMessage(
                            message = error.message
                                ?: "No se pudo actualizar el estado del pedido"
                        )
                    )
                }
        }
    }
}