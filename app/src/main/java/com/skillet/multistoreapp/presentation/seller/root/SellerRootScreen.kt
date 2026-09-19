package com.skillet.multistoreapp.presentation.seller.root

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
import com.skillet.multistoreapp.presentation.components.SellerBottomBar
import com.skillet.multistoreapp.presentation.order.OrderViewModel
import com.skillet.multistoreapp.presentation.seller.categories.form.SellerCategoryFormScreen
import com.skillet.multistoreapp.presentation.seller.categories.form.SellerCategoryFormViewModel
import com.skillet.multistoreapp.presentation.seller.categories.list.SellerCategoriesListViewModel
import com.skillet.multistoreapp.presentation.seller.categories.list.SellerCategoriesScreen
import com.skillet.multistoreapp.presentation.seller.home.SellerHomeScreen
import com.skillet.multistoreapp.presentation.seller.order.SellerOrderDetailScreen
import com.skillet.multistoreapp.presentation.seller.order.SellerOrderDetailViewModel
import com.skillet.multistoreapp.presentation.seller.order.SellerOrderScreen
import com.skillet.multistoreapp.presentation.seller.products.form.SellerProductFormViewModel
import com.skillet.multistoreapp.presentation.seller.products.form.SellerProductsFormScreen
import com.skillet.multistoreapp.presentation.seller.products.list.SellerProductsListScreen
import com.skillet.multistoreapp.presentation.seller.products.list.SellerProductsListViewModel

@Composable
fun SellerRootScreen(
    rootNavController: NavController
) {
    val sellerNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            SellerBottomBar(navController = sellerNavController)
        }
    ) { paddingValues ->

        NavHost(
            navController = sellerNavController,
            startDestination = AppRoute.SellerHome.route,
            modifier = Modifier.padding(paddingValues)
        ){
            composable (AppRoute.SellerHome.route){backStackEntry ->
                val viewModel: AppViewModel = hiltViewModel(backStackEntry)
                SellerHomeScreen(
                    navController = rootNavController,
                    viewModel = viewModel
                )
            }
            composable(AppRoute.SellerCategoriesList.route){ backStackEntry ->
                val viewModel: SellerCategoriesListViewModel = hiltViewModel(backStackEntry)

                SellerCategoriesScreen(
                    viewModel = viewModel,
                    onGoToForm = { categoryId ->
                        sellerNavController.navigate(
                            AppRoute.SellerCategoryForm.createRoute(categoryId)
                        )
                    }
                )
            }

            composable (
                route = AppRoute.SellerCategoryForm.route,
                arguments = listOf(
                    navArgument(
                        AppRoute.SellerCategoryForm.ARG_CATEGORY_ID
                    ){
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ){backStackEntry ->

                val viewModel: SellerCategoryFormViewModel = hiltViewModel(backStackEntry)
                SellerCategoryFormScreen(
                    viewModel = viewModel,
                    onBack = {
                        sellerNavController.popBackStack()
                    }
                )
            }
            composable(route = AppRoute.SellerProductList.route) { backStackEntry ->

                val viewModel: SellerProductsListViewModel = hiltViewModel(backStackEntry)
                SellerProductsListScreen(
                    viewModel = viewModel,
                    onGoToForm = {
                        sellerNavController.navigate(AppRoute.SellerProductForm.route)
                    }

                )
            }

            composable (route = AppRoute.SellerProductForm.route){ backStackEntry ->
                val viewModel: SellerProductFormViewModel = hiltViewModel(backStackEntry)
                SellerProductsFormScreen(
                    viewModel = viewModel,
                    onBack = {
                        sellerNavController.popBackStack()
                    }
                )
            }
            composable (route = AppRoute.SellerOrders.route){ backStackEntry ->
                val viewModel: OrderViewModel = hiltViewModel(backStackEntry)
                SellerOrderScreen(
                    viewModel = viewModel,
                    onGoToOrderDetail = { orderId ->
                        sellerNavController.navigate(
                            "seller/order-detail/$orderId"
                        )
                    }
                )
            }

            composable (
                route = AppRoute.SellerOrderDetail.route,
                arguments = listOf(
                    navArgument(
                        "orderId"
                    ){
                        type = NavType.StringType
                    }
                )
            ){  backStackEntry ->

                val viewModel: SellerOrderDetailViewModel = hiltViewModel(backStackEntry)
                SellerOrderDetailScreen(
                    viewModel = viewModel,
                    onGoToBack = {
                        sellerNavController.popBackStack()
                    }
                )
            }
        }
    }
}