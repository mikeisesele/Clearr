package com.mikeisesele.clearr.ui.feature.goals.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.data.model.GoalSummary
import com.mikeisesele.clearr.data.model.HistoryEntry
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
import com.mikeisesele.clearr.ui.feature.goals.previews.previewGoalSummary
import com.mikeisesele.clearr.ui.feature.goals.utils.goalPalette
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.ClearrUiColors
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
internal fun GoalsNavBar(title: String, onBack: (() -> Unit)? = null) {
    ClearrTopBar(
        title = title,
        showLeading = onBack != null,
        leadingIcon = "←",
        onLeadingClick = onBack,
        actionIcon = null,
        onActionClick = null,
        leadingContainerColor = Color.Transparent
    )
}

@Composable
internal fun AllClearedBanner(colors: ClearrUiColors) {
    Row(
        modifier = Modifier.fillMaxWidth().background(ClearrColors.EmeraldBg).padding(horizontal = ClearrDimens.dp20, vertical = ClearrDimens.dp12),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🎉", fontSize = ClearrTextSizes.sp18)
        Spacer(Modifier.width(ClearrDimens.dp8))
        Text("All goals cleared for today!", fontSize = ClearrTextSizes.sp14, fontWeight = FontWeight.Bold, color = ClearrColors.Emerald)
    }
}

@Composable
internal fun GoalsSwipeHintStrip(colors: ClearrUiColors) {
    Box(modifier = Modifier.fillMaxWidth().height(ClearrDimens.dp28).background(colors.bg), contentAlignment = Alignment.Center) {
        Text("Swipe left to delete", fontSize = ClearrTextSizes.sp11, color = colors.muted)
    }
}

@Composable
internal fun SwipeableGoalRow(
    summary: GoalSummary,
    isLast: Boolean,
    colors: ClearrUiColors,
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
            if (value == SwipeToDismissBoxValue.EndToStart) onDelete(summary.goal.id)
            false
        }
    )
    val doneThisPeriod = summary.isDoneThisPeriod
    val palette = goalPalette(summary.goal.colorToken)

    LaunchedEffect(hintDeleteAnimation) {
        if (hintDeleteAnimation && !hintShown) {
            hintShown = true
            hintOffset.animateTo(-maxHintOffsetPx, animationSpec = tween(280))
            hintOffset.animateTo(0f, animationSpec = tween(260))
            onHintAnimationPlayed()
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.matchParentSize().background(ClearrColors.BrandDanger).padding(horizontal = ClearrDimens.dp20),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ClearrColors.Surface, modifier = Modifier.size(ClearrDimens.dp22).alpha(hintAlpha))
        }

        SwipeToDismissBox(
            state = dismissState,
            modifier = Modifier.graphicsLayer { translationX = hintOffset.value },
            backgroundContent = {
                Box(
                    modifier = Modifier.fillMaxSize().background(ClearrColors.BrandDanger).padding(horizontal = ClearrDimens.dp20),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ClearrColors.Surface, modifier = Modifier.size(ClearrDimens.dp22))
                }
            },
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(colors.surface).alpha(if (doneThisPeriod) 0.6f else 1f)
                    .combinedClickable(onClick = { onTap(summary) }, onLongClick = { onLongPress(summary) })
                    .padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp14),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp13)
            ) {
                Surface(modifier = Modifier.size(ClearrDimens.dp42), shape = RoundedCornerShape(ClearrDimens.dp12), color = if (doneThisPeriod) ClearrColors.EmeraldBg else palette.background) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (doneThisPeriod) "✓" else summary.goal.emoji, fontSize = ClearrTextSizes.sp20, color = if (doneThisPeriod) ClearrColors.Emerald else Color.Unspecified)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(summary.goal.title, fontSize = ClearrTextSizes.sp15, fontWeight = FontWeight.SemiBold, color = if (doneThisPeriod) colors.muted else colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.width(ClearrDimens.dp6))
                            Surface(shape = RoundedCornerShape(ClearrDimens.dp10), color = colors.card) {
                                Text(
                                    text = if (summary.goal.frequency == GoalFrequency.DAILY) "Daily" else "Weekly",
                                    modifier = Modifier.padding(horizontal = ClearrDimens.dp7, vertical = ClearrDimens.dp1),
                                    fontSize = ClearrTextSizes.sp11,
                                    color = colors.muted
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥", fontSize = ClearrTextSizes.sp13)
                            Spacer(Modifier.width(ClearrDimens.dp3))
                            Text(summary.currentStreak.toString(), fontSize = ClearrTextSizes.sp13, fontWeight = FontWeight.Bold, color = if (summary.currentStreak > 0) ClearrColors.Amber else colors.muted)
                        }
                    }

                    Spacer(Modifier.height(ClearrDimens.dp4))
                    Text(summary.goal.target ?: "No target set", fontSize = ClearrTextSizes.sp12, color = colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(ClearrDimens.dp6))
                    HistoryDots(summary.recentHistory, palette.color)
                }
            }
        }

        if (!isLast) HorizontalDivider(color = colors.border, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
internal fun HistoryDots(history: List<HistoryEntry>, color: Color) {
    val colors = LocalClearrUiColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp3), verticalAlignment = Alignment.CenterVertically) {
        history.forEach { entry ->
            val dotColor by animateColorAsState(targetValue = if (entry.isDone) color else colors.border, label = "goal_history_dot")
            Box(modifier = Modifier.size(ClearrDimens.dp7).background(dotColor, CircleShape))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoalDetailSheet(
    summary: GoalSummary,
    colors: ClearrUiColors,
    onDismiss: () -> Unit,
    onMarkDone: (String) -> Unit
) {
    val doneThisPeriod = summary.isDoneThisPeriod
    val palette = goalPalette(summary.goal.colorToken)
    val completionPct = (summary.completionRate * 100f).roundToInt().coerceIn(0, 100)
    val historyTitle = if (summary.goal.frequency == GoalFrequency.DAILY) "LAST 7 DAYS" else "LAST 7 WEEKS"

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)).padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp24),
            contentAlignment = Alignment.Center
        ) {
            Surface(modifier = Modifier.fillMaxWidth().heightIn(max = 640.dp), shape = RoundedCornerShape(ClearrDimens.dp20), color = colors.surface) {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = ClearrDimens.dp20, vertical = ClearrDimens.dp8)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(ClearrDimens.dp40))
                        Text("Goal Detail", fontSize = ClearrTextSizes.sp16, fontWeight = FontWeight.SemiBold, color = colors.text)
                        TextButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.muted) }
                    }

                    Spacer(Modifier.height(ClearrDimens.dp8))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp14)) {
                        Surface(modifier = Modifier.size(ClearrDimens.dp52), shape = RoundedCornerShape(ClearrDimens.dp14), color = if (doneThisPeriod) ClearrColors.EmeraldBg else palette.background) {
                            Box(contentAlignment = Alignment.Center) { Text(summary.goal.emoji, fontSize = ClearrTextSizes.sp24) }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(summary.goal.title, fontSize = ClearrTextSizes.sp18, fontWeight = FontWeight.Bold, color = colors.text)
                            Text("${summary.goal.target ?: "No target"} · ${if (summary.goal.frequency == GoalFrequency.DAILY) "Daily" else "Weekly"}", fontSize = ClearrTextSizes.sp13, color = colors.muted)
                        }
                    }

                    Spacer(Modifier.height(ClearrDimens.dp20))
                    Row(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp10), modifier = Modifier.fillMaxWidth()) {
                        GoalStatTile("Streak", "${summary.currentStreak}🔥", if (summary.currentStreak > 0) ClearrColors.Amber else colors.muted, if (summary.currentStreak > 0) ClearrColors.AmberBg else colors.card, Modifier.weight(1f))
                        GoalStatTile("Best", "${summary.bestStreak}🏆", ClearrColors.Violet, ClearrColors.VioletBg, Modifier.weight(1f))
                        val (rateFg, rateBg) = when {
                            completionPct >= 70 -> ClearrColors.Emerald to ClearrColors.EmeraldBg
                            completionPct >= 40 -> ClearrColors.Amber to ClearrColors.AmberBg
                            else -> ClearrColors.Coral to ClearrColors.CoralBg
                        }
                        GoalStatTile("Rate", "$completionPct%", rateFg, rateBg, Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(ClearrDimens.dp20))
                    Text(historyTitle, fontSize = ClearrTextSizes.sp12, fontWeight = FontWeight.SemiBold, color = colors.muted)
                    Spacer(Modifier.height(ClearrDimens.dp10))
                    Row(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp6), modifier = Modifier.fillMaxWidth()) {
                        summary.recentHistory.forEach { entry ->
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(modifier = Modifier.fillMaxWidth().height(ClearrDimens.dp36), shape = RoundedCornerShape(ClearrDimens.dp8), color = if (entry.isDone) palette.color else colors.card) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(if (entry.isDone) "✓" else "—", fontSize = if (entry.isDone) ClearrTextSizes.sp14 else ClearrTextSizes.sp11, color = if (entry.isDone) ClearrColors.Surface else ClearrColors.Border)
                                    }
                                }
                                Spacer(Modifier.height(ClearrDimens.dp4))
                                Text(entry.label, fontSize = ClearrTextSizes.sp9, color = colors.muted)
                            }
                        }
                    }

                    Spacer(Modifier.height(ClearrDimens.dp24))
                    if (!doneThisPeriod) {
                        Surface(modifier = Modifier.fillMaxWidth().clickable { onMarkDone(summary.goal.id) }, shape = RoundedCornerShape(ClearrDimens.dp14), color = palette.color, shadowElevation = ClearrDimens.dp8) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = ClearrDimens.dp15)) {
                                Text("Mark as Done Today ✓", color = ClearrColors.Surface, fontSize = ClearrTextSizes.sp15, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    } else {
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(ClearrDimens.dp14), color = ClearrColors.EmeraldBg) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = ClearrDimens.dp14)) {
                                Text("✓ Completed for this period", color = ClearrColors.Emerald, fontSize = ClearrTextSizes.sp14, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(ClearrDimens.dp8))
                }
            }
        }
    }
}

@Composable
internal fun GoalStatTile(label: String, value: String, fg: Color, bg: Color, modifier: Modifier = Modifier) {
    val colors = LocalClearrUiColors.current
    Surface(modifier = modifier, shape = RoundedCornerShape(ClearrDimens.dp12), color = bg) {
        Column(modifier = Modifier.padding(horizontal = ClearrDimens.dp8, vertical = ClearrDimens.dp12), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = ClearrTextSizes.sp18, fontWeight = FontWeight.ExtraBold, color = fg)
            Spacer(Modifier.height(ClearrDimens.dp2))
            Text(label, fontSize = ClearrTextSizes.sp11, fontWeight = FontWeight.SemiBold, color = colors.muted)
        }
    }
}

@Composable
internal fun GoalsEmptyState(modifier: Modifier = Modifier) {
    val colors = LocalClearrUiColors.current
    Column(modifier = modifier.fillMaxWidth().background(colors.bg), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🎯", fontSize = ClearrTextSizes.sp40)
        Spacer(Modifier.height(ClearrDimens.dp12))
        Text("No goals yet", fontSize = ClearrTextSizes.sp16, fontWeight = FontWeight.Bold, color = colors.text)
        Spacer(Modifier.height(ClearrDimens.dp4))
        Text(
            text = "Add your first goal and start building a streak.",
            modifier = Modifier.padding(horizontal = ClearrDimens.dp32),
            fontSize = ClearrTextSizes.sp13,
            color = colors.muted,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun GoalRowPreview() {
    ClearrTheme {
        SwipeableGoalRow(
            summary = previewGoalSummary,
            isLast = true,
            colors = LocalClearrUiColors.current,
            hintDeleteAnimation = false,
            onHintAnimationPlayed = {},
            onDelete = {},
            onTap = {},
            onLongPress = {}
        )
    }
}
