package com.aprovecha.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.aprovecha.app.feature.auth.ui.LoginScreen
import com.aprovecha.app.feature.auth.ui.ProfileScreen
import com.aprovecha.app.feature.auth.ui.RegisterScreen
import com.aprovecha.app.feature.products.ui.FavoritesScreen
import com.aprovecha.app.feature.products.ui.HomeConsumerScreen
import com.aprovecha.app.feature.products.ui.PackDetailScreen
import com.aprovecha.app.feature.reservations.ui.HomeCommerceScreen
import com.aprovecha.app.feature.reservations.ui.MyReservationsScreen
import com.aprovecha.app.feature.reservations.ui.PendingReservationsScreen
import com.aprovecha.app.feature.reservations.ui.PublishPackScreen
import com.aprovecha.app.ui.components.AprovechaBottomBar

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME_CONSUMER = "home_consumer"
    const val PACK_DETAIL = "pack_detail/{packId}"
    const val MY_RESERVATIONS = "my_reservations"
    const val HOME_COMMERCE = "home_commerce"
    const val PUBLISH_PACK = "publish_pack"
    const val PROFILE = "profile"
    const val FAVORITES = "favorites"
    const val PENDING_RESERVATIONS = "pending_reservations"

    fun packDetail(packId: Long) = "pack_detail/$packId"
}

private val bottomBarRoutes = setOf(
    Routes.HOME_CONSUMER,
    Routes.FAVORITES,
    Routes.MY_RESERVATIONS,
    Routes.HOME_COMMERCE,
    Routes.PENDING_RESERVATIONS,
    Routes.PROFILE
)

@Composable
fun AprovechaNavGraph(navController: NavHostController) {
    var isCommerce by remember { mutableStateOf(false) }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = { role ->
                        isCommerce = role == "COMMERCE"
                        navController.navigateToHomeByRole(role)
                    },
                    onGoToRegister = { navController.navigate(Routes.REGISTER) }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = { role ->
                        isCommerce = role == "COMMERCE"
                        navController.navigateToHomeByRole(role)
                    },
                    onGoToLogin = { navController.popBackStack() }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    onLogout = {
                        isCommerce = false
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.HOME_CONSUMER) {
                HomeConsumerScreen(
                    onPackClick = { packId -> navController.navigate(Routes.packDetail(packId)) }
                )
            }

            composable(
                route = Routes.PACK_DETAIL,
                arguments = listOf(navArgument("packId") { type = NavType.LongType })
            ) { backStackEntry ->
                val packId = backStackEntry.arguments?.getLong("packId") ?: return@composable
                PackDetailScreen(
                    packId = packId,
                    onBack = { navController.popBackStack() },
                    onReserveSuccess = {
                        navController.navigate(Routes.MY_RESERVATIONS) {
                            popUpTo(Routes.HOME_CONSUMER) { inclusive = false }
                        }
                    }
                )
            }

            composable(Routes.MY_RESERVATIONS) {
                MyReservationsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onPackClick = { packId -> navController.navigate(Routes.packDetail(packId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.HOME_COMMERCE) {
                HomeCommerceScreen(
                    onPublishPack = { navController.navigate(Routes.PUBLISH_PACK) }
                )
            }

            composable(Routes.PENDING_RESERVATIONS) {
                PendingReservationsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.PUBLISH_PACK) {
                PublishPackScreen(
                    onBack = { navController.popBackStack() },
                    onPublishSuccess = { navController.popBackStack() }
                )
            }
        }

        if (showBottomBar) {
            val homeRoute = if (isCommerce) Routes.HOME_COMMERCE else Routes.HOME_CONSUMER
            AprovechaBottomBar(
                currentRoute = currentRoute ?: "",
                isCommerce = isCommerce,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(homeRoute) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }
    }
}

private fun NavHostController.navigateToHomeByRole(role: String) {
    val targetRoute = if (role == "CONSUMER") Routes.HOME_CONSUMER else Routes.HOME_COMMERCE
    navigate(targetRoute) {
        popUpTo(Routes.LOGIN) { inclusive = true }
    }
}
