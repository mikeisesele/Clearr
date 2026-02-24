package com.mikeisesele.clearr

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import com.mikeisesele.clearr.ui.navigation.DuesNavHost
import com.mikeisesele.clearr.ui.commons.state.ThemeMode
import com.mikeisesele.clearr.ui.theme.ClearrTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        // Call with transparent bars so Compose owns the full window.
        // We re-apply with proper light/dark style inside setContent once
        // we know each screen's visual mode.
        enableEdgeToEdge()
        setContent {
            var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }

            ClearrTheme(themeMode = themeMode) {
                DuesNavHost(onThemeChange = { themeMode = it })
            }
        }
    }
}
