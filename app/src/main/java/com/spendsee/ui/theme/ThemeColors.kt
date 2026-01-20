package com.spendsee.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Central theme color mapping with light and dark mode support
 * Following the Ocean mockup pattern with dark mode variants
 */
data class ThemeColors(
    val id: String,
    val name: String,
    val isPremium: Boolean,

    // Light Mode Colors
    val background: Color,           // Very light tint
    val surface: Color,              // Light tint
    val border: Color,               // Medium tint
    val accent: Color,               // Darker, saturated
    val inactive: Color,             // Inactive elements
    val gradientStart: Color,        // Gradient start
    val gradientEnd: Color,          // Gradient end

    // Dark Mode Colors
    val darkBackground: Color = Color(0xFF222222),        // Soft black
    val darkSurface: Color = Color(0xFF2A2A2A),           // Slightly lighter dark
    val darkBorder: Color = Color(0xFF71717A),            // Zinc gray
    val darkAccent: Color,                                 // Brightened accent for contrast
    val darkInactive: Color = Color(0xFFA0A0A0),          // Secondary text
    val darkText: Color = Color(0xFFE0E0E0),              // Primary text
    val darkGradientStart: Color,                          // Lighter gradient for dark mode
    val darkGradientEnd: Color                             // Lighter gradient for dark mode
) {
    // Helper function to get colors based on dark mode
    fun getBackground(isDark: Boolean) = if (isDark) darkBackground else background
    fun getSurface(isDark: Boolean) = if (isDark) darkSurface else surface
    fun getBorder(isDark: Boolean) = if (isDark) darkBorder else border
    fun getAccent(isDark: Boolean) = if (isDark) darkAccent else accent
    fun getInactive(isDark: Boolean) = if (isDark) darkInactive else inactive
    fun getGradientStart(isDark: Boolean) = if (isDark) darkGradientStart else gradientStart
    fun getGradientEnd(isDark: Boolean) = if (isDark) darkGradientEnd else gradientEnd
    fun getText(isDark: Boolean) = if (isDark) darkText else Color(0xFF1A1A1A)
}

object ThemeColorSchemes {

    // Ocean Theme
    val Ocean = ThemeColors(
        id = "ocean",
        name = "Ocean",
        isPremium = true,
        // Light mode
        background = Color(0xFFEFFFFF),
        surface = Color(0xFFDAF4F3),
        border = Color(0xFFAAD4D3),
        accent = Color(0xFF418E8C),
        inactive = Color(0xFF676767),
        gradientStart = Color(0xFF72CCD5),
        gradientEnd = Color(0xFFB9DAA3),
        // Dark mode - brightened accent and lighter gradients
        darkAccent = Color(0xFF5AB3AF),
        darkGradientStart = Color(0xFF8ED9E0),
        darkGradientEnd = Color(0xFFCCE8BA)
    )

    // Rose Theme
    val Rose = ThemeColors(
        id = "rose",
        name = "Rose",
        isPremium = true,
        // Light mode - More red tones to differentiate from Sakura
        background = Color(0xFFFFEBEE),
        surface = Color(0xFFFFCDD2),
        border = Color(0xFFEF9A9A),
        accent = Color(0xFFC84B7B),
        inactive = Color(0xFF676767),
        gradientStart = Color(0xFFE88BA8),
        gradientEnd = Color(0xFFD8A3C7),
        // Dark mode
        darkAccent = Color(0xFFE66B96),
        darkGradientStart = Color(0xFFF5A3C0),
        darkGradientEnd = Color(0xFFE8BBDC)
    )

    // Sunset Theme
    val Sunset = ThemeColors(
        id = "sunset",
        name = "Sunset",
        isPremium = true,
        // Light mode
        background = Color(0xFFFFF4EF),
        surface = Color(0xFFFFDDCC),
        border = Color(0xFFFFBBA3),
        accent = Color(0xFFE85D3A),
        inactive = Color(0xFF676767),
        gradientStart = Color(0xFFF97316),
        gradientEnd = Color(0xFFFFB366),
        // Dark mode
        darkAccent = Color(0xFFFF8555),
        darkGradientStart = Color(0xFFFF9B4A),
        darkGradientEnd = Color(0xFFFFCC88)
    )

    // Forest Theme
    val Forest = ThemeColors(
        id = "forest",
        name = "Forest",
        isPremium = true,
        // Light mode
        background = Color(0xFFEFF9F0),
        surface = Color(0xFFD4F1D7),
        border = Color(0xFFAADDAF),
        accent = Color(0xFF2D7A3E),
        inactive = Color(0xFF676767),
        gradientStart = Color(0xFF5BC470),
        gradientEnd = Color(0xFF9FE6A8),
        // Dark mode
        darkAccent = Color(0xFF4A9C5A),
        darkGradientStart = Color(0xFF7ADB8C),
        darkGradientEnd = Color(0xFFB8F0C0)
    )

    // Lavender Theme
    val Lavender = ThemeColors(
        id = "lavender",
        name = "Lavender",
        isPremium = true,
        // Light mode
        background = Color(0xFFF8F0FF),
        surface = Color(0xFFE8D9F5),
        border = Color(0xFFD0B3E6),
        accent = Color(0xFF7E3FAF),
        inactive = Color(0xFF676767),
        gradientStart = Color(0xFFA67FC9),
        gradientEnd = Color(0xFFC9A3E6),
        // Dark mode
        darkAccent = Color(0xFFA05AD1),
        darkGradientStart = Color(0xFFC199E0),
        darkGradientEnd = Color(0xFFDDBBF0)
    )

    // Mint Theme
    val Mint = ThemeColors(
        id = "mint",
        name = "Mint",
        isPremium = true,
        // Light mode
        background = Color(0xFFEFFFF8),
        surface = Color(0xFFD4F5E8),
        border = Color(0xFFAAE6D0),
        accent = Color(0xFF2D8F6E),
        inactive = Color(0xFF676767),
        gradientStart = Color(0xFF5BCBA3),
        gradientEnd = Color(0xFF9FE6CA),
        // Dark mode
        darkAccent = Color(0xFF4AB091),
        darkGradientStart = Color(0xFF78DBC0),
        darkGradientEnd = Color(0xFFB8F0DD)
    )

    // Sakura Theme (Soft pink-lavender)
    val Sakura = ThemeColors(
        id = "sakura",
        name = "Sakura",
        isPremium = true,
        // Light mode - matching the soft pink/lavender UI
        background = Color(0xFFFFF0F8),
        surface = Color(0xFFF5D4E8),
        border = Color(0xFFE8B4D4),
        accent = Color(0xFFC77BB8),
        inactive = Color(0xFF676767),
        gradientStart = Color(0xFFE8A3D0),
        gradientEnd = Color(0xFFD4B3E8),
        // Dark mode
        darkAccent = Color(0xFFE89BCE),
        darkGradientStart = Color(0xFFF5BBE0),
        darkGradientEnd = Color(0xFFE8CCF5)
    )

    // Monochrome Theme
    val Monochrome = ThemeColors(
        id = "monochrome",
        name = "Monochrome",
        isPremium = true,
        // Light mode
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF5F5F5),
        border = Color(0xFFD4D4D4),
        accent = Color(0xFF404040),
        inactive = Color(0xFF676767),
        gradientStart = Color(0xFF737373),
        gradientEnd = Color(0xFFA3A3A3),
        // Dark mode
        darkAccent = Color(0xFF9CA3AF),
        darkGradientStart = Color(0xFFB8BFC9),
        darkGradientEnd = Color(0xFFD1D5DB)
    )

    // Default/Free Theme (Blue)
    val Default = ThemeColors(
        id = "default",
        name = "Default",
        isPremium = false,
        // Light mode
        background = Color(0xFFEFF6FF),
        surface = Color(0xFFDBEAFE),
        border = Color(0xFFBFDBFE),
        accent = Color(0xFF2563EB),
        inactive = Color(0xFF676767),
        gradientStart = Color(0xFF60A5FA),
        gradientEnd = Color(0xFF93C5FD),
        // Dark mode
        darkAccent = Color(0xFF60A5FA),
        darkGradientStart = Color(0xFF93C5FD),
        darkGradientEnd = Color(0xFFBADBFE)
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
        Sakura,
        Monochrome
    )

    // Get theme by ID
    fun themeById(id: String): ThemeColors {
        return allThemes.firstOrNull { it.id == id } ?: Ocean
    }
}
