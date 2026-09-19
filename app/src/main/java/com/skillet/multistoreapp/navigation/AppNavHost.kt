package com.skillet.multistoreapp.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.skillet.multistoreapp.presentation.customer.root.CustomerRootScreen
import com.skillet.multistoreapp.presentation.login.LoginScreen
import com.skillet.multistoreapp.presentation.login.LoginViewModel
import com.skillet.multistoreapp.presentation.registerCustomer.RegisterCustomerScreen
import com.skillet.multistoreapp.presentation.registerCustomer.RegisterCustomerViewModel
import com.skillet.multistoreapp.presentation.registerSeller.RegisterSellerScreen
import com.skillet.multistoreapp.presentation.registerSeller.RegisterSellerViewModel
import com.skillet.multistoreapp.presentation.registerStore.RegisterStoreScreen
import com.skillet.multistoreapp.presentation.registerStore.RegisterStoreViewModel
import com.skillet.multistoreapp.presentation.selectRole.SelectRoleScreen
import com.skillet.multistoreapp.presentation.seller.products.form.SellerProductsFormScreen
import com.skillet.multistoreapp.presentation.seller.products.list.SellerProductsListScreen
import com.skillet.multistoreapp.presentation.seller.root.SellerRootScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(AppRoute.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onGoToSelectRole = {
                    navController.navigate(AppRoute.SelectRole.route)
                },
                onNavigateByRole = { role ->
                    when (role){
                        "CUSTOMER" -> navController.navigate(AppRoute.CutomerRoot.route){
                            popUpTo(AppRoute.Login.route){
                                inclusive = true
                            }
                        }
                        "SELLER" -> navController.navigate(AppRoute.SellerRoot.route){
                            popUpTo(AppRoute.Login.route){
                                inclusive = true
                            }
                        }
                        else -> {
                            navController.navigate(AppRoute.Login.route)
                        }
                    }
                }
            )
        }

        composable(AppRoute.SelectRole.route) {
            SelectRoleScreen(
                onGoToRegisterSeller = {
                    navController.navigate(AppRoute.RegisterSeller.route)
                },
                onGoToRegisterCustomer = {
                    navController.navigate(AppRoute.RegisterCustomer.route)
                }
            )
        }

        composable(AppRoute.RegisterSeller.route) {
            RegisterSellerScreen(
                viewModel = hiltViewModel(),
                onBack = {
                    navController.popBackStack()
                },
                onFinishRegisterSelect = {
                    navController.navigate(AppRoute.RegisterStore.route)
                }
            )
        }

        composable(AppRoute.RegisterCustomer.route) {

            val viewModel: RegisterCustomerViewModel = hiltViewModel()

            RegisterCustomerScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                },
                onNavigateHome = {
                    navController.navigate(AppRoute.CutomerRoot.route) {
                        popUpTo(AppRoute.RegisterCustomer.route) {//Se elimina la ruta actual de la pila de navegación
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(AppRoute.RegisterSeller.route) {

            val viewModel: RegisterSellerViewModel = hiltViewModel()
            RegisterSellerScreen(
                viewModel = viewModel,
                onFinishRegisterSelect = { sellerUid ->
                    navController.navigate(
                        AppRoute.RegisterStore.createRoute(sellerUid)
                    )
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = AppRoute.RegisterStore.route,
            arguments = listOf(
                navArgument(AppRoute.RegisterStore.ARG_SELLER_UID) {
                    type = NavType.StringType
                    nullable = false
                }
            )) {
            val viewModel: RegisterStoreViewModel = hiltViewModel()

            RegisterStoreScreen(
                viewModel = viewModel,
                onNavigateHome = {
                    navController.navigate(AppRoute.SellerRoot.route) {
                        popUpTo(AppRoute.RegisterStore.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(AppRoute.CutomerRoot.route){
            CustomerRootScreen(
                rootNavController = navController
            )
        }
        composable(AppRoute.SellerRoot.route){
            SellerRootScreen(
                rootNavController = navController
            )
        }
    }
}