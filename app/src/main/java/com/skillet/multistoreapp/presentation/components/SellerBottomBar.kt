package com.skillet.multistoreapp.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.skillet.multistoreapp.navigation.AppRoute

@Composable
fun SellerBottomBar(
    navController: NavController
){
    val currentRoute = navController
        .currentBackStackEntryAsState() //devuelve un estado
        .value //devuelve el valor del estado
        ?.destination //devuelve la ruta actual
        ?.route //devuelve la ruta actual

    val items = listOf(
        AppRoute.SellerHome,
        AppRoute.SellerCategoriesList,
        AppRoute.SellerProductList,
        AppRoute.SellerOrders
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.route == currentRoute,
                onClick = {
                    if(item.route != currentRoute){
                        navController.navigate(item.route){
                            popUpTo(navController.graph.startDestinationId){
                                saveState = true //Guarda el estado de la pantalla
                            }
                            launchSingleTop = true //Evita que se creen varias instancias de la misma pantalla
                            restoreState = true //Restaura el estado de la pantalla
                        }
                    }
                },
                icon = {
                    when(item){
                        AppRoute.SellerHome -> {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = "Inicio"
                            )
                        }
                        AppRoute.SellerCategoriesList -> {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = "Categorias"
                            )
                        }
                        AppRoute.SellerProductList -> {
                            Icon(
                                Icons.Default.ShoppingBag,
                                contentDescription = "Productos"
                            )
                        }
                        AppRoute.SellerOrders -> {
                            Icon(
                                Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = "Pedidos"
                            )
                        }
                        else -> {}
                    }
                },
                label = {
                    Text(
                        when(item){
                            AppRoute.SellerHome -> "Inicio"
                            AppRoute.SellerCategoriesList -> "Categorias"
                            AppRoute.SellerProductList -> "Productos"
                            AppRoute.SellerOrders -> "Pedidos"
                            else -> ""
                        }
                    )
                }
            )
        }
    }
}
