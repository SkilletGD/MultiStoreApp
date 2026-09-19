package com.skillet.multistoreapp.presentation.registerCustomer

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skillet.multistoreapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterCustomerScreen(
    viewModel: RegisterCustomerViewModel,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit
){

    val state by viewModel.uiState.collectAsStateWithLifecycle()  //collectAsStateWithLifecycle se utiliza por si esat en segundoplano no se borre la vista
    val context = LocalContext.current

    LaunchedEffect(Unit){

        viewModel.effect.collect {effect ->
            when(effect){
                is CustomerEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show() //Muestra un mensaje de error en la pantalla
                }
                is CustomerEffect.NavigateToHome -> {
                    onNavigateHome() //siempre ahcer que se navege desde la ui, nunca de la viemodel
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Registro como cliente")
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
                painter = painterResource(id = R.drawable.ic_customer_register),
                contentDescription = "Registrarme como cliente",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            //nombres del cliente
            OutlinedTextField(
                value = state.firstName,
                onValueChange = {newFisrtName ->
                    viewModel.onEvent(CustomerEvent.OnFirstNameChanged(newFisrtName))
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Nombres") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Icono de Nombres"
                    )

                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            //apellidos del cliente
            OutlinedTextField(
                value = state.lastName,
                onValueChange = { newLastName ->
                    viewModel.onEvent(CustomerEvent.OnLastNameChanged(newLastName))
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Apellidos") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Icono de Apellidos"
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            //correo del cliente
            OutlinedTextField(
                value = state.email,
                onValueChange = { newEmail ->
                    viewModel.onEvent(CustomerEvent.OnEmailChanged(newEmail))
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Correo Electrónico") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Icono de Correo Electrónico"
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            //contraseña del cliente
            OutlinedTextField(
                value = state.password,
                onValueChange = {newPassword ->
                    viewModel.onEvent(CustomerEvent.OnPasswordChanged(newPassword))
                },
                modifier = Modifier.fillMaxWidth(),
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

            //confirmar contraseña del cliente
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { newConfirmPassword ->
                    viewModel.onEvent(CustomerEvent.OnConfirmPasswordChanged(newConfirmPassword))
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

            Spacer(modifier = Modifier.height(30.dp))

            //boton de registro de un cliente
            Button(
                onClick = {
                    viewModel.onEvent(CustomerEvent.OnRegisterClick)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row{
                    Text(
                        text = "Registrarme"
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