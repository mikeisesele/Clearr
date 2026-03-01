package com.mikeisesele.clearr.ui.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardClearanceScore
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerHealth
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerType
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardUiModel
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardUrgencyItem
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardUrgencySeverity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    state: DashboardUiModel,
    isLoading: Boolean,
    onDismissUrgency: (String) -> Unit,
    onQuickAction: (DashboardTrackerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = dashboardPalette()
    Column(modifier = modifier.fillMaxSize().background(palette.bg)) {
        PeriodContextBar(period = state.periodLabel, days = state.daysLabel, palette = palette)

        if (!isLoading && state.visibleTiles.isEmpty()) {
            DashboardEmptyState(
                onNavigateToTab = onQuickAction,
                palette = palette,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (!isLoading && state.visibleTiles.isNotEmpty()) {
                    item {
                        TrackerHealthTiles(score = state.score, visibleTiles = state.visibleTiles, palette = palette)
                    }
                }
                if (!isLoading) {
                    when {
                        state.urgencyItems.isNotEmpty() -> {
                            item {
                                Text(
                                    text = "Needs attention",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.muted,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                UrgencyStrip(
                                    state = state,
                                    palette = palette,
                                    onDismissUrgency = onDismissUrgency,
                                    onQuickAction = onQuickAction,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        state.score.hasAnyClearedTile() -> item {
                            AllClearCard(hasTrackers = true, palette = palette, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }

        if (!isLoading && state.visibleTiles.isNotEmpty()) {
            QuickActionRow(
                palette = palette,
                onLogSpend = { onQuickAction(DashboardTrackerType.BUDGET) },
                onMarkGoal = { onQuickAction(DashboardTrackerType.GOALS) },
                onReviewTodos = { onQuickAction(DashboardTrackerType.TODOS) },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    }
}

private data class DashboardPalette(
    val bg: Color,
    val surface: Color,
    val card: Color,
    val border: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val green: Color,
    val amber: Color,
    val red: Color,
)

@Composable
private fun dashboardPalette(): DashboardPalette {
    val scheme = MaterialTheme.colorScheme
    return DashboardPalette(
        bg = scheme.background,
        surface = scheme.surface,
        card = scheme.surfaceVariant,
        border = scheme.outline.copy(alpha = 0.25f),
        text = scheme.onBackground,
        muted = scheme.onSurfaceVariant,
        accent = scheme.primary,
        green = Color(0xFF00A67E),
        amber = Color(0xFFF59E0B),
        red = Color(0xFFEF4444)
    )
}

private fun DashboardTrackerType.accentColor(): Color = when (this) {
    DashboardTrackerType.BUDGET -> Color(0xFF3B82F6)
    DashboardTrackerType.GOALS -> Color(0xFF00A67E)
    DashboardTrackerType.TODOS -> Color(0xFFF59E0B)
}

private fun DashboardTrackerType.backgroundColor(): Color = when (this) {
    DashboardTrackerType.BUDGET -> Color(0xFFEFF6FF)
    DashboardTrackerType.GOALS -> Color(0xFFE6F7F3)
    DashboardTrackerType.TODOS -> Color(0xFFFEF3C7)
}

private fun DashboardClearanceScore.hasAnyClearedTile(): Boolean =
    listOf(budget, goals, todos).any { it.percent >= 90 }

@Composable
private fun PeriodContextBar(period: String, days: String, palette: DashboardPalette, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = period, style = MaterialTheme.typography.titleSmall, color = palette.text)
        Text(text = days, style = MaterialTheme.typography.bodySmall, color = palette.muted)
    }
}

@Composable
private fun TrackerHealthTiles(
    score: DashboardClearanceScore,
    visibleTiles: List<DashboardTrackerType>,
    palette: DashboardPalette,
    modifier: Modifier = Modifier,
) {
    val tiles = remember(score, visibleTiles) {
        listOf(score.budget, score.goals, score.todos).filter { it.trackerType in visibleTiles }
    }
    if (tiles.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        tiles.chunked(2).forEachIndexed { rowIndex, rowTiles ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowTiles.forEachIndexed { columnIndex, tile ->
                    TrackerTile(tile = tile, delayMs = ((rowIndex * 2 + columnIndex) * 60).toLong(), palette = palette, modifier = Modifier.weight(1f))
                }
                if (rowTiles.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TrackerTile(tile: DashboardTrackerHealth, delayMs: Long, palette: DashboardPalette, modifier: Modifier = Modifier) {
    val health = remember(tile.percent, palette) { tile.percent.toTileHealth(palette) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(delayMs) {
        delay(delayMs)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(initialScale = 0.88f, animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeIn(tween(280)),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1.3f).clip(RoundedCornerShape(18.dp)).background(health.bgColor),
            contentAlignment = Alignment.TopStart,
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = tile.trackerType.icon, style = MaterialTheme.typography.titleMedium)
                    Text(text = tile.trackerType.label, style = MaterialTheme.typography.labelMedium, color = health.textColor.copy(alpha = 0.75f), fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
                Text(text = "${tile.percent}%", fontSize = 28.sp, fontWeight = FontWeight.Black, color = health.textColor, letterSpacing = (-1).sp)
                Spacer(Modifier.height(6.dp))
                Text(text = tile.detail, style = MaterialTheme.typography.bodyMedium, color = health.textColor.copy(alpha = 0.78f))
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(99.dp)).background(health.trackColor)) {
                    Box(modifier = Modifier.fillMaxWidth(fraction = (tile.percent / 100f).coerceIn(0f, 1f)).height(5.dp).clip(RoundedCornerShape(99.dp)).background(health.barColor))
                }
                Spacer(Modifier.height(4.dp))
                Text(text = tile.statusLabel, style = MaterialTheme.typography.labelSmall, color = health.textColor.copy(alpha = 0.68f))
            }
        }
    }
}

private data class TileHealth(val bgColor: Color, val textColor: Color, val barColor: Color, val trackColor: Color)

private fun Int.toTileHealth(palette: DashboardPalette): TileHealth = when {
    this == 0 -> TileHealth(palette.card, palette.muted, palette.border, palette.border)
    this < 35 -> TileHealth(Color(0xFFFEE2E2), palette.red, palette.red, palette.red.copy(alpha = 0.18f))
    this < 70 -> TileHealth(Color(0xFFFEF3C7), palette.amber, palette.amber, palette.amber.copy(alpha = 0.18f))
    this < 90 -> TileHealth(Color(0xFFEFF6FF), Color(0xFF3B82F6), Color(0xFF3B82F6), Color(0xFF3B82F6).copy(alpha = 0.18f))
    else -> TileHealth(Color(0xFFE6F7F3), palette.green, palette.green, palette.green.copy(alpha = 0.18f))
}

@Composable
private fun UrgencyStrip(
    state: DashboardUiModel,
    palette: DashboardPalette,
    onDismissUrgency: (String) -> Unit,
    onQuickAction: (DashboardTrackerType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        state.urgencyItems.forEach { item ->
            UrgencyCard(
                item = item,
                palette = palette,
                onDismiss = { onDismissUrgency(item.id) },
                onQuickAction = onQuickAction
            )
        }
    }
}

@Composable
private fun UrgencyCard(
    item: DashboardUrgencyItem,
    palette: DashboardPalette,
    onDismiss: () -> Unit,
    onQuickAction: (DashboardTrackerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    var cardWidth by remember { mutableFloatStateOf(0f) }
    val thresholdFraction = 0.4f
    val isActionRevealed = offsetX.value > cardWidth * thresholdFraction
    val shape = RoundedCornerShape(20.dp)
    val severityColor = when (item.severity) {
        DashboardUrgencySeverity.CRITICAL -> palette.red
        DashboardUrgencySeverity.WARNING -> palette.amber
        DashboardUrgencySeverity.INFO -> palette.green
    }

    Box(
        modifier = modifier.fillMaxWidth().semantics {
            contentDescription = "${item.message}. Swipe right to ${item.actionLabel}. Swipe left to dismiss."
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    when {
                        offsetX.value > 0f -> item.trackerType.accentColor().copy(alpha = 0.12f)
                        offsetX.value < 0f -> palette.red.copy(alpha = 0.14f)
                        else -> palette.surface
                    }
                )
                .border(1.dp, palette.border, shape)
        ) {
            if (offsetX.value > 0f) {
                Text(
                    text = item.actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = item.trackerType.accentColor(),
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                )
            }
            if (offsetX.value < 0f) {
                Text(
                    text = "Dismiss",
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.red,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
                )
            }
        }

        Surface(
            color = palette.surface,
            shape = shape,
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .onGloballyPositioned { cardWidth = it.size.width.toFloat() }
                .pointerInput(item.id) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                        },
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offsetX.value >= cardWidth * thresholdFraction -> {
                                        offsetX.animateTo(cardWidth * 0.44f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 320f))
                                    }
                                    offsetX.value <= -(cardWidth * thresholdFraction) -> {
                                        offsetX.animateTo(-cardWidth * 1.1f, animationSpec = tween(180))
                                        onDismiss()
                                    }
                                    else -> offsetX.animateTo(0f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 320f))
                                }
                            }
                        }
                    )
                }
                .clickable(enabled = isActionRevealed) {
                    if (isActionRevealed) {
                        onQuickAction(item.trackerType)
                        scope.launch { offsetX.animateTo(0f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f)) }
                    }
                }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(99.dp)).background(severityColor))
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.message, style = MaterialTheme.typography.bodyMedium, color = palette.text)
                    Spacer(Modifier.height(4.dp))
                    Text(text = item.trackerType.label, style = MaterialTheme.typography.labelSmall, color = palette.muted)
                }
                AnimatedVisibility(
                    visible = isActionRevealed,
                    enter = slideInHorizontally { it / 3 } + fadeIn(),
                    exit = slideOutHorizontally { it / 3 } + fadeOut()
                ) {
                    Text(text = item.actionLabel, style = MaterialTheme.typography.labelMedium, color = item.trackerType.accentColor(), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private data class EmptyStateCard(
    val trackerType: DashboardTrackerType,
    val description: String,
    val ctaLabel: String,
)

private val emptyStateCards = listOf(
    EmptyStateCard(DashboardTrackerType.BUDGET, "Plan your monthly spend by category. Know exactly where your money goes.", "Plan budget"),
    EmptyStateCard(DashboardTrackerType.GOALS, "Set recurring habits and targets. Track streaks and completion over time.", "Build goals"),
    EmptyStateCard(DashboardTrackerType.TODOS, "Capture personal tasks and deadlines. Clear obligations one by one.", "Start todos")
)

@Composable
private fun DashboardEmptyState(
    onNavigateToTab: (DashboardTrackerType) -> Unit,
    palette: DashboardPalette,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { emptyStateCards.size })
    val currentPage = pagerState.currentPage.coerceIn(0, emptyStateCards.lastIndex)
    val activeCard = emptyStateCards[currentPage]
    val containerTint by animateColorAsState(
        targetValue = lerp(palette.bg, activeCard.trackerType.backgroundColor(), 0.32f),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "empty_state_container_tint",
    )

    Column(
        modifier = modifier.fillMaxWidth().background(containerTint).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Text(text = "Nothing\nto clear\nyet.", style = MaterialTheme.typography.displaySmall, color = palette.text, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Text(text = "Start with:", style = MaterialTheme.typography.bodyMedium, color = palette.muted, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 0.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        ) { page ->
            EmptyTrackerCard(card = emptyStateCards[page], onNavigateToTab = onNavigateToTab, palette = palette)
        }

        Spacer(Modifier.height(20.dp))
        PagerDots(count = emptyStateCards.size, current = currentPage, activeColor = activeCard.trackerType.accentColor())
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun EmptyTrackerCard(card: EmptyStateCard, onNavigateToTab: (DashboardTrackerType) -> Unit, palette: DashboardPalette) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 320.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(card.trackerType.backgroundColor())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = card.trackerType.icon, style = MaterialTheme.typography.displaySmall)
        Text(text = card.trackerType.label, style = MaterialTheme.typography.titleLarge, color = card.trackerType.accentColor())
        Text(text = card.description, style = MaterialTheme.typography.bodyMedium, color = palette.text.copy(alpha = 0.75f))
        Spacer(Modifier.weight(1f, fill = true))
        Button(
            onClick = { onNavigateToTab(card.trackerType) },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = card.trackerType.accentColor(), contentColor = palette.surface),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(text = card.ctaLabel, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun PagerDots(count: Int, current: Int, activeColor: Color) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(count) { index ->
                val isActive = index == current
                val color by animateColorAsState(
                    targetValue = if (isActive) activeColor else Color(0xFFDDDDDD),
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    label = "dot_color_$index",
                )
                val width by animateDpAsState(
                    targetValue = if (isActive) 20.dp else 6.dp,
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    label = "dot_width_$index",
                )
                Box(modifier = Modifier.width(width).height(6.dp).clip(CircleShape).background(color))
            }
        }
    }
}

@Composable
private fun AllClearCard(hasTrackers: Boolean, palette: DashboardPalette, modifier: Modifier = Modifier) {
    Surface(
        color = if (hasTrackers) palette.green.copy(alpha = 0.12f) else palette.card,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(20.dp)) {
            Text(
                text = if (hasTrackers) "All clear this period 🎉" else "Set up your first tracker to see your score.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (hasTrackers) palette.green else palette.text,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuickActionRow(
    palette: DashboardPalette,
    onLogSpend: () -> Unit,
    onMarkGoal: () -> Unit,
    onReviewTodos: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(label = "Log spend", palette = palette, onClick = onLogSpend, modifier = Modifier.weight(1f))
        QuickActionButton(label = "Mark done", palette = palette, onClick = onMarkGoal, modifier = Modifier.weight(1f))
        QuickActionButton(label = "Review", palette = palette, onClick = onReviewTodos, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun QuickActionButton(label: String, palette: DashboardPalette, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .scale(scale.value)
            .background(palette.card, RoundedCornerShape(16.dp))
            .clickable {
                scope.launch {
                    scale.animateTo(0.94f, animationSpec = spring(stiffness = 600f))
                    scale.animateTo(1f, animationSpec = spring(stiffness = 420f))
                }
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = palette.accent)
    }
}
