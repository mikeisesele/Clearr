package com.mikeisesele.clearr.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ClearrUiColors(
    val bg: Color,
    val surface: Color,
    val card: Color,
    val border: Color,
    val accent: Color,
    val green: Color,
    val amber: Color,
    val red: Color,
    val text: Color,
    val muted: Color,
    val dim: Color,
    val isDark: Boolean
)

val LocalClearrUiColors = staticCompositionLocalOf {
    ClearrUiColors(
        bg = ClearrColors.DarkBackground,
        surface = ClearrColors.DarkSurface,
        card = ClearrColors.DarkCard,
        border = ClearrColors.DarkBorder,
        accent = ClearrColors.BrandPrimary,
        green = ClearrColors.BrandSecondary,
        amber = ClearrColors.BrandAccent,
        red = ClearrColors.BrandDanger,
        text = ClearrColors.DarkTextPrimary,
        muted = ClearrColors.DarkTextMuted,
        dim = ClearrColors.DarkInactive,
        isDark = true
    )
}
