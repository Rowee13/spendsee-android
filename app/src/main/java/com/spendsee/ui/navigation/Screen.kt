package com.spendsee.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.feathericons.*

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
        icon = FeatherIcons.FileText
    )

    object Analysis : Screen(
        route = "analysis",
        title = "Analysis",
        icon = FeatherIcons.PieChart
    )

    object Budgets : Screen(
        route = "budgets",
        title = "Budgets",
        icon = FeatherIcons.DollarSign
    )

    object Accounts : Screen(
        route = "accounts",
        title = "Accounts",
        icon = FeatherIcons.CreditCard
    )

    object Settings : Screen(
        route = "settings",
        title = "Settings",
        icon = FeatherIcons.Settings
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
