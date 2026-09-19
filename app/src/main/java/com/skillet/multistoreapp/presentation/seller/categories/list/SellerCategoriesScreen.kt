package com.skillet.multistoreapp.presentation.seller.categories.list

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skillet.multistoreapp.presentation.seller.categories.form.SellerCategoryFormEffect
import com.skillet.multistoreapp.presentation.seller.categories.form.SellerCategoryItem

@Composable
fun SellerCategoriesScreen(
    viewModel : SellerCategoriesListViewModel,
    onGoToForm: (String?) -> Unit

) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(Unit){
        viewModel.effect.collect{ effect ->
            when(effect){
                is SellerCatgoriesListEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onGoToForm(null)
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Agregar nueva categoria"
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if(state.listLoading){
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    CircularProgressIndicator()
                }
            }else{
                if(state.categories.isEmpty()){
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Text(text = "No hay categorias registradas aun")
                    }
                }else{
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ){
                        items(state.categories){ category ->
                            SellerCategoryItem(
                                onGoToForm = {
                                    onGoToForm(category.id)
                                },
                                onDelete = {selectedCategory ->
                                    viewModel.onEvent(
                                        SellerCatgoriesListEvent.DeleteCategory(selectedCategory)
                                    )
                                },
                                category = category
                            )
                        }
                    }
                }
            }
        }
    }
}