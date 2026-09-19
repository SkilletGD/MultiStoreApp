package com.skillet.multistoreapp.presentation.seller.categories.form

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skillet.multistoreapp.R

@Composable
fun SellerCategoryFormScreen(
    viewModel: SellerCategoryFormViewModel,
    onBack: () -> Unit
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit){
        viewModel.effect.collect{ effect ->
            when(effect){
                is SellerCategoryFormEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                SellerCategoryFormEffect.NavigateBack -> {
                    onBack()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if(state.isEditMode) "Editar categoria" else "Crear categoria",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.titleLarge
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_category),
                    contentDescription = "Icono de categoria",
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.CenterHorizontally)
                )

                OutlinedTextField(
                    value = state.name,
                    onValueChange = {newCategory->
                        viewModel.onEvent(SellerCategoryFormEvent.OnNameChanged(newCategory))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre de la categoria") },
                    singleLine = true,
                    enabled = !state.isLoading

                )

                state.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Button(
                    onClick = {
                        viewModel.onEvent(SellerCategoryFormEvent.OnSaveClick)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !state.isLoading
                ) {
                    Text(text = if(state.isEditMode) "Actualizar categoria" else "Crear categoria")
                }
            }
        }

    }
}