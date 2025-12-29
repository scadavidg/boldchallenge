package com.boldchallenge.ui.theme

import android.app.Activity
import android.os.Build
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

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlueLight,
    onPrimary = DeepNight,
    primaryContainer = SkyBlueDark,
    onPrimaryContainer = CloudWhite,
    
    secondary = SunsetOrange,
    onSecondary = DeepNight,
    secondaryContainer = Color(0xFF4A3728),
    onSecondaryContainer = SunsetGold,
    
    tertiary = SunsetPink,
    onTertiary = DeepNight,
    
    background = DeepNight,
    onBackground = TextPrimaryDark,
    
    surface = NightBlue,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondaryDark,
    
    error = ErrorRed,
    onError = Color.White,
    
    outline = StormGray,
    outlineVariant = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBlue,
    onPrimary = Color.White,
    primaryContainer = SkyBlueLight,
    onPrimaryContainer = SkyBlueDark,
    
    secondary = SunsetOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF3E0),
    onSecondaryContainer = Color(0xFF5D4037),
    
    tertiary = SunsetPink,
    onTertiary = Color.White,
    
    background = CloudWhite,
    onBackground = TextPrimaryLight,
    
    surface = Color.White,
    onSurface = TextPrimaryLight,
    surfaceVariant = MistGray,
    onSurfaceVariant = TextSecondaryLight,
    
    error = ErrorRed,
    onError = Color.White,
    
    outline = StormGray,
    outlineVariant = MistGray
)

@Composable
fun BoldchallengeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to use our custom colors
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
