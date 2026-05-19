package com.aprovecha.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aprovecha.app.feature.auth.ui.LoginScreen
import com.aprovecha.app.feature.auth.ui.RegisterScreen
import com.aprovecha.app.feature.products.ui.HomeConsumerScreen
import com.aprovecha.app.feature.products.ui.PackDetailScreen
import com.aprovecha.app.feature.reservations.ui.HomeCommerceScreen
import com.aprovecha.app.feature.reservations.ui.MyReservationsScreen
import com.aprovecha.app.feature.reservations.ui.PublishPackScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME_CONSUMER = "home_consumer"
    const val PACK_DETAIL = "pack_detail/{packId}"
    const val MY_RESERVATIONS = "my_reservations"
    const val HOME_COMMERCE = "home_commerce"
    const val PUBLISH_PACK = "publish_pack"

    fun packDetail(packId: Long) = "pack_detail/$packId"
}

@Composable
fun AprovechaNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // ── Auth ───────────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { role ->
                    if (role == "CONSUMER") {
                        navController.navigate(Routes.HOME_CONSUMER) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.HOME_COMMERCE) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                },
                onGoToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { role ->
                    if (role == "CONSUMER") {
                        navController.navigate(Routes.HOME_CONSUMER) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.HOME_COMMERCE) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        // ── Consumer ────────────────────────────────────────────────────────────
        composable(Routes.HOME_CONSUMER) {
            HomeConsumerScreen(
                onPackClick = { packId -> navController.navigate(Routes.packDetail(packId)) },
                onGoToReservations = { navController.navigate(Routes.MY_RESERVATIONS) }
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
                onReserveSuccess = { navController.navigate(Routes.MY_RESERVATIONS) }
            )
        }

        composable(Routes.MY_RESERVATIONS) {
            MyReservationsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── Commerce ────────────────────────────────────────────────────────────
        composable(Routes.HOME_COMMERCE) {
            HomeCommerceScreen(
                onPublishPack = { navController.navigate(Routes.PUBLISH_PACK) }
            )
        }

        composable(Routes.PUBLISH_PACK) {
            PublishPackScreen(
                onBack = { navController.popBackStack() },
                onPublishSuccess = { navController.popBackStack() }
            )
        }
    }
}
