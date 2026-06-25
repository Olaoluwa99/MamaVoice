package com.tech.mamavoice.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended brand colors that don't map cleanly onto Material's [androidx.compose.material3.ColorScheme].
 *
 * Access via [com.tech.mamavoice.ui.theme.MamaTheme] inside any composable wrapped by
 * [MamaVoiceTheme], e.g. `MamaTheme.colors.success`.
 */
@Immutable
data class MamaExtraColors(
    val success: Color,            // "On track" / "Stable" indicators
    val onSuccess: Color,
    val successContainer: Color,
    val accent: Color,             // coral accent (warnings, log out)
    val accentContainer: Color,
    val onAccentContainer: Color,
    val voiceGradientStart: Color, // greeting / hero / tip card gradient
    val voiceGradientEnd: Color,
    val cardVeg: Color,            // food category card fills
    val onCardVeg: Color,
    val cardProtein: Color,
    val onCardProtein: Color,
    val cardFruit: Color,
    val onCardFruit: Color,
    val cardDairy: Color,
    val onCardDairy: Color,
    val chip: Color,               // unselected filter chip / pill background
    val onChip: Color,
    val logoMark: Color            // brand mark tint
)

val LightExtraColors = MamaExtraColors(
    success = Color(0xFF1E8E63),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFD6EFE0),
    accent = MamaCoral,
    accentContainer = Color(0xFFF8DCCE),
    onAccentContainer = Color(0xFF7A3A22),
    voiceGradientStart = Color(0xFF0F7E6F),
    voiceGradientEnd = Color(0xFF0B5C50),
    cardVeg = Color(0xFFBFE3CF),
    onCardVeg = Color(0xFF0B4338),
    cardProtein = Color(0xFFF0D7A6),
    onCardProtein = Color(0xFF5A4326),
    cardFruit = Color(0xFFF4C9A8),
    onCardFruit = Color(0xFF6B3B22),
    cardDairy = Color(0xFFCFE3E0),
    onCardDairy = Color(0xFF234A45),
    chip = Color(0xFFEFE7DF),
    onChip = Color(0xFF5E6E68),
    logoMark = MamaPrimary
)

val DarkExtraColors = MamaExtraColors(
    success = Color(0xFF3FCB95),
    onSuccess = Color(0xFF06231D),
    successContainer = Color(0xFF12463A),
    accent = MamaCoral,
    accentContainer = Color(0xFF4A2A1E),
    onAccentContainer = Color(0xFFF6C9B3),
    voiceGradientStart = Color(0xFF16574A),
    voiceGradientEnd = Color(0xFF0E2A24),
    cardVeg = Color(0xFF2E6B56),
    onCardVeg = Color(0xFFE6F4EC),
    cardProtein = Color(0xFF6E5320),
    onCardProtein = Color(0xFFF6E6C4),
    cardFruit = Color(0xFF8A4A2C),
    onCardFruit = Color(0xFFF8DCCB),
    cardDairy = Color(0xFF274F49),
    onCardDairy = Color(0xFFD9EDEA),
    chip = Color(0xFF1E3A33),
    onChip = Color(0xFF9FB4AD),
    logoMark = MamaPrimaryMint
)

val LocalMamaExtraColors = staticCompositionLocalOf { LightExtraColors }
