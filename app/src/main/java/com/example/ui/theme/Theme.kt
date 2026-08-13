package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AlUfuqDarkColorScheme = darkColorScheme(
    primary = SacredGold,
    onPrimary = ObsidianNavy,
    primaryContainer = DeepNavySurface,
    onPrimaryContainer = WarmIvory,
    secondary = EmeraldGreen,
    onSecondary = PureWhite,
    tertiary = TerracottaSunset,
    onTertiary = PureWhite,
    background = ObsidianNavy,
    onBackground = WarmIvory,
    surface = MidnightNavy,
    onSurface = WarmIvory,
    surfaceVariant = CardNavySurface,
    onSurfaceVariant = MutedText,
    outline = MutedBorder
)

private val AlUfuqLightColorScheme = lightColorScheme(
    primary = SacredGold,
    onPrimary = ObsidianNavy,
    primaryContainer = WarmIvory,
    onPrimaryContainer = ObsidianNavy,
    secondary = EmeraldGreen,
    onSecondary = PureWhite,
    tertiary = TerracottaSunset,
    onTertiary = PureWhite,
    background = Color(0xFFF6F4EF),
    onBackground = ObsidianNavy,
    surface = PureWhite,
    onSurface = ObsidianNavy,
    surfaceVariant = Color(0xFFEBE6DC),
    onSurfaceVariant = Color(0xFF6B6459),
    outline = Color(0xFFD4CEC0)
)

@Composable
fun AlUfuqTheme(
    darkTheme: Boolean = true, // Default to obsidian luxury theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AlUfuqDarkColorScheme else AlUfuqLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
