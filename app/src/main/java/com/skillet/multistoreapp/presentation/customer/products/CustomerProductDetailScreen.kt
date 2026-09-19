package com.skillet.multistoreapp.presentation.customer.products

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skillet.multistoreapp.navigation.AppRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProdutcDetailScreen(
    viewModel: CustomerProductDetailViewModel,
    onNavigateToCart: () -> Unit
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(Unit){
        viewModel.effect.collect { effect ->
            when (effect) {
                is CustomerProductDetailEffect.ShowMessage -> {
                    Toast.makeText(
                        context,
                        effect.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                CustomerProductDetailEffect.NavigateToCart -> {
                    onNavigateToCart()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Detalle del producto")
                }
            )
        },
        bottomBar = {
            if (state.product != null && state.product!!.stock > 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Selector de cantidad
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        IconButton(
                            onClick = { 
                                if (state.quantity > 1) {
                                    viewModel.onEvent(CustomerProductDetailEvent.OnQuantityChanged(state.quantity - 1))
                                }
                            },
                            enabled = state.quantity > 1
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Menos")
                        }
                        
                        Text(
                            text = state.quantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        IconButton(
                            onClick = { 
                                if (state.quantity < state.product!!.stock) {
                                    viewModel.onEvent(CustomerProductDetailEvent.OnQuantityChanged(state.quantity + 1))
                                }
                            },
                            enabled = state.quantity < state.product!!.stock
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Más")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.onEvent(CustomerProductDetailEvent.AddProductToCart)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddShoppingCart,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Carrito")
                        }

                        Button(
                            onClick = {
                                viewModel.onEvent(CustomerProductDetailEvent.BuyNow)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Comprar")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if(state.isLoading){
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = "Cargando...")
            }
        }else{
            val product = state.product

            if(product != null){
                CustomerProductDetailContent(
                    product = product,
                    modifier = Modifier.padding(paddingValues)
                )
            }else{

            }
        }
    }
}