package com.mikeisesele.clearr.ui.feature.dashboard.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.mikeisesele.clearr.ui.feature.dashboard.utils.BlobGradientStops
import com.mikeisesele.clearr.ui.feature.dashboard.utils.BlobSpeed
import com.mikeisesele.clearr.ui.feature.dashboard.utils.drawBlobPath
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

private val ambientStops = listOf(
    BlobGradientStops(ClearrColors.CoralSurface, ClearrColors.Coral),
    BlobGradientStops(ClearrColors.AmberSurface, ClearrColors.Amber),
    BlobGradientStops(ClearrColors.BlueSurface, ClearrColors.Blue),
    BlobGradientStops(ClearrColors.VioletSurface, ClearrColors.Violet),
    BlobGradientStops(ClearrColors.EmeraldSurface, ClearrColors.Emerald),
)

@Composable
internal fun AmbientMorphBlob(
    modifier: Modifier = Modifier,
    blobSize: Dp = ClearrDimens.dp120,
) {
    val colors = LocalDuesColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "ambient-blob")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1_000_000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient-time"
    )
    val paletteProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = ambientStops.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient-palette"
    )

    val (lightColor, deepColor) = remember(paletteProgress) {
        interpolateAmbientStops(paletteProgress)
    }
    val glowColor = remember(deepColor, colors.bg) {
        lerp(deepColor, colors.bg, 0.48f)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(blobSize)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gradient = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to lightColor,
                    1f to deepColor
                ),
                center = Offset(size.width * 0.38f, size.height * 0.35f),
                radius = size.minDimension * 0.65f
            )
            scale(1.08f) {
                drawBlobPath(
                    t = time * BlobSpeed,
                    gradient = null,
                    solidColor = glowColor.copy(alpha = 0.34f)
                )
            }
            drawBlobPath(
                t = time * BlobSpeed,
                gradient = gradient,
                solidColor = null
            )
        }
    }
}

private fun interpolateAmbientStops(progress: Float): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    val wrapped = progress % ambientStops.size
    val startIndex = wrapped.toInt()
    val endIndex = (startIndex + 1) % ambientStops.size
    val fraction = wrapped - startIndex
    val start = ambientStops[startIndex]
    val end = ambientStops[endIndex]
    return lerp(start.light, end.light, fraction) to lerp(start.deep, end.deep, fraction)
}

@Preview(showBackground = true)
@Composable
private fun AmbientMorphBlobPreview() {
    ClearrTheme {
        AmbientMorphBlob()
    }
}
