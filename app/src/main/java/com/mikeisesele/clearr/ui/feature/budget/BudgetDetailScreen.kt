package com.mikeisesele.clearr.ui.feature.budget

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetStatus
import com.mikeisesele.clearr.data.model.CategorySummary
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDS
import com.mikeisesele.clearr.ui.theme.fromToken
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun BudgetDetailScreen(
    trackerId: Long,
    onNavigateBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var loggingCategory by remember { mutableStateOf<CategorySummary?>(null) }
    var showAddCategory by remember { mutableStateOf(false) }

    if (state.trackerId != trackerId) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClearrColors.BrandBackground)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                BudgetSummarySection(
                    trackerName = state.trackerName,
                    summary = state.budgetSummary,
                    frequency = state.frequency,
                    periods = state.periods,
                    selectedPeriodId = state.selectedPeriodId,
                    onNavigateBack = onNavigateBack,
                    onFrequencyChange = { viewModel.onAction(BudgetAction.SetFrequency(it)) },
                    onPeriodSelect = { viewModel.onAction(BudgetAction.SelectPeriod(it)) }
                )
            }
            item { Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)) }
            item {
                BudgetCategoryList(
                    summaries = state.categorySummaries,
                    overBudgetNames = state.budgetSummary.overBudgetCategories.map { it.category.name },
                    onCategoryTap = { loggingCategory = it },
                    onAddCategory = { showAddCategory = true }
                )
            }
            item { Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp96)) }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20, bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24)
                .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp52),
            color = ClearrColors.Emerald,
            shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
            shadowElevation = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable {
                val defaultCategory = state.categorySummaries.firstOrNull { it.status != BudgetStatus.CLEARED }
                    ?: state.categorySummaries.firstOrNull()
                loggingCategory = defaultCategory
            }) {
                Text("+", color = ClearrColors.Surface, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp28, fontWeight = FontWeight.Bold)
            }
        }
    }

    loggingCategory?.let { category ->
        LogExpenseSheet(
            category = category,
            onDismiss = { loggingCategory = null },
            onSave = { amountNaira, note ->
                viewModel.onAction(BudgetAction.LogExpense(category.category.id, amountNaira, note))
                loggingCategory = null
            }
        )
    }

    if (showAddCategory) {
        AddCategorySheet(
            onDismiss = { showAddCategory = false },
            onAdd = { name, icon, token, amount ->
                viewModel.onAction(BudgetAction.AddCategory(name, icon, token, amount))
                showAddCategory = false
            }
        )
    }
}

@Composable
private fun BudgetSummarySection(
    trackerName: String,
    summary: com.mikeisesele.clearr.data.model.BudgetSummary,
    frequency: BudgetFrequency,
    periods: List<com.mikeisesele.clearr.data.model.BudgetPeriod>,
    selectedPeriodId: Long?,
    onNavigateBack: () -> Unit,
    onFrequencyChange: (BudgetFrequency) -> Unit,
    onPeriodSelect: (Long) -> Unit
) {
    val spacing = ClearrDS.spacing
    val subtitle = periods.firstOrNull { it.id == selectedPeriodId }?.label.orEmpty()
    Column(modifier = Modifier.fillMaxWidth().background(ClearrColors.Surface)) {
        ClearrTopBar(
            title = trackerName,
            subtitle = subtitle,
            leadingIcon = "←",
            onLeadingClick = onNavigateBack,
            actionIcon = "⋯",
            onActionClick = {},
            actionContainerColor = ClearrColors.NavBg
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.lg),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(color = ClearrColors.BrandBackground, shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp9)) {
                Row(modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)) {
                    listOf(BudgetFrequency.MONTHLY, BudgetFrequency.WEEKLY).forEach { mode ->
                        val selected = mode == frequency
                        Surface(
                            color = if (selected) ClearrColors.Surface else ClearrColors.Transparent,
                            shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp7),
                            shadowElevation = if (selected) com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2 else com.mikeisesele.clearr.ui.theme.ClearrDimens.dp0,
                            modifier = Modifier.clickable { onFrequencyChange(mode) }
                        ) {
                            Text(
                                text = if (mode == BudgetFrequency.MONTHLY) "Monthly" else "Weekly",
                                modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6),
                                color = if (selected) ClearrColors.BrandText else ClearrColors.TextMuted,
                                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(top = spacing.sm, start = spacing.lg, end = spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6)
        ) {
            items(periods, key = { it.id }) { period ->
                val selected = period.id == selectedPeriodId
                Surface(
                    color = if (selected) ClearrColors.Emerald else ClearrColors.BrandBackground,
                    shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20),
                    modifier = Modifier.clickable { onPeriodSelect(period.id) }
                ) {
                    Text(
                        period.label,
                        modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp5),
                        color = if (selected) ClearrColors.Surface else ClearrColors.TextMuted,
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Surface(
            color = ClearrColors.BrandBackground,
            shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
            modifier = Modifier
                .fillMaxWidth()
                .padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
            ) {
                DonutRingChart(spentKobo = summary.totalSpentKobo, plannedKobo = summary.totalPlannedKobo)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Spent", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12, color = ClearrColors.TextMuted)
                    Text(
                        text = formatKobo(summary.totalSpentKobo),
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp26,
                        fontWeight = FontWeight.Bold,
                        color = ClearrColors.BrandText
                    )
                    Text(
                        text = "of ${formatKobo(summary.totalPlannedKobo)} planned",
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12,
                        color = ClearrColors.TextMuted
                    )
                    Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
                    val over = summary.totalRemainingKobo < 0
                    Surface(
                        color = if (over) ClearrColors.CoralBg else ClearrColors.EmeraldSurface,
                        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20)
                    ) {
                        Text(
                            text = if (over) {
                                "${formatKobo(summary.totalRemainingKobo.absoluteValue)} over budget"
                            } else {
                                "${formatKobo(summary.totalRemainingKobo)} remaining"
                            },
                            modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4),
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp11,
                            fontWeight = FontWeight.SemiBold,
                            color = if (over) ClearrColors.Coral else ClearrColors.Emerald
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutRingChart(spentKobo: Long, plannedKobo: Long, modifier: Modifier = Modifier) {
    val pctRaw = if (plannedKobo > 0L) spentKobo.toFloat() / plannedKobo else 0f
    val pct = pctRaw.coerceAtMost(1f)
    val animatedPct by animateFloatAsState(targetValue = pct, label = "ring_pct")
    val color = when {
        pctRaw > 1f -> ClearrColors.Coral
        pctRaw == 1f -> ClearrColors.Emerald
        else -> ClearrColors.Emerald
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp72)) {
        Canvas(modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp72)) {
            val stroke = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6.toPx()
            val radius = (size.minDimension / 2f) - stroke
            drawCircle(
                color = ClearrColors.Border,
                radius = radius,
                style = Stroke(width = stroke)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedPct,
                useCenter = false,
                topLeft = Offset(stroke, stroke),
                size = Size(size.width - stroke * 2, size.height - stroke * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${(pctRaw.coerceAtMost(1f) * 100).roundToInt()}%", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp9, fontWeight = FontWeight.Bold, color = ClearrColors.TextSecondary)
            Text("used", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp7, color = ClearrColors.TextMuted)
        }
    }
}

@Composable
private fun BudgetCategoryList(
    summaries: List<CategorySummary>,
    overBudgetNames: List<String>,
    onCategoryTap: (CategorySummary) -> Unit,
    onAddCategory: () -> Unit
) {
    val spacing = ClearrDS.spacing
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.lg)) {
        AnimatedVisibility(
            visible = overBudgetNames.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(color = ClearrColors.CoralBg, shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "⚠ ${overBudgetNames.joinToString(", ")} ${if (overBudgetNames.size == 1) "is" else "are"} over budget",
                    modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10),
                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13,
                    color = ClearrColors.Coral,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10))
        Text("CATEGORIES", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13, color = ClearrColors.TextMuted, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))

        Surface(color = ClearrColors.Surface, shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12), modifier = Modifier.fillMaxWidth()) {
            Column {
                summaries.forEachIndexed { index, summary ->
                    BudgetCategoryRow(summary = summary, onClick = { onCategoryTap(summary) })
                    if (index < summaries.lastIndex) {
                        HorizontalDivider(color = ClearrColors.Border)
                    }
                }
            }
        }

        Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))

        Surface(
            color = ClearrColors.Surface,
            shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAddCategory() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)
            ) {
                Surface(color = ClearrColors.Emerald, shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp9), modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp36)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", color = ClearrColors.Surface, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp18, fontWeight = FontWeight.Bold)
                    }
                }
                Text("Add Category", color = ClearrColors.Emerald, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun BudgetCategoryRow(summary: CategorySummary, onClick: () -> Unit) {
    val pct = summary.percentUsed.coerceAtMost(1f)
    val animatedPct by animateFloatAsState(targetValue = pct, label = "row_pct")
    val token = ClearrColors.fromToken(summary.category.colorToken)
    val statusText = when (summary.status) {
        BudgetStatus.OVER_BUDGET -> "+${formatKobo(summary.remainingAmountKobo.absoluteValue)}"
        BudgetStatus.CLEARED -> "Cleared ✓"
        BudgetStatus.NEAR_LIMIT, BudgetStatus.ON_TRACK -> "${formatKobo(summary.remainingAmountKobo.coerceAtLeast(0L))} left"
    }
    val statusColor = when (summary.status) {
        BudgetStatus.OVER_BUDGET -> ClearrColors.Coral
        BudgetStatus.CLEARED -> ClearrColors.Emerald
        BudgetStatus.NEAR_LIMIT, BudgetStatus.ON_TRACK -> ClearrColors.TextMuted
    }
    val progressColor = when (summary.status) {
        BudgetStatus.OVER_BUDGET -> ClearrColors.Coral
        BudgetStatus.CLEARED -> ClearrColors.Emerald
        BudgetStatus.NEAR_LIMIT, BudgetStatus.ON_TRACK -> token.color
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)
    ) {
        Surface(color = token.background, shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp9), modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp36)) {
            Box(contentAlignment = Alignment.Center) {
                Text(summary.category.icon, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp17)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(summary.category.name, color = ClearrColors.BrandText, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15, fontWeight = FontWeight.Medium)
                Text(statusText, color = statusColor, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp5))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4)
                        .background(ClearrColors.Border, RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp99))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedPct)
                            .height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4)
                            .background(progressColor, RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp99))
                    )
                }
                Text(formatKobo(summary.spentAmountKobo), fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp11, color = ClearrColors.TextMuted, fontWeight = FontWeight.Medium)
            }
        }
        Text("›", color = ClearrColors.Border, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogExpenseSheet(
    category: CategorySummary,
    onDismiss: () -> Unit,
    onSave: (amountNaira: Double, note: String?) -> Unit
) {
    var amount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    val amountNaira = amount.toDoubleOrNull() ?: 0.0

    BackHandler(onBack = onDismiss)
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ClearrColors.Surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Text("Log Expense", fontWeight = FontWeight.SemiBold, color = ClearrColors.BrandText)
                TextButton(onClick = { if (amountNaira > 0.0) onSave(amountNaira, note) }, enabled = amountNaira > 0.0) {
                    Text("Add", color = if (amountNaira > 0.0) ClearrColors.Emerald else ClearrColors.TextMuted)
                }
            }
            val token = ClearrColors.fromToken(category.category.colorToken)
            Surface(color = token.background, shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10), modifier = Modifier.padding(top = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)) {
                Row(modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8), horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8), verticalAlignment = Alignment.CenterVertically) {
                    Text(category.category.icon, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp18)
                    Column {
                        Text(category.category.name, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13, fontWeight = FontWeight.SemiBold, color = ClearrColors.BrandText)
                        Text(
                            if (category.remainingAmountKobo >= 0) "${formatKobo(category.remainingAmountKobo)} remaining" else "${formatKobo(category.remainingAmountKobo.absoluteValue)} over",
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp11,
                            color = ClearrColors.TextMuted
                        )
                    }
                }
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("₦", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp32, color = if (amount.isBlank()) ClearrColors.Border else ClearrColors.BrandText)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { ch -> ch.isDigit() } },
                    modifier = Modifier.width(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp210),
                    textStyle = MaterialTheme.typography.headlineLarge.copy(textAlign = TextAlign.Center, color = ClearrColors.BrandText),
                    placeholder = { Text("0", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    singleLine = true
                )
            }
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add a note (optional)") },
                singleLine = true
            )
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24))
        }
    }
}

private data class CategoryPreset(
    val name: String,
    val icon: String,
    val colorToken: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCategorySheet(
    onDismiss: () -> Unit,
    onAdd: (name: String, icon: String, colorToken: String, plannedAmountNaira: Double) -> Unit
) {
    var step by rememberSaveable { mutableStateOf(0) }
    var selected by remember { mutableStateOf(categoryPresets.first()) }
    var name by rememberSaveable { mutableStateOf("") }
    var plannedAmount by rememberSaveable { mutableStateOf("") }

    BackHandler(onBack = onDismiss)
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ClearrColors.Surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { if (step == 0) onDismiss() else step = 0 }) {
                    Text(if (step == 0) "Cancel" else "Back")
                }
                Text(if (step == 0) "New Category" else "Configure", fontWeight = FontWeight.SemiBold)
                TextButton(
                    enabled = step == 1 && name.isNotBlank() && (plannedAmount.toDoubleOrNull() ?: 0.0) > 0.0,
                    onClick = {
                        onAdd(name.trim(), selected.icon, selected.colorToken, plannedAmount.toDouble())
                    }
                ) {
                    Text("Add")
                }
            }

            if (step == 0) {
                Text("SELECT PRESET", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13, color = ClearrColors.TextMuted, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4, top = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6, bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
                Surface(color = ClearrColors.Surface, shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12), modifier = Modifier.fillMaxWidth()) {
                    Column {
                        categoryPresets.forEachIndexed { index, preset ->
                            val token = ClearrColors.fromToken(preset.colorToken)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selected = preset
                                        name = if (preset.name == "Custom") "" else preset.name
                                        step = 1
                                    }
                                    .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp13),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)
                            ) {
                                Surface(color = token.background, shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8), modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp34)) {
                                    Box(contentAlignment = Alignment.Center) { Text(preset.icon, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp17) }
                                }
                                Text(preset.name, modifier = Modifier.weight(1f), fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15, fontWeight = FontWeight.Medium, color = ClearrColors.BrandText)
                                Text("›", color = ClearrColors.Border)
                            }
                            if (index < categoryPresets.lastIndex) HorizontalDivider(color = ClearrColors.Border)
                        }
                    }
                }
            } else {
                val token = ClearrColors.fromToken(selected.colorToken)
                Surface(color = token.background, shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12), modifier = Modifier.fillMaxWidth().padding(top = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6)) {
                    Row(modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)) {
                        Text(selected.icon, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp24)
                        Column {
                            Text(name.ifBlank { "Category name" }, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15, fontWeight = FontWeight.SemiBold, color = ClearrColors.BrandText)
                            Text(
                                if ((plannedAmount.toDoubleOrNull() ?: 0.0) > 0.0) formatKobo(((plannedAmount.toDoubleOrNull() ?: 0.0) * 100).toLong()) else "Set a budget",
                                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12,
                                color = ClearrColors.TextMuted
                            )
                        }
                    }
                }
                Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10))
                OutlinedTextField(
                    value = plannedAmount,
                    onValueChange = { plannedAmount = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Planned Amount (₦)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24))
        }
    }
}

private fun formatKobo(kobo: Long): String {
    val naira = kobo / 100
    return "₦" + "%,d".format(naira)
}

private val categoryPresets = listOf(
    CategoryPreset("Housing", "🏠", "Violet"),
    CategoryPreset("Food", "🍔", "Orange"),
    CategoryPreset("Transport", "🚗", "Blue"),
    CategoryPreset("Health", "💊", "Teal"),
    CategoryPreset("Savings", "💰", "Amber"),
    CategoryPreset("Entertainment", "🎬", "Purple"),
    CategoryPreset("Utilities", "💡", "Violet"),
    CategoryPreset("Shopping", "🛍", "Orange"),
    CategoryPreset("Education", "📚", "Blue"),
    CategoryPreset("Custom", "✦", "Teal")
)
