package com.mikeisesele.clearr.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.mikeisesele.clearr.ui.state.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary          = ClearrColors.Violet,
    onPrimary        = Color.White,
    primaryContainer = Indigo600,
    onPrimaryContainer = Color.White,
    secondary        = ClearrColors.Emerald,
    onSecondary      = Color.White,
    background       = ClearrColors.DarkBackground,
    onBackground     = ClearrColors.DarkTextPrimary,
    surface          = ClearrColors.DarkSurface,
    onSurface        = ClearrColors.DarkTextPrimary,
    surfaceVariant   = ClearrColors.DarkCard,
    onSurfaceVariant = ClearrColors.DarkTextMuted,
    outline          = ClearrColors.DarkBorder,
    error            = ClearrColors.Coral,
    onError          = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary          = ClearrColors.Violet,
    onPrimary        = Color.White,
    primaryContainer = ClearrColors.VioletBg,
    onPrimaryContainer = ClearrColors.Violet,
    secondary        = ClearrColors.Emerald,
    onSecondary      = Color.White,
    background       = ClearrColors.Background,
    onBackground     = ClearrColors.TextPrimary,
    surface          = ClearrColors.Surface,
    onSurface        = ClearrColors.TextPrimary,
    surfaceVariant   = LightCard,
    onSurfaceVariant = ClearrColors.TextSecondary,
    outline          = ClearrColors.Border,
    error            = ClearrColors.Coral,
    onError          = Color.White
)

/**
 * Theme-aware color bag used throughout the app via LocalDuesColors.current.
 * Maps Clearr brand tokens to semantic slots used by existing composables.
 */
data class DuesColors(
    val bg: Color,
    val surface: Color,
    val card: Color,
    val border: Color,
    /** Primary interactive accent — Clearr Violet */
    val accent: Color,
    /** Positive / cleared — Clearr Emerald */
    val green: Color,
    /** Caution / pending — Clearr Amber */
    val amber: Color,
    /** Danger / unpaid — Clearr Coral */
    val red: Color,
    val text: Color,
    val muted: Color,
    val dim: Color,
    val isDark: Boolean
)

val LocalDuesColors = staticCompositionLocalOf {
    DuesColors(
        bg      = ClearrColors.DarkBackground,
        surface = ClearrColors.DarkSurface,
        card    = ClearrColors.DarkCard,
        border  = ClearrColors.DarkBorder,
        accent  = ClearrColors.Violet,
        green   = ClearrColors.Emerald,
        amber   = ClearrColors.Amber,
        red     = ClearrColors.Coral,
        text    = ClearrColors.DarkTextPrimary,
        muted   = ClearrColors.DarkTextMuted,
        dim     = ClearrColors.DarkInactive,
        isDark  = true
    )
}

/** Light-mode DuesColors instance using Clearr tokens */
private fun lightDuesColors() = DuesColors(
    bg      = ClearrColors.Background,
    surface = ClearrColors.Surface,
    card    = LightCard,
    border  = ClearrColors.Border,
    accent  = ClearrColors.Violet,
    green   = ClearrColors.Emerald,
    amber   = ClearrColors.Amber,
    red     = ClearrColors.Coral,
    text    = ClearrColors.TextPrimary,
    muted   = ClearrColors.TextSecondary,
    dim     = ClearrColors.Inactive,
    isDark  = false
)

/** Dark-mode DuesColors instance using Clearr tokens */
private fun darkDuesColors() = DuesColors(
    bg      = ClearrColors.DarkBackground,
    surface = ClearrColors.DarkSurface,
    card    = ClearrColors.DarkCard,
    border  = ClearrColors.DarkBorder,
    accent  = ClearrColors.Violet,
    green   = ClearrColors.Emerald,
    amber   = ClearrColors.Amber,
    red     = ClearrColors.Coral,
    text    = ClearrColors.DarkTextPrimary,
    muted   = ClearrColors.DarkTextMuted,
    dim     = ClearrColors.DarkInactive,
    isDark  = true
)

/** Root theme composable for the app. */
@Composable
fun ClearrTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Dynamic color on API 31+ (Material You), falls back to Clearr palette
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val duesColors = if (darkTheme) darkDuesColors() else lightDuesColors()

    CompositionLocalProvider(LocalDuesColors provides duesColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography,
            content     = content
        )
    }
}
