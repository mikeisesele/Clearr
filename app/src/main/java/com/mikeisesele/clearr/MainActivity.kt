package com.mikeisesele.clearr

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import dagger.hilt.android.AndroidEntryPoint
import com.mikeisesele.clearr.ui.navigation.ClearrNavHost
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
            ClearrTheme(themeMode = ThemeMode.LIGHT) {
                ClearrNavHost()
            }
        }
    }
}
