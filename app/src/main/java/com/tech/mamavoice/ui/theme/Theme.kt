package com.tech.mamavoice.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = MamaPrimary,
    onPrimary = MamaOnPrimary,
    primaryContainer = MamaPrimaryLight,
    secondary = MamaSecondary,
    onSecondary = MamaOnSecondary,
    secondaryContainer = MamaSecondaryLight,
    tertiary = MamaTertiary,
    onTertiary = MamaOnTertiary,
    tertiaryContainer = MamaTertiaryLight,
    error = MamaError,
    onError = MamaOnError,
    errorContainer = MamaErrorContainer,
    onErrorContainer = MamaOnErrorContainer,
    background = MamaBackground,
    onBackground = MamaOnBackground,
    surface = MamaSurface,
    onSurface = MamaOnSurface,
    surfaceVariant = MamaSurfaceVariant,
    onSurfaceVariant = MamaOnSurfaceVariant,
    outline = MamaOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = MamaPrimaryDarkTheme,
    onPrimary = MamaPrimaryDark,
    primaryContainer = MamaPrimaryDark,
    secondary = MamaSecondaryDarkTheme,
    onSecondary = MamaSecondaryDark,
    secondaryContainer = MamaSecondaryDark,
    tertiary = MamaTertiaryDarkTheme,
    onTertiary = MamaTertiaryDark,
    tertiaryContainer = MamaTertiaryDark,
    error = MamaError,
    onError = MamaOnError,
    errorContainer = MamaErrorContainer,
    onErrorContainer = MamaOnErrorContainer,
    background = MamaBackgroundDark,
    onBackground = MamaOnBackgroundDark,
    surface = MamaSurfaceDark,
    onSurface = MamaOnSurfaceDark,
    surfaceVariant = MamaSurfaceVariantDark,
    onSurfaceVariant = MamaOnSurfaceVariantDark,
    outline = MamaOutline
)

/**
 * MamaVoice app theme using Material3.
 *
 * Note: MaterialExpressiveTheme requires material3 1.5.0-alpha+ which needs
 * compileSdk 37 and AGP 9.1.0+. Once the project upgrades to those,
 * swap MaterialTheme -> MaterialExpressiveTheme for spring-based motion.
 */
@Composable
fun MamaVoiceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to use our brand colors
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