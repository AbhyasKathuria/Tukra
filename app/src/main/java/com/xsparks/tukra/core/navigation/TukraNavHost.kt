package com.xsparks.tukra.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.xsparks.tukra.core.network.TokenStore
import com.xsparks.tukra.feature.admin.AdminOverviewScreen
import com.xsparks.tukra.feature.auth.AuthViewModel
import com.xsparks.tukra.feature.auth.LoginScreen
import com.xsparks.tukra.feature.auth.RegisterScreen
import com.xsparks.tukra.feature.home.HomeScreen
import com.xsparks.tukra.feature.mess.MessMenuScreen
import com.xsparks.tukra.feature.onboarding.OnboardingScreen
import com.xsparks.tukra.feature.splash.SplashScreen
import com.xsparks.tukra.feature.vendor.VendorQueueScreen

@Composable
fun TukraNavHost(
    navController: NavHostController,
    tokenStore: TokenStore
) {
    val authViewModel = remember { AuthViewModel(tokenStore) }

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
                onLogout = {
                    navController.navigate(Route.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.VendorQueue> {
            VendorQueueScreen(
                tokenStore = tokenStore,
                onLogout = {
                    navController.navigate(Route.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.MessMenu> {
            MessMenuScreen(
                tokenStore = tokenStore,
                onLogout = {
                    navController.navigate(Route.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.AdminOverview> {
            AdminOverviewScreen(
                tokenStore = tokenStore,
                onLogout = {
                    navController.navigate(Route.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
