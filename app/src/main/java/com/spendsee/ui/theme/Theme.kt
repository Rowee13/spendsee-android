package com.spendsee.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DefaultPrimaryDark,
    secondary = DefaultAccentDark,
    tertiary = TransferBlue,
    background = DarkContentBackground,
    surface = DarkStatsCard,
    onPrimary = SystemBackgroundLight,
    onSecondary = SystemBackgroundLight,
    onTertiary = SystemBackgroundLight,
    onBackground = SystemBackgroundLight,
    onSurface = SystemBackgroundLight,
)

private val LightColorScheme = lightColorScheme(
    primary = DefaultPrimaryLight,
    secondary = DefaultAccentLight,
    tertiary = TransferBlue,
    background = SystemBackgroundLight,
    surface = SystemGray6Light,
    onPrimary = SystemBackgroundLight,
    onSecondary = SystemBackgroundDark,
    onTertiary = SystemBackgroundLight,
    onBackground = SystemBackgroundDark,
    onSurface = SystemBackgroundDark,
)

@Composable
fun SpendSeeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    selectedScheme: AppColorScheme = AppColorSchemes.Default,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme(
            primary = selectedScheme.primaryDark,
            secondary = selectedScheme.accentDark,
            tertiary = TransferBlue,
            background = DarkContentBackground,
            surface = selectedScheme.cardBackgroundDark,
            onPrimary = SystemBackgroundDark,  // Dark text on light primary colors
            onSecondary = SystemBackgroundDark, // Dark text on light secondary colors
            onTertiary = SystemBackgroundLight,
            onBackground = SystemBackgroundLight,
            onSurface = SystemBackgroundLight,
            primaryContainer = selectedScheme.primaryDark.copy(alpha = 0.3f), // Dimmed primary
            onPrimaryContainer = SystemBackgroundLight, // White text on dimmed container
        )

        else -> lightColorScheme(
            primary = selectedScheme.primaryLight,
            secondary = selectedScheme.accentLight,
            tertiary = TransferBlue,
            background = SystemBackgroundLight,
            surface = selectedScheme.cardBackgroundLight,
            onPrimary = SystemBackgroundLight, // White text on dark primary
            onSecondary = SystemBackgroundDark,
            onTertiary = SystemBackgroundLight,
            onBackground = SystemBackgroundDark,
            onSurface = SystemBackgroundDark,
            primaryContainer = selectedScheme.primaryLight.copy(alpha = 0.15f), // Light tint
            onPrimaryContainer = selectedScheme.primaryLight, // Primary color text on light container
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
