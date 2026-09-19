package com.skillet.multistoreapp.presentation.registerStore

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skillet.multistoreapp.R
import com.skillet.multistoreapp.presentation.components.CategoryDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterStoreScreen(
    viewModel: RegisterStoreViewModel,
    onNavigateHome: () -> Unit
){

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is StoreEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is StoreEffect.NavigateToStoreHome -> {
                    onNavigateHome()
                }
            }
        }
    }

    var selecterCategory by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Registrar Tienda")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_store_register),
                contentDescription = "Registrarme mi tienda",
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            //nombre de la tienda
            OutlinedTextField(
                value = state.storeName,
                onValueChange = {newStoreName ->
                    viewModel.onEvent(StoreEvent.OnStoreNameChanged(newStoreName))
                },
                modifier = Modifier.fillMaxSize(),
                label = { Text(text = "Nombre de la tienda") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Icono de Nombre de la tienda"
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            //descripcion de la tienda
            OutlinedTextField(
                value = state.storeDescription,
                onValueChange = { newStoreDescription ->
                    viewModel.onEvent(StoreEvent.OnStoreDescriptionChanged(newStoreDescription))
                },
                modifier = Modifier.fillMaxSize(),
                label = { Text(text = "Descripcion de la tienda") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Icono de Descripcion de la tienda"
                    )
                },
                minLines = 3,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(12.dp))

            //categoria de la tienda
            CategoryDropdown(
                selectedCategory = selecterCategory,
                onCategorySelected = {category ->
                    selecterCategory = category

                    viewModel.onEvent(StoreEvent.OnStoreCategoryChanged(category))
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.onEvent(StoreEvent.OnNextClick)
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Row{
                    Text(
                        text = "Siguiente"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Icono Siguiente"
                    )
                }
            }

        }
    }
}