package com.skillet.multistoreapp.presentation.registerStore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillet.multistoreapp.core.model.Store
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

data class UiState(
    val storeName: String = "",
    val storeDescription: String = "",
    val soreCategory: String = "",
    val isLoading: Boolean = false,
    val errorMesage: String? = null,
)

sealed interface StoreEvent {
    data class OnStoreNameChanged(
        val value: String,
    ) : StoreEvent
    data class OnStoreDescriptionChanged(
        val value: String,
    ) : StoreEvent
    data class OnStoreCategoryChanged(
        val value: String,
    ) : StoreEvent
    data object OnNextClick: StoreEvent

}

sealed interface StoreEffect {
    data class ShowMessage(
        val message: String,
    ) : StoreEffect
    data object NavigateToStoreHome : StoreEffect
}

@HiltViewModel
class RegisterStoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val selectUid: String = requireNotNull(
        savedStateHandle["sellerUid"]
    ){
        "El sellerUid es requerido"
    }
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<StoreEffect>()
    val effect: SharedFlow<StoreEffect> = _effect.asSharedFlow()

    fun onEvent(event: StoreEvent){
        when(event){
            is StoreEvent.OnStoreNameChanged -> updateStorename(event.value)
            is StoreEvent.OnStoreDescriptionChanged -> updateStoreDescrption(event.value)
            is StoreEvent.OnStoreCategoryChanged -> updateStoreCategory(event.value)
            is StoreEvent.OnNextClick -> registerStore()
        }
    }

    private fun updateStorename(value: String) {
        _uiState.update { current ->
            current.copy(
                storeName = value,
                errorMesage = null
            )
        }
    }
    private fun updateStoreDescrption(value: String) {
        _uiState.update { current ->
            current.copy(
                storeDescription = value,
                errorMesage = null
            )
        }
    }
    private fun updateStoreCategory(value: String) {
        _uiState.update { current ->
            current.copy(
                soreCategory = value,
                errorMesage = null
            )
        }
    }

    private fun registerStore(){
        if(_uiState.value.isLoading) return


        viewModelScope.launch {
            //validaciones
            val storeNameTrimed: String = _uiState.value.storeName.trim()
            val storeDescriptionTrimed: String = _uiState.value.storeDescription.trim()
            val storeCategoryTrimed: String = _uiState.value.soreCategory.trim()

            if(storeNameTrimed.isEmpty()){
                _effect.emit(StoreEffect.ShowMessage("Ingresa el nombre de la tienda"))
                return@launch
            }
            if(storeDescriptionTrimed.isEmpty()){
                _effect.emit(StoreEffect.ShowMessage("Ingresa la descripción de la tienda"))
                return@launch
            }
            if(storeCategoryTrimed.isEmpty()){
                _effect.emit(StoreEffect.ShowMessage("Ingresa la categoría de la tienda"))
                return@launch
            }

            //pasamos a estado de carga
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMesage = null
                )
            }

            //

            val store = Store(
                sellerId = selectUid, //uid de vendedor
                name = storeNameTrimed,
                description = storeDescriptionTrimed,
                category = storeCategoryTrimed
            )

            val result = storeRepository.createStore(store)

            result.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        storeName = "",
                        storeDescription = "",
                        soreCategory = "",
                        isLoading = false,
                        errorMesage = null
                    )
                }
                _effect.emit(StoreEffect.ShowMessage("La tienda se a creado con éxito"))
                _effect.emit(StoreEffect.NavigateToStoreHome)
            }.onFailure {e ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMesage = e.message ?: "No se pudo registrar la tienda"
                    )
                }
                _effect.emit(StoreEffect.ShowMessage(e.message ?: "No se pudo registrar la tienda"))
            }
        }
    }

}