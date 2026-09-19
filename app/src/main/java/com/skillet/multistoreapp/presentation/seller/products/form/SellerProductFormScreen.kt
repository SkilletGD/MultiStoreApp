package com.skillet.multistoreapp.presentation.seller.products.form

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.BedroomParent
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProductsFormScreen(
    viewModel: SellerProductFormViewModel,
    onBack: () -> Unit
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    val iamgePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            viewModel.onEvent(SellerProductFormEvent.OnSelectImage(uri))
        }
    )

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SellerProductFormEffect.ShowMessage -> {
                    Toast.makeText(
                        context,
                        effect.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                SellerProductFormEffect.NavigateBack -> {
                    onBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Agregar Producto")
                },
                navigationIcon = {
                    IconButton(
                        onClick = { }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            if(uiState.selectUri != null){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray)
                ) {
                    AsyncImage(
                        model = uiState.selectUri,
                        contentDescription = "Imagen Seleccionada",
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }

                OutlinedButton(
                    onClick = {
                        viewModel.onEvent(SellerProductFormEvent.OnClearSelection)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Quitar Imagen"
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "Quitar Imagen")
                }

            }else{
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEAEAEA)),
                    contentAlignment = Alignment.Center
                ){
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = "Seleccionar Imagen",
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Seleccionar Imagen",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    iamgePickerLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth()
            ){
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Seleccionar Imagen"
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if(uiState.selectUri != null) "Quitar Imagen" else "Seleccionar Imagen"
                )
            }

            //NOmbre del producto
            OutlinedTextField(
                value = uiState.name,
                onValueChange = {newName ->
                    viewModel.onEvent(
                        SellerProductFormEvent.OnNameChanged(newName)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Nombre del producto")
                },
                placeholder = {
                    Text(text = "Ingresa el nombre del producto")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = "Nombre del producto"
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Words
                )
            )

            //descripcion
            OutlinedTextField(
                value = uiState.description,
                onValueChange = {newDescription ->
                    viewModel.onEvent(
                        SellerProductFormEvent.OnDescriptionChanged(newDescription)
                    )
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                label = {
                    Text(text = "Descripcion del producto")
                },
                placeholder = {
                    Text(text = "Ingresa la descripcion del producto")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = "Descripcion del producto"
                    )
                },
                minLines = 4,
                maxLines = 6,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Sentences
                )
            )

            //listad e categorias
            SellerCategoryDropdown(
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                enabled = !uiState.categoriesLoading,
                onSelect = {category ->
                    viewModel.onEvent(
                        SellerProductFormEvent.OnCatgeroySelected(category.id)
                    )
                }
            )

            //precio
            OutlinedTextField(
                value = uiState.price,
                onValueChange = {newPrice ->
                    if(newPrice.matches(
                            Regex("^\\d*\\.?\\d*$")
                    )){
                        viewModel.onEvent(
                            SellerProductFormEvent.OnPriceChanged(newPrice)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Precio del producto")
                    },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Sell,
                        contentDescription = "Precio del producto"
                    )
                },
                prefix = {
                    Text(text = "USD")
                },
                placeholder = {
                    Text(text = "Ejemplo: 30.99")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )
            )

            //stock
            OutlinedTextField(
                value = uiState.stock,
                onValueChange = {newStock ->
                    if(newStock.matches(
                            Regex("^\\d*$")
                        )){
                            viewModel.onEvent(
                                SellerProductFormEvent.OnStockChanged(newStock)
                            )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text(text = "Stock del producto")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.BedroomParent,
                        contentDescription = "Stock del producto"
                    )
                },
                placeholder = {
                    Text(text = "Ejemplo: 10")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            //Caracteristicas del producto
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChecklistRtl,
                            contentDescription = "Caracteristicas"
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Caracteristica",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold

                        )
                    }

                    Text(
                        text = "Agregar detalles que describen tu producto.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    uiState.attributes.forEachIndexed { index, attribute ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            //Caracteristica -> attribute.first
                            OutlinedTextField(
                                value = attribute.first,
                                onValueChange = { newName ->
                                    viewModel.onEvent(
                                        SellerProductFormEvent.OnUpdateAttributeName(
                                            index = index,
                                            value = newName
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                label = {
                                    Text(text = "Caracteristicas")
                                },
                                placeholder = {
                                    Text(text = "Ej: Color")
                                },
                                singleLine = true
                            )

                            //Detalle -> attribute.second
                            OutlinedTextField(
                                value = attribute.second,
                                onValueChange = { newDetail ->
                                    viewModel.onEvent(
                                        SellerProductFormEvent.OnupdateAttributeValue(
                                            index = index,
                                            value = newDetail
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                label = {
                                    Text(text = "Detalle")
                                },
                                placeholder = {
                                    Text(text = "Ej: Rojo")
                                },
                                singleLine = true
                            )

                            IconButton(
                                onClick = {
                                    viewModel.onEvent(
                                        SellerProductFormEvent.OnDeleteAttribute(
                                            index = index
                                        )
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar Caracteristica",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.onEvent(
                                SellerProductFormEvent.OnAddAttribute
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar Caracteristica"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Agregar Caracteristica")
                    }
                }

                Button(
                    onClick = {
                        viewModel.onEvent(
                            SellerProductFormEvent.OnSaveProduct
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ){
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Crear Producto"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Crear Producto"
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}