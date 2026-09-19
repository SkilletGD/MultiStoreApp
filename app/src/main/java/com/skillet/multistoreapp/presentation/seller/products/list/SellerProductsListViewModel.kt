package com.skillet.multistoreapp.presentation.seller.products.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillet.multistoreapp.core.model.Product
import com.skillet.multistoreapp.domain.repository.AuthRepository
import com.skillet.multistoreapp.domain.repository.ProductRepository
import com.skillet.multistoreapp.domain.repository.ProductStorageRepository
import com.skillet.multistoreapp.domain.repository.StoreRepository
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

data class SellerProductsListUiState(
    val storeId: String = "",
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showSecurityDialog: Boolean = false,
    val productToDelete: Product? = null
)
sealed interface SellerProductsListEffect {
    data class ShowMessage(
        val message: String
    ): SellerProductsListEffect
}

@HiltViewModel
class SellerProductsListViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository,
    private val productRepository: ProductRepository,
    private val productStorage: ProductStorageRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(SellerProductsListUiState())
    val uiState: StateFlow<SellerProductsListUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SellerProductsListEffect>()
    val effect: SharedFlow<SellerProductsListEffect> = _effect.asSharedFlow()


    init {
        loadStore()
    }
    private fun loadStore(){
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val sellerId = authRepository.getCurrentUser()?.uid

            if(sellerId.isNullOrBlank()) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = "No hay usuario autenticado"
                    )
                }
                _effect.emit(
                    SellerProductsListEffect.ShowMessage(
                        message = "No hay usuario autenticado"
                    )
                )
                return@launch
            }
            storeRepository.getStoreBySeller(sellerId)
                .onSuccess { store ->
                    if(store == null){
                        _uiState.update { current ->
                            current.copy(
                                isLoading = false
                            )
                        }
                        _effect.emit(
                            SellerProductsListEffect.ShowMessage(
                                message = "No tienes una tienda registrada"
                            )
                        )
                        return@onSuccess
                    }
                    _uiState.update { current ->
                        current.copy(
                            storeId = store.id
                        )
                    }

                    loadProducts(
                        storeId = store.id
                    )
                }
                .onFailure { error ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error desconocido al obtener la tienda"
                        )
                    }
                }

        }
    }

    private fun loadProducts(
        storeId: String
    ){
        if(storeId.isBlank()) return

        viewModelScope.launch {
            productRepository.getProductsByStoreFlow(
                storeId = storeId
            ).catch { error ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error desconocido al obtener los productos"
                    )
                }
                _effect.emit(
                    SellerProductsListEffect.ShowMessage(
                        message = error.message ?: "Error desconocido al obtener los productos"
                    )
                )
            }.collect { products ->
                _uiState.update { current ->
                    current.copy(
                        products = products,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteProduct(product: Product) {
        _uiState.update { it.copy(showSecurityDialog = true, productToDelete = product) }
    }

    fun onDismissSecurityDialog() {
        _uiState.update { it.copy(showSecurityDialog = false, productToDelete = null) }
    }

    fun onConfirmDelete(password: String) {
        val product = _uiState.value.productToDelete ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showSecurityDialog = false) }
            
            authRepository.verifyPassword(password).onSuccess {
                // Borrar imagen del storage
                if (product.storagePath.isNotBlank()) {
                    productStorage.deleteProductImage(product.storagePath)
                }

                // Borrar de firestore
                productRepository.deleteProduct(product.id).onSuccess {
                    _uiState.update { it.copy(isLoading = false, productToDelete = null) }
                    _effect.emit(SellerProductsListEffect.ShowMessage("Producto eliminado"))
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                    _effect.emit(SellerProductsListEffect.ShowMessage(error.message ?: "Error al eliminar"))
                }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false, productToDelete = null) }
                _effect.emit(SellerProductsListEffect.ShowMessage("Contraseña incorrecta. No se pudo eliminar."))
            }
        }
    }

}