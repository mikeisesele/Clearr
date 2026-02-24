package com.mikeisesele.clearr.ui.feature.goals

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.data.model.GoalSummary
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.fromToken
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun GoalsDetailScreen(
    trackerId: Long,
    onNavigateBack: () -> Unit,
    onAddGoal: () -> Unit,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var detailGoal by remember { mutableStateOf<GoalSummary?>(null) }

    if (state.trackerId != trackerId) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClearrColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GoalsNavBar(
                title = state.trackerName,
                doneCount = state.doneCount,
                totalCount = state.totalCount,
                onBack = onNavigateBack
            )

            AnimatedVisibility(
                visible = state.allDoneThisPeriod,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut()
            ) {
                AllClearedBanner()
            }

            SwipeHintStrip()

            if (!state.isLoading && state.summaries.isEmpty()) {
                GoalsEmptyState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(state.summaries, key = { _, summary -> summary.goal.id }) { index, summary ->
                        SwipeableGoalRow(
                            summary = summary,
                            isLast = index == state.summaries.lastIndex,
                            onDone = { viewModel.onAction(GoalsAction.MarkDone(it)) },
                            onTap = { detailGoal = it }
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20, bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24)
                .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp52)
                .clickable { onAddGoal() },
            shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
            color = ClearrColors.Violet,
            shadowElevation = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("+", color = ClearrColors.Surface, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp24, fontWeight = FontWeight.Bold)
            }
        }
    }

    detailGoal?.let { summary ->
        GoalDetailSheet(
            summary = summary,
            onDismiss = { detailGoal = null },
            onMarkDone = {
                viewModel.onAction(GoalsAction.MarkDone(it))
                detailGoal = null
            }
        )
    }
}

@Composable
private fun GoalsNavBar(
    title: String,
    doneCount: Int,
    totalCount: Int,
    onBack: () -> Unit
) {
    ClearrTopBar(
        title = title,
        subtitle = "$doneCount/$totalCount cleared today",
        leadingIcon = "←",
        onLeadingClick = onBack,
        actionIcon = null,
        onActionClick = null
    )
}

@Composable
private fun AllClearedBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ClearrColors.EmeraldBg)
            .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🎉", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp18)
        Spacer(modifier = Modifier.width(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
        Text(
            text = "All goals cleared for today!",
            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14,
            fontWeight = FontWeight.Bold,
            color = ClearrColors.Emerald
        )
    }
}

@Composable
private fun SwipeHintStrip() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp28)
            .background(ClearrColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Swipe right to mark a goal done for today",
            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp11,
            color = ClearrColors.TextMuted
        )
    }
}

@Composable
private fun SwipeableGoalRow(
    summary: GoalSummary,
    isLast: Boolean,
    onDone: (String) -> Unit,
    onTap: (GoalSummary) -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val maxSwipePx = with(density) { com.mikeisesele.clearr.ui.theme.ClearrDimens.dp130.toPx() }
    val thresholdPx = with(density) { com.mikeisesele.clearr.ui.theme.ClearrDimens.dp90.toPx() }
    val tapThresholdPx = with(density) { com.mikeisesele.clearr.ui.theme.ClearrDimens.dp5.toPx() }

    val offsetX = remember(summary.goal.id) { Animatable(0f) }
    var rowWidthPx by remember { mutableStateOf(0f) }
    var dragMagnitudePx by remember { mutableStateOf(0f) }

    val doneThisPeriod = summary.isDoneThisPeriod
    val palette = goalPalette(summary.goal.colorToken)
    val bgColor = if (offsetX.value > 20f) ClearrColors.Emerald else ClearrColors.Border

    Box(modifier = Modifier.fillMaxWidth().background(bgColor)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (offsetX.value > 20f) "✓ Done!" else "",
                color = ClearrColors.Surface,
                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp18,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ClearrColors.Surface)
                .alpha(if (doneThisPeriod) 0.6f else 1f)
                .onSizeChanged { rowWidthPx = it.width.toFloat() }
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(summary.goal.id, doneThisPeriod) {
                    detectTapGestures(onTap = {
                        if (dragMagnitudePx < tapThresholdPx) onTap(summary)
                        dragMagnitudePx = 0f
                    })
                }
                .pointerInput(summary.goal.id, doneThisPeriod) {
                    if (doneThisPeriod) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { dragMagnitudePx = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragMagnitudePx += abs(dragAmount)
                            scope.launch {
                                val next = (offsetX.value + dragAmount).coerceIn(0f, maxSwipePx)
                                offsetX.snapTo(next)
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value >= thresholdPx) {
                                    offsetX.animateTo(rowWidthPx.coerceAtLeast(maxSwipePx), spring())
                                    onDone(summary.goal.id)
                                } else {
                                    offsetX.animateTo(0f, spring())
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetX.animateTo(0f, spring()) }
                        }
                    )
                }
                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp13)
        ) {
            Surface(
                modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp42),
                shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
                color = if (doneThisPeriod) ClearrColors.EmeraldBg else palette.background
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (doneThisPeriod) "✓" else summary.goal.emoji,
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp20,
                        color = if (doneThisPeriod) ClearrColors.Emerald else Color.Unspecified
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = summary.goal.title,
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15,
                            fontWeight = FontWeight.SemiBold,
                            color = if (doneThisPeriod) ClearrColors.TextMuted else ClearrColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6))
                        Surface(shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10), color = ClearrColors.Background) {
                            Text(
                                text = if (summary.goal.frequency == GoalFrequency.DAILY) "Daily" else "Weekly",
                                modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp7, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1),
                                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp11,
                                color = ClearrColors.TextMuted
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13)
                        Spacer(Modifier.width(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp3))
                        Text(
                            text = summary.currentStreak.toString(),
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.currentStreak > 0) ClearrColors.Amber else ClearrColors.TextMuted
                        )
                    }
                }

                Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4))
                Text(
                    text = summary.goal.target ?: "No target set",
                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12,
                    color = ClearrColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6))
                HistoryDots(history = summary.recentHistory, color = palette.color)
            }
        }

        if (!isLast) HorizontalDivider(color = ClearrColors.Border, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun HistoryDots(
    history: List<com.mikeisesele.clearr.data.model.HistoryEntry>,
    color: Color
) {
    Row(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp3), verticalAlignment = Alignment.CenterVertically) {
        history.forEach { entry ->
            val dotColor by animateColorAsState(
                targetValue = if (entry.isDone) color else ClearrColors.Border,
                label = "goal_history_dot"
            )
            Box(
                modifier = Modifier
                    .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp7)
                    .background(dotColor, CircleShape)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalDetailSheet(
    summary: GoalSummary,
    onDismiss: () -> Unit,
    onMarkDone: (String) -> Unit
) {
    val doneThisPeriod = summary.isDoneThisPeriod
    val palette = goalPalette(summary.goal.colorToken)
    val completionPct = (summary.completionRate * 100f).roundToInt().coerceIn(0, 100)
    val historyTitle = if (summary.goal.frequency == GoalFrequency.DAILY) "LAST 7 DAYS" else "LAST 7 WEEKS"

    val addSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { target -> target != SheetValue.Hidden }
    )

    BackHandler(enabled = true) {}
    ModalBottomSheet(
        onDismissRequest = {},
        containerColor = ClearrColors.Surface,
        sheetState = addSheetState,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = ClearrColors.TextSecondary)
                }
                Text("Goal Detail", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp16, fontWeight = FontWeight.SemiBold, color = ClearrColors.TextPrimary)
                Spacer(modifier = Modifier.width(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp40))
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14)) {
                Surface(
                    modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp52),
                    shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14),
                    color = if (doneThisPeriod) ClearrColors.EmeraldBg else palette.background
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (doneThisPeriod) "✓" else summary.goal.emoji, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp24)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(summary.goal.title, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp18, fontWeight = FontWeight.Bold, color = ClearrColors.TextPrimary)
                    Text(
                        text = "${summary.goal.target ?: "No target"} · ${if (summary.goal.frequency == GoalFrequency.DAILY) "Daily" else "Weekly"}",
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13,
                        color = ClearrColors.TextMuted
                    )
                }
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20))
            Row(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10), modifier = Modifier.fillMaxWidth()) {
                StatTile(
                    label = "Streak",
                    value = "${summary.currentStreak}🔥",
                    fg = if (summary.currentStreak > 0) ClearrColors.Amber else ClearrColors.TextMuted,
                    bg = if (summary.currentStreak > 0) ClearrColors.AmberBg else ClearrColors.Background,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "Best",
                    value = "${summary.bestStreak}🏆",
                    fg = ClearrColors.Violet,
                    bg = ClearrColors.VioletBg,
                    modifier = Modifier.weight(1f)
                )
                val (rateFg, rateBg) = when {
                    completionPct >= 70 -> ClearrColors.Emerald to ClearrColors.EmeraldBg
                    completionPct >= 40 -> ClearrColors.Amber to ClearrColors.AmberBg
                    else -> ClearrColors.Coral to ClearrColors.CoralBg
                }
                StatTile(
                    label = "Rate",
                    value = "$completionPct%",
                    fg = rateFg,
                    bg = rateBg,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20))
            Text(historyTitle, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12, fontWeight = FontWeight.SemiBold, color = ClearrColors.TextMuted)
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10))
            Row(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6), modifier = Modifier.fillMaxWidth()) {
                summary.recentHistory.forEach { entry ->
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp36),
                            shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8),
                            color = if (entry.isDone) palette.color else ClearrColors.Background
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (entry.isDone) "✓" else "—",
                                    fontSize = if (entry.isDone) com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14 else com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp11,
                                    fontWeight = FontWeight.Bold,
                                    color = if (entry.isDone) ClearrColors.Surface else ClearrColors.Border
                                )
                            }
                        }
                        Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4))
                        Text(entry.label, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp9, color = ClearrColors.TextMuted, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24))
            if (!doneThisPeriod) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onMarkDone(summary.goal.id)
                        },
                    shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14),
                    color = palette.color,
                    shadowElevation = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp15)) {
                        Text(
                            "Mark as Done Today ✓",
                            color = ClearrColors.Surface,
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14),
                    color = ClearrColors.EmeraldBg
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14)) {
                        Text(
                            "✓ Completed for this period",
                            color = ClearrColors.Emerald,
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    fg: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12), color = bg) {
        Column(
            modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp18, fontWeight = FontWeight.ExtraBold, color = fg)
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2))
            Text(label, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp11, fontWeight = FontWeight.SemiBold, color = ClearrColors.TextMuted)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGoalScreen(
    trackerId: Long,
    onClose: () -> Unit,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.trackerId != trackerId) return

    var title by rememberSaveable { mutableStateOf("") }
    var emoji by rememberSaveable { mutableStateOf("🎯") }
    var target by rememberSaveable { mutableStateOf("") }
    var frequency by rememberSaveable { mutableStateOf(GoalFrequency.DAILY) }
    var colorToken by rememberSaveable { mutableStateOf("Purple") }
    var showAllIcons by rememberSaveable { mutableStateOf(false) }
    val titleFocusRequester = remember { FocusRequester() }
    val canSubmit = title.trim().isNotEmpty()

    val emojis = listOf(
        "🎯", "🏃", "💰", "📚", "🥗", "🚿",
        "💪", "🧘", "✍️", "🎸", "🌅", "💊",
        "🧠", "🎨", "🧹", "🛌", "💼", "🧾",
        "🍎", "🏊", "🚶", "📖", "🧑‍💻", "🎵"
    )
    val visibleEmojis = if (showAllIcons) emojis else emojis.take(6)
    val colorTokens = listOf("Purple", "Emerald", "Blue", "Amber", "Coral")
    val palette = goalPalette(colorToken)

    LaunchedEffect(Unit) { titleFocusRequester.requestFocus() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClearrColors.Background)
            .statusBarsPadding()
            .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)
            .navigationBarsPadding()
    ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp34))
                Text("New Goal", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp16, fontWeight = FontWeight.SemiBold, color = ClearrColors.TextPrimary)
                Surface(
                    modifier = Modifier
                        .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp34)
                        .clickable { onClose() },
                    shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10),
                    color = ClearrColors.Background
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ClearrColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
                color = palette.background.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)
                ) {
                    Surface(
                        modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp44),
                        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
                        color = palette.background
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(emoji, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp22)
                        }
                    }
                    Column {
                        Text(
                            text = title.ifBlank { "Goal name" },
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp16,
                            fontWeight = FontWeight.Bold,
                            color = ClearrColors.TextPrimary
                        )
                        Text(
                            text = "${target.ifBlank { "Set a target" }} · ${if (frequency == GoalFrequency.DAILY) "Daily" else "Weekly"}",
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12,
                            color = ClearrColors.TextMuted
                        )
                    }
                }
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16))
            SectionTitle("ICON")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8), verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)) {
                visibleEmojis.forEach { value ->
                    Surface(
                        modifier = Modifier
                            .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp38)
                            .clickable { emoji = value },
                        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10),
                        color = if (emoji == value) palette.background else ClearrColors.Background,
                        border = BorderStroke(
                            width = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2,
                            color = if (emoji == value) palette.color else ClearrColors.Transparent
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(value, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp18)
                        }
                    }
                }
            }
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6))
            TextButton(
                onClick = { showAllIcons = !showAllIcons },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text(
                    text = if (showAllIcons) "Show fewer icons" else "Show more icons",
                    color = palette.color,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16))
            SectionTitle("COLOR")
            Row(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10)) {
                colorTokens.forEach { token ->
                    val tokenPalette = goalPalette(token)
                    Surface(
                        modifier = Modifier
                            .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp28)
                            .clickable { colorToken = token },
                        shape = CircleShape,
                        color = tokenPalette.color,
                        border = BorderStroke(
                            width = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp3,
                            color = if (colorToken == token) ClearrColors.TextPrimary else ClearrColors.Transparent
                        )
                    ) {}
                }
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16))
            SectionTitle("GOAL NAME")
            GoalSheetInput(
                value = title,
                onValueChange = { title = it },
                placeholder = "e.g. Exercise",
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester)
            )

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
            SectionTitle("TARGET (OPTIONAL)")
            GoalSheetInput(
                value = target,
                onValueChange = { target = it },
                placeholder = "e.g. 30 mins, ₦10,000, 20 pages",
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16))
            SectionTitle("FREQUENCY")
            Row(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8), modifier = Modifier.fillMaxWidth()) {
                listOf(GoalFrequency.DAILY, GoalFrequency.WEEKLY).forEach { value ->
                    val selected = value == frequency
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp44)
                            .clickable { frequency = value },
                        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10),
                        color = if (selected) palette.background else ClearrColors.Background,
                        border = BorderStroke(
                            width = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2,
                            color = if (selected) palette.color else ClearrColors.Transparent
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (value == GoalFrequency.DAILY) "Daily" else "Weekly",
                                color = if (selected) palette.color else ClearrColors.TextMuted,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    viewModel.onAction(
                        GoalsAction.AddGoal(
                            title = title.trim(),
                            emoji = emoji,
                            colorToken = colorToken,
                            target = target.trim().ifBlank { null },
                            frequency = frequency
                        )
                    )
                    onClose()
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.color,
                    disabledContainerColor = ClearrColors.Border
                )
            ) {
                Text("Add Goal", color = ClearrColors.Surface, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
    }
}

@Composable
private fun SectionTitle(label: String) {
    Text(
        text = label,
        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12,
        fontWeight = FontWeight.SemiBold,
        color = ClearrColors.TextMuted,
        modifier = Modifier.padding(start = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4)
    )
}

@Composable
private fun GoalSheetInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
        color = ClearrColors.Background,
        border = BorderStroke(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1, ClearrColors.Border),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14,
                vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp13
            )
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = TextStyle(
                    color = ClearrColors.TextPrimary,
                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isBlank()) {
                        Text(placeholder, color = ClearrColors.TextMuted, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15)
                    }
                    inner()
                }
            )
        }
    }
}

@Composable
private fun GoalsEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ClearrColors.Background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎯", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp40)
        Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
        Text("No goals yet", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp16, fontWeight = FontWeight.Bold, color = ClearrColors.TextPrimary)
        Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4))
        Text(
            text = "Add your first goal and start building a streak.",
            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13,
            color = ClearrColors.TextMuted
        )
    }
}

private data class GoalPalette(
    val color: Color,
    val background: Color
)

private fun goalPalette(token: String): GoalPalette {
    val scheme = ClearrColors.fromToken(token)
    return GoalPalette(color = scheme.color, background = scheme.background)
}
