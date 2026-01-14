package com.spendsee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.spendsee.managers.ThemeManager
import com.spendsee.ui.navigation.MainNavigation
import com.spendsee.ui.theme.AppColorSchemes
import com.spendsee.ui.theme.SpendSeeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeManager = remember { ThemeManager.getInstance(this) }
            val isDarkMode by themeManager.isDarkMode.collectAsState()
            val selectedThemeId by themeManager.selectedTheme.collectAsState()
            val selectedScheme = AppColorSchemes.themeById(selectedThemeId)

            SpendSeeTheme(
                darkTheme = isDarkMode,
                selectedScheme = selectedScheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }
}
