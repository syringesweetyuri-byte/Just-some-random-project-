package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = WaGreen,
    onPrimary = Color.White,
    primaryContainer = WaTealDark,
    onPrimaryContainer = Color.White,
    secondary = WaTeal,
    onSecondary = Color.White,
    tertiary = WaLightGreen,
    background = WaDarkBackground,
    onBackground = WaDarkTextPrimary,
    surface = WaDarkSurface,
    onSurface = WaDarkTextPrimary,
    surfaceVariant = WaDarkSurfaceVariant,
    onSurfaceVariant = WaDarkTextSecondary,
    outline = Color(0xFF374248)
)

private val LightColorScheme = lightColorScheme(
    primary = WaTealDark,
    onPrimary = Color.White,
    primaryContainer = WaTeal,
    onPrimaryContainer = Color.White,
    secondary = WaGreen,
    onSecondary = Color.White,
    tertiary = WaLightGreen,
    background = WaLightBackground,
    onBackground = WaLightTextPrimary,
    surface = WaLightSurface,
    onSurface = WaLightTextPrimary,
    surfaceVariant = WaLightSurfaceVariant,
    onSurfaceVariant = WaLightTextSecondary,
    outline = Color(0xFFD1D7DB)
)

@Composable
fun WAClassicTheme(
    darkTheme: Boolean = true, // Default to dark for AMOLED battery conservation on Galaxy Note 3
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
