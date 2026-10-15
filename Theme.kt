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
    primary = IndigoLight,
    onPrimary = Color.White,
    primaryContainer = IndigoDark,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = AmberBulb,
    onSecondary = Color.Black,
    secondaryContainer = AmberBulbDark,
    onSecondaryContainer = Color.White,
    tertiary = CyanAccent,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextLight,
    surface = DarkCard,
    onSurface = TextLight,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextLightSecondary,
    error = CoralWrong,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = IndigoDark,
    secondary = AmberBulb,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = AmberBulbLight,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = CyanAccentDark,
    onTertiary = Color.White,
    background = PaperBackgroundLight,
    onBackground = TextDark,
    surface = PaperCardLight,
    onSurface = TextDark,
    surfaceVariant = PaperSurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    error = CoralWrong,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded puzzle vibe
    content: @Composable () -> Unit
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
