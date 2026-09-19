package com.skillet.multistoreapp.presentation.seller.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.skillet.multistoreapp.navigation.AppRoute
import com.skillet.multistoreapp.presentation.app.AppEffect
import com.skillet.multistoreapp.presentation.app.AppViewModel

@Composable
fun SellerHomeScreen(
    navController: NavController,
    viewModel: AppViewModel
){
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AppEffect.NavigateToLogin -> {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Button(
            onClick = {
                viewModel.logOut()
            }
        ) {
            Text(
                text = "Cerrar sesion",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}