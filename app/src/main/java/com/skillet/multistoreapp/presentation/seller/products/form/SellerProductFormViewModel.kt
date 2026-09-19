package com.skillet.multistoreapp.presentation.seller.products.form

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillet.multistoreapp.core.model.Category
import com.skillet.multistoreapp.core.model.Product
import com.skillet.multistoreapp.domain.repository.AuthRepository
import com.skillet.multistoreapp.domain.repository.CategoryRepository
import com.skillet.multistoreapp.domain.repository.ProductRepository
import com.skillet.multistoreapp.domain.repository.ProductStorageRepository
import com.skillet.multistoreapp.domain.repository.StoreRepository
import com.skillet.multistoreapp.navigation.AppRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class SellerProductFormUiState(
    val selectUri: Uri? = null,
    val imageUrl: String? = null,
    val storagePath: String? = null,
    val productId: String? = null,
    val storeId: String = "",
    val selectedCategoryId: String = "",
    val categories: List<Category> = emptyList(),
    val categoriesLoading: Boolean = false,
    val errorMessage: String? = null,
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val stock: String = "",
    val attributes: List<Pair<String, String>> = emptyList(),
    val isLoading: Boolean = false,
    val showSecurityDialog: Boolean = false,
    val pendingAction: (() -> Unit)? = null
)

sealed interface SellerProductFormEvent {
    data class OnSelectImage(
        val uri: Uri?,
    ) : SellerProductFormEvent

    data object OnClearSelection : SellerProductFormEvent

    data class OnCatgeroySelected(
        val categoryId: String,
    ) : SellerProductFormEvent

    data class OnNameChanged(
        val value: String,
    ) : SellerProductFormEvent

    data class OnDescriptionChanged(
        val value: String,
    ) : SellerProductFormEvent

    data class OnPriceChanged(
        val value: String,
    ) : SellerProductFormEvent

    data class OnStockChanged(
        val value: String,
    ) : SellerProductFormEvent

    data object OnAddAttribute : SellerProductFormEvent

    data class OnUpdateAttributeName(
        val index: Int,
        val value: String,
    ) : SellerProductFormEvent

    data class OnupdateAttributeValue(
        val index: Int,
        val value: String,
    ) : SellerProductFormEvent

    data class OnDeleteAttribute(
        val index: Int,
    ) : SellerProductFormEvent

    data object OnSaveProduct : SellerProductFormEvent

    data class OnConfirmSecurity(
        val password: String
    ) : SellerProductFormEvent

    data object OnDismissSecurity : SellerProductFormEvent
}

sealed interface SellerProductFormEffect {
    data class ShowMessage(
        val message: String,
    ) : SellerProductFormEffect

    data object NavigateBack : SellerProductFormEffect
}

@HiltViewModel
class SellerProductFormViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val storeRepository: StoreRepository,
    private val authRepository: AuthRepository,
    private val productStorage: ProductStorageRepository,
    private val productFirestore: ProductRepository,
    @ApplicationContext private val appContext: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(SellerProductFormUiState())
    val uiState: StateFlow<SellerProductFormUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SellerProductFormEffect>()
    val effect: SharedFlow<SellerProductFormEffect> = _effect.asSharedFlow()

    private val productId: String? = savedStateHandle[AppRoute.SellerProductForm.ARG_PRODUCT_ID]

    fun onEvent(event: SellerProductFormEvent) {
        when (event) {
            is SellerProductFormEvent.OnSelectImage -> onImageSelected(event.uri)
            is SellerProductFormEvent.OnClearSelection -> onClearSelectedImage()
            is SellerProductFormEvent.OnCatgeroySelected -> onCategoySelected(event.categoryId)
            is SellerProductFormEvent.OnNameChanged -> updatename(event.value)
            is SellerProductFormEvent.OnDescriptionChanged -> updateDescription(event.value)
            is SellerProductFormEvent.OnPriceChanged -> updatePrice(event.value)
            is SellerProductFormEvent.OnStockChanged -> updateStock(event.value)
            SellerProductFormEvent.OnAddAttribute -> addAttribute()
            is SellerProductFormEvent.OnUpdateAttributeName -> updateAttributeName(
                index = event.index,
                name = event.value
            )
            is SellerProductFormEvent.OnupdateAttributeValue -> updateAttributeValue(
                index = event.index,
                value = event.value
            )
            is SellerProductFormEvent.OnDeleteAttribute -> deleteAttribute(
                index = event.index
            )
            is SellerProductFormEvent.OnSaveProduct -> saveProduct()
            is SellerProductFormEvent.OnConfirmSecurity -> confirmSecurity(event.password)
            SellerProductFormEvent.OnDismissSecurity -> dismissSecurity()

        }
    }

    private fun dismissSecurity() {
        _uiState.update { it.copy(showSecurityDialog = false, pendingAction = null) }
    }

    private fun confirmSecurity(password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showSecurityDialog = false) }
            authRepository.verifyPassword(password).onSuccess {
                _uiState.value.pendingAction?.invoke()
                _uiState.update { it.copy(pendingAction = null) }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false, pendingAction = null) }
                _effect.emit(SellerProductFormEffect.ShowMessage("Contraseña incorrecta. Operación cancelada."))
            }
        }
    }

    init {
        loadCategories()
        if (productId != null) {
            loadProduct(productId)
        }
    }

    private fun loadProduct(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            productFirestore.getProductById(id).onSuccess { product ->
                if (product != null) {
                    _uiState.update { current ->
                        current.copy(
                            productId = product.id,
                            name = product.name,
                            description = product.description,
                            price = product.price.toString(),
                            stock = product.stock.toString(),
                            selectedCategoryId = product.categoryId,
                            imageUrl = product.imageUrl,
                            storagePath = product.storagePath,
                            attributes = product.attributes.toList(),
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Producto no encontrado") }
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    private fun onImageSelected(uri: Uri?) {
        if (uri == null) {
            viewModelScope.launch {
                _effect.emit(
                    SellerProductFormEffect.ShowMessage(
                        message = "No se selecciono ninguna imagen"
                    )
                )
            }
            return
        }
        _uiState.update { current ->
            current.copy(
                selectUri = uri
            )
        }
    }

    private fun onClearSelectedImage() {
        _uiState.update { current ->
            current.copy(
                selectUri = null
            )
        }
        viewModelScope.launch {
            _effect.emit(
                SellerProductFormEffect.ShowMessage(
                    message = "Se quito la imagen seleccionada"
                )
            )
        }
    }

    private fun onCategoySelected(
        categoryId: String,
    ) {
        _uiState.update { current ->
            current.copy(
                selectedCategoryId = categoryId
            )
        }
    }

    private fun updatename(value: String) {
        _uiState.update { current ->
            current.copy(
                name = value,
                errorMessage = null
            )
        }
    }

    private fun updateDescription(value: String) {
        _uiState.update { current ->
            current.copy(
                description = value,
                errorMessage = null
            )
        }
    }

    private fun updatePrice(value: String) {
        _uiState.update { current ->
            current.copy(
                price = value,
                errorMessage = null
            )
        }
    }

    private fun updateStock(value: String) {
        _uiState.update { current ->
            current.copy(
                stock = value,
                errorMessage = null
            )
        }
    }

    private fun addAttribute() {
        val currentAttributes = _uiState.value.attributes.toMutableList()

        currentAttributes.add("" to "")

        _uiState.update { current ->
            current.copy(
                attributes = currentAttributes
            )
        }
    }

    private fun updateAttributeName(
        index: Int,
        name: String,
    ) {
        val attributes = _uiState.value.attributes.toMutableList()

        if (index !in attributes.indices) return

        val currentAttribute = attributes[index]

        attributes[index] = name to currentAttribute.second

        _uiState.update { current ->
            current.copy(
                attributes = attributes
            )
        }
    }

    private fun updateAttributeValue(
        index: Int,
        value: String,
    ) {
        val attributes = _uiState.value.attributes.toMutableList()

        if (index !in attributes.indices) return

        val currentAttribute = attributes[index]

        attributes[index] = currentAttribute.first to value

        _uiState.update { current ->
            current.copy(
                attributes = attributes
            )
        }
    }

    private fun deleteAttribute(
        index: Int,
    ) {
        val attributes = _uiState.value.attributes.toMutableList()

        if (index in attributes.indices) {
            attributes.removeAt(index)
        }

        _uiState.update { current ->
            current.copy(
                attributes = attributes
            )
        }
    }

    private fun saveProduct() {
        if (_uiState.value.isLoading) return

        val state = _uiState.value

        val name = state.name.trim()
        val description = state.description.trim()
        val categoryId = state.selectedCategoryId.trim()
        val storeId = state.storeId.trim()
        val price = state.price.toDoubleOrNull()
        val stock = state.stock.toIntOrNull()

        if (name.isBlank()) {
            viewModelScope.launch {
                _effect.emit(
                    SellerProductFormEffect.ShowMessage(
                        message = "Ingresa un nombre para el producto"
                    )
                )
            }
            return
        }
        if (description.isBlank()) {
            viewModelScope.launch {
                _effect.emit(
                    SellerProductFormEffect.ShowMessage(
                        message = "Ingresa una descripcion para el producto"
                    )
                )
            }
            return
        }
        if (categoryId.isBlank()) {
            viewModelScope.launch {
                _effect.emit(
                    SellerProductFormEffect.ShowMessage(
                        message = "Selecciona una categoria para el producto"
                    )
                )
            }
            return
        }
        if (storeId.isBlank()) {
            viewModelScope.launch {
                _effect.emit(
                    SellerProductFormEffect.ShowMessage(
                        message = "No se encontro la tienda"
                    )
                )
            }
            return
        }
        if (price == null || price <= 0) {
            viewModelScope.launch {
                _effect.emit(
                    SellerProductFormEffect.ShowMessage(
                        message = "Ingresa un precio para el producto"
                    )
                )
            }
            return
        }
        if (stock == null || stock < 0) {
            viewModelScope.launch {
                _effect.emit(
                    SellerProductFormEffect.ShowMessage(
                        message = "Ingresa un stock valido para el producto"
                    )
                )
            }
            return
        }
        if (state.selectUri == null && state.imageUrl == null) {
            viewModelScope.launch {
                _effect.emit(
                    SellerProductFormEffect.ShowMessage(
                        message = "Selecciona una imagen para el producto"
                    )
                )
            }
            return
        }

        if (state.productId == null) {
            _uiState.update { it.copy(showSecurityDialog = true, pendingAction = {
                createProduct(price = price, stock = stock)
            }) }
        } else {
            _uiState.update { it.copy(showSecurityDialog = true, pendingAction = {
                updateProduct(price = price, stock = stock)
            }) }
        }
    }

    private fun updateProduct(
        price: Double,
        stock: Int,
    ) {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val state = _uiState.value
            val uri = state.selectUri

            if (uri != null) {
                // Si seleccionó una nueva imagen, primero la subimos y borramos la anterior
                val resolver = appContext.contentResolver
                val fileResult: Result<Pair<ByteArray, String>> = runCatching {
                    val byte = readBytesFormUri(resolver, uri)
                    val extension = getExtensionFromUri(resolver, uri)
                    byte to extension
                }

                fileResult.onSuccess { (bytes, extension) ->
                    val user = authRepository.getCurrentUser()
                    val sellerId = user?.uid ?: ""
                    
                    // Borrar imagen anterior si existe
                    state.storagePath?.let { path ->
                        productStorage.deleteProductImage(path)
                    }

                    val storageResult = productStorage.uploadProductImage(
                        sellerId = sellerId,
                        storeId = state.storeId,
                        byteArray = bytes,
                        extension = extension
                    )

                    storageResult.onSuccess { (downloadUrl, storagePath) ->
                        performUpdate(
                            price = price,
                            stock = stock,
                            imageUrl = downloadUrl,
                            storagePath = storagePath
                        )
                    }.onFailure { error ->
                        handleError(error.message ?: "Error al subir la imagen")
                    }
                }.onFailure { error ->
                    handleError(error.message ?: "Error al procesar la imagen")
                }
            } else {
                // Si no cambió la imagen, solo actualizamos los campos de texto
                performUpdate(
                    price = price,
                    stock = stock,
                    imageUrl = state.imageUrl ?: "",
                    storagePath = state.storagePath ?: ""
                )
            }
        }
    }

    private suspend fun performUpdate(
        price: Double,
        stock: Int,
        imageUrl: String,
        storagePath: String
    ) {
        val state = _uiState.value
        val product = Product(
            id = state.productId ?: "",
            name = state.name,
            description = state.description,
            price = price,
            stock = stock,
            categoryId = state.selectedCategoryId,
            storeId = state.storeId,
            imageUrl = imageUrl,
            storagePath = storagePath,
            attributes = state.attributes.associate { it.first to it.second }
        )

        productFirestore.updateProduct(product).onSuccess {
            _uiState.update { it.copy(isLoading = false) }
            _effect.emit(SellerProductFormEffect.ShowMessage("Producto actualizado exitosamente"))
            _effect.emit(SellerProductFormEffect.NavigateBack)
        }.onFailure { error ->
            handleError(error.message ?: "No se pudo actualizar el producto")
        }
    }

    private suspend fun handleError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
        _effect.emit(SellerProductFormEffect.ShowMessage(message))
    }

    private fun createProduct(
        price: Double,
        stock: Int,
    ) {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val state = _uiState.value

            val uri = state.selectUri

            if (uri == null) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = "Debe seleccionar una imagen"
                    )
                }
                _effect.emit(
                    SellerProductFormEffect.ShowMessage(
                        message = "Debe seleccionar una imagen"
                    )
                )
                return@launch
            }
            val resolver = appContext.contentResolver
            val fileResult: Result<Pair<ByteArray, String>> =
                runCatching {
                    val byte: ByteArray = readBytesFormUri(
                        contentResolver = resolver,
                        uri = uri
                    )
                    val extension = getExtensionFromUri(
                        contentResolver = resolver,
                        uri = uri
                    )

                    byte to extension
                }

            val (bytes, extension) = fileResult.getOrElse {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Error desconocido al subir la imagen"
                    )
                }
                _effect.emit(
                    SellerProductFormEffect.ShowMessage(
                        message = it.message ?: "Error desconocido al subir la imagen"
                    )
                )
                return@launch
            }

            val maxSizeByte: Int = 1 * 1024 * 1024

            if (bytes.size > maxSizeByte) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = "La imagen no puede superar los 1MB"
                    )
                }
                _effect.emit(
                    SellerProductFormEffect.ShowMessage(
                        message = "La imagen no puede superar los 1MB"
                    )
                )
                return@launch
            }

            val user = authRepository.getCurrentUser()
            val sellerId = user?.uid

            if (sellerId.isNullOrBlank()) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = "No hay usuario no autenticado"
                    )
                }
            }

            val storageResult = productStorage.uploadProductImage(
                sellerId = sellerId,
                storeId = state.storeId,
                byteArray = bytes,
                extension = extension
            )

            storageResult.onSuccess { (downloadUrl, storagePath) ->
                val product = Product(
                    name = state.name,
                    description = state.description,
                    price = price,
                    stock = stock,
                    categoryId = state.selectedCategoryId,
                    storeId = state.storeId,
                    imageUrl = downloadUrl,
                    storagePath = storagePath,
                    attributes = state.attributes.associate {
                        it.first to it.second
                    }
                )

                val firestoreResult = productFirestore.createProduct(product)

                firestoreResult.onSuccess {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false
                        )
                    }

                    clearFormInternal()

                    _effect.emit(
                        SellerProductFormEffect.ShowMessage(
                            message = "Producto creado exitosamente"
                        )
                    )
                    _effect.emit(
                        SellerProductFormEffect.NavigateBack
                    )


                }.onFailure { error ->
                    runCatching {
                        productStorage.deleteProductImage(storagePath = storagePath)

                    }
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error desconocido al crear el producto"
                        )
                    }
                    _effect.emit(
                        SellerProductFormEffect.ShowMessage(
                            message = error.message ?: "No se pudo guardar el producto"
                        )
                    )

                }

            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error desconocido al subir la imagen"
                    )
                }
                _effect.emit(
                    SellerProductFormEffect.ShowMessage(
                        message = error.message ?: "Error desconocido al subir la imagen"
                    )
                )
            }
        }
    }



    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    categoriesLoading = true,
                    errorMessage = null
                )
            }
            val user = authRepository.getCurrentUser()

            val sellerId = user?.uid

            if (sellerId == null) {
                _uiState.update { current ->
                    current.copy(
                        categoriesLoading = false,
                    )
                }

                _effect.emit(
                    SellerProductFormEffect.ShowMessage(
                        message = "No se encontro el vendedor"
                    )
                )
                return@launch
            }
            storeRepository.getStoreBySeller(sellerId)
                .onSuccess { store ->
                    if (store == null) {
                        _uiState.update { current ->
                            current.copy(
                                categoriesLoading = false,
                            )
                        }
                        _effect.emit(
                            SellerProductFormEffect.ShowMessage(
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

                    categoryRepository.getCategoriesByStoreFlow(store.id)
                        .collect { categories ->
                            _uiState.update { current ->
                                current.copy(
                                    categories = categories,
                                    categoriesLoading = false
                                )
                            }
                        }

                }
                .onFailure { error ->
                    _uiState.update { current ->
                        current.copy(
                            categoriesLoading = false,
                            errorMessage = error.message
                        )
                    }
                    _effect.emit(
                        SellerProductFormEffect.ShowMessage(
                            message = error.message ?: "Error desconocido al obtener la tienda"
                        )
                    )
                }
        }
    }

    private fun readBytesFormUri(
        contentResolver: ContentResolver,
        uri: Uri,
    ): ByteArray {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw Exception("No se pudo abrir el archivo seleccionado")

        return inputStream.use { stream ->
            val bytes: ByteArray = stream.readBytes()

            bytes
        }
    }

    private fun getExtensionFromUri(
        contentResolver: ContentResolver,
        uri: Uri,
    ): String {

        val mimeType: String? = contentResolver.getType(uri)

        val extFromMime: String? = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

        val clean: String = extFromMime?.trim()?.lowercase()?.removePrefix(".") ?: "jpg"

        return if (clean == "jpeg") "jpg" else clean
    }

    private fun clearFormInternal(){
        _uiState.update { current ->
            current.copy(
                name = "",
                description = "",
                price = "",
                stock = "",
                selectedCategoryId = "",
                selectUri = null,
                attributes = emptyList(),
                errorMessage = null
            )
        }
    }
}