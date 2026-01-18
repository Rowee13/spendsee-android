package com.spendsee.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * App color scheme matching iOS version with mockup design support
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
    val cardBackgroundDark: Color,
    val backgroundLight: Color,  // Full screen background for light mode
    val backgroundDark: Color    // Full screen background for dark mode
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
        cardBackgroundLight = Color(0xFFFFFFFF),
        cardBackgroundDark = Color(0xFF1C1C1E),
        backgroundLight = Color(0xFFE3F2FD),  // Light blue tint
        backgroundDark = Color(0xFF0D1B2A)     // Dark blue
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
        cardBackgroundLight = Color(0xFFFFFFFF),
        cardBackgroundDark = Color(0xFF164E63),
        backgroundLight = Color(0xFFCFF5FF),  // Light cyan
        backgroundDark = Color(0xFF0A2F3A)     // Dark ocean blue
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
        cardBackgroundLight = Color(0xFFFFFFFF),
        cardBackgroundDark = Color(0xFF7C2D12),
        backgroundLight = Color(0xFFFFE4D6),  // Light peach
        backgroundDark = Color(0xFF3A1506)     // Dark orange
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
        cardBackgroundLight = Color(0xFFFFFFFF),
        cardBackgroundDark = Color(0xFF14532D),
        backgroundLight = Color(0xFFD9F5E3),  // Light green
        backgroundDark = Color(0xFF0A2817)     // Dark forest
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
        cardBackgroundLight = Color(0xFFFFFFFF),
        cardBackgroundDark = Color(0xFF581C87),
        backgroundLight = Color(0xFFF3E5FF),  // Light lavender
        backgroundDark = Color(0xFF2A0E44)     // Dark purple
    )

    // Mint (Premium) - Based on mockup design
    val Mint = AppColorScheme(
        id = "mint",
        name = "Mint",
        isPremium = true,
        primaryLight = Color(0xFF5A9E9E),
        primaryDark = Color(0xFF80CBC4),   // Lighter mint for dark mode
        accentLight = Color(0xFF34C759),
        accentDark = Color(0xFFA5D6A7),    // Lighter green
        cardBackgroundLight = Color(0xFFFFFFFF),
        cardBackgroundDark = Color(0xFF134E4A),
        backgroundLight = Color(0xFFB8E5E5),  // Mockup teal background
        backgroundDark = Color(0xFF0D3A3A)     // Dark teal
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
        cardBackgroundLight = Color(0xFFFFFFFF),
        cardBackgroundDark = Color(0xFF881337),
        backgroundLight = Color(0xFFFFE4EC),  // Light rose
        backgroundDark = Color(0xFF44091A)     // Dark rose
    )

    // Monochrome (Premium) - Black & white version of mockup
    val Monochrome = AppColorScheme(
        id = "monochrome",
        name = "Monochrome",
        isPremium = true,
        primaryLight = Color(0xFF18181B),
        primaryDark = Color(0xFFE0E0E0),   // Much lighter gray for visibility
        accentLight = Color(0xFF52525B),
        accentDark = Color(0xFFF5F5F5),    // Very light gray
        cardBackgroundLight = Color(0xFFFFFFFF),
        cardBackgroundDark = Color(0xFF27272A),
        backgroundLight = Color(0xFFF5F5F5),  // Light gray
        backgroundDark = Color(0xFF0A0A0A)     // Almost black
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
