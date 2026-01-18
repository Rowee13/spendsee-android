package com.spendsee.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spendsee.ui.screens.accounts.AccountsScreen
import com.spendsee.ui.screens.analysis.AnalysisScreen
import com.spendsee.ui.screens.budgets.BudgetsScreen
import com.spendsee.ui.screens.records.RecordsScreen
import com.spendsee.ui.screens.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFEFFFFF),  // Light background matching gradient bottom
                tonalElevation = 0.dp  // Remove shadow
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF418E8C),  // Active color from mockup
                            selectedTextColor = Color(0xFF418E8C),  // Active text color
                            indicatorColor = Color(0xFF418E8C).copy(alpha = 0.15f),  // Light indicator
                            unselectedIconColor = Color(0xFF6A9E9E).copy(alpha = 0.6f),  // Inactive color
                            unselectedTextColor = Color(0xFF6A9E9E).copy(alpha = 0.6f)   // Inactive text
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Records.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Records.route) {
                RecordsScreen()
            }
            composable(Screen.Analysis.route) {
                AnalysisScreen()
            }
            composable(Screen.Budgets.route) {
                BudgetsScreen()
            }
            composable(Screen.Accounts.route) {
                AccountsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
