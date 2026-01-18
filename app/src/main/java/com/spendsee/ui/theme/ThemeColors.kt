package com.spendsee.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Central theme color mapping with 4 shades per theme
 * Following the Ocean mockup pattern
 */
data class ThemeColors(
    val id: String,
    val name: String,
    val isPremium: Boolean,
    // Main background - very light tint
    val background: Color,
    // Cards and surfaces - light tint
    val surface: Color,
    // Borders - medium tint
    val border: Color,
    // Active/Accent - darker, saturated
    val accent: Color,
    // Inactive elements
    val inactive: Color = Color(0xFF676767),
    // Gradient colors for profit/spent cards
    val gradientStart: Color,
    val gradientEnd: Color
)

object ThemeColorSchemes {

    // Ocean Theme (Current mockup implementation)
    val Ocean = ThemeColors(
        id = "ocean",
        name = "Ocean",
        isPremium = true,
        background = Color(0xFFEFFFFF),      // Very light cyan
        surface = Color(0xFFDAF4F3),         // Light teal
        border = Color(0xFFAAD4D3),          // Medium teal
        accent = Color(0xFF418E8C),          // Dark teal
        gradientStart = Color(0xFF72CCD5),   // Medium cyan
        gradientEnd = Color(0xFFB9DAA3)      // Light green
    )

    // Rose Theme (Following Ocean pattern)
    val Rose = ThemeColors(
        id = "rose",
        name = "Rose",
        isPremium = true,
        background = Color(0xFFFFEFF5),      // Very light rose
        surface = Color(0xFFF8DBE8),         // Light rose
        border = Color(0xFFE8B4CD),          // Medium rose
        accent = Color(0xFFC84B7B),          // Dark rose/pink
        gradientStart = Color(0xFFE88BA8),   // Medium rose
        gradientEnd = Color(0xFFD8A3C7)      // Light purple-rose
    )

    // Sunset Theme (Following Ocean pattern)
    val Sunset = ThemeColors(
        id = "sunset",
        name = "Sunset",
        isPremium = true,
        background = Color(0xFFFFF4EF),      // Very light peach
        surface = Color(0xFFFFDDCC),         // Light coral
        border = Color(0xFFFFBBA3),          // Medium coral
        accent = Color(0xFFE85D3A),          // Dark orange
        gradientStart = Color(0xFFF97316),   // Orange
        gradientEnd = Color(0xFFFFB366)      // Light orange
    )

    // Forest Theme (Following Ocean pattern)
    val Forest = ThemeColors(
        id = "forest",
        name = "Forest",
        isPremium = true,
        background = Color(0xFFEFF9F0),      // Very light green
        surface = Color(0xFFD4F1D7),         // Light green
        border = Color(0xFFAADDAF),          // Medium green
        accent = Color(0xFF2D7A3E),          // Dark forest green
        gradientStart = Color(0xFF5BC470),   // Medium green
        gradientEnd = Color(0xFF9FE6A8)      // Light green
    )

    // Lavender Theme (Following Ocean pattern)
    val Lavender = ThemeColors(
        id = "lavender",
        name = "Lavender",
        isPremium = true,
        background = Color(0xFFF8F0FF),      // Very light purple
        surface = Color(0xFFE8D9F5),         // Light lavender
        border = Color(0xFFD0B3E6),          // Medium purple
        accent = Color(0xFF7E3FAF),          // Dark purple
        gradientStart = Color(0xFFA67FC9),   // Medium purple
        gradientEnd = Color(0xFFC9A3E6)      // Light purple
    )

    // Mint Theme (Following Ocean pattern)
    val Mint = ThemeColors(
        id = "mint",
        name = "Mint",
        isPremium = true,
        background = Color(0xFFEFFFF8),      // Very light mint
        surface = Color(0xFFD4F5E8),         // Light mint
        border = Color(0xFFAAE6D0),          // Medium mint
        accent = Color(0xFF2D8F6E),          // Dark mint
        gradientStart = Color(0xFF5BCBA3),   // Medium mint
        gradientEnd = Color(0xFF9FE6CA)      // Light mint
    )

    // Monochrome Theme (Black & white version)
    val Monochrome = ThemeColors(
        id = "monochrome",
        name = "Monochrome",
        isPremium = true,
        background = Color(0xFFFFFFFF),      // White
        surface = Color(0xFFF5F5F5),         // Light gray
        border = Color(0xFFD4D4D4),          // Medium gray
        accent = Color(0xFF404040),          // Dark gray
        gradientStart = Color(0xFF737373),   // Medium gray
        gradientEnd = Color(0xFFA3A3A3)      // Light gray
    )

    // Default/Free Theme (Blue - following Ocean pattern)
    val Default = ThemeColors(
        id = "default",
        name = "Default",
        isPremium = false,
        background = Color(0xFFEFF6FF),      // Very light blue
        surface = Color(0xFFDBEAFE),         // Light blue
        border = Color(0xFFBFDBFE),          // Medium blue
        accent = Color(0xFF2563EB),          // Dark blue
        gradientStart = Color(0xFF60A5FA),   // Medium blue
        gradientEnd = Color(0xFF93C5FD)      // Light blue
    )

    // All available themes
    val allThemes = listOf(
        Default,
        Ocean,
        Rose,
        Sunset,
        Forest,
        Lavender,
        Mint,
        Monochrome
    )

    // Get theme by ID
    fun themeById(id: String): ThemeColors {
        return allThemes.firstOrNull { it.id == id } ?: Ocean
    }
}
