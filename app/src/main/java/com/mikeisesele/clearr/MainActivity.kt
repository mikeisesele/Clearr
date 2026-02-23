package com.mikeisesele.clearr

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import dagger.hilt.android.AndroidEntryPoint
import com.mikeisesele.clearr.ui.navigation.DuesNavHost
import com.mikeisesele.clearr.ui.state.ThemeMode
import com.mikeisesele.clearr.ui.theme.ClearrTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Call with transparent bars so Compose owns the full window.
        // We re-apply with proper light/dark style inside setContent once
        // we know the theme mode.
        enableEdgeToEdge()
        setContent {
            var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }

            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> systemDark
            }

            // Re-apply edge-to-edge whenever the effective dark/light mode changes
            // so status bar icons are light on dark bg and dark on light bg.
            LaunchedEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
            }

            ClearrTheme(themeMode = themeMode) {
                DuesNavHost(onThemeChange = { themeMode = it })
            }
        }
    }
}
