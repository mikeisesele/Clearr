package com.mikeisesele.clearr.ui.feature.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardClearanceScore
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerHealth
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerType
import com.mikeisesele.clearr.ui.feature.dashboard.utils.previewDashboardUi
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import kotlinx.coroutines.delay

@Composable
internal fun ClearanceScoreSection(
    score: DashboardClearanceScore,
    visibleTiles: List<DashboardTrackerType>,
    modifier: Modifier = Modifier
) {
    TrackerHealthTiles(score = score, visibleTiles = visibleTiles, modifier = modifier)
}

@Composable
internal fun TrackerHealthTiles(
    score: DashboardClearanceScore,
    visibleTiles: List<DashboardTrackerType>,
    modifier: Modifier = Modifier,
) {
    val tiles = remember(score) {
        listOf(
            score.budget,
            score.goals,
            score.dues,
            score.todos
        )
    }
    val filteredTiles = remember(tiles, visibleTiles) {
        tiles.filter { it.trackerType in visibleTiles }
    }

    if (filteredTiles.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp10),
    ) {
        filteredTiles.chunked(2).forEachIndexed { rowIndex, rowTiles ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp10),
            ) {
                rowTiles.forEachIndexed { columnIndex, tile ->
                    TrackerTile(
                        tile = tile,
                        delayMs = ((rowIndex * 2 + columnIndex) * 60).toLong(),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowTiles.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TrackerTile(
    tile: DashboardTrackerHealth,
    delayMs: Long,
    modifier: Modifier = Modifier,
) {
    val colors = LocalDuesColors.current
    val health = remember(tile.percent, colors) { tile.percent.toTileHealth(colors) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(delayMs) {
        delay(delayMs)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.88f,
            animationSpec = tween(280, easing = FastOutSlowInEasing),
        ) + fadeIn(tween(280)),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.3f)
                .clip(RoundedCornerShape(ClearrDimens.dp18))
                .background(health.bgColor),
            contentAlignment = Alignment.TopStart,
        ) {
            Column(
                modifier = Modifier.padding(ClearrDimens.dp14),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp6),
                ) {
                    Text(
                        text = tile.trackerType.icon,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = tile.trackerType.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = health.textColor.copy(alpha = 0.75f),
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(Modifier.height(ClearrDimens.dp8))

                Text(
                    text = "${tile.percent}%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = health.textColor,
                    letterSpacing = (-1).sp,
                )

                Spacer(Modifier.height(ClearrDimens.dp6))

                Text(
                    text = tile.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = health.textColor.copy(alpha = 0.78f),
                )

                Spacer(Modifier.height(ClearrDimens.dp8))

                HealthProgressBar(
                    pct = tile.percent,
                    color = health.barColor,
                    trackColor = health.trackColor,
                )

                Spacer(Modifier.height(ClearrDimens.dp4))

                Text(
                    text = tile.statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = health.textColor.copy(alpha = 0.68f),
                )
            }
        }
    }
}

@Composable
private fun HealthProgressBar(
    pct: Int,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ClearrDimens.dp5)
            .clip(RoundedCornerShape(ClearrDimens.dp99))
            .background(trackColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = (pct / 100f).coerceIn(0f, 1f))
                .height(ClearrDimens.dp5)
                .clip(RoundedCornerShape(ClearrDimens.dp99))
                .background(color),
        )
    }
}

private data class TileHealth(
    val bgColor: Color,
    val textColor: Color,
    val barColor: Color,
    val trackColor: Color,
)

private fun Int.toTileHealth(colors: com.mikeisesele.clearr.ui.theme.DuesColors): TileHealth = when {
    this == 0 -> TileHealth(
        bgColor = colors.card,
        textColor = colors.muted,
        barColor = colors.border,
        trackColor = colors.border,
    )

    this < 35 -> TileHealth(
        bgColor = ClearrColors.CoralBg,
        textColor = ClearrColors.Coral,
        barColor = ClearrColors.Coral,
        trackColor = ClearrColors.Coral.copy(alpha = 0.18f),
    )

    this < 70 -> TileHealth(
        bgColor = ClearrColors.AmberBg,
        textColor = ClearrColors.Amber,
        barColor = ClearrColors.Amber,
        trackColor = ClearrColors.Amber.copy(alpha = 0.18f),
    )

    this < 90 -> TileHealth(
        bgColor = ClearrColors.BlueBg,
        textColor = ClearrColors.Blue,
        barColor = ClearrColors.Blue,
        trackColor = ClearrColors.Blue.copy(alpha = 0.18f),
    )

    else -> TileHealth(
        bgColor = ClearrColors.EmeraldBg,
        textColor = ClearrColors.Emerald,
        barColor = ClearrColors.Emerald,
        trackColor = ClearrColors.Emerald.copy(alpha = 0.18f),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F7FB)
@Composable
private fun TrackerHealthTilesPreview() {
    ClearrTheme {
        TrackerHealthTiles(
            score = previewDashboardUi.score,
            visibleTiles = previewDashboardUi.visibleTiles,
            modifier = Modifier.padding(ClearrDimens.dp20),
        )
    }
}
