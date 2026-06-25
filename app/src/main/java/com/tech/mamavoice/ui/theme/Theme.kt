package com.tech.mamavoice.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val LightColorScheme = lightColorScheme(
    primary = MamaPrimary,
    onPrimary = MamaOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = MamaPrimaryDeep,
    onSecondary = MamaOnPrimary,
    secondaryContainer = LightPrimaryContainer,
    onSecondaryContainer = LightOnPrimaryContainer,
    tertiary = MamaCoral,
    onTertiary = MamaOnPrimary,
    error = MamaCoralDeep,
    onError = MamaOnPrimary,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = MamaPrimaryMint,
    onPrimary = MamaOnPrimaryDark,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = MamaPrimaryMint,
    onSecondary = MamaOnPrimaryDark,
    secondaryContainer = DarkPrimaryContainer,
    onSecondaryContainer = DarkOnPrimaryContainer,
    tertiary = MamaCoral,
    onTertiary = MamaOnPrimaryDark,
    error = MamaCoral,
    onError = MamaOnPrimaryDark,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

/**
 * Accessor for MamaVoice's extended (non-Material) brand colors.
 * Usage: `MamaTheme.colors.success`
 */
object MamaTheme {
    val colors: MamaExtraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMamaExtraColors.current
}

/**
 * MamaVoice app theme using Material3.
 *
 * Note: dynamic color is intentionally disabled — the brand palette is fixed so the
 * warm-teal identity is consistent across devices.
 */
@Composable
fun MamaVoiceTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extraColors = if (darkTheme) DarkExtraColors else LightExtraColors

    CompositionLocalProvider(LocalMamaExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
