package com.spendsee.managers

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(loadDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _selectedTheme = MutableStateFlow(loadTheme())
    val selectedTheme: StateFlow<String> = _selectedTheme.asStateFlow()

    private fun loadDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    private fun loadTheme(): String {
        return prefs.getString(KEY_THEME, THEME_DEFAULT) ?: THEME_DEFAULT
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _isDarkMode.value = enabled
    }

    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
        _selectedTheme.value = theme
    }

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_THEME = "selected_theme"

        const val THEME_DEFAULT = "default"
        const val THEME_OCEAN = "ocean"
        const val THEME_SUNSET = "sunset"
        const val THEME_FOREST = "forest"
        const val THEME_LAVENDER = "lavender"
        const val THEME_MINT = "mint"
        const val THEME_ROSE = "rose"
        const val THEME_MONOCHROME = "monochrome"

        @Volatile
        private var instance: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
