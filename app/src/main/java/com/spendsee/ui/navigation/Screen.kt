package com.spendsee.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing all navigation destinations in the app
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Records : Screen(
        route = "records",
        title = "Records",
        icon = Icons.Default.List
    )

    object Analysis : Screen(
        route = "analysis",
        title = "Analysis",
        icon = Icons.Default.BarChart
    )

    object Budgets : Screen(
        route = "budgets",
        title = "Budgets",
        icon = Icons.Default.AccountBalance
    )

    object Accounts : Screen(
        route = "accounts",
        title = "Accounts",
        icon = Icons.Default.AccountBalanceWallet
    )

    object Settings : Screen(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings
    )

    companion object {
        val bottomNavItems = listOf(
            Records,
            Analysis,
            Budgets,
            Accounts,
            Settings
        )
    }
}
