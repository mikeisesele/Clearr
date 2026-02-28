package com.mikeisesele.clearr.ui.feature.budget.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.data.model.BudgetStatus
import com.mikeisesele.clearr.data.model.CategorySummary
import com.mikeisesele.clearr.ui.feature.budget.previews.previewBudgetSummaries
import com.mikeisesele.clearr.ui.feature.budget.utils.formatKobo
import com.mikeisesele.clearr.ui.feature.budget.utils.formatKoboFull
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import com.mikeisesele.clearr.ui.theme.fromToken
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@Composable
internal fun BudgetCategoryTable(
    summaries: List<CategorySummary>,
    overBudgetNames: List<String>,
    showSwipeHint: Boolean,
    onCategoryTap: (CategorySummary) -> Unit,
    onCategoryDelete: (Long) -> Unit,
    onSwipeHintDisplayed: () -> Unit,
    colors: DuesColors
) {
    var hintedCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(summaries, showSwipeHint) {
        if (showSwipeHint && summaries.isNotEmpty()) {
            hintedCategoryId = summaries.take(4).random().category.id
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bg)
            .clip(RoundedCornerShape(topStart = ClearrDimens.dp24, topEnd = ClearrDimens.dp24))
            .padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp12)
    ) {
        Box(
            modifier = Modifier
                .width(ClearrDimens.dp36)
                .height(ClearrDimens.dp4)
                .background(colors.border, RoundedCornerShape(ClearrDimens.dp99))
                .align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(ClearrDimens.dp8))

        if (overBudgetNames.isNotEmpty()) {
            Surface(color = colors.red.copy(alpha = if (colors.isDark) 0.20f else 0.12f), shape = RoundedCornerShape(ClearrDimens.dp10), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "⚠ ${overBudgetNames.joinToString(", ")} ${if (overBudgetNames.size == 1) "is" else "are"} over budget",
                    modifier = Modifier.padding(horizontal = ClearrDimens.dp14, vertical = ClearrDimens.dp10),
                    fontSize = ClearrTextSizes.sp13,
                    color = colors.red
                )
            }
            Spacer(Modifier.height(ClearrDimens.dp12))
        }

        Column(verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp10), modifier = Modifier.fillMaxWidth()) {
            summaries.forEach { summary ->
                key(summary.category.id) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp10)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            SwipeableBudgetCategoryRow(
                                summary = summary,
                                colors = colors,
                                onDelete = { onCategoryDelete(summary.category.id) },
                                shouldHintSwipe = showSwipeHint && summary.category.id == hintedCategoryId,
                                onSwipeHintFinished = {
                                    hintedCategoryId = null
                                    onSwipeHintDisplayed()
                                }
                            )
                        }
                        Surface(
                            modifier = Modifier.size(56.dp).clickable { onCategoryTap(summary) },
                            shape = RoundedCornerShape(ClearrDimens.dp16),
                            color = ClearrColors.fromToken(summary.category.colorToken).background
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "+",
                                    fontSize = ClearrTextSizes.sp22,
                                    color = when (summary.status) {
                                        BudgetStatus.OVER_BUDGET -> colors.red
                                        BudgetStatus.CLEARED -> colors.green
                                        else -> ClearrColors.fromToken(summary.category.colorToken).color
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(ClearrDimens.dp12))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwipeableBudgetCategoryRow(
    summary: CategorySummary,
    colors: DuesColors,
    onDelete: () -> Unit,
    shouldHintSwipe: Boolean,
    onSwipeHintFinished: () -> Unit
) {
    val cardShape = RoundedCornerShape(ClearrDimens.dp10)
    val hintOffset = remember { Animatable(0f) }
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.35f },
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onDelete()
            false
        }
    )

    LaunchedEffect(shouldHintSwipe) {
        if (shouldHintSwipe) {
            delay(300)
            hintOffset.animateTo(-24f, animationSpec = tween(durationMillis = 220))
            hintOffset.animateTo(0f, animationSpec = tween(durationMillis = 280))
            onSwipeHintFinished()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize().clip(cardShape).background(colors.red),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    "Delete",
                    modifier = Modifier.padding(end = ClearrDimens.dp16),
                    color = Color.White,
                    fontSize = ClearrTextSizes.sp13
                )
            }
        }
    ) {
        Surface(
            color = colors.surface,
            shape = cardShape,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth().graphicsLayer { translationX = hintOffset.value }
        ) {
            BudgetCategoryRow(summary = summary, colors = colors)
        }
    }
}

@Composable
internal fun BudgetCategoryRow(
    summary: CategorySummary,
    colors: DuesColors
) {
    val pct = summary.percentUsed.coerceAtMost(1f)
    val animPct by animateFloatAsState(targetValue = pct, label = "row_pct")
    val token = ClearrColors.fromToken(summary.category.colorToken)
    val barColor = when (summary.status) {
        BudgetStatus.OVER_BUDGET -> colors.red
        BudgetStatus.CLEARED -> colors.green
        else -> token.color
    }
    val glow = when (summary.status) {
        BudgetStatus.OVER_BUDGET -> colors.red.copy(alpha = 0.35f)
        BudgetStatus.CLEARED -> colors.green.copy(alpha = 0.35f)
        else -> token.color.copy(alpha = 0.30f)
    }
    val leftText = when (summary.status) {
        BudgetStatus.OVER_BUDGET -> "over ${formatKobo(summary.remainingAmountKobo.absoluteValue)}"
        BudgetStatus.CLEARED -> "cleared ✓"
        else -> "${formatKobo(summary.remainingAmountKobo.coerceAtLeast(0L))} left"
    }
    val leftColor = when (summary.status) {
        BudgetStatus.OVER_BUDGET -> colors.red
        BudgetStatus.CLEARED -> colors.green
        else -> colors.muted
    }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = ClearrDimens.dp12, vertical = ClearrDimens.dp11)
    ) {
        Row(
            modifier = Modifier.clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)
        ) {
            Text(summary.category.icon, fontSize = ClearrTextSizes.sp18, modifier = Modifier.width(ClearrDimens.dp32))
            Text(summary.category.name, fontSize = ClearrTextSizes.sp14, color = colors.text, modifier = Modifier.weight(1f))
            Text(leftText, fontSize = ClearrTextSizes.sp12, color = leftColor)
            Text(if (expanded) "▲" else "▼", fontSize = ClearrTextSizes.sp9, color = colors.muted)
        }

        Spacer(Modifier.height(ClearrDimens.dp8))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = ClearrDimens.dp40)
                .height(ClearrDimens.dp5)
                .clip(RoundedCornerShape(ClearrDimens.dp99))
                .background(colors.border)
        ) {
            if (animPct > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animPct)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(ClearrDimens.dp99))
                        .background(barColor)
                        .drawBehind { drawRect(color = glow, blendMode = BlendMode.Screen) }
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = ClearrDimens.dp40, top = ClearrDimens.dp10),
                horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp20)
            ) {
                Column {
                    Text("SPENT", fontSize = ClearrTextSizes.sp9, color = colors.muted, letterSpacing = 0.5.sp)
                    Text(formatKoboFull(summary.spentAmountKobo), fontSize = ClearrTextSizes.sp13, color = barColor)
                }
                Column {
                    Text("PLANNED", fontSize = ClearrTextSizes.sp9, color = colors.muted, letterSpacing = 0.5.sp)
                    Text(formatKoboFull(summary.plannedAmountKobo), fontSize = ClearrTextSizes.sp13, color = colors.muted)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun BudgetCategoryTablePreview() {
    ClearrTheme {
        BudgetCategoryTable(
            summaries = previewBudgetSummaries,
            overBudgetNames = emptyList(),
            showSwipeHint = false,
            onCategoryTap = {},
            onCategoryDelete = {},
            onSwipeHintDisplayed = {},
            colors = LocalDuesColors.current
        )
    }
}
