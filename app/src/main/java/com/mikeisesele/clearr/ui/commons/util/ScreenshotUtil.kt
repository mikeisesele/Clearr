package com.mikeisesele.clearr.ui.commons.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Captures the visible pixels of a [View] using [PixelCopy] (API 26+).
 * PixelCopy reads from the hardware compositor, so it correctly captures
 * Compose's hardware-accelerated rendering — unlike view.draw() which
 * only works for software-rendered views.
 *
 * [onResult] is called on the main thread with the bitmap (or null on failure).
 */
fun captureViewWithPixelCopy(
    view: View,
    window: Window,
    onResult: (Bitmap?) -> Unit
) {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

    // Compute view bounds relative to the window
    val locationInWindow = IntArray(2)
    view.getLocationInWindow(locationInWindow)
    val rect = Rect(
        locationInWindow[0],
        locationInWindow[1],
        locationInWindow[0] + view.width,
        locationInWindow[1] + view.height
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        PixelCopy.request(
            window,
            rect,
            bitmap,
            { copyResult ->
                onResult(if (copyResult == PixelCopy.SUCCESS) bitmap else null)
            },
            Handler(Looper.getMainLooper())
        )
    } else {
        // Fallback for < API 26 (shouldn't happen since minSdk=26, but keeps compiler happy)
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)
        onResult(bitmap)
    }
}

/**
 * Saves a [Bitmap] to the app's cache directory and returns a content:// URI via FileProvider.
 */
fun saveBitmapToCache(
    context: Context,
    bitmap: Bitmap,
    filename: String = "dues_share.png"
): android.net.Uri {
    val file = File(context.cacheDir, filename)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}

/**
 * Applies a lightweight privacy mask over areas that typically contain names/details
 * on tracker screens before sharing screenshots.
 */
fun redactSensitiveZones(source: Bitmap): Bitmap {
    val mutable = source.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutable)
    val paint = Paint().apply {
        color = 0xAAFFFFFF.toInt()
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    val left = (mutable.width * 0.06f).toInt()
    val right = (mutable.width * 0.78f).toInt()
    val top = (mutable.height * 0.24f).toInt()
    val bottom = (mutable.height * 0.84f).toInt()
    canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
    return mutable
}

/**
 * Shares an image URI via the system share sheet.
 */
fun shareImageUri(
    context: Context,
    uri: android.net.Uri,
    title: String = "Share Dues Summary"
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, title))
}
