package com.mikeisesele.clearr.ui.feature.goals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
import com.mikeisesele.clearr.core.ai.GoalAiResult
import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.data.model.GoalSummary
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import com.mikeisesele.clearr.ui.theme.fromToken
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun GoalsDetailScreen(
    trackerId: Long,
    onNavigateBack: () -> Unit,
    onAddGoal: () -> Unit,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalDuesColors.current
    var detailGoal by remember { mutableStateOf<GoalSummary?>(null) }
    var renameTarget by remember { mutableStateOf<GoalSummary?>(null) }
    var renameValue by remember { mutableStateOf("") }
    var playDeleteHint by rememberSaveable { mutableStateOf(true) }

    if (state.trackerId != trackerId) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GoalsNavBar(
                title = state.trackerName,
                onBack = onNavigateBack
            )

            AnimatedVisibility(
                visible = state.allDoneThisPeriod,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut()
            ) {
                AllClearedBanner(colors = colors)
            }
            // state.aiInsight?.let { insight ->
            //     Box(
            //         modifier = Modifier
            //             .fillMaxWidth()
            //             .background(colors.bg)
            //             .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6)
            //     ) {
            //         Text(
            //             text = insight,
            //             color = colors.muted,
            //             fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12
            //         )
            //     }
            // }

            if (!state.isLoading && state.summaries.isEmpty()) {
                GoalsEmptyState(modifier = Modifier.weight(1f))
            } else {
                SwipeHintStrip(colors = colors)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(state.summaries, key = { _, summary -> summary.goal.id }) { index, summary ->
                        SwipeableGoalRow(
                            summary = summary,
                            isLast = index == state.summaries.lastIndex,
                            colors = colors,
                            hintDeleteAnimation = index == 0 && playDeleteHint,
                            onHintAnimationPlayed = { playDeleteHint = false },
                            onDelete = { viewModel.onAction(GoalsAction.Delete(it)) },
                            onTap = { detailGoal = it },
                            onLongPress = {
                                renameTarget = it
                                renameValue = it.goal.title
                            }
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
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
            colors = colors,
            onDismiss = { detailGoal = null },
            onMarkDone = {
                viewModel.onAction(GoalsAction.MarkDone(it))
                detailGoal = null
            }
        )
    }

    renameTarget?.let { summary ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { renameTarget = null },
            containerColor = colors.surface,
            title = { Text("Rename Goal", color = colors.text) },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Title") }
                )
            },
            confirmButton = {
                Button(
                    enabled = renameValue.isNotBlank(),
                    onClick = {
                        viewModel.onAction(GoalsAction.Rename(summary.goal.id, renameValue))
                        renameTarget = null
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel", color = colors.muted) }
            }
        )
    }
}

@Composable
private fun GoalsNavBar(
    title: String,
    onBack: () -> Unit
) {
    ClearrTopBar(
        title = title,
        leadingIcon = "←",
        onLeadingClick = onBack,
        actionIcon = null,
        onActionClick = null,
        leadingContainerColor = Color.Transparent
    )
}

@Composable
private fun AllClearedBanner(colors: DuesColors) {
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
private fun SwipeHintStrip(colors: DuesColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp28)
            .background(colors.bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Swipe left to delete",
            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp11,
            color = colors.muted
        )
    }
}

@Composable
private fun SwipeableGoalRow(
    summary: GoalSummary,
    isLast: Boolean,
    colors: DuesColors,
    hintDeleteAnimation: Boolean,
    onHintAnimationPlayed: () -> Unit,
    onDelete: (String) -> Unit,
    onTap: (GoalSummary) -> Unit,
    onLongPress: (GoalSummary) -> Unit
) {
    val maxHintOffsetPx = 64f
    val hintOffset = remember(summary.goal.id) { Animatable(0f) }
    var hintShown by rememberSaveable(summary.goal.id) { mutableStateOf(false) }
    val hintAlpha = (kotlin.math.abs(hintOffset.value) / maxHintOffsetPx).coerceIn(0f, 1f)
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.35f },
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(summary.goal.id)
            }
            false
        }
    )

    val doneThisPeriod = summary.isDoneThisPeriod
    val palette = goalPalette(summary.goal.colorToken)

    LaunchedEffect(hintDeleteAnimation) {
        if (hintDeleteAnimation && !hintShown) {
            hintShown = true
            hintOffset.animateTo(
                targetValue = -maxHintOffsetPx,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 280)
            )
            hintOffset.animateTo(
                targetValue = 0f,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 260)
            )
            onHintAnimationPlayed()
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(ClearrColors.BrandDanger)
                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = ClearrColors.Surface,
                modifier = Modifier
                    .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp22)
                    .alpha(hintAlpha)
            )
        }

        SwipeToDismissBox(
            state = dismissState,
            modifier = Modifier.graphicsLayer { translationX = hintOffset.value },
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ClearrColors.BrandDanger)
                        .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ClearrColors.Surface,
                        modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp22)
                    )
                }
            },
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .alpha(if (doneThisPeriod) 0.6f else 1f)
                    .combinedClickable(
                        onClick = { onTap(summary) },
                        onLongClick = { onLongPress(summary) }
                    )
                    .padding(
                        horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16,
                        vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14
                    ),
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
                                color = if (doneThisPeriod) colors.muted else colors.text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.width(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6))
                            Surface(shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10), color = colors.card) {
                                Text(
                                    text = if (summary.goal.frequency == GoalFrequency.DAILY) "Daily" else "Weekly",
                                    modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp7, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1),
                                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp11,
                                    color = colors.muted
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
                                color = if (summary.currentStreak > 0) ClearrColors.Amber else colors.muted
                            )
                        }
                    }

                    Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4))
                    Text(
                        text = summary.goal.target ?: "No target set",
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12,
                        color = colors.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6))
                    HistoryDots(history = summary.recentHistory, color = palette.color)
                }
            }
        }

        if (!isLast) HorizontalDivider(color = colors.border, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
@Composable
private fun HistoryDots(
    history: List<com.mikeisesele.clearr.data.model.HistoryEntry>,
    color: Color
) {
    val colors = LocalDuesColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp3), verticalAlignment = Alignment.CenterVertically) {
        history.forEach { entry ->
            val dotColor by animateColorAsState(
                targetValue = if (entry.isDone) color else colors.border,
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
    colors: DuesColors,
    onDismiss: () -> Unit,
    onMarkDone: (String) -> Unit
) {
    val doneThisPeriod = summary.isDoneThisPeriod
    val palette = goalPalette(summary.goal.colorToken)
    val completionPct = (summary.completionRate * 100f).roundToInt().coerceIn(0, 100)
    val historyTitle = if (summary.goal.frequency == GoalFrequency.DAILY) "LAST 7 DAYS" else "LAST 7 WEEKS"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 640.dp),
                shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20),
                color = colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp40))
                        Text("Goal Detail", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp16, fontWeight = FontWeight.SemiBold, color = colors.text)
                        TextButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = colors.muted
                            )
                        }
                    }

                    Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14)) {
                        Surface(
                            modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp52),
                            shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14),
                            color = if (doneThisPeriod) ClearrColors.EmeraldBg else palette.background
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(summary.goal.emoji, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp24)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(summary.goal.title, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp18, fontWeight = FontWeight.Bold, color = colors.text)
                            Text(
                                text = "${summary.goal.target ?: "No target"} · ${if (summary.goal.frequency == GoalFrequency.DAILY) "Daily" else "Weekly"}",
                                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13,
                                color = colors.muted
                            )
                        }
                    }

                    Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20))
                    Row(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10), modifier = Modifier.fillMaxWidth()) {
                        StatTile(
                            label = "Streak",
                            value = "${summary.currentStreak}🔥",
                            fg = if (summary.currentStreak > 0) ClearrColors.Amber else colors.muted,
                            bg = if (summary.currentStreak > 0) ClearrColors.AmberBg else colors.card,
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
                    Text(historyTitle, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12, fontWeight = FontWeight.SemiBold, color = colors.muted)
                    Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10))
                    Row(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6), modifier = Modifier.fillMaxWidth()) {
                        summary.recentHistory.forEach { entry ->
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp36),
                                    shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8),
                                    color = if (entry.isDone) palette.color else colors.card
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
                                Text(entry.label, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp9, color = colors.muted, fontWeight = FontWeight.SemiBold)
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
                    Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
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
    val colors = LocalDuesColors.current
    Surface(modifier = modifier, shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12), color = bg) {
        Column(
            modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp18, fontWeight = FontWeight.ExtraBold, color = fg)
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2))
            Text(label, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp11, fontWeight = FontWeight.SemiBold, color = colors.muted)
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
    val colors = LocalDuesColors.current
    if (state.trackerId != trackerId) return

    var title by rememberSaveable { mutableStateOf("") }
    var emoji by rememberSaveable { mutableStateOf("🎯") }
    var target by rememberSaveable { mutableStateOf("") }
    var frequency by rememberSaveable { mutableStateOf(GoalFrequency.DAILY) }
    var colorToken by rememberSaveable { mutableStateOf("Purple") }
    var showAllIcons by rememberSaveable { mutableStateOf(false) }
    val titleFocusRequester = remember { FocusRequester() }
    val canSubmit = title.trim().isNotEmpty()
    var aiDraft by remember { mutableStateOf<GoalAiResult?>(null) }
    var aiLoading by remember { mutableStateOf(false) }
    var frequencyTouched by rememberSaveable { mutableStateOf(false) }
    var emojiTouched by rememberSaveable { mutableStateOf(false) }
    var colorTouched by rememberSaveable { mutableStateOf(false) }
    var targetTouched by rememberSaveable { mutableStateOf(false) }

    val emojis = listOf(
        "🎯", "🏃", "💰", "📚", "🥗", "🚿",
        "💪", "🧘", "✍️", "🎸", "🌅", "💊",
        "🧠", "🎨", "🧹", "🛌", "💼", "🧾",
        "🍎", "🏊", "🚶", "📖", "🧑‍💻", "🎵"
    )
    val firstRowEmojis = emojis.take(6)
    val extraEmojis = emojis.drop(6)
    val colorTokens = listOf("Purple", "Emerald", "Blue", "Amber", "Coral")
    val palette = goalPalette(colorToken)

    LaunchedEffect(Unit) { titleFocusRequester.requestFocus() }
    LaunchedEffect(title, target) {
        if (title.trim().length < 3) {
            aiDraft = null
            aiLoading = false
            return@LaunchedEffect
        }
        aiLoading = true
        delay(350)
        val inferred = ClearrEdgeAi.inferGoalNanoAware(
            title = title,
            target = target,
            frequency = frequency,
            emoji = emoji,
            colorToken = colorToken
        )
        aiDraft = inferred
        if (!targetTouched && target.isBlank() && !inferred.suggestedTarget.isNullOrBlank()) {
            target = inferred.suggestedTarget
        }
        if (!frequencyTouched) {
            frequency = inferred.suggestedFrequency
        }
        if (!emojiTouched) {
            emoji = inferred.suggestedEmoji
        }
        if (!colorTouched) {
            colorToken = inferred.suggestedColorToken
        }
        aiLoading = false
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)
            .navigationBarsPadding()
    ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp34)
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.text
                    )
                }
                Text("New Goal", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp16, fontWeight = FontWeight.SemiBold, color = colors.text)
                Spacer(modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp34))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
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
                                color = colors.text
                            )
                            Text(
                                text = "${target.ifBlank { "Set a target" }} · ${if (frequency == GoalFrequency.DAILY) "Daily" else "Weekly"}",
                                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12,
                                color = colors.text.copy(alpha = 0.78f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16))
                // if (title.isNotBlank()) {
                //     Text(
                //         text = when {
                //             aiLoading -> "AI: Thinking..."
                //             aiDraft != null -> {
                //                 val draft = aiDraft!!
                //                 val freq = draft.suggestedFrequency.name.lowercase().replaceFirstChar { it.uppercase() }
                //                 "AI: ${draft.suggestedEmoji} $freq${draft.suggestedTarget?.let { " · target $it" } ?: ""}"
                //             }
                //             else -> "AI: No suggestion yet"
                //         },
                //         fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12,
                //         color = colors.muted,
                //         modifier = Modifier.padding(start = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4)
                //     )
                //     Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
                // }
                SectionTitle("ICON")
                Column(modifier = Modifier.animateContentSize()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8), verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)) {
                        firstRowEmojis.forEach { value ->
                            Surface(
                                modifier = Modifier
                                    .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp38)
                                    .clickable {
                                        emojiTouched = true
                                        emoji = value
                                    },
                                shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10),
                                color = if (emoji == value) palette.background else colors.card,
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
                    AnimatedVisibility(visible = showAllIcons) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8),
                            verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)
                        ) {
                            extraEmojis.forEach { value ->
                                Surface(
                                    modifier = Modifier
                                        .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp38)
                                        .clickable {
                                            emojiTouched = true
                                            emoji = value
                                        },
                                    shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10),
                                    color = if (emoji == value) palette.background else colors.card,
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
                                .clickable {
                                    colorTouched = true
                                    colorToken = token
                                },
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
                    onValueChange = { title = capitalizeFirstTypedCharacter(it) },
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
                    onValueChange = {
                        targetTouched = true
                        target = it
                    },
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
                                .clickable {
                                    frequencyTouched = true
                                    frequency = value
                                },
                            shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10),
                                color = if (selected) palette.background else colors.card,
                            border = BorderStroke(
                                width = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2,
                                color = if (selected) palette.color else ClearrColors.Transparent
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (value == GoalFrequency.DAILY) "Daily" else "Weekly",
                                    color = if (selected) palette.color else colors.muted,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24))
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
                        disabledContainerColor = colors.border
                    ),
                    contentPadding = PaddingValues(vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
                ) {
                    Text("Add Goal", color = ClearrColors.Surface, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
            }
    }
}

@Composable
private fun SectionTitle(label: String) {
    val colors = LocalDuesColors.current
    Text(
        text = label,
        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12,
        fontWeight = FontWeight.SemiBold,
        color = colors.muted,
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
    val colors = LocalDuesColors.current
    Surface(
        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
        color = colors.card,
        border = BorderStroke(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1, colors.border),
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
                cursorBrush = SolidColor(colors.muted),
                textStyle = TextStyle(
                    color = colors.text,
                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isBlank()) {
                        Text(placeholder, color = colors.muted, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15)
                    }
                    inner()
                }
            )
        }
    }
}

@Composable
private fun GoalsEmptyState(modifier: Modifier = Modifier) {
    val colors = LocalDuesColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎯", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp40)
        Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
        Text("No goals yet", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp16, fontWeight = FontWeight.Bold, color = colors.text)
        Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4))
        Text(
            text = "Add your first goal and start building a streak.",
            modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp32),
            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13,
            color = colors.muted,
            textAlign = TextAlign.Center
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

private fun capitalizeFirstTypedCharacter(input: String): String {
    val firstVisibleIndex = input.indexOfFirst { !it.isWhitespace() }
    if (firstVisibleIndex == -1) return input
    val char = input[firstVisibleIndex]
    val upper = char.uppercaseChar()
    if (char == upper) return input
    return buildString(input.length) {
        append(input, 0, firstVisibleIndex)
        append(upper)
        append(input.substring(firstVisibleIndex + 1))
    }
}
