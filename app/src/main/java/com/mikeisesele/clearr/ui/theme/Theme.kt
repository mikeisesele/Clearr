package com.mikeisesele.clearr.ui.theme

import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.mikeisesele.clearr.ui.commons.state.ThemeMode

/** Root theme composable for the app. */
@Composable
fun ClearrTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Light mode is temporarily forced globally. Keep ThemeMode in the API so
    // dynamic behavior can be restored later without changing call sites.
    val darkTheme = false

    // Dynamic color on API 31+ (Material You), falls back to Clearr palette
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ClearrDarkColorScheme
        else -> ClearrLightColorScheme
    }

    val uiColors = if (darkTheme) darkClearrUiColors() else lightClearrUiColors()

    ClearrThemeCore(
        colorScheme = colorScheme,
        uiColors = uiColors,
        typography = Typography,
        content = content
    )
}
