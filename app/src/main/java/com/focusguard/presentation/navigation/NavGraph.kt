package com.focusguard.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.focusguard.presentation.screen.appselect.AppSelectScreen
import com.focusguard.presentation.screen.home.HomeScreen
import com.focusguard.presentation.screen.permissions.PermissionsScreen
import com.focusguard.presentation.screen.stats.StatsScreen
import com.focusguard.presentation.util.hasRequiredPermissions

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val startDestination = remember {
        if (hasRequiredPermissions(context)) Screen.Home.route else Screen.Permissions.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Permissions.route) {
            PermissionsScreen(
                onAllPermissionsGranted = {
                    // Resume and the Continue button can both fire; only leave once.
                    if (navController.currentDestination?.route != Screen.Permissions.route) {
                        return@PermissionsScreen
                    }
                    // Opened from Home to fix permissions: just go back.
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Permissions.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAppSelect = { navController.navigate(Screen.AppSelect.route) },
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToPermissions = {
                    navController.navigate(Screen.Permissions.route) { launchSingleTop = true }
                }
            )
        }
        composable(Screen.AppSelect.route) {
            AppSelectScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Stats.route) {
            StatsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
