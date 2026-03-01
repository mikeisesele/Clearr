package com.mikeisesele.clearr.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

val ClearrDarkColorScheme = darkColorScheme(
    primary = ClearrColors.BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Indigo600,
    onPrimaryContainer = Color.White,
    secondary = ClearrColors.BrandSecondary,
    onSecondary = Color.White,
    background = ClearrColors.DarkBackground,
    onBackground = ClearrColors.DarkTextPrimary,
    surface = ClearrColors.DarkSurface,
    onSurface = ClearrColors.DarkTextPrimary,
    surfaceVariant = ClearrColors.DarkCard,
    onSurfaceVariant = ClearrColors.DarkTextMuted,
    outline = ClearrColors.DarkBorder,
    error = ClearrColors.BrandDanger,
    onError = Color.White
)

val ClearrLightColorScheme = lightColorScheme(
    primary = ClearrColors.BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = ClearrColors.VioletBg,
    onPrimaryContainer = ClearrColors.BrandPrimary,
    secondary = ClearrColors.BrandSecondary,
    onSecondary = Color.White,
    background = ClearrColors.BrandBackground,
    onBackground = ClearrColors.BrandText,
    surface = ClearrColors.Surface,
    onSurface = ClearrColors.BrandText,
    surfaceVariant = LightCard,
    onSurfaceVariant = ClearrColors.TextSecondary,
    outline = ClearrColors.Border,
    error = ClearrColors.BrandDanger,
    onError = Color.White
)

fun lightClearrUiColors() = ClearrUiColors(
    bg = ClearrColors.BrandBackground,
    surface = ClearrColors.Surface,
    card = LightCard,
    border = ClearrColors.Border,
    accent = ClearrColors.BrandPrimary,
    green = ClearrColors.BrandSecondary,
    amber = ClearrColors.BrandAccent,
    red = ClearrColors.BrandDanger,
    text = ClearrColors.BrandText,
    muted = ClearrColors.TextSecondary,
    dim = ClearrColors.Inactive,
    isDark = false
)

fun darkClearrUiColors() = ClearrUiColors(
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

@Composable
fun ClearrThemeCore(
    colorScheme: ColorScheme,
    uiColors: ClearrUiColors,
    typography: Typography,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalClearrUiColors provides uiColors,
        LocalClearrSpacing provides ClearrSpacing(),
        LocalClearrRadii provides ClearrRadii(),
        LocalClearrSizes provides ClearrSizes()
    ) {
        val radii = ClearrRadii()
        val shapes = Shapes(
            small = RoundedCornerShape(radii.sm),
            medium = RoundedCornerShape(radii.md),
            large = RoundedCornerShape(radii.lg)
        )
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}
