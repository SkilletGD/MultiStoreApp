package com.skillet.multistoreapp.presentation.customer.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillet.multistoreapp.core.model.Product
import com.skillet.multistoreapp.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerProductsUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
sealed interface CustomerProductsEffect {
    data class ShowMessage(
        val message: String
    ): CustomerProductsEffect
}

@HiltViewModel
class CustomerProductsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val storeId: String = savedStateHandle.get<String>("storeId") ?: ""

    private val _uiState = MutableStateFlow(CustomerProductsUiState())
    val uiState: StateFlow<CustomerProductsUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CustomerProductsEffect>()
    val effect: SharedFlow<CustomerProductsEffect> = _effect.asSharedFlow()


    init{
        if(storeId.isNotBlank()){
            loadProducts()
        }else{
            viewModelScope.launch {
                _effect.emit(
                    CustomerProductsEffect.ShowMessage(
                        message = "Tienda no válida"
                    )
                )
            }
        }
    }
    private fun loadProducts(
    ){
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }
            productRepository
                .getProductsByStoreFlow(storeId)
                .catch { exception->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "No se pudo cargar los productos desconocido"
                        )
                    }
                    _effect.emit(
                        CustomerProductsEffect.ShowMessage(
                            message = exception.message ?: "No se pudo cargar los productos"
                        )
                    )
                }
                .collect{ products ->
                    _uiState.update { current ->
                        current.copy(
                            products = products,
                            isLoading = false
                        )
                    }
                }
        }
    }

}