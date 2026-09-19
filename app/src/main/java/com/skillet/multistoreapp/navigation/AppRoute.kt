package com.skillet.multistoreapp.navigation

sealed class AppRoute(
    val route: String
) {

    data object Login : AppRoute(route = "login")

    data object SelectRole : AppRoute(route = "select_role")

    data object RegisterSeller : AppRoute(route = "register_seller")

    data object RegisterCustomer : AppRoute(route = "register_customer")

    data object RegisterStore : AppRoute(route = "register_store/{sellerUid}"){
        const val ARG_SELLER_UID = "sellerUid"

        fun createRoute(sellerUid: String): String{
            return "register_store/$sellerUid"
        }
    }

    data object CustomerHome : AppRoute(route = "customer_home")

    data object SellerHome : AppRoute(route = "seller_home")

    data object CutomerRoot: AppRoute(route = "cutomer_root")
    data object SellerRoot: AppRoute(route = "seller_root")

    data object CustomerStores : AppRoute(route = "customer_stores")


    data object SellerCategoriesList: AppRoute(route = "seller/categories/list")

    data object SellerCategoryForm: AppRoute(route = "seller/catgeory/form?categoryId={categoryId}"){
        const val ARG_CATEGORY_ID = "categoryId"

        fun createRoute(categoryId: String? = null): String{
            return if(categoryId.isNullOrBlank()){
                "seller/catgeory/form"
            }else{
                "seller/catgeory/form?categoryId=$categoryId"
            }
        }
    }

    object SellerProductList: AppRoute(route = "seller/products/list")
    object SellerProductForm: AppRoute(route = "seller/product/form")

    object CustomerProducts: AppRoute(route = "customer/product/{storeId}")

    object CutomerProductDetail: AppRoute(route = "customer/product-detail/{productId}")

    object CustomerCart: AppRoute(route = "customer/cart")

    object CustomerOrders: AppRoute(route = "customer/orders")

    object SellerOrders: AppRoute(route = "seller/orders")

    object SellerOrderDetail: AppRoute(route = "seller/order-detail/{orderId}")

}