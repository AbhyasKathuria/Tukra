package com.xsparks.tukra.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.xsparks.tukra.core.cart.CartManager
import com.xsparks.tukra.core.designsystem.components.AdminBottomBar
import com.xsparks.tukra.core.designsystem.components.AdminNavTab
import com.xsparks.tukra.core.designsystem.components.MessBottomBar
import com.xsparks.tukra.core.designsystem.components.MessNavTab
import com.xsparks.tukra.core.designsystem.components.StudentNavTab
import com.xsparks.tukra.core.designsystem.components.VendorNavTab
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.feature.admin.AdminAnalyticsScreen
import com.xsparks.tukra.feature.admin.AdminOutletsScreen
import com.xsparks.tukra.feature.admin.AdminOverviewScreen
import com.xsparks.tukra.feature.admin.AdminSyncScreen
import com.xsparks.tukra.feature.admin.AdminViewModel
import com.xsparks.tukra.feature.auth.AuthViewModel
import com.xsparks.tukra.feature.auth.LoginScreen
import com.xsparks.tukra.feature.auth.RegisterScreen
import com.xsparks.tukra.feature.cart.CartScreen
import com.xsparks.tukra.feature.checkout.CheckoutScreen
import com.xsparks.tukra.feature.checkout.CheckoutViewModel
import com.xsparks.tukra.feature.history.OrdersHistoryScreen
import com.xsparks.tukra.feature.history.OrdersViewModel
import com.xsparks.tukra.feature.home.HomeScreen
import com.xsparks.tukra.feature.home.HomeViewModel
import com.xsparks.tukra.feature.menu.MenuViewModel
import com.xsparks.tukra.feature.menu.OutletMenuScreen
import com.xsparks.tukra.feature.mess.MessCounterScreen
import com.xsparks.tukra.feature.mess.MessCounterViewModel
import com.xsparks.tukra.feature.mess.MessMenuScreen
import com.xsparks.tukra.feature.mess.MessMenuViewModel
import com.xsparks.tukra.feature.mess.MessReservationsScreen
import com.xsparks.tukra.feature.mess.MessReservationsViewModel
import com.xsparks.tukra.feature.mess.HostelMessScreen
import com.xsparks.tukra.feature.rescue.TinyFishFoodRescueScreen
import com.xsparks.tukra.feature.onboarding.OnboardingScreen
import com.xsparks.tukra.feature.profile.ProfileScreen
import com.xsparks.tukra.feature.profile.ProfileViewModel
import com.xsparks.tukra.feature.scan.PlateScanScreen
import com.xsparks.tukra.feature.scan.PlateScanViewModel
import com.xsparks.tukra.feature.splash.SplashScreen
import com.xsparks.tukra.feature.tracking.TrackingScreen
import com.xsparks.tukra.feature.tracking.TrackingViewModel
import com.xsparks.tukra.feature.vendor.VendorCapacityScreen
import com.xsparks.tukra.feature.vendor.VendorCapacityViewModel
import com.xsparks.tukra.feature.vendor.VendorMenuScreen
import com.xsparks.tukra.feature.vendor.VendorMenuViewModel
import com.xsparks.tukra.feature.vendor.VendorProposalsScreen
import com.xsparks.tukra.feature.vendor.VendorProposalsViewModel
import com.xsparks.tukra.feature.vendor.VendorQueueScreen
import com.xsparks.tukra.feature.vendor.VendorQueueViewModel
import com.xsparks.tukra.feature.wallet.WalletScreen
import com.xsparks.tukra.feature.wallet.WalletViewModel

@Composable
fun TukraNavHost(
    navController: NavHostController,
    tokenStore: TokenStore
) {
    val context = LocalContext.current
    val authViewModel = remember { AuthViewModel(tokenStore) }
    val homeViewModel = remember { HomeViewModel(tokenStore) }
    val menuViewModel = remember { MenuViewModel(tokenStore) }
    val ordersViewModel = remember { OrdersViewModel(tokenStore) }
    val walletViewModel = remember { WalletViewModel(tokenStore) }
    val plateScanViewModel = remember { PlateScanViewModel(tokenStore) }
    val profileViewModel = remember { ProfileViewModel(tokenStore) }
    val vendorQueueViewModel = remember { VendorQueueViewModel(tokenStore) }
    val vendorMenuViewModel = remember { VendorMenuViewModel(tokenStore) }
    val vendorCapacityViewModel = remember { VendorCapacityViewModel(tokenStore) }
    val vendorProposalsViewModel = remember { VendorProposalsViewModel(tokenStore) }
    val messMenuViewModel = remember { MessMenuViewModel(tokenStore) }
    val messReservationsViewModel = remember { MessReservationsViewModel(tokenStore) }
    val messCounterViewModel = remember { MessCounterViewModel(tokenStore) }
    val adminViewModel = remember { AdminViewModel(tokenStore) }
    val cartManager = remember { CartManager.getInstance(context) }

    fun navigateForRole(role: String) {
        val destination: Route = when (role.uppercase()) {
            "VENDOR" -> Route.VendorQueue
            "MESS_OPERATOR" -> Route.MessMenu
            "ADMIN" -> Route.AdminOverview
            else -> Route.StudentHome
        }
        navController.navigate(destination) {
            popUpTo<Route.Login> { inclusive = true }
        }
    }

    fun handleStudentTabSelection(tab: StudentNavTab) {
        val targetRoute: Route = when (tab) {
            StudentNavTab.HOME -> Route.StudentHome
            StudentNavTab.ORDERS -> Route.OrdersHistory
            StudentNavTab.SCAN -> Route.ScanPlate
            StudentNavTab.WALLET -> Route.Wallet
            StudentNavTab.PROFILE -> Route.Profile
        }
        navController.navigate(targetRoute) {
            popUpTo<Route.StudentHome> { inclusive = false }
            launchSingleTop = true
        }
    }

    fun handleVendorTabSelection(tab: VendorNavTab) {
        val targetRoute: Route = when (tab) {
            VendorNavTab.QUEUE -> Route.VendorQueue
            VendorNavTab.MENU -> Route.VendorMenu
            VendorNavTab.CAPACITY -> Route.VendorCapacity
            VendorNavTab.PROPOSALS -> Route.VendorProposals
        }
        navController.navigate(targetRoute) {
            popUpTo<Route.VendorQueue> { inclusive = false }
            launchSingleTop = true
        }
    }

    fun handleMessTabSelection(tab: MessNavTab) {
        val targetRoute: Route = when (tab) {
            MessNavTab.MENU -> Route.MessMenu
            MessNavTab.RESERVATIONS -> Route.MessReservations
            MessNavTab.COUNTER -> Route.MessCounter
        }
        navController.navigate(targetRoute) {
            popUpTo<Route.MessMenu> { inclusive = false }
            launchSingleTop = true
        }
    }

    fun handleAdminTabSelection(tab: AdminNavTab) {
        val targetRoute: Route = when (tab) {
            AdminNavTab.OVERVIEW -> Route.AdminOverview
            AdminNavTab.OUTLETS -> Route.AdminOutlets
            AdminNavTab.ANALYTICS -> Route.AdminAnalytics
            AdminNavTab.SYNC -> Route.AdminSync
        }
        navController.navigate(targetRoute) {
            popUpTo<Route.AdminOverview> { inclusive = false }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Route.Splash
    ) {
        composable<Route.Splash> {
            SplashScreen(
                tokenStore = tokenStore,
                onNavigateToHome = {
                    navController.navigate(Route.StudentHome) {
                        popUpTo<Route.Splash> { inclusive = true }
                    }
                },
                onNavigateToVendor = {
                    navController.navigate(Route.VendorQueue) {
                        popUpTo<Route.Splash> { inclusive = true }
                    }
                },
                onNavigateToMess = {
                    navController.navigate(Route.MessMenu) {
                        popUpTo<Route.Splash> { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Route.AdminOverview) {
                        popUpTo<Route.Splash> { inclusive = true }
                    }
                },
                onNavigateToAuth = {
                    navController.navigate(Route.Onboarding) {
                        popUpTo<Route.Splash> { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Onboarding> {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Route.Login) {
                        popUpTo<Route.Onboarding> { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Login> {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { role ->
                    navigateForRole(role)
                },
                onNavigateToRegister = {
                    navController.navigate(Route.Register)
                }
            )
        }

        composable<Route.Register> {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { role ->
                    navigateForRole(role)
                },
                onNavigateToLogin = {
                    navController.navigate(Route.Login) {
                        popUpTo<Route.Register> { inclusive = true }
                    }
                }
            )
        }

        composable<Route.StudentHome> {
            HomeScreen(
                tokenStore = tokenStore,
                viewModel = homeViewModel,
                onOutletClick = { id, name ->
                    navController.navigate(Route.OutletMenu(outletId = id, outletName = name))
                },
                onViewCartClick = {
                    navController.navigate(Route.Cart)
                },
                onWalletClick = {
                    navController.navigate(Route.Wallet)
                },
                onTabSelected = { tab ->
                    handleStudentTabSelection(tab)
                },
                onHostelMessClick = {
                    navController.navigate(Route.HostelMess)
                },
                onFoodRescueClick = { dish ->
                    navController.navigate(Route.TinyFishFoodRescue(initialDish = dish))
                }
            )
        }

        composable<Route.OutletMenu> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.OutletMenu>()
            OutletMenuScreen(
                outletId = route.outletId,
                outletName = route.outletName,
                viewModel = menuViewModel,
                onBackClick = { navController.popBackStack() },
                onViewCartClick = { navController.navigate(Route.Cart) }
            )
        }

        composable<Route.Cart> {
            CartScreen(
                tokenStore = tokenStore,
                onBackClick = { navController.popBackStack() },
                onProceedToCheckout = { outletId, outletName, slotId, slotStartsAt ->
                    navController.navigate(
                        Route.Checkout(
                            outletId = outletId,
                            outletName = outletName,
                            slotId = slotId,
                            slotStartsAt = slotStartsAt
                        )
                    )
                }
            )
        }

        composable<Route.Checkout> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Checkout>()
            val checkoutViewModel = remember { CheckoutViewModel(tokenStore, cartManager) }
            CheckoutScreen(
                outletId = route.outletId,
                outletName = route.outletName,
                slotId = route.slotId,
                slotStartsAt = route.slotStartsAt,
                viewModel = checkoutViewModel,
                onBackClick = { navController.popBackStack() },
                onOrderPlaced = { orderId ->
                    navController.navigate(Route.Tracking(orderId)) {
                        popUpTo<Route.StudentHome> { inclusive = false }
                    }
                }
            )
        }

        composable<Route.Tracking> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Tracking>()
            val trackingViewModel = remember { TrackingViewModel(tokenStore) }
            TrackingScreen(
                orderId = route.orderId,
                viewModel = trackingViewModel,
                onBackClick = {
                    navController.navigate(Route.StudentHome) {
                        popUpTo<Route.StudentHome> { inclusive = true }
                    }
                },
                onFoodRescueClick = { dish ->
                    navController.navigate(Route.TinyFishFoodRescue(initialDish = dish))
                }
            )
        }

        composable<Route.OrdersHistory> {
            OrdersHistoryScreen(
                tokenStore = tokenStore,
                viewModel = ordersViewModel,
                onOrderClick = { orderId ->
                    navController.navigate(Route.Tracking(orderId))
                },
                onTabSelected = { tab ->
                    handleStudentTabSelection(tab)
                },
                onFoodRescueClick = { dish ->
                    navController.navigate(Route.TinyFishFoodRescue(initialDish = dish))
                }
            )
        }

        composable<Route.Wallet> {
            WalletScreen(
                tokenStore = tokenStore,
                viewModel = walletViewModel,
                onTabSelected = { tab ->
                    handleStudentTabSelection(tab)
                }
            )
        }

        composable<Route.ScanPlate> {
            PlateScanScreen(
                tokenStore = tokenStore,
                viewModel = plateScanViewModel,
                onTabSelected = { tab ->
                    handleStudentTabSelection(tab)
                }
            )
        }

        composable<Route.Profile> {
            ProfileScreen(
                tokenStore = tokenStore,
                viewModel = profileViewModel,
                onLogout = {
                    navController.navigate(Route.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onTabSelected = { tab ->
                    handleStudentTabSelection(tab)
                }
            )
        }

        composable<Route.HostelMess> {
            HostelMessScreen(
                tokenStore = tokenStore,
                onBackClick = { navController.popBackStack() },
                onTabSelected = { tab -> handleStudentTabSelection(tab) },
                onFoodRescueClick = { dish ->
                    navController.navigate(Route.TinyFishFoodRescue(initialDish = dish))
                }
            )
        }

        composable<Route.TinyFishFoodRescue> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.TinyFishFoodRescue>()
            TinyFishFoodRescueScreen(
                initialDish = route.initialDish,
                initialReason = route.reason,
                tokenStore = tokenStore,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Route.VendorQueue> {
            VendorQueueScreen(
                tokenStore = tokenStore,
                viewModel = vendorQueueViewModel,
                onTabSelected = { handleVendorTabSelection(it) },
                onLogout = {
                    navController.navigate(Route.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.VendorMenu> {
            VendorMenuScreen(
                tokenStore = tokenStore,
                viewModel = vendorMenuViewModel,
                onTabSelected = { handleVendorTabSelection(it) },
                onNavigateToSync = {
                    navController.navigate(Route.VendorProposals)
                }
            )
        }

        composable<Route.VendorCapacity> {
            VendorCapacityScreen(
                tokenStore = tokenStore,
                viewModel = vendorCapacityViewModel,
                onTabSelected = { handleVendorTabSelection(it) }
            )
        }

        composable<Route.VendorProposals> {
            VendorProposalsScreen(
                tokenStore = tokenStore,
                viewModel = vendorProposalsViewModel,
                onTabSelected = { handleVendorTabSelection(it) }
            )
        }

        // Mess Flow
        composable<Route.MessMenu> {
            MessMenuScreen(
                tokenStore = tokenStore,
                viewModel = messMenuViewModel,
                onTabSelected = { handleMessTabSelection(it) },
                onLogout = {
                    navController.navigate(Route.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.MessReservations> {
            MessReservationsScreen(
                tokenStore = tokenStore,
                viewModel = messReservationsViewModel,
                onTabSelected = { handleMessTabSelection(it) }
            )
        }

        composable<Route.MessCounter> {
            MessCounterScreen(
                tokenStore = tokenStore,
                viewModel = messCounterViewModel,
                onTabSelected = { handleMessTabSelection(it) }
            )
        }

        // Admin Flow
        composable<Route.AdminOverview> {
            AdminOverviewScreen(
                tokenStore = tokenStore,
                viewModel = adminViewModel,
                onTabSelected = { handleAdminTabSelection(it) },
                onLogout = {
                    navController.navigate(Route.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.AdminOutlets> {
            AdminOutletsScreen(
                tokenStore = tokenStore,
                viewModel = adminViewModel,
                onTabSelected = { handleAdminTabSelection(it) }
            )
        }

        composable<Route.AdminAnalytics> {
            AdminAnalyticsScreen(
                tokenStore = tokenStore,
                viewModel = adminViewModel,
                onTabSelected = { handleAdminTabSelection(it) }
            )
        }

        composable<Route.AdminSync> {
            AdminSyncScreen(
                tokenStore = tokenStore,
                viewModel = adminViewModel,
                onTabSelected = { handleAdminTabSelection(it) }
            )
        }
    }
}
