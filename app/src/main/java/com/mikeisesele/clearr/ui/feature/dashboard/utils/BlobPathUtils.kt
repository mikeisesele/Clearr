package com.mikeisesele.clearr.ui.feature.dashboard.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class WaveMode(val lobes: Int, val amp: Float, val speed: Float, val phase: Float)

private val waveModes = listOf(
    WaveMode(lobes = 3, amp = 5.5f, speed = 1.0f, phase = 0.0f),
    WaveMode(lobes = 5, amp = 3.0f, speed = 1.7f, phase = 1.2f),
    WaveMode(lobes = 7, amp = 1.5f, speed = 2.3f, phase = 2.6f),
)

internal const val BlobNumPoints = 180
internal const val BlobSpeed = 0.0009f
private const val BlobTension = 0.4f

internal data class BlobGradientStops(
    val light: Color,
    val deep: Color,
)

internal fun DrawScope.drawBlobPath(
    t: Float,
    gradient: Brush?,
    solidColor: Color?,
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val baseR = size.minDimension * 0.41f
    val ampScale = size.minDimension / 200f
    val path = buildBlobPath(t, cx, cy, baseR, ampScale)

    when {
        gradient != null -> drawPath(path = path, brush = gradient)
        solidColor != null -> drawPath(path = path, color = solidColor)
    }
}

private fun buildBlobPath(
    t: Float,
    cx: Float,
    cy: Float,
    baseR: Float,
    ampScale: Float,
): Path {
    val pts = Array(BlobNumPoints) { i ->
        val angle = (i.toFloat() / BlobNumPoints) * 2f * PI.toFloat() - (PI / 2f).toFloat()
        val wave = waveModes.fold(0f) { sum, mode ->
            sum + sin(mode.lobes * angle + t * mode.speed + mode.phase) * mode.amp * ampScale
        }
        val r = baseR + wave
        Offset(cx + cos(angle) * r, cy + sin(angle) * r)
    }

    val n = pts.size
    return Path().apply {
        for (i in 0 until n) {
            val p0 = pts[(i - 1 + n) % n]
            val p1 = pts[i]
            val p2 = pts[(i + 1) % n]
            val p3 = pts[(i + 2) % n]

            val cp1 = Offset(
                x = p1.x + (p2.x - p0.x) * BlobTension,
                y = p1.y + (p2.y - p0.y) * BlobTension,
            )
            val cp2 = Offset(
                x = p2.x - (p3.x - p1.x) * BlobTension,
                y = p2.y - (p3.y - p1.y) * BlobTension,
            )

            if (i == 0) moveTo(p1.x, p1.y)
            cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, p2.x, p2.y)
        }
        close()
    }
}
