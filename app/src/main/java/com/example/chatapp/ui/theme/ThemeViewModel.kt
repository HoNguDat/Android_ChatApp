package com.example.chatapp.ui.theme

import android.app.Application
import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(AppThemeOption.fromName(prefs.getString(KEY_THEME, null)))
    val theme: StateFlow<AppThemeOption> = _theme.asStateFlow()

    fun setTheme(option: AppThemeOption) {
        _theme.value = option
        prefs.edit().putString(KEY_THEME, option.name).apply()
    }

    private companion object {
        const val KEY_THEME = "theme"
    }
}

class ThemeController(
    val current: AppThemeOption,
    val onChange: (AppThemeOption) -> Unit
)

val LocalThemeController = staticCompositionLocalOf {
    ThemeController(AppThemeOption.LIGHT) {}
}