package com.mikeisesele.clearr.ui.feature.dashboard.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardClearanceScore
import com.mikeisesele.clearr.ui.feature.dashboard.utils.previewDashboardUi
import com.mikeisesele.clearr.ui.feature.dashboard.utils.BlobGradientStops
import com.mikeisesele.clearr.ui.feature.dashboard.utils.BlobSpeed
import com.mikeisesele.clearr.ui.feature.dashboard.utils.drawBlobPath
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import kotlinx.coroutines.delay

@Composable
internal fun ClearanceScoreSection(
    score: DashboardClearanceScore,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ClearanceScoreBlob(score = score.overall)
        Spacer(Modifier.height(ClearrDimens.dp16))
        SubMetricBubbles(score = score)
    }
}

@Composable
private fun ClearanceScoreBlob(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = ClearrDimens.dp200,
) {
    val colors = LocalDuesColors.current
    val tier = remember(score) { score.toScoreTier() }
    val lightColor by animateColorAsState(
        targetValue = tier.stops.light,
        animationSpec = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
        label = "blob_light"
    )
    val deepColor by animateColorAsState(
        targetValue = tier.stops.deep,
        animationSpec = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
        label = "blob_deep"
    )
    val glowColor by animateColorAsState(
        targetValue = lerp(tier.stops.deep, colors.bg, 0.42f),
        animationSpec = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
        label = "blob_glow"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "blob-time")
    val t by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1_000_000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_000_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob-t"
    )

    val gradient = remember(lightColor, deepColor) {
        Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to lightColor,
                1.0f to deepColor,
            ),
            center = Offset(220f * 0.38f, 220f * 0.35f),
            radius = 220f * 0.65f,
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Clearance score: $score. Health: ${tier.label}" }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            scale(1.08f) {
                drawBlobPath(
                    t = t * BlobSpeed,
                    gradient = null,
                    solidColor = glowColor.copy(alpha = 0.38f)
                )
            }
            drawBlobPath(
                t = t * BlobSpeed,
                gradient = gradient,
                solidColor = null
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(
                targetState = score,
                transitionSpec = {
                    slideInVertically { it / 2 } + fadeIn() togetherWith
                        slideOutVertically { -it / 2 } + fadeOut()
                },
                label = "score-number"
            ) { value ->
                Text(
                    text = "$value",
                    style = MaterialTheme.typography.displayMedium.copy(letterSpacing = (-2).sp),
                    color = ClearrColors.Surface
                )
            }
            Text(
                text = tier.label,
                color = ClearrColors.Surface.copy(alpha = 0.85f),
                fontSize = 11.sp,
                letterSpacing = 2.5.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            )
        }
    }
}

private data class ScoreTier(
    val stops: BlobGradientStops,
    val label: String,
)

private fun Int.toScoreTier(): ScoreTier = when {
    this <= 30 -> ScoreTier(
        stops = BlobGradientStops(
            light = ClearrColors.CoralSurface,
            deep = ClearrColors.Coral
        ),
        label = "CRITICAL"
    )
    this <= 50 -> ScoreTier(
        stops = BlobGradientStops(
            light = ClearrColors.AmberSurface,
            deep = ClearrColors.Amber
        ),
        label = "CAUTION"
    )
    this <= 70 -> ScoreTier(
        stops = BlobGradientStops(
            light = ClearrColors.BlueSurface,
            deep = ClearrColors.Blue
        ),
        label = "PROGRESS"
    )
    this <= 89 -> ScoreTier(
        stops = BlobGradientStops(
            light = ClearrColors.VioletSurface,
            deep = ClearrColors.Violet
        ),
        label = "GOOD"
    )
    else -> ScoreTier(
        stops = BlobGradientStops(
            light = ClearrColors.EmeraldSurface,
            deep = ClearrColors.Emerald
        ),
        label = "CLEARED"
    )
}

@Composable
private fun SubMetricBubbles(score: DashboardClearanceScore) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(
            Triple("💳", "Budget", score.budget),
            Triple("🎯", "Goals", score.goals),
            Triple("₦", "Remit", score.dues),
            Triple("☑", "Todos", score.todos)
        ).forEachIndexed { index, (icon, label, value) ->
            OrganicSubMetricBubble(
                icon = icon,
                label = label,
                value = value,
                delayIndex = index
            )
        }
    }
}

@Composable
private fun OrganicSubMetricBubble(
    icon: String,
    label: String,
    value: Int,
    delayIndex: Int
) {
    val colors = LocalDuesColors.current
    val pulse = rememberInfiniteTransition(label = "bubble_$label")
    val widthFactor by pulse.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400 + (delayIndex * 120), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubble_width_$label"
    )
    val heightFactor by pulse.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600 + (delayIndex * 110), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubble_height_$label"
    )
    val tiltFactor by pulse.animateFloat(
        initialValue = 0.14f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800 + (delayIndex * 90), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubble_tilt_$label"
    )
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayIndex * 60L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.6f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(width = ClearrDimens.dp72, height = ClearrDimens.dp80)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val bubblePath = createBubblePath(size, widthFactor, heightFactor, tiltFactor)
                drawPath(
                    path = bubblePath,
                    color = colors.card
                )
                drawPath(
                    path = bubblePath,
                    color = ClearrColors.Surface.copy(alpha = 0.08f),
                    style = Stroke(width = size.minDimension * 0.018f)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = icon, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "$value%",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.accent
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.muted
                )
            }
        }
    }
}

private fun createBubblePath(
    size: Size,
    widthFactor: Float,
    heightFactor: Float,
    tiltFactor: Float
): Path {
    val width = size.width
    val height = size.height
    val left = width * (0.10f - (widthFactor - 1f) * 0.12f)
    val right = width * (0.90f + (widthFactor - 1f) * 0.12f)
    val top = height * (0.08f - (heightFactor - 1f) * 0.10f)
    val bottom = height * (0.92f + (heightFactor - 1f) * 0.10f)
    val centerX = width * 0.5f

    return Path().apply {
        moveTo(centerX, top)
        cubicTo(
            width * (0.76f + tiltFactor * 0.15f), height * 0.02f,
            width * 0.98f, height * 0.24f,
            right, height * 0.48f
        )
        cubicTo(
            width * 0.94f, height * 0.76f,
            width * 0.72f, height * 1.00f,
            centerX * 1.02f, bottom
        )
        cubicTo(
            width * 0.30f, height * 1.00f,
            width * 0.04f, height * 0.78f,
            left, height * 0.48f
        )
        cubicTo(
            width * 0.04f, height * 0.18f,
            width * (0.24f - tiltFactor * 0.10f), height * 0.02f,
            centerX, top
        )
        close()
    }
}

@Preview(showBackground = true)
@Composable
private fun ClearanceScoreSectionPreview() {
    ClearrTheme {
        ClearanceScoreSection(score = previewDashboardUi.score, modifier = Modifier.padding(ClearrDimens.dp20))
    }
}
