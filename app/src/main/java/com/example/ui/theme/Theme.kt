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
    primary = UbuntuTerracottaLight,
    onPrimary = Color.White,
    primaryContainer = UbuntuTerracottaDark,
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = UbuntuTealLight,
    onSecondary = Color(0xFF003831),
    secondaryContainer = UbuntuTealDark,
    onSecondaryContainer = Color(0xFFBCEEE5),
    tertiary = UbuntuGold,
    background = UbuntuDarkBg,
    onBackground = UbuntuDarkText,
    surface = UbuntuDarkSurface,
    onSurface = UbuntuDarkText,
    surfaceVariant = UbuntuDarkSurfaceVariant,
    onSurfaceVariant = UbuntuDarkTextMuted,
    outline = Color(0xFF4C4742)
)

private val LightColorScheme = lightColorScheme(
    primary = UbuntuTerracotta,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCF),
    onPrimaryContainer = UbuntuTerracottaDark,
    secondary = UbuntuTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0F4EE),
    onSecondaryContainer = UbuntuTealDark,
    tertiary = UbuntuGoldDark,
    background = UbuntuLinen,
    onBackground = UbuntuTextPrimary,
    surface = UbuntuSurface,
    onSurface = UbuntuTextPrimary,
    surfaceVariant = UbuntuSurfaceVariant,
    onSurfaceVariant = UbuntuTextMuted,
    outline = UbuntuBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve Ubuntu custom branding by default
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
