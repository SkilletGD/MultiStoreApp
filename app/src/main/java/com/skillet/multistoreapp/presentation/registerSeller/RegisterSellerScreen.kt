package com.skillet.multistoreapp.presentation.registerSeller

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skillet.multistoreapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterSellerScreen(
    viewModel: RegisterSellerViewModel,
    onBack: () -> Unit,
    onFinishRegisterSelect: (String) -> Unit
) {


    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SellerEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is SellerEffect.NavigateToRegisterScreen -> {
                    onFinishRegisterSelect(effect.sellerId)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Registrarme como vendedor")
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onBack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
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
                painter = painterResource(id = R.drawable.ic_seller_register),
                contentDescription = "Registrarme como vendedor",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            //nombres del vendedor
            OutlinedTextField(
                value = state.firstName,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { newFirstName ->
                    viewModel.onEvent(
                        SellerEvent.OnFirstNameChanged(
                            newFirstName
                        )
                    )
                },
                label = { Text(text = "Nombres") },
                leadingIcon = {
                    Icon(
                       imageVector = Icons.Default.Person,
                        contentDescription = "Icono Nombres"
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            //apellidos del vendedor
            OutlinedTextField(
                value = state.lastName,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {newLastName ->
                    viewModel.onEvent(SellerEvent.OnLastNameChanged(newLastName))
                },
                label = { Text(text = "Apellidos") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Icono Apellidos"
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            //correo del vendedor
            OutlinedTextField(
                value = state.email,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {newEmail ->
                    viewModel.onEvent(SellerEvent.OnEmailChanged(newEmail))
                },
                label = { Text(text = "Correo Electrónico") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Icono de Correo Electronico"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            //contraseña del vendedor
            OutlinedTextField(
                value = state.password,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {newPassword ->
                    viewModel.onEvent(SellerEvent.OnPasswordChanged(newPassword))
                },
                label = { Text(text = "Contraseña") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Icono de Contraseña"
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            //confirmar contraseña del vendedor
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { newConfirmPassword ->
                    viewModel.onEvent(SellerEvent.OnConfirmPasswordChanged(newConfirmPassword))
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Confirmar Contraseña") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Icono de Confirmar Contraseña"
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            //telefono del vendedor
            OutlinedTextField(
                value = state.phone,
                onValueChange = {newPhone ->
                    viewModel.onEvent(SellerEvent.OnPhoneChanged(newPhone))
                },

                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Teléfono") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Icono de Teléfono"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    viewModel.onEvent(SellerEvent.OnNextClick)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row {
                    Text(
                        text = "Siguiente"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Siguiente"
                    )
                }
            }
        }
    }
}