package com.spendsee.managers

import android.content.Context
import com.spendsee.ui.theme.ThemeColorSchemes
import com.spendsee.ui.theme.ThemeColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _currentTheme = MutableStateFlow(loadTheme())
    val currentTheme: StateFlow<ThemeColors> = _currentTheme.asStateFlow()

    private fun loadTheme(): ThemeColors {
        val themeId = prefs.getString("selected_theme", "ocean") ?: "ocean"
        return ThemeColorSchemes.themeById(themeId)
    }

    fun setTheme(themeId: String) {
        val theme = ThemeColorSchemes.themeById(themeId)
        _currentTheme.value = theme
        prefs.edit().putString("selected_theme", themeId).apply()
    }

    fun setTheme(theme: ThemeColors) {
        _currentTheme.value = theme
        prefs.edit().putString("selected_theme", theme.id).apply()
    }

    companion object {
        @Volatile
        private var INSTANCE: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemeManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
