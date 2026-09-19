package com.skillet.multistoreapp.presentation.customer.root

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skillet.multistoreapp.navigation.AppRoute
import com.skillet.multistoreapp.presentation.app.AppViewModel
import com.skillet.multistoreapp.presentation.components.CustomerBottomBar
import com.skillet.multistoreapp.presentation.customer.cart.CustomerCartScreen
import com.skillet.multistoreapp.presentation.customer.cart.CustomerCartViewModel
import com.skillet.multistoreapp.presentation.customer.home.CustomerHomeScreen
import com.skillet.multistoreapp.presentation.customer.order.CustomerOrderScreen
import com.skillet.multistoreapp.presentation.customer.products.CustomerProductDetailViewModel
import com.skillet.multistoreapp.presentation.customer.products.CustomerProductsScreen
import com.skillet.multistoreapp.presentation.customer.products.CustomerProductsViewModel
import com.skillet.multistoreapp.presentation.customer.products.CustomerProdutcDetailScreen
import com.skillet.multistoreapp.presentation.customer.stores.CustomerStoresScreen
import com.skillet.multistoreapp.presentation.customer.stores.CustomerStoresViewModel
import com.skillet.multistoreapp.presentation.order.OrderViewModel

@Composable
fun CustomerRootScreen(
    rootNavController: NavController
) {
    val customerNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            CustomerBottomBar(navController = customerNavController)

        }
    ){ paddingValues ->

        NavHost(
            navController = customerNavController,
            startDestination = AppRoute.CustomerHome.route,
            modifier = Modifier.padding(paddingValues)
        ){
            composable(AppRoute.CustomerHome.route){ backStackEntry ->
                val viewModel: AppViewModel = hiltViewModel(backStackEntry)
                CustomerHomeScreen(
                    navController = rootNavController,
                    viewModel = viewModel
                )
            }
            composable(AppRoute.CustomerStores.route){ backStackEntry ->
                val viewModel: CustomerStoresViewModel = hiltViewModel(backStackEntry)
                CustomerStoresScreen(
                    viewModel = viewModel,
                    onGoToProducts = { storeId ->
                        customerNavController.navigate("customer/product/$storeId")
                    }
                )
            }

            composable (
                route = AppRoute.CustomerProducts.route,
                arguments = listOf(
                    navArgument("storeId"){
                        type = NavType.StringType
                    }
                )
            ){ backStackEntry ->
                val viewModel: CustomerProductsViewModel = hiltViewModel(backStackEntry)
                CustomerProductsScreen(
                    viewModel = viewModel,
                    onGoToProductDetail = { productId ->
                        customerNavController.navigate("customer/product-detail/$productId")
                    }
                )
            }

            composable (
                route = AppRoute.CutomerProductDetail.route,
                arguments = listOf(
                    navArgument("productId"){
                        type = NavType.StringType
                    }
                )
            ){ backStackEntry ->
                val viewModel: CustomerProductDetailViewModel = hiltViewModel(backStackEntry)

                CustomerProdutcDetailScreen(
                    viewModel = viewModel
                )
            }

            composable (AppRoute.CustomerCart.route){ backStackEntry ->
                val viewModel: CustomerCartViewModel = hiltViewModel(backStackEntry)
                CustomerCartScreen(
                    viewModel = viewModel
                )
            }

            composable (AppRoute.CustomerOrders.route){ backStackEntry ->
                val viewModel: OrderViewModel = hiltViewModel(backStackEntry)
                CustomerOrderScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}