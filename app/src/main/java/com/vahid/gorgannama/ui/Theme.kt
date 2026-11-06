package com.vahid.gorgannama.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Hyrcanian forest green, Golestan saffron, Gorgan pomegranate
val Forest = Color(0xFF1E5B4B)
val Saffron = Color(0xFFE2A33B)
val OnSaffron = Color(0xFF3A2600)
val Pomegranate = Color(0xFFB4442E)

private val Light = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDE8DD),
    onPrimaryContainer = Color(0xFF0E3A2F),
    secondary = Saffron,
    onSecondary = OnSaffron,
    secondaryContainer = Color(0xFFFFE4B5),
    onSecondaryContainer = OnSaffron,
    tertiary = Pomegranate,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD2),
    onTertiaryContainer = Color(0xFF3F0D04),
    background = Color(0xFFF2F5F1),
    onBackground = Color(0xFF17201D),
    surface = Color(0xFFFBFCFA),
    onSurface = Color(0xFF17201D),
    surfaceVariant = Color(0xFFDDE5E0),
    onSurfaceVariant = Color(0xFF414945),
    outline = Color(0xFF717975)
)

private val Dark = darkColorScheme(
    primary = Color(0xFF8FD3BA),
    onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF14513F),
    onPrimaryContainer = Color(0xFFCDE8DD),
    secondary = Color(0xFFF3C06A),
    onSecondary = Color(0xFF412C00),
    secondaryContainer = Color(0xFF5C4200),
    onSecondaryContainer = Color(0xFFFFE4B5),
    tertiary = Color(0xFFFFB4A3),
    onTertiary = Color(0xFF5F1606),
    tertiaryContainer = Color(0xFF7F2C1A),
    onTertiaryContainer = Color(0xFFFFDAD2),
    background = Color(0xFF0F1513),
    onBackground = Color(0xFFDEE4E0),
    surface = Color(0xFF151C19),
    onSurface = Color(0xFFDEE4E0),
    surfaceVariant = Color(0xFF3F4945),
    onSurfaceVariant = Color(0xFFBFC9C3)
)

@Composable
fun GorganTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        typography = Typography(),
        content = content
    )
}
