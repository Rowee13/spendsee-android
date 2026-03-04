package com.spendsee

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.spendsee.managers.PasscodeManager
import com.spendsee.managers.ThemeManager
import com.spendsee.ui.navigation.MainNavigation
import com.spendsee.ui.screens.security.PasscodeLockScreen
import com.spendsee.ui.screens.security.PasscodeMode
import com.spendsee.ui.theme.AppColorSchemes
import com.spendsee.ui.theme.SpendSeeTheme

class MainActivity : ComponentActivity() {

    // Holds the tab route to navigate to when the app is opened via a notification tap.
    // Backed by Compose state so MainNavigation reacts when onNewIntent updates it.
    private var notificationNavigateTo by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNotificationIntent(intent)

        setContent {
            val themeManager = remember { ThemeManager.getInstance(this) }
            val passcodeManager = remember { PasscodeManager.getInstance(this) }
            val currentTheme by themeManager.currentTheme.collectAsState()
            val isDarkMode by themeManager.isDarkMode.collectAsState()

            // Track if app is locked
            var isLocked by remember {
                mutableStateOf(passcodeManager.isPasscodeEnabled())
            }

            SpendSeeTheme {
                // Set status bar and navigation bar colors dynamically based on theme and dark mode
                SideEffect {
                    window.statusBarColor = currentTheme.getBackground(isDarkMode).toArgb()
                    window.navigationBarColor = currentTheme.getBackground(isDarkMode).toArgb()

                    // Light status bar icons for light mode, dark icons for dark mode
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !isDarkMode
                        isAppearanceLightNavigationBars = !isDarkMode
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = currentTheme.getBackground(isDarkMode)
                ) {
                    if (isLocked) {
                        PasscodeLockScreen(
                            mode = PasscodeMode.ENTER,
                            onUnlocked = {
                                isLocked = false
                            }
                        )
                    } else {
                        MainNavigation(
                            navigateTo = notificationNavigateTo,
                            onNavigationHandled = { notificationNavigateTo = null }
                        )
                    }
                }
            }
        }
    }

    // Called when the activity is already running and a notification is tapped
    // (e.g. app is in the foreground or backstack).
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        notificationNavigateTo = intent?.getStringExtra("navigate_to")
    }
}
