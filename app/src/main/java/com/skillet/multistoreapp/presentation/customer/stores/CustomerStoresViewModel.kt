package com.skillet.multistoreapp.presentation.customer.stores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillet.multistoreapp.core.model.Store
import com.skillet.multistoreapp.domain.repository.AuthRepository
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

data class CustomerStoresUiState(
    val stores: List<Store> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface CustomerStoresEffect {
    data class ShowMessage(
        val message: String
    ): CustomerStoresEffect
}

@HiltViewModel
class CustomerStoresViewModel @Inject constructor(
    private val storeRepository: StoreRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(CustomerStoresUiState())
    val uiState: StateFlow<CustomerStoresUiState> = _uiState.asStateFlow()
    private val _effect = MutableSharedFlow<CustomerStoresEffect>()
    val effect: SharedFlow<CustomerStoresEffect> = _effect.asSharedFlow()

    init{
        loadStores()
    }
    private fun loadStores(){
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }
            storeRepository.getAllStores()
                .onSuccess { stores ->
                    _uiState.update { current ->
                        current.copy(
                            stores = stores,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error desconocido al obtener las tiendas"
                        )
                    }
                    _effect.emit(
                        CustomerStoresEffect.ShowMessage(
                            message = error.message ?: "Error desconocido al obtener las tiendas"
                        )
                    )
                }
        }
    }
}