package com.focusguard.presentation.navigation

sealed class Screen(val route: String) {
    object Permissions : Screen("permissions")
    object Home : Screen("home")
    object AppSelect : Screen("app_select")
}
