package com.focusguard.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.focusguard.presentation.screen.appselect.AppSelectScreen
import com.focusguard.presentation.screen.home.HomeScreen
import com.focusguard.presentation.screen.permissions.PermissionsScreen
import com.focusguard.presentation.screen.permissions.hasOverlayPermission
import com.focusguard.presentation.screen.permissions.hasUsageStatsPermission

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val hasPermissions = hasUsageStatsPermission(context) && hasOverlayPermission(context)
    val startDestination = if (hasPermissions) Screen.Home.route else Screen.Permissions.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Permissions.route) {
            PermissionsScreen(
                onAllPermissionsGranted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Permissions.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAppSelect = {
                    navController.navigate(Screen.AppSelect.route)
                }
            )
        }
        composable(Screen.AppSelect.route) {
            AppSelectScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
