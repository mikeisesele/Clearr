package com.mikeisesele.clearr.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.mikeisesele.clearr.ui.theme.ClearrColors

@Composable
internal fun ApplySystemBars(darkIcons: Boolean) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() as? ComponentActivity }
    val transparent = android.graphics.Color.TRANSPARENT
    val lightNav = ClearrColors.BrandBackground.toArgb()
    val darkNav = ClearrColors.DarkBackground.toArgb()

    DisposableEffect(activity, darkIcons) {
        activity?.enableEdgeToEdge(
            statusBarStyle = if (darkIcons) {
                SystemBarStyle.light(transparent, transparent)
            } else {
                SystemBarStyle.dark(transparent)
            },
            navigationBarStyle = if (darkIcons) {
                SystemBarStyle.light(lightNav, lightNav)
            } else {
                SystemBarStyle.dark(darkNav)
            }
        )
        onDispose {}
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
