package com.mikeisesele.clearr.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

@Composable
expect fun platformTypography(): Typography

@Composable
fun ClearrSharedTheme(
    darkTheme: Boolean = false,
    colorSchemeOverride: ColorScheme? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = colorSchemeOverride ?: if (darkTheme) ClearrDarkColorScheme else ClearrLightColorScheme
    val uiColors = if (darkTheme) darkClearrUiColors() else lightClearrUiColors()
    ClearrThemeCore(
        colorScheme = colorScheme,
        uiColors = uiColors,
        typography = platformTypography(),
        content = content
    )
}
