package com.skillet.multistoreapp.presentation.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.skillet.multistoreapp.navigation.AppNavHost

@Composable
fun AppRoot(
    viewModel: AppViewModel = hiltViewModel()
){

    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()

    if(startDestination == null){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            CircularProgressIndicator()
        }

        return
    }

    val navController = rememberNavController()

    AppNavHost(
        navController = navController,
        startDestination = startDestination!!
    )

}