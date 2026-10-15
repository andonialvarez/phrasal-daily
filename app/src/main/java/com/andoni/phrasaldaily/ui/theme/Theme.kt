package com.andoni.phrasaldaily.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.andoni.phrasaldaily.data.ThemeMode

val Indigo = Color(0xFF4F46E5)
val Orange = Color(0xFFF97316)
val Teal = Color(0xFF0D9488)
val Flame = Color(0xFFFF7A1A)

private val Light = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E0FF),
    onPrimaryContainer = Color(0xFF14105E),
    secondary = Orange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE3D0),
    onSecondaryContainer = Color(0xFF4A1A00),
    tertiary = Teal,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCCF2EC),
    onTertiaryContainer = Color(0xFF00332D),
    background = Color(0xFFF8F7FF),
    onBackground = Color(0xFF1B1B21),
    surface = Color(0xFFF8F7FF),
    onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFE4E1EC),
    onSurfaceVariant = Color(0xFF47464F),
    surfaceContainer = Color(0xFFEFEDF7),
    surfaceContainerHigh = Color(0xFFE9E7F2),
    surfaceContainerLow = Color(0xFFF4F2FC),
    outline = Color(0xFF787680),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFC0C1FF),
    onPrimary = Color(0xFF221C8F),
    primaryContainer = Color(0xFF3A33B8),
    onPrimaryContainer = Color(0xFFE0E0FF),
    secondary = Color(0xFFFFB68C),
    onSecondary = Color(0xFF552100),
    secondaryContainer = Color(0xFF7A3300),
    onSecondaryContainer = Color(0xFFFFDBC8),
    tertiary = Color(0xFF7DD8CB),
    onTertiary = Color(0xFF003731),
    tertiaryContainer = Color(0xFF005048),
    onTertiaryContainer = Color(0xFFA6F4E6),
    background = Color(0xFF131218),
    onBackground = Color(0xFFE5E1EA),
    surface = Color(0xFF131218),
    onSurface = Color(0xFFE5E1EA),
    surfaceVariant = Color(0xFF47464F),
    onSurfaceVariant = Color(0xFFC8C5D0),
    surfaceContainer = Color(0xFF201F26),
    surfaceContainerHigh = Color(0xFF2A2930),
    surfaceContainerLow = Color(0xFF1B1B21),
    outline = Color(0xFF928F9A),
)

private val AppTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Bold),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    )
}

@Composable
fun isDark(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun PhrasalTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isDark(mode)) Dark else Light,
        typography = AppTypography,
        content = content,
    )
}
