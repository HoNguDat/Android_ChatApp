package com.example.chatapp.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(
    val isDark: Boolean,
    val topBar: Color,
    val onTopBar: Color,
    val accent: Color,
    val onAccent: Color,
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val divider: Color,
    val unreadBg: Color,
    val navIndicator: Color,
    val chatBackground: Color,
    val bubbleMe: Color,
    val bubbleOther: Color,
    val bubbleDeleted: Color,
    val inputField: Color,
    val errorBg: Color
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

// SÁNG: giống hệt giao diện cũ
val LightAppColors = AppColors(
    isDark = false,
    topBar = Color(0xFF0068FF), onTopBar = Color.White,
    accent = Color(0xFF0068FF), onAccent = Color.White,
    background = Color(0xFFF4F5F7), surface = Color.White,
    textPrimary = Color.Black, textSecondary = Color.Gray, textTertiary = Color.DarkGray,
    divider = Color(0xFFE5E5E5), unreadBg = Color(0xFFEDF5FF), navIndicator = Color(0xFFE8F1FF),
    chatBackground = Color(0xFFE2E7ED),
    bubbleMe = Color(0xFFE3F2FD), bubbleOther = Color.White, bubbleDeleted = Color(0xFFF0F0F0),
    inputField = Color(0xFFF1F2F4), errorBg = Color(0xFFFFF0F0)
)

val DarkAppColors = AppColors(
    isDark = true,
    topBar = Color(0xFF1F1F1F), onTopBar = Color.White,
    accent = Color(0xFF4D9BFF), onAccent = Color(0xFF0A0A0A),
    background = Color(0xFF121212), surface = Color(0xFF1E1E1E),
    textPrimary = Color(0xFFF2F2F2), textSecondary = Color(0xFFA0A0A0), textTertiary = Color(0xFFBDBDBD),
    divider = Color(0xFF2C2C2C), unreadBg = Color(0xFF1B2A3F), navIndicator = Color(0xFF2A3A52),
    chatBackground = Color(0xFF0E0E0E),
    bubbleMe = Color(0xFF2B4C7E), bubbleOther = Color(0xFF2A2A2A), bubbleDeleted = Color(0xFF242424),
    inputField = Color(0xFF2A2A2A), errorBg = Color(0xFF3A1F1F)
)

val NavyAppColors = AppColors(
    isDark = true,
    topBar = Color(0xFF14243D), onTopBar = Color.White,
    accent = Color(0xFF4FC3F7), onAccent = Color(0xFF06121F),
    background = Color(0xFF0B1727), surface = Color(0xFF12233A),
    textPrimary = Color(0xFFEAF2FF), textSecondary = Color(0xFF90A4BE), textTertiary = Color(0xFFB5C4D8),
    divider = Color(0xFF1F3350), unreadBg = Color(0xFF17304F), navIndicator = Color(0xFF1F3F66),
    chatBackground = Color(0xFF08121F),
    bubbleMe = Color(0xFF1F4E79), bubbleOther = Color(0xFF1A2F4D), bubbleDeleted = Color(0xFF15263D),
    inputField = Color(0xFF1A2F4D), errorBg = Color(0xFF3A2024)
)

val PurpleAppColors = AppColors(
    isDark = true,
    topBar = Color(0xFF2A1B4A), onTopBar = Color.White,
    accent = Color(0xFFC4A1FF), onAccent = Color(0xFF150E26),
    background = Color(0xFF150E26), surface = Color(0xFF21163A),
    textPrimary = Color(0xFFF1EBFF), textSecondary = Color(0xFFA99BC7), textTertiary = Color(0xFFC9BEE0),
    divider = Color(0xFF33264F), unreadBg = Color(0xFF2C1F4D), navIndicator = Color(0xFF3D2C66),
    chatBackground = Color(0xFF100A1F),
    bubbleMe = Color(0xFF4A3480), bubbleOther = Color(0xFF2B1F47), bubbleDeleted = Color(0xFF221838),
    inputField = Color(0xFF2B1F47), errorBg = Color(0xFF3A1F2A)
)

val ForestAppColors = AppColors(
    isDark = true,
    topBar = Color(0xFF1B3226), onTopBar = Color.White,
    accent = Color(0xFF6FCF97), onAccent = Color(0xFF0A140E),
    background = Color(0xFF0D1A13), surface = Color(0xFF14271D),
    textPrimary = Color(0xFFE8F5EC), textSecondary = Color(0xFF8FB09C), textTertiary = Color(0xFFB4CDBD),
    divider = Color(0xFF213B2C), unreadBg = Color(0xFF1A3527), navIndicator = Color(0xFF25503A),
    chatBackground = Color(0xFF0A140E),
    bubbleMe = Color(0xFF245A3C), bubbleOther = Color(0xFF193226), bubbleDeleted = Color(0xFF142419),
    inputField = Color(0xFF193226), errorBg = Color(0xFF3A2020)
)

enum class AppThemeOption(val label: String, val colors: AppColors) {
    LIGHT("Sáng (mặc định)", LightAppColors),
    DARK("Tối", DarkAppColors),
    NAVY("Xanh navy", NavyAppColors),
    PURPLE("Tím đêm", PurpleAppColors),
    FOREST("Xanh rêu", ForestAppColors);

    companion object {
        fun fromName(name: String?): AppThemeOption =
            entries.firstOrNull { it.name == name } ?: LIGHT
    }
}