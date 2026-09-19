package com.skillet.multistoreapp.presentation.seller.categories.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillet.multistoreapp.core.model.Category
import com.skillet.multistoreapp.domain.repository.AuthRepository
import com.skillet.multistoreapp.domain.repository.CategoryRepository
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

data class SellerCategeoryFormUiState(
    val name: String = "",
    val storeId: String = "",
    val isEditMode: Boolean = false,

    val isLoading: Boolean = false,
    val errorMessage: String? = "",
)

sealed interface SellerCategoryFormEvent {
    data class OnNameChanged(
        val name: String,
    ) : SellerCategoryFormEvent

    data object OnSaveClick : SellerCategoryFormEvent
    data object OnClearForm : SellerCategoryFormEvent
}

sealed interface SellerCategoryFormEffect {
    data class ShowMessage(
        val message: String,
    ) : SellerCategoryFormEffect

    data object NavigateBack : SellerCategoryFormEffect
}

@HiltViewModel
class SellerCategoryFormViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val categoryId: String? = savedStateHandle["categoryId"]

    private val _uiState = MutableStateFlow(
        SellerCategeoryFormUiState(
            isEditMode = categoryId != null
        )
    )
    val uiState: StateFlow<SellerCategeoryFormUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SellerCategoryFormEffect>()
    val effect: SharedFlow<SellerCategoryFormEffect> = _effect.asSharedFlow()

    fun onEvent(event: SellerCategoryFormEvent) {
        when (event) {
            is SellerCategoryFormEvent.OnNameChanged -> updateName(event.name)

            SellerCategoryFormEvent.OnSaveClick -> saveCategory()

            SellerCategoryFormEvent.OnClearForm -> clearForm()
        }
    }


    init {
        loadStoreId()

        if (categoryId != null) {
            loadCategoryById(categoryId)
        }
    }

    private fun loadStoreId() {
        viewModelScope.launch {

            val user = authRepository.getCurrentUser()

            val sellerId = user?.uid

            if (sellerId.isNullOrBlank()) {
                _effect.emit(SellerCategoryFormEffect.ShowMessage("No hay usuario autenticado"))
                return@launch
            }

            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val result = storeRepository.getStoreBySeller(sellerId)

            result.onSuccess { store ->
                if (store == null) {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false
                        )
                    }
                    _effect.emit(SellerCategoryFormEffect.ShowMessage("No tienes una tienda registrada"))
                    return@onSuccess
                }
                _uiState.update { current ->
                    current.copy(
                        storeId = store.id,
                        isLoading = false
                    )
                }

            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
                _effect.emit(SellerCategoryFormEffect.ShowMessage("Error al cargar la tienda"))
            }
        }

    }

    private fun updateName(value: String) {
        _uiState.update { current ->
            current.copy(
                name = value
            )
        }
    }

    private fun loadCategoryById(id: String) {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isLoading = true
                )
            }

            categoryRepository.getCategoriesById(id)
                .onSuccess { category ->
                    _uiState.update { current ->
                        current.copy(
                            name = category!!.name,
                            storeId = category.storeId,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al cargar la categoria"
                        )
                    }
                    _effect.emit(
                        SellerCategoryFormEffect.ShowMessage(
                            error.message ?: "Error al cargar la categoria"
                        )
                    )
                }
        }
    }

    private fun saveCategory() {
        if (_uiState.value.isEditMode) true

        val state = _uiState.value

        val name = state.name.trim()

        val storeId = state.storeId

        if (name.isBlank()) {
            viewModelScope.launch {
                _effect.emit(SellerCategoryFormEffect.ShowMessage("Ingrese el nombre"))
            }
            return
        }
        if (storeId.isBlank()) {
            viewModelScope.launch {
                _effect.emit(SellerCategoryFormEffect.ShowMessage("No hay tienda registrada"))
            }
            return
        }

        if (state.isEditMode) {
            updateCategory(name)
        } else {
            createCategory(
                name,
                storeId
            )
        }
    }

    private fun createCategory(
        name: String,
        storeId: String,
    ) {
        viewModelScope.launch {

            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val category = Category(
                name = name,
                storeId = storeId
            )

            val result = categoryRepository.createCategory(category)

            result.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false
                    )
                }

                clearFormInternal()

                _effect.emit(SellerCategoryFormEffect.ShowMessage("Categoria creada correctamente"))
                _effect.emit(SellerCategoryFormEffect.NavigateBack)
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
                _effect.emit(
                    SellerCategoryFormEffect.ShowMessage(
                        error.message ?: "Error al crear la categoria"
                    )
                )
            }
        }
    }

    private fun updateCategory(
        name: String,
    ) {
        viewModelScope.launch {
            val id = categoryId ?: return@launch

            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val category = Category(
                id = id,
                name = name,
                storeId = _uiState.value.storeId
            )

            val result = categoryRepository.updateCategory(category)

            result.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false
                    )
                }
                _effect.emit(SellerCategoryFormEffect.ShowMessage("Categoria actualizada correctamente"))
                _effect.emit(SellerCategoryFormEffect.NavigateBack)
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al actualizar la categoria"
                    )
                }
                _effect.emit(
                    SellerCategoryFormEffect.ShowMessage(
                        error.message ?: "Error al actualizar la categoria"
                    )
                )
            }
        }
    }

    private fun clearFormInternal() {
        _uiState.update { current ->
            current.copy(
                name = "",
                errorMessage = null,
                isEditMode = false
            )
        }
    }

    private fun clearForm() {
        clearFormInternal()
        viewModelScope.launch {
            _effect.emit(
                SellerCategoryFormEffect.ShowMessage(
                    "Formulario limpiado"
                )
            )
        }
    }
}