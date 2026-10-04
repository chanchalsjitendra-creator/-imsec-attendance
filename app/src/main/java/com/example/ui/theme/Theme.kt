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

private val DarkColorScheme = darkColorScheme(
    primary = ImsecBlue,
    onPrimary = SlateCanvas,
    primaryContainer = ImsecNavy,
    onPrimaryContainer = ImsecLightBlue,
    secondary = WarmAmber,
    onSecondary = DarkCanvas,
    secondaryContainer = DarkBorder,
    onSecondaryContainer = SoftAmber,
    tertiary = EmeraldSuccess,
    background = DarkCanvas,
    surface = DarkCard,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = ImsecNavy,
    onPrimary = SlateCard,
    primaryContainer = ImsecLightBlue,
    onPrimaryContainer = ImsecDarkNavy,
    secondary = WarmAmber,
    onSecondary = SlateCard,
    secondaryContainer = SoftAmber,
    onSecondaryContainer = WarmAmber,
    tertiary = EmeraldSuccess,
    background = SlateCanvas,
    surface = SlateCard,
    onBackground = SlateTextPrimary,
    onSurface = SlateTextPrimary,
    outline = SlateBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our collegiate palette by default for strong identity
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
