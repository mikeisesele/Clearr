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
            item { Spacer(Modifier.height(12.dp)) }
            item {
                BudgetCategoryList(
                    summaries = state.categorySummaries,
                    overBudgetNames = state.budgetSummary.overBudgetCategories.map { it.category.name },
                    onCategoryTap = { loggingCategory = it },
                    onAddCategory = { showAddCategory = true }
                )
            }
            item { Spacer(Modifier.height(96.dp)) }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .size(52.dp),
            color = ClearrColors.Emerald,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 10.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable {
                val defaultCategory = state.categorySummaries.firstOrNull { it.status != BudgetStatus.CLEARED }
                    ?: state.categorySummaries.firstOrNull()
                loggingCategory = defaultCategory
            }) {
                Text("+", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
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
    Column(modifier = Modifier.fillMaxWidth().background(ClearrColors.Surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg, vertical = spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = ClearrColors.BrandBackground,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable { onNavigateBack() }) {
                    Text("←", color = ClearrColors.BrandText, fontSize = 15.sp)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(trackerName, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = ClearrColors.BrandText)
                Text(periods.firstOrNull { it.id == selectedPeriodId }?.label.orEmpty(), fontSize = 12.sp, color = ClearrColors.TextMuted)
            }
            Surface(
                color = ClearrColors.BrandBackground,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("⋯", color = ClearrColors.TextSecondary, fontSize = 18.sp)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.lg),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(color = ClearrColors.BrandBackground, shape = RoundedCornerShape(9.dp)) {
                Row(modifier = Modifier.padding(2.dp)) {
                    listOf(BudgetFrequency.MONTHLY, BudgetFrequency.WEEKLY).forEach { mode ->
                        val selected = mode == frequency
                        Surface(
                            color = if (selected) ClearrColors.Surface else Color.Transparent,
                            shape = RoundedCornerShape(7.dp),
                            shadowElevation = if (selected) 2.dp else 0.dp,
                            modifier = Modifier.clickable { onFrequencyChange(mode) }
                        ) {
                            Text(
                                text = if (mode == BudgetFrequency.MONTHLY) "Monthly" else "Weekly",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                                color = if (selected) ClearrColors.BrandText else ClearrColors.TextMuted,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(top = spacing.sm, start = spacing.lg, end = spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(periods, key = { it.id }) { period ->
                val selected = period.id == selectedPeriodId
                Surface(
                    color = if (selected) ClearrColors.Emerald else ClearrColors.BrandBackground,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable { onPeriodSelect(period.id) }
                ) {
                    Text(
                        period.label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        color = if (selected) Color.White else ClearrColors.TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Surface(
            color = ClearrColors.BrandBackground,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DonutRingChart(spentKobo = summary.totalSpentKobo, plannedKobo = summary.totalPlannedKobo)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Spent", fontSize = 12.sp, color = ClearrColors.TextMuted)
                    Text(
                        text = formatKobo(summary.totalSpentKobo),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClearrColors.BrandText
                    )
                    Text(
                        text = "of ${formatKobo(summary.totalPlannedKobo)} planned",
                        fontSize = 12.sp,
                        color = ClearrColors.TextMuted
                    )
                    Spacer(Modifier.height(8.dp))
                    val over = summary.totalRemainingKobo < 0
                    Surface(
                        color = if (over) ClearrColors.CoralBg else ClearrColors.EmeraldSurface,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = if (over) {
                                "${formatKobo(summary.totalRemainingKobo.absoluteValue)} over budget"
                            } else {
                                "${formatKobo(summary.totalRemainingKobo)} remaining"
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
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

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(72.dp)) {
        Canvas(modifier = Modifier.size(72.dp)) {
            val stroke = 6.dp.toPx()
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
            Text("${(pctRaw.coerceAtMost(1f) * 100).roundToInt()}%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ClearrColors.TextSecondary)
            Text("used", fontSize = 7.sp, color = ClearrColors.TextMuted)
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
            Surface(color = ClearrColors.CoralBg, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "⚠ ${overBudgetNames.joinToString(", ")} ${if (overBudgetNames.size == 1) "is" else "are"} over budget",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    fontSize = 13.sp,
                    color = ClearrColors.Coral,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Text("CATEGORIES", fontSize = 13.sp, color = ClearrColors.TextMuted, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        Surface(color = ClearrColors.Surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column {
                summaries.forEachIndexed { index, summary ->
                    BudgetCategoryRow(summary = summary, onClick = { onCategoryTap(summary) })
                    if (index < summaries.lastIndex) {
                        HorizontalDivider(color = ClearrColors.Border)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Surface(
            color = ClearrColors.Surface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAddCategory() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(color = ClearrColors.Emerald, shape = RoundedCornerShape(9.dp), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text("Add Category", color = ClearrColors.Emerald, fontSize = 15.sp, fontWeight = FontWeight.Medium)
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(color = token.background, shape = RoundedCornerShape(9.dp), modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(summary.category.icon, fontSize = 17.sp)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(summary.category.name, color = ClearrColors.BrandText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(statusText, color = statusColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(ClearrColors.Border, RoundedCornerShape(99.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedPct)
                            .height(4.dp)
                            .background(progressColor, RoundedCornerShape(99.dp))
                    )
                }
                Text(formatKobo(summary.spentAmountKobo), fontSize = 11.sp, color = ClearrColors.TextMuted, fontWeight = FontWeight.Medium)
            }
        }
        Text("›", color = ClearrColors.Border, fontSize = 14.sp)
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
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Text("Log Expense", fontWeight = FontWeight.SemiBold, color = ClearrColors.BrandText)
                TextButton(onClick = { if (amountNaira > 0.0) onSave(amountNaira, note) }, enabled = amountNaira > 0.0) {
                    Text("Add", color = if (amountNaira > 0.0) ClearrColors.Emerald else ClearrColors.TextMuted)
                }
            }
            val token = ClearrColors.fromToken(category.category.colorToken)
            Surface(color = token.background, shape = RoundedCornerShape(10.dp), modifier = Modifier.padding(top = 8.dp)) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(category.category.icon, fontSize = 18.sp)
                    Column {
                        Text(category.category.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ClearrColors.BrandText)
                        Text(
                            if (category.remainingAmountKobo >= 0) "${formatKobo(category.remainingAmountKobo)} remaining" else "${formatKobo(category.remainingAmountKobo.absoluteValue)} over",
                            fontSize = 11.sp,
                            color = ClearrColors.TextMuted
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("₦", fontSize = 32.sp, color = if (amount.isBlank()) ClearrColors.Border else ClearrColors.BrandText)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { ch -> ch.isDigit() } },
                    modifier = Modifier.width(210.dp),
                    textStyle = MaterialTheme.typography.headlineLarge.copy(textAlign = TextAlign.Center, color = ClearrColors.BrandText),
                    placeholder = { Text("0", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    singleLine = true
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add a note (optional)") },
                singleLine = true
            )
            Spacer(Modifier.height(24.dp))
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
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
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
                Text("SELECT PRESET", fontSize = 13.sp, color = ClearrColors.TextMuted, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 8.dp))
                Surface(color = ClearrColors.Surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
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
                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(color = token.background, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(34.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Text(preset.icon, fontSize = 17.sp) }
                                }
                                Text(preset.name, modifier = Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = ClearrColors.BrandText)
                                Text("›", color = ClearrColors.Border)
                            }
                            if (index < categoryPresets.lastIndex) HorizontalDivider(color = ClearrColors.Border)
                        }
                    }
                }
            } else {
                val token = ClearrColors.fromToken(selected.colorToken)
                Surface(color = token.background, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(selected.icon, fontSize = 24.sp)
                        Column {
                            Text(name.ifBlank { "Category name" }, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = ClearrColors.BrandText)
                            Text(
                                if ((plannedAmount.toDoubleOrNull() ?: 0.0) > 0.0) formatKobo(((plannedAmount.toDoubleOrNull() ?: 0.0) * 100).toLong()) else "Set a budget",
                                fontSize = 12.sp,
                                color = ClearrColors.TextMuted
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = plannedAmount,
                    onValueChange = { plannedAmount = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Planned Amount (₦)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))
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
