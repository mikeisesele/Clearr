package com.mikeisesele.clearr

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import dagger.hilt.android.AndroidEntryPoint
import com.mikeisesele.clearr.ui.navigation.DuesNavHost
import com.mikeisesele.clearr.ui.commons.state.ThemeMode
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        val transparent = android.graphics.Color.TRANSPARENT
        val lightNav = ClearrColors.BrandBackground.toArgb()
        val darkNav = ClearrColors.DarkBackground.toArgb()

        // Bootstrap style before Compose mounts; screen-level updates are applied in ApplySystemBars.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(transparent, transparent),
            navigationBarStyle = SystemBarStyle.auto(lightNav, darkNav)
        )
        setContent {
            var themeMode by remember { mutableStateOf(ThemeMode.LIGHT) }
            ClearrTheme(themeMode = themeMode) {
                DuesNavHost(onThemeChange = { themeMode = ThemeMode.LIGHT })
            }
        }
    }
}
