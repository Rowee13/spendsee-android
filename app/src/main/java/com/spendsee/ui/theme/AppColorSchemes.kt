package com.spendsee.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * App color scheme matching iOS version
 */
data class AppColorScheme(
    val id: String,
    val name: String,
    val isPremium: Boolean,
    val primaryLight: Color,
    val primaryDark: Color,
    val accentLight: Color,
    val accentDark: Color,
    val cardBackgroundLight: Color,
    val cardBackgroundDark: Color
)

object AppColorSchemes {
    // Default (Free) - Blue
    val Default = AppColorScheme(
        id = "default",
        name = "Default",
        isPremium = false,
        primaryLight = Color(0xFF007AFF),
        primaryDark = Color(0xFF64B5F6),  // Brighter blue for dark mode
        accentLight = Color(0xFF007AFF),
        accentDark = Color(0xFF90CAF9),   // Even lighter blue
        cardBackgroundLight = Color(0xFFF2F2F7),
        cardBackgroundDark = Color(0xFF1C1C1E)
    )

    // Ocean (Premium)
    val Ocean = AppColorScheme(
        id = "ocean",
        name = "Ocean",
        isPremium = true,
        primaryLight = Color(0xFF0A7EA4),
        primaryDark = Color(0xFF4DD0E1),   // Much brighter cyan for dark mode
        accentLight = Color(0xFF06D6A0),
        accentDark = Color(0xFF80CBC4),    // Lighter teal
        cardBackgroundLight = Color(0xFFE0F2FE),
        cardBackgroundDark = Color(0xFF164E63)
    )

    // Sunset (Premium)
    val Sunset = AppColorScheme(
        id = "sunset",
        name = "Sunset",
        isPremium = true,
        primaryLight = Color(0xFFF97316),
        primaryDark = Color(0xFFFFAB91),   // Lighter coral for dark mode
        accentLight = Color(0xFFEC4899),
        accentDark = Color(0xFFF8BBD0),    // Lighter pink
        cardBackgroundLight = Color(0xFFFFF7ED),
        cardBackgroundDark = Color(0xFF7C2D12)
    )

    // Forest (Premium)
    val Forest = AppColorScheme(
        id = "forest",
        name = "Forest",
        isPremium = true,
        primaryLight = Color(0xFF16A34A),
        primaryDark = Color(0xFF81C784),   // Lighter green for dark mode
        accentLight = Color(0xFF84CC16),
        accentDark = Color(0xFFC5E1A5),    // Lighter lime
        cardBackgroundLight = Color(0xFFF0FDF4),
        cardBackgroundDark = Color(0xFF14532D)
    )

    // Lavender (Premium)
    val Lavender = AppColorScheme(
        id = "lavender",
        name = "Lavender",
        isPremium = true,
        primaryLight = Color(0xFF9333EA),
        primaryDark = Color(0xFFCE93D8),   // Lighter purple for dark mode
        accentLight = Color(0xFFEC4899),
        accentDark = Color(0xFFF8BBD0),    // Lighter pink
        cardBackgroundLight = Color(0xFFFAF5FF),
        cardBackgroundDark = Color(0xFF581C87)
    )

    // Mint (Premium)
    val Mint = AppColorScheme(
        id = "mint",
        name = "Mint",
        isPremium = true,
        primaryLight = Color(0xFF14B8A6),
        primaryDark = Color(0xFF80CBC4),   // Lighter mint for dark mode
        accentLight = Color(0xFF10B981),
        accentDark = Color(0xFFA5D6A7),    // Lighter green
        cardBackgroundLight = Color(0xFFF0FDFA),
        cardBackgroundDark = Color(0xFF134E4A)
    )

    // Rose (Premium)
    val Rose = AppColorScheme(
        id = "rose",
        name = "Rose",
        isPremium = true,
        primaryLight = Color(0xFFE11D48),
        primaryDark = Color(0xFFF48FB1),   // Lighter rose for dark mode
        accentLight = Color(0xFFBE123C),
        accentDark = Color(0xFFF8BBD0),    // Lighter pink
        cardBackgroundLight = Color(0xFFFFF1F2),
        cardBackgroundDark = Color(0xFF881337)
    )

    // Monochrome (Premium)
    val Monochrome = AppColorScheme(
        id = "monochrome",
        name = "Monochrome",
        isPremium = true,
        primaryLight = Color(0xFF18181B),
        primaryDark = Color(0xFFE0E0E0),   // Much lighter gray for visibility
        accentLight = Color(0xFF52525B),
        accentDark = Color(0xFFF5F5F5),    // Very light gray
        cardBackgroundLight = Color(0xFFFAFAFA),
        cardBackgroundDark = Color(0xFF27272A)
    )

    // All available themes
    val allThemes = listOf(
        Default,
        Ocean,
        Sunset,
        Forest,
        Lavender,
        Mint,
        Rose,
        Monochrome
    )

    // Get theme by ID
    fun themeById(id: String): AppColorScheme {
        return allThemes.firstOrNull { it.id == id } ?: Default
    }
}
