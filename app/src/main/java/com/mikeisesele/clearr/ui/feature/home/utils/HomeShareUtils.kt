package com.mikeisesele.clearr.ui.feature.home.utils

import android.app.Activity
import android.content.Context
import android.view.View
import com.mikeisesele.clearr.ui.commons.util.captureViewWithPixelCopy
import com.mikeisesele.clearr.ui.commons.util.redactSensitiveZones
import com.mikeisesele.clearr.ui.commons.util.saveBitmapToCache
import com.mikeisesele.clearr.ui.commons.util.shareImageUri

internal fun shareHomeScreenshot(
    context: Context,
    view: View,
    redactSensitive: Boolean
) {
    val window = (context as? Activity)?.window ?: return
    captureViewWithPixelCopy(view, window) { bitmap ->
        if (bitmap != null) {
            try {
                val processed = if (redactSensitive) redactSensitiveZones(bitmap) else bitmap
                val uri = saveBitmapToCache(context, processed, "dues_tracker_${System.currentTimeMillis()}.png")
                shareImageUri(context, uri, "Share Dues Summary")
            } catch (_: Exception) {
            }
        }
    }
}
