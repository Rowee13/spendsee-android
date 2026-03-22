package com.spendsee.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Central theme color mapping with light and dark mode support.
 * Dark mode defaults are aligned with iOS system colors for cross-platform consistency.
 */
data class ThemeColors(
    val id: String,
    val name: String,
    val isPremium: Boolean,

    // Light Mode Colors
    val background: Color,           // Screen background (very light tint)
    val surface: Color,              // Cards, elevated surfaces (light tint)
    val border: Color,               // Dividers, outlines (medium tint)
    val accent: Color,               // Primary interactive color (buttons, FABs, icons)
    val inactive: Color,             // Secondary text, disabled elements
    val textColor: Color = Color(0xFF1A1A1A), // Primary text — theme-aware, defaults to near-black
    val gradientStart: Color,        // Gradient start
    val gradientEnd: Color,          // Gradient end

    // Dark Mode Colors — defaults match iOS/Apple system dark colors
    val darkBackground: Color = Color(0xFF1C1C1E),   // iOS system dark background
    val darkSurface: Color = Color(0xFF2C2C2E),       // iOS system grouped background
    val darkBorder: Color = Color(0xFF48484A),         // iOS system separator
    val darkAccent: Color,                             // Brightened accent for dark mode contrast
    val darkInactive: Color = Color(0xFF8E8E93),       // iOS secondary label
    val darkText: Color = Color(0xFFE0E0E0),           // Primary text in dark mode
    val darkGradientStart: Color,
    val darkGradientEnd: Color
) {
    fun getBackground(isDark: Boolean) = if (isDark) darkBackground else background
    fun getSurface(isDark: Boolean) = if (isDark) darkSurface else surface
    fun getBorder(isDark: Boolean) = if (isDark) darkBorder else border
    fun getAccent(isDark: Boolean) = if (isDark) darkAccent else accent
    fun getInactive(isDark: Boolean) = if (isDark) darkInactive else inactive
    fun getGradientStart(isDark: Boolean) = if (isDark) darkGradientStart else gradientStart
    fun getGradientEnd(isDark: Boolean) = if (isDark) darkGradientEnd else gradientEnd
    fun getText(isDark: Boolean) = if (isDark) darkText else textColor
}

object ThemeColorSchemes {

    // ── Default (Blue) ────────────────────────────────────────────────────────
    val Default = ThemeColors(
        id = "default",
        name = "Default",
        isPremium = false,
        background = Color(0xFFF5F9FF),
        surface = Color(0xFFE8F1FF),
        border = Color(0xFFC5D9F0),
        accent = Color(0xFF007AFF),
        inactive = Color(0xFF676767),
        textColor = Color(0xFF1060C0),
        gradientStart = Color(0xFF5A9FF5),
        gradientEnd = Color(0xFF8CBEF8),
        darkAccent = Color(0xFF5BA8F0),
        darkGradientStart = Color(0xFF93C5FD),
        darkGradientEnd = Color(0xFFBADBFE)
    )

    // ── Ocean ─────────────────────────────────────────────────────────────────
    val Ocean = ThemeColors(
        id = "ocean",
        name = "Ocean",
        isPremium = true,
        background = Color(0xFFF0FAFB),
        surface = Color(0xFFE6F8F7),
        border = Color(0xFFB8DCDB),
        accent = Color(0xFF0891B2),
        inactive = Color(0xFF676767),
        textColor = Color(0xFF086585),
        gradientStart = Color(0xFF68C5CE),
        gradientEnd = Color(0xFFB5D89F),
        darkAccent = Color(0xFF45C8D8),
        darkGradientStart = Color(0xFF8ED9E0),
        darkGradientEnd = Color(0xFFCCE8BA)
    )

    // ── Sunset ────────────────────────────────────────────────────────────────
    val Sunset = ThemeColors(
        id = "sunset",
        name = "Sunset",
        isPremium = true,
        background = Color(0xFFFFF8F5),
        surface = Color(0xFFFFEEE3),
        border = Color(0xFFF5CCBC),
        accent = Color(0xFFE8680A),
        inactive = Color(0xFF676767),
        textColor = Color(0xFFC05C0A),
        gradientStart = Color(0xFFE8680A),
        gradientEnd = Color(0xFFF5A34D),
        darkAccent = Color(0xFFFFAB91),
        darkGradientStart = Color(0xFFFF9B4A),
        darkGradientEnd = Color(0xFFFFCC88)
    )

    // ── Forest ────────────────────────────────────────────────────────────────
    val Forest = ThemeColors(
        id = "forest",
        name = "Forest",
        isPremium = true,
        background = Color(0xFFF3FCF4),
        surface = Color(0xFFE2F6E4),
        border = Color(0xFFBDE4BF),
        accent = Color(0xFF158A3E),
        inactive = Color(0xFF676767),
        textColor = Color(0xFF136E35),
        gradientStart = Color(0xFF58B96D),
        gradientEnd = Color(0xFF98DCA2),
        darkAccent = Color(0xFF7BC07E),
        darkGradientStart = Color(0xFF7ADB8C),
        darkGradientEnd = Color(0xFFB8F0C0)
    )

    // ── Lavender ──────────────────────────────────────────────────────────────
    val Lavender = ThemeColors(
        id = "lavender",
        name = "Lavender",
        isPremium = true,
        background = Color(0xFFFAF5FF),
        surface = Color(0xFFEFE4F8),
        border = Color(0xFFDCC6EC),
        accent = Color(0xFF7C28CC),
        inactive = Color(0xFF676767),
        textColor = Color(0xFF6A20B5),
        gradientStart = Color(0xFF9A72C0),
        gradientEnd = Color(0xFFC09ADE),
        darkAccent = Color(0xFFC485D0),
        darkGradientStart = Color(0xFFC199E0),
        darkGradientEnd = Color(0xFFDDBBF0)
    )

    // ── Mint ──────────────────────────────────────────────────────────────────
    val Mint = ThemeColors(
        id = "mint",
        name = "Mint",
        isPremium = true,
        background = Color(0xFFF3FFF9),
        surface = Color(0xFFE2F9EF),
        border = Color(0xFFBBEBDA),
        accent = Color(0xFF0EA090),
        inactive = Color(0xFF676767),
        textColor = Color(0xFF0A8F80),
        gradientStart = Color(0xFF55C09A),
        gradientEnd = Color(0xFF95DEC2),
        darkAccent = Color(0xFF7AC4BC),
        darkGradientStart = Color(0xFF78DBC0),
        darkGradientEnd = Color(0xFFB8F0DD)
    )

    // ── Rose ──────────────────────────────────────────────────────────────────
    val Rose = ThemeColors(
        id = "rose",
        name = "Rose",
        isPremium = true,
        background = Color(0xFFFFF2F4),
        surface = Color(0xFFFFE0E3),
        border = Color(0xFFF5B5B8),
        accent = Color(0xFFC8183F),
        inactive = Color(0xFF676767),
        textColor = Color(0xFFA81035),
        gradientStart = Color(0xFFDC80A0),
        gradientEnd = Color(0xFFCC98BE),
        darkAccent = Color(0xFFF08AAC),
        darkGradientStart = Color(0xFFF5A3C0),
        darkGradientEnd = Color(0xFFE8BBDC)
    )

    // ── Sakura ────────────────────────────────────────────────────────────────
    val Sakura = ThemeColors(
        id = "sakura",
        name = "Sakura",
        isPremium = true,
        background = Color(0xFFFFF5FB),
        surface = Color(0xFFFBE8F5),
        border = Color(0xFFEFCAE4),
        accent = Color(0xFFA860A0),
        inactive = Color(0xFF676767),
        textColor = Color(0xFF8C4890),
        gradientStart = Color(0xFFDF98C8),
        gradientEnd = Color(0xFFCBAADF),
        darkAccent = Color(0xFFE095C8),
        darkGradientStart = Color(0xFFF5BBE0),
        darkGradientEnd = Color(0xFFE8CCF5)
    )

    // ── Monochrome ────────────────────────────────────────────────────────────
    val Monochrome = ThemeColors(
        id = "monochrome",
        name = "Monochrome",
        isPremium = true,
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF4F4F5),
        border = Color(0xFFD1D1D6),
        accent = Color(0xFF48484F),
        inactive = Color(0xFF676767),
        textColor = Color(0xFF1A1A1A),
        gradientStart = Color(0xFF6E6E78),
        gradientEnd = Color(0xFFA0A0A8),
        darkAccent = Color(0xFFC0C0C8),
        darkGradientStart = Color(0xFFB8BFC9),
        darkGradientEnd = Color(0xFFD1D5DB)
    )

    // ── Clean Variants ────────────────────────────────────────────────────────
    // White backgrounds, neutral gray surfaces, near-black text.
    // Brand color is accent-only (buttons, icons, progress bars) — not used for text or surfaces.

    val DefaultClean = ThemeColors(
        id = "default-clean",
        name = "Default Clean",
        isPremium = true,
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF2F2F7),
        border = Color(0xFFE0E0E5),
        accent = Color(0xFF5580B8),
        inactive = Color(0xFF676767),
        textColor = Color(0xFF1A1A1A),
        gradientStart = Color(0xFF5580B8),
        gradientEnd = Color(0xFF7AA0D8),
        darkAccent = Color(0xFF88AADC),
        darkText = Color(0xFFF2F2F7),
        darkGradientStart = Color(0xFF88AADC),
        darkGradientEnd = Color(0xFFAACCED)
    )

    val OceanClean = ThemeColors(
        id = "ocean-clean",
        name = "Ocean Clean",
        isPremium = true,
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF2F2F7),
        border = Color(0xFFE0E0E5),
        accent = Color(0xFF4295A8),
        inactive = Color(0xFF676767),
        textColor = Color(0xFF1A1A1A),
        gradientStart = Color(0xFF4295A8),
        gradientEnd = Color(0xFF78C8B8),
        darkAccent = Color(0xFF72C0D0),
        darkText = Color(0xFFF2F2F7),
        darkGradientStart = Color(0xFF72C0D0),
        darkGradientEnd = Color(0xFF98DDD0)
    )

    val SunsetClean = ThemeColors(
        id = "sunset-clean",
        name = "Sunset Clean",
        isPremium = true,
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF2F2F7),
        border = Color(0xFFE0E0E5),
        accent = Color(0xFFC97845),
        inactive = Color(0xFF676767),
        textColor = Color(0xFF1A1A1A),
        gradientStart = Color(0xFFC97845),
        gradientEnd = Color(0xFFD89868),
        darkAccent = Color(0xFFE8A878),
        darkText = Color(0xFFF2F2F7),
        darkGradientStart = Color(0xFFE8A878),
        darkGradientEnd = Color(0xFFF0C09A)
    )

    val ForestClean = ThemeColors(
        id = "forest-clean",
        name = "Forest Clean",
        isPremium = true,
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF2F2F7),
        border = Color(0xFFE0E0E5),
        accent = Color(0xFF52956A),
        inactive = Color(0xFF676767),
        textColor = Color(0xFF1A1A1A),
        gradientStart = Color(0xFF52956A),
        gradientEnd = Color(0xFF78B888),
        darkAccent = Color(0xFF88C898),
        darkText = Color(0xFFF2F2F7),
        darkGradientStart = Color(0xFF88C898),
        darkGradientEnd = Color(0xFFA8D8B0)
    )

    val LavenderClean = ThemeColors(
        id = "lavender-clean",
        name = "Lavender Clean",
        isPremium = true,
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF2F2F7),
        border = Color(0xFFE0E0E5),
        accent = Color(0xFF8A62B5),
        inactive = Color(0xFF676767),
        textColor = Color(0xFF1A1A1A),
        gradientStart = Color(0xFF8A62B5),
        gradientEnd = Color(0xFFA882CC),
        darkAccent = Color(0xFFB898D8),
        darkText = Color(0xFFF2F2F7),
        darkGradientStart = Color(0xFFB898D8),
        darkGradientEnd = Color(0xFFCEB8E8)
    )

    val MintClean = ThemeColors(
        id = "mint-clean",
        name = "Mint Clean",
        isPremium = true,
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF2F2F7),
        border = Color(0xFFE0E0E5),
        accent = Color(0xFF3AA898),
        inactive = Color(0xFF676767),
        textColor = Color(0xFF1A1A1A),
        gradientStart = Color(0xFF3AA898),
        gradientEnd = Color(0xFF62C0A8),
        darkAccent = Color(0xFF78C8BC),
        darkText = Color(0xFFF2F2F7),
        darkGradientStart = Color(0xFF78C8BC),
        darkGradientEnd = Color(0xFF98D8CC)
    )

    val RoseClean = ThemeColors(
        id = "rose-clean",
        name = "Rose Clean",
        isPremium = true,
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF2F2F7),
        border = Color(0xFFE0E0E5),
        accent = Color(0xFFB85070),
        inactive = Color(0xFF676767),
        textColor = Color(0xFF1A1A1A),
        gradientStart = Color(0xFFB85070),
        gradientEnd = Color(0xFFD07090),
        darkAccent = Color(0xFFE090A8),
        darkText = Color(0xFFF2F2F7),
        darkGradientStart = Color(0xFFE090A8),
        darkGradientEnd = Color(0xFFF0AEBE)
    )

    val SakuraClean = ThemeColors(
        id = "sakura-clean",
        name = "Sakura Clean",
        isPremium = true,
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF2F2F7),
        border = Color(0xFFE0E0E5),
        accent = Color(0xFFA86098),
        inactive = Color(0xFF676767),
        textColor = Color(0xFF1A1A1A),
        gradientStart = Color(0xFFA86098),
        gradientEnd = Color(0xFFC080B0),
        darkAccent = Color(0xFFD098C8),
        darkText = Color(0xFFF2F2F7),
        darkGradientStart = Color(0xFFD098C8),
        darkGradientEnd = Color(0xFFE0B8D8)
    )

    val MonochromeClean = ThemeColors(
        id = "monochrome-clean",
        name = "Monochrome Clean",
        isPremium = true,
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF2F2F7),
        border = Color(0xFFE0E0E5),
        accent = Color(0xFF606068),
        inactive = Color(0xFF676767),
        textColor = Color(0xFF1A1A1A),
        gradientStart = Color(0xFF606068),
        gradientEnd = Color(0xFF909098),
        darkAccent = Color(0xFFC0C0C8),
        darkText = Color(0xFFF2F2F7),
        darkGradientStart = Color(0xFFC0C0C8),
        darkGradientEnd = Color(0xFFD8D8E0)
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
        Sakura,
        Monochrome,
        // Clean variants
        DefaultClean,
        OceanClean,
        SunsetClean,
        ForestClean,
        LavenderClean,
        MintClean,
        RoseClean,
        SakuraClean,
        MonochromeClean
    )

    fun themeById(id: String): ThemeColors {
        return allThemes.firstOrNull { it.id == id } ?: Default
    }
}
