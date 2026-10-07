package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkCrimsonPrimary,
    onPrimary = DarkCrimsonOnPrimary,
    primaryContainer = DarkCrimsonPrimaryContainer,
    onPrimaryContainer = DarkCrimsonOnPrimaryContainer,
    secondary = DarkCoralSecondary,
    onSecondary = DarkCoralOnSecondary,
    secondaryContainer = DarkCoralSecondaryContainer,
    onSecondaryContainer = DarkCoralOnSecondaryContainer,
    tertiary = DarkCyanTertiary,
    onTertiary = DarkCyanOnTertiary,
    tertiaryContainer = DarkCyanTertiaryContainer,
    onTertiaryContainer = DarkCyanOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = LightCrimsonPrimary,
    onPrimary = LightCrimsonOnPrimary,
    primaryContainer = LightCrimsonPrimaryContainer,
    onPrimaryContainer = LightCrimsonOnPrimaryContainer,
    secondary = LightCoralSecondary,
    onSecondary = LightCoralOnSecondary,
    secondaryContainer = LightCoralSecondaryContainer,
    onSecondaryContainer = LightCoralOnSecondaryContainer,
    tertiary = LightCyanTertiary,
    onTertiary = LightCyanOnTertiary,
    tertiaryContainer = LightCyanTertiaryContainer,
    onTertiaryContainer = LightCyanOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
