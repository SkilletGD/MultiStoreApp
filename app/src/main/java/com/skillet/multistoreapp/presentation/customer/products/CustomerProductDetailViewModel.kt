package com.skillet.multistoreapp.presentation.customer.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillet.multistoreapp.core.model.Product
import com.skillet.multistoreapp.domain.repository.CartRepository
import com.skillet.multistoreapp.domain.repository.ProductRepository
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

data class CustomerProductDetailUiState(
    val productId: String = "",
    val product: Product? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface CustomerProductDetailEffect {
    data class ShowMessage(
        val message: String
    ): CustomerProductDetailEffect
}

sealed interface CustomerProductDetailEvent {
    object AddProductToCart: CustomerProductDetailEvent
}

@HiltViewModel
class CustomerProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val productId: String = savedStateHandle.get<String>("productId") ?: ""

    private val _uiState = MutableStateFlow(CustomerProductDetailUiState(productId = productId))
    val uiState: StateFlow<CustomerProductDetailUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CustomerProductDetailEffect>()
    val effect: SharedFlow<CustomerProductDetailEffect> = _effect.asSharedFlow()

    fun onEvent(event: CustomerProductDetailEvent){
        when(event){
            CustomerProductDetailEvent.AddProductToCart -> addProductToCart()
        }
    }
    init {
        if(productId.isNotBlank()){
            loadProduct()
        }else{
            viewModelScope.launch {
                _effect.emit(
                    CustomerProductDetailEffect.ShowMessage(
                        message = "Producto no válido"
                    )
                )
            }
        }
    }

    private fun loadProduct(){
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }
            productRepository
                .getProductById(productId)
                .onSuccess { product ->
                    _uiState.update { current ->
                        current.copy(
                            product = product,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error desconocido al obtener el producto"
                        )
                    }
                    _effect.emit(
                        CustomerProductDetailEffect.ShowMessage(
                            message = error.message ?: "Error desconocido al obtener el producto"
                        )
                    )
                }
        }
    }

    fun addProductToCart(){
        val product = uiState.value.product ?: return

        viewModelScope.launch {
            cartRepository.addProduct(
                product = product
            ).onSuccess {
                _effect.emit(
                    CustomerProductDetailEffect.ShowMessage(
                        message = "Producto agregado al carrito"
                    )
                )
            }.onFailure { error ->
                _effect.emit(
                    CustomerProductDetailEffect.ShowMessage(
                        message = error.message ?: "Error desconocido al agregar el producto al carrito"
                    )
                )
            }
        }
    }

}