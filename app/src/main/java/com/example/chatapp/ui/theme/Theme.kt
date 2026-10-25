package com.example.chatapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ChatAppTheme(
    themeOption: AppThemeOption = AppThemeOption.LIGHT,
    content: @Composable () -> Unit
) {
    val c = themeOption.colors

    val colorScheme = if (c.isDark) {
        darkColorScheme(
            primary = c.accent, onPrimary = c.onAccent,
            secondary = c.accent, tertiary = c.accent,
            background = c.background, onBackground = c.textPrimary,
            surface = c.surface, onSurface = c.textPrimary,
            surfaceVariant = c.inputField, onSurfaceVariant = c.textSecondary,
            surfaceContainer = c.surface, surfaceContainerHigh = c.surface,
            surfaceContainerHighest = c.inputField,
            outline = c.textSecondary, outlineVariant = c.divider
        )
    } else {
        lightColorScheme(
            primary = c.accent, onPrimary = c.onAccent,
            background = c.background, onBackground = c.textPrimary,
            surface = c.surface, onSurface = c.textPrimary,
            surfaceContainer = c.surface, surfaceContainerHigh = c.surface
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = !c.isDark
        }
    }

    CompositionLocalProvider(LocalAppColors provides c) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}