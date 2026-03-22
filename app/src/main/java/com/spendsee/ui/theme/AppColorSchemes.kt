package com.spendsee.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * App color scheme for Material3 ColorScheme generation.
 * Mirrors the iOS AppColorScheme structure for cross-platform consistency.
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
    val backgroundLight: Color,
    val backgroundDark: Color
)

object AppColorSchemes {

    // ── Default (Blue) ────────────────────────────────────────────────────────
    val Default = AppColorScheme(
        id = "default",
        name = "Default",
        isPremium = false,
        primaryLight = Color(0xFF007AFF),
        primaryDark = Color(0xFF5BA8F0),
        accentLight = Color(0xFF007AFF),
        accentDark = Color(0xFF85C0F5),
        cardBackgroundLight = Color(0xFFF2F2F7),
        cardBackgroundDark = Color(0xFF1C1C1E),
        backgroundLight = Color(0xFFF5F9FF),
        backgroundDark = Color(0xFF1C1C1E)
    )

    // ── Ocean ─────────────────────────────────────────────────────────────────
    val Ocean = AppColorScheme(
        id = "ocean",
        name = "Ocean",
        isPremium = true,
        primaryLight = Color(0xFF0A7EA4),
        primaryDark = Color(0xFF45C8D8),
        accentLight = Color(0xFF0891B2),
        accentDark = Color(0xFF1EC5DE),
        cardBackgroundLight = Color(0xFFE0F2FE),
        cardBackgroundDark = Color(0xFF164E63),
        backgroundLight = Color(0xFFF0FAFB),
        backgroundDark = Color(0xFF1C1C1E)
    )

    // ── Sunset ────────────────────────────────────────────────────────────────
    val Sunset = AppColorScheme(
        id = "sunset",
        name = "Sunset",
        isPremium = true,
        primaryLight = Color(0xFFE8680A),
        primaryDark = Color(0xFFFFAB91),
        accentLight = Color(0xFFB83280),
        accentDark = Color(0xFFF5B4CB),
        cardBackgroundLight = Color(0xFFFFF7ED),
        cardBackgroundDark = Color(0xFF7C2D12),
        backgroundLight = Color(0xFFFFF8F5),
        backgroundDark = Color(0xFF1C1C1E)
    )

    // ── Forest ────────────────────────────────────────────────────────────────
    val Forest = AppColorScheme(
        id = "forest",
        name = "Forest",
        isPremium = true,
        primaryLight = Color(0xFF158A3E),
        primaryDark = Color(0xFF81C784),
        accentLight = Color(0xFF6BA512),
        accentDark = Color(0xFFBCDBA0),
        cardBackgroundLight = Color(0xFFF0FDF4),
        cardBackgroundDark = Color(0xFF14532D),
        backgroundLight = Color(0xFFF3FCF4),
        backgroundDark = Color(0xFF1C1C1E)
    )

    // ── Lavender ──────────────────────────────────────────────────────────────
    val Lavender = AppColorScheme(
        id = "lavender",
        name = "Lavender",
        isPremium = true,
        primaryLight = Color(0xFF7C28CC),
        primaryDark = Color(0xFFC88ED2),
        accentLight = Color(0xFFC4317F),
        accentDark = Color(0xFFF2B0C8),
        cardBackgroundLight = Color(0xFFFAF5FF),
        cardBackgroundDark = Color(0xFF581C87),
        backgroundLight = Color(0xFFFAF5FF),
        backgroundDark = Color(0xFF1C1C1E)
    )

    // ── Mint ──────────────────────────────────────────────────────────────────
    val Mint = AppColorScheme(
        id = "mint",
        name = "Mint",
        isPremium = true,
        primaryLight = Color(0xFF0EA090),
        primaryDark = Color(0xFF7AC4BC),
        accentLight = Color(0xFF0C9E6D),
        accentDark = Color(0xFF9ECFA0),
        cardBackgroundLight = Color(0xFFF0FDFA),
        cardBackgroundDark = Color(0xFF134E4A),
        backgroundLight = Color(0xFFF3FFF9),
        backgroundDark = Color(0xFF1C1C1E)
    )

    // ── Rose ──────────────────────────────────────────────────────────────────
    val Rose = AppColorScheme(
        id = "rose",
        name = "Rose",
        isPremium = true,
        primaryLight = Color(0xFFC8183F),
        primaryDark = Color(0xFFF48FB1),
        accentLight = Color(0xFFA81035),
        accentDark = Color(0xFFF2B2C8),
        cardBackgroundLight = Color(0xFFFFF1F2),
        cardBackgroundDark = Color(0xFF881337),
        backgroundLight = Color(0xFFFFF2F4),
        backgroundDark = Color(0xFF1C1C1E)
    )

    // ── Sakura ────────────────────────────────────────────────────────────────
    val Sakura = AppColorScheme(
        id = "sakura",
        name = "Sakura",
        isPremium = true,
        primaryLight = Color(0xFFA860A0),
        primaryDark = Color(0xFFE095C8),
        accentLight = Color(0xFFA860A0),
        accentDark = Color(0xFFE095C8),
        cardBackgroundLight = Color(0xFFFBE8F5),
        cardBackgroundDark = Color(0xFF2C2C2E),
        backgroundLight = Color(0xFFFFF5FB),
        backgroundDark = Color(0xFF1C1C1E)
    )

    // ── Monochrome ────────────────────────────────────────────────────────────
    val Monochrome = AppColorScheme(
        id = "monochrome",
        name = "Monochrome",
        isPremium = true,
        primaryLight = Color(0xFF18181B),
        primaryDark = Color(0xFFE0E0E0),
        accentLight = Color(0xFF48484F),
        accentDark = Color(0xFFF5F5F5),
        cardBackgroundLight = Color(0xFFFAFAFA),
        cardBackgroundDark = Color(0xFF27272A),
        backgroundLight = Color(0xFFFFFFFF),
        backgroundDark = Color(0xFF1C1C1E)
    )

    // ── Clean Variants ────────────────────────────────────────────────────────
    // White/neutral backgrounds, dark pastel brand accents only.

    val DefaultClean = AppColorScheme(
        id = "default-clean",
        name = "Default Clean",
        isPremium = true,
        primaryLight = Color(0xFF5580B8),
        primaryDark = Color(0xFF88AADC),
        accentLight = Color(0xFF5580B8),
        accentDark = Color(0xFF88AADC),
        cardBackgroundLight = Color(0xFFF2F2F7),
        cardBackgroundDark = Color(0xFF2C2C2E),
        backgroundLight = Color(0xFFFFFFFF),
        backgroundDark = Color(0xFF1C1C1E)
    )

    val OceanClean = AppColorScheme(
        id = "ocean-clean",
        name = "Ocean Clean",
        isPremium = true,
        primaryLight = Color(0xFF4295A8),
        primaryDark = Color(0xFF72C0D0),
        accentLight = Color(0xFF4295A8),
        accentDark = Color(0xFF72C0D0),
        cardBackgroundLight = Color(0xFFF2F2F7),
        cardBackgroundDark = Color(0xFF2C2C2E),
        backgroundLight = Color(0xFFFFFFFF),
        backgroundDark = Color(0xFF1C1C1E)
    )

    val SunsetClean = AppColorScheme(
        id = "sunset-clean",
        name = "Sunset Clean",
        isPremium = true,
        primaryLight = Color(0xFFC97845),
        primaryDark = Color(0xFFE8A878),
        accentLight = Color(0xFFC97845),
        accentDark = Color(0xFFE8A878),
        cardBackgroundLight = Color(0xFFF2F2F7),
        cardBackgroundDark = Color(0xFF2C2C2E),
        backgroundLight = Color(0xFFFFFFFF),
        backgroundDark = Color(0xFF1C1C1E)
    )

    val ForestClean = AppColorScheme(
        id = "forest-clean",
        name = "Forest Clean",
        isPremium = true,
        primaryLight = Color(0xFF52956A),
        primaryDark = Color(0xFF88C898),
        accentLight = Color(0xFF52956A),
        accentDark = Color(0xFF88C898),
        cardBackgroundLight = Color(0xFFF2F2F7),
        cardBackgroundDark = Color(0xFF2C2C2E),
        backgroundLight = Color(0xFFFFFFFF),
        backgroundDark = Color(0xFF1C1C1E)
    )

    val LavenderClean = AppColorScheme(
        id = "lavender-clean",
        name = "Lavender Clean",
        isPremium = true,
        primaryLight = Color(0xFF8A62B5),
        primaryDark = Color(0xFFB898D8),
        accentLight = Color(0xFF8A62B5),
        accentDark = Color(0xFFB898D8),
        cardBackgroundLight = Color(0xFFF2F2F7),
        cardBackgroundDark = Color(0xFF2C2C2E),
        backgroundLight = Color(0xFFFFFFFF),
        backgroundDark = Color(0xFF1C1C1E)
    )

    val MintClean = AppColorScheme(
        id = "mint-clean",
        name = "Mint Clean",
        isPremium = true,
        primaryLight = Color(0xFF3AA898),
        primaryDark = Color(0xFF78C8BC),
        accentLight = Color(0xFF3AA898),
        accentDark = Color(0xFF78C8BC),
        cardBackgroundLight = Color(0xFFF2F2F7),
        cardBackgroundDark = Color(0xFF2C2C2E),
        backgroundLight = Color(0xFFFFFFFF),
        backgroundDark = Color(0xFF1C1C1E)
    )

    val RoseClean = AppColorScheme(
        id = "rose-clean",
        name = "Rose Clean",
        isPremium = true,
        primaryLight = Color(0xFFB85070),
        primaryDark = Color(0xFFE090A8),
        accentLight = Color(0xFFB85070),
        accentDark = Color(0xFFE090A8),
        cardBackgroundLight = Color(0xFFF2F2F7),
        cardBackgroundDark = Color(0xFF2C2C2E),
        backgroundLight = Color(0xFFFFFFFF),
        backgroundDark = Color(0xFF1C1C1E)
    )

    val SakuraClean = AppColorScheme(
        id = "sakura-clean",
        name = "Sakura Clean",
        isPremium = true,
        primaryLight = Color(0xFFA86098),
        primaryDark = Color(0xFFD098C8),
        accentLight = Color(0xFFA86098),
        accentDark = Color(0xFFD098C8),
        cardBackgroundLight = Color(0xFFF2F2F7),
        cardBackgroundDark = Color(0xFF2C2C2E),
        backgroundLight = Color(0xFFFFFFFF),
        backgroundDark = Color(0xFF1C1C1E)
    )

    val MonochromeClean = AppColorScheme(
        id = "monochrome-clean",
        name = "Monochrome Clean",
        isPremium = true,
        primaryLight = Color(0xFF606068),
        primaryDark = Color(0xFFC0C0C8),
        accentLight = Color(0xFF606068),
        accentDark = Color(0xFFC0C0C8),
        cardBackgroundLight = Color(0xFFF2F2F7),
        cardBackgroundDark = Color(0xFF2C2C2E),
        backgroundLight = Color(0xFFFFFFFF),
        backgroundDark = Color(0xFF1C1C1E)
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

    fun themeById(id: String): AppColorScheme {
        return allThemes.firstOrNull { it.id == id } ?: Default
    }
}
