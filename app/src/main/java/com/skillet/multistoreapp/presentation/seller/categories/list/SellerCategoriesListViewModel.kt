package com.skillet.multistoreapp.presentation.seller.categories.list

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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SellerCatgoriesListUiState(
    val categories: List<Category> = emptyList(),
    val listLoading: Boolean = false,
    val errorMessage: String? = null
)
sealed interface SellerCatgoriesListEvent{
    data class DeleteCategory(
        val category: Category
    ): SellerCatgoriesListEvent

}
sealed interface SellerCatgoriesListEffect{
    data class ShowMessage(
        val message: String
    ): SellerCatgoriesListEffect
}


@HiltViewModel
class SellerCategoriesListViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SellerCatgoriesListUiState())
    val uiState: StateFlow<SellerCatgoriesListUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SellerCatgoriesListEffect>()
    val effect: SharedFlow<SellerCatgoriesListEffect> = _effect.asSharedFlow()

    init {
        observeCategoryInRealTime()
    }


    fun onEvent(event: SellerCatgoriesListEvent){
        when(event){
            is SellerCatgoriesListEvent.DeleteCategory -> deleteCategory(event.category)
        }
    }
    private var isObservingCategories = false

    fun observeCategoryInRealTime(){
        if(isObservingCategories){
            return
        }

        isObservingCategories = true

        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    listLoading = true
                )
            }
            val user = authRepository.getCurrentUser()

            val sellerId = user?.uid

            if(sellerId.isNullOrBlank()){
                _uiState.update { current ->
                    current.copy(
                        listLoading = false
                    )
                }
                _effect.emit(SellerCatgoriesListEffect.ShowMessage("No hay usuario autenticado"))
                return@launch
            }
            try {
                val storeResult = storeRepository.getStoreBySeller(sellerId)

                storeResult.onSuccess { store ->
                    if(store == null){
                        _uiState.update { current ->
                            current.copy(
                                listLoading = false
                            )
                        }
                        _effect.emit(SellerCatgoriesListEffect.ShowMessage("No tienes una tienda registrada"))
                        return@onSuccess
                    }

                    categoryRepository.getCategoriesByStoreFlow(store.id).collect{ categoriesList ->
                        _uiState.update { current ->
                            current.copy(
                                categories = categoriesList,
                                listLoading = false
                            )

                        }
                    }
                }.onFailure { error ->
                    _uiState.update { current ->
                        current.copy(
                            listLoading = false,
                            errorMessage = error.message ?: "Error al obtener la tienda"
                        )
                    }
                    _effect.emit(SellerCatgoriesListEffect.ShowMessage("Error al obtener la tienda"))
                }
            }catch (e: Exception){
                isObservingCategories = false

                _uiState.update { current ->
                    current.copy(
                        listLoading = false,
                        errorMessage = e.message ?: "Error al obtener las categorias"
                    )
                }
                _effect.emit(SellerCatgoriesListEffect.ShowMessage("Error al obtener las categorias"))
            }
        }
    }

    private fun deleteCategory(category: Category){
        viewModelScope.launch {
            val result = categoryRepository.deleteCategory(category.id)

            result.onSuccess {
                _effect.emit(SellerCatgoriesListEffect.ShowMessage("Categoria eliminada correctamente"))
            }.onFailure { error ->
                _effect.emit(SellerCatgoriesListEffect.ShowMessage(error.message ?: "Error al eliminar la categoria"))
            }
        }
    }

}