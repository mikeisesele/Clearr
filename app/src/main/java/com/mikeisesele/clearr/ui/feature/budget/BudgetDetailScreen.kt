package com.mikeisesele.clearr.ui.feature.budget

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetPeriod
import com.mikeisesele.clearr.data.model.BudgetStatus
import com.mikeisesele.clearr.data.model.BudgetSummary
import com.mikeisesele.clearr.data.model.CategorySummary
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import com.mikeisesele.clearr.ui.theme.fromToken
import kotlin.math.absoluteValue

// ── SCREEN ENTRY POINT ────────────────────────────────────────────────────────
// No change to function signature or ViewModel wiring.
@Composable
fun BudgetDetailScreen(
    trackerId: Long,
    onNavigateBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalDuesColors.current

    // CHANGED: loggingCategory is now nullable to support "no pre-selection" mode
    // Previously always required a CategorySummary. Now null = sheet opens with chip picker.
    var loggingCategory by remember { mutableStateOf<CategorySummary?>(null) }
    var showLogSheet by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }

    if (state.trackerId != trackerId) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ClearrTopBar(
                title = state.trackerName,
                leadingIcon = "←",
                onLeadingClick = onNavigateBack,
                actionIcon = null,
                onActionClick = null,
                leadingContainerColor = ClearrColors.Transparent
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // ── HERO SECTION ───────────────────────────────────────────────
                item {
                    BudgetHeroSection(
                        summary = state.budgetSummary,
                        frequency = state.frequency,
                        periods = state.periods,
                        selectedPeriodId = state.selectedPeriodId,
                        onFrequencyChange = { viewModel.onAction(BudgetAction.SetFrequency(it)) },
                        onPeriodSelect = { viewModel.onAction(BudgetAction.SelectPeriod(it)) },
                        colors = colors
                    )
                }
                // ── CATEGORY TABLE SECTION ─────────────────────────────────────
                item {
                    BudgetCategoryTable(
                        summaries = state.categorySummaries,
                        overBudgetNames = state.budgetSummary.overBudgetCategories.map { it.category.name },
                        aiInsight = state.aiInsight,
                        onCategoryTap = { cat ->
                            loggingCategory = cat
                            showLogSheet = true
                        },
                        onLogExpenseTap = {
                            loggingCategory = null  // No pre-selection
                            showLogSheet = true
                        },
                        colors = colors
                    )
                }
                item { Spacer(Modifier.height(ClearrDimens.dp96)) }
                item { Spacer(Modifier.navigationBarsPadding()) }
            }
        }

        // ── FAB: Add Category (Violet, bottom-end) ────────────────────────────
        // CHANGED: FAB now triggers Add Category, not Log Expense.
        // Log Expense is accessible via rows and inline CTA.
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = ClearrDimens.dp20, bottom = ClearrDimens.dp24)
                .size(ClearrDimens.dp52),
            color = ClearrColors.Violet,
            shape = RoundedCornerShape(ClearrDimens.dp16),
            shadowElevation = ClearrDimens.dp10
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.clickable { showAddCategory = true }
            ) {
                Text(
                    "⊞",
                    color = ClearrColors.Surface,
                    fontSize = ClearrTextSizes.sp22,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // ── LOG EXPENSE SHEET ─────────────────────────────────────────────────────
    // CHANGED: preselectedCategory replaces required category param.
    // Sheet now shows category chips for selection when preselectedCategory is null.
    if (showLogSheet) {
        LogExpenseSheet(
            allCategories = state.categorySummaries,
            preselectedCategory = loggingCategory,
            onDismiss = {
                showLogSheet = false
                loggingCategory = null
            },
            onSave = { category, amountNaira, note ->
                viewModel.onAction(BudgetAction.LogExpense(category.category.id, amountNaira, note))
                showLogSheet = false
                loggingCategory = null
            }
        )
    }

    // ── ADD CATEGORY SHEET ────────────────────────────────────────────────────
    // No change to AddCategorySheet contract.
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

// ── HERO SECTION ──────────────────────────────────────────────────────────────
// NEW composable replacing BudgetSummarySection.
// Shows: remaining/over balance, health meter, period chips.
// Removed: frequency toggle (still wired if needed, pass onFrequencyChange).
@Composable
private fun BudgetHeroSection(
    summary: BudgetSummary,
    frequency: BudgetFrequency,
    periods: List<BudgetPeriod>,
    selectedPeriodId: Long?,
    onFrequencyChange: (BudgetFrequency) -> Unit,
    onPeriodSelect: (Long) -> Unit,
    colors: DuesColors
) {
    val over = summary.totalRemainingKobo < 0
    val balanceColor = if (over) colors.red else colors.green

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bg)
            .padding(horizontal = ClearrDimens.dp20, vertical = ClearrDimens.dp20)
    ) {
        // Big balance number
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (over) "Over Budget" else "Remaining",
                fontSize = ClearrTextSizes.sp11,
                color = colors.muted,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(ClearrDimens.dp6))
            Text(
                text = formatKobo(summary.totalRemainingKobo.absoluteValue),
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                color = balanceColor,
                letterSpacing = (-1).sp
            )
            Spacer(Modifier.height(ClearrDimens.dp4))
            Text(
                text = "${formatKobo(summary.totalSpentKobo)} spent of ${formatKobo(summary.totalPlannedKobo)}",
                fontSize = ClearrTextSizes.sp13,
                color = colors.muted
            )
        }

        Spacer(Modifier.height(ClearrDimens.dp20))

        // Health meter
        BudgetHealthMeter(
            summary = summary,
            colors = colors
        )

        Spacer(Modifier.height(ClearrDimens.dp16))

        // Period chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp6)
        ) {
            items(periods, key = { it.id }) { period ->
                val selected = period.id == selectedPeriodId
                Surface(
                    color = if (selected) colors.accent else colors.surface,
                    shape = RoundedCornerShape(ClearrDimens.dp99),
                    modifier = Modifier.clickable { onPeriodSelect(period.id) }
                ) {
                    Text(
                        text = period.label,
                        modifier = Modifier.padding(
                            horizontal = ClearrDimens.dp14,
                            vertical = ClearrDimens.dp5
                        ),
                        fontSize = ClearrTextSizes.sp12,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) ClearrColors.Surface else colors.text
                    )
                }
            }
        }
    }
}

// ── HEALTH METER ──────────────────────────────────────────────────────────────
@Composable
private fun BudgetHealthMeter(
    summary: BudgetSummary,
    colors: DuesColors
) {
    val pct = (summary.totalSpentKobo.toFloat() / summary.totalPlannedKobo).coerceIn(0f, 1f)
    val animPct by animateFloatAsState(targetValue = pct, label = "health_meter")
    val over = summary.totalRemainingKobo < 0
    val healthLabel = when {
        over -> "Overspent"
        pct > 0.85f -> "Tight"
        else -> "Healthy"
    }
    val healthColor = when {
        over -> colors.red
        pct > 0.85f -> colors.amber
        else -> colors.green
    }

    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(ClearrDimens.dp12)
    ) {
        Column(modifier = Modifier.padding(ClearrDimens.dp14)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Budget health", fontSize = ClearrTextSizes.sp12, color = colors.muted)
                Text(healthLabel, fontSize = ClearrTextSizes.sp12, fontWeight = FontWeight.Bold, color = healthColor)
            }
            Spacer(Modifier.height(ClearrDimens.dp8))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ClearrDimens.dp6)
                    .clip(RoundedCornerShape(ClearrDimens.dp99))
                    .background(colors.border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animPct)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(ClearrDimens.dp99))
                        .background(healthColor)
                )
            }
            Spacer(Modifier.height(ClearrDimens.dp6))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${formatKobo(summary.totalSpentKobo)} spent",
                    fontSize = ClearrTextSizes.sp11,
                    color = colors.muted
                )
                Text(
                    "${formatKobo(summary.totalPlannedKobo)} planned",
                    fontSize = ClearrTextSizes.sp11,
                    color = colors.muted
                )
            }
        }
    }
}

// ── CATEGORY TABLE (light section) ───────────────────────────────────────────
// NEW composable replacing BudgetCategoryList.
// Uses Maya Chen's compact grid layout: icon · name+bar · spent · left
@Composable
private fun BudgetCategoryTable(
    summaries: List<CategorySummary>,
    overBudgetNames: List<String>,
    aiInsight: String?,
    onCategoryTap: (CategorySummary) -> Unit,
    onLogExpenseTap: () -> Unit,
    colors: DuesColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bg)
            .clip(RoundedCornerShape(topStart = ClearrDimens.dp24, topEnd = ClearrDimens.dp24))
            .padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp20)
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .width(ClearrDimens.dp36)
                .height(ClearrDimens.dp4)
                .background(colors.border, RoundedCornerShape(ClearrDimens.dp99))
                .align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(ClearrDimens.dp16))

        // Over budget alert
        if (overBudgetNames.isNotEmpty()) {
            Surface(
                color = colors.red.copy(alpha = if (colors.isDark) 0.20f else 0.12f),
                shape = RoundedCornerShape(ClearrDimens.dp10),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚠ ${overBudgetNames.joinToString(", ")} ${if (overBudgetNames.size == 1) "is" else "are"} over budget",
                    modifier = Modifier.padding(horizontal = ClearrDimens.dp14, vertical = ClearrDimens.dp10),
                    fontSize = ClearrTextSizes.sp13,
                    color = colors.red,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(ClearrDimens.dp12))
        }

        // AI insight
        if (!aiInsight.isNullOrBlank()) {
            Surface(
                color = colors.amber.copy(alpha = if (colors.isDark) 0.22f else 0.16f),
                shape = RoundedCornerShape(ClearrDimens.dp10),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(ClearrDimens.dp12),
                    horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)
                ) {
                    Text("💡", fontSize = ClearrTextSizes.sp16)
                    Text(
                        text = aiInsight,
                        fontSize = ClearrTextSizes.sp12,
                        color = colors.text,
                        lineHeight = 18.sp
                    )
                }
            }
            Spacer(Modifier.height(ClearrDimens.dp12))
        }

        // Column headers (Maya-style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ClearrDimens.dp12, vertical = ClearrDimens.dp4)
        ) {
            Spacer(Modifier.width(ClearrDimens.dp32))
            Spacer(Modifier.width(ClearrDimens.dp8))
            Text(
                "CATEGORY", modifier = Modifier.weight(1f),
                fontSize = ClearrTextSizes.sp10, fontWeight = FontWeight.Bold,
                color = colors.muted, letterSpacing = 0.6.sp
            )
            Text(
                "SPENT", modifier = Modifier.width(70.dp),
                fontSize = ClearrTextSizes.sp10, fontWeight = FontWeight.Bold,
                color = colors.muted, textAlign = TextAlign.End,
                letterSpacing = 0.6.sp
            )
            Text(
                "LEFT", modifier = Modifier.width(56.dp),
                fontSize = ClearrTextSizes.sp10, fontWeight = FontWeight.Bold,
                color = colors.muted, textAlign = TextAlign.End,
                letterSpacing = 0.6.sp
            )
        }

        // Category rows
        Surface(
            color = colors.surface,
            shape = RoundedCornerShape(ClearrDimens.dp14),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                summaries.forEachIndexed { index, summary ->
                    BudgetCategoryRow(
                        summary = summary,
                        colors = colors,
                        onClick = { onCategoryTap(summary) }
                    )
                    if (index < summaries.lastIndex) {
                        HorizontalDivider(
                            color = colors.border,
                            modifier = Modifier.padding(start = 52.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(ClearrDimens.dp12))

        // Log Expense inline CTA (Maya-style row)
        Surface(
            color = colors.surface,
            shape = RoundedCornerShape(ClearrDimens.dp14),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogExpenseTap() }
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = ClearrDimens.dp16,
                    vertical = ClearrDimens.dp13
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp12)
            ) {
                Surface(
                    color = colors.green.copy(alpha = if (colors.isDark) 0.20f else 0.12f),
                    shape = RoundedCornerShape(ClearrDimens.dp10),
                    modifier = Modifier.size(ClearrDimens.dp36)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("₦", color = colors.green, fontSize = ClearrTextSizes.sp18, fontWeight = FontWeight.Bold)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Log Expense", fontSize = ClearrTextSizes.sp14, fontWeight = FontWeight.SemiBold, color = colors.text)
                    Text("Tap a category row or here", fontSize = ClearrTextSizes.sp11, color = colors.muted)
                }
                Text("+", color = colors.green, fontSize = ClearrTextSizes.sp22, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── CATEGORY ROW ──────────────────────────────────────────────────────────────
// CHANGED layout: now uses grid-style with progress bar inline under name.
// Old: icon · [name, bar] · status chip · chevron
// New: icon · [name, bar] · spent · left
@Composable
private fun BudgetCategoryRow(
    summary: CategorySummary,
    colors: DuesColors,
    onClick: () -> Unit
) {
    val pct = summary.percentUsed.coerceAtMost(1f)
    val animPct by animateFloatAsState(targetValue = pct, label = "row_pct")
    val token = ClearrColors.fromToken(summary.category.colorToken)
    val barColor = when (summary.status) {
        BudgetStatus.OVER_BUDGET -> colors.red
        BudgetStatus.CLEARED     -> colors.green
        else                     -> token.color
    }
    val leftText = when (summary.status) {
        BudgetStatus.OVER_BUDGET -> "-${formatKobo(summary.remainingAmountKobo.absoluteValue)}"
        BudgetStatus.CLEARED     -> "✓"
        else                     -> formatKobo(summary.remainingAmountKobo.coerceAtLeast(0L))
    }
    val leftColor = when (summary.status) {
        BudgetStatus.OVER_BUDGET -> colors.red
        BudgetStatus.CLEARED     -> colors.green
        else                     -> colors.muted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = ClearrDimens.dp12, vertical = ClearrDimens.dp11),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)
    ) {
        // Icon
        Text(summary.category.icon, fontSize = ClearrTextSizes.sp18, modifier = Modifier.width(ClearrDimens.dp32))
        // Name + bar
        Column(modifier = Modifier.weight(1f)) {
            Text(
                summary.category.name,
                fontSize = ClearrTextSizes.sp14,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
            Spacer(Modifier.height(ClearrDimens.dp4))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ClearrDimens.dp4)
                    .clip(RoundedCornerShape(ClearrDimens.dp99))
                    .background(colors.border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animPct)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(ClearrDimens.dp99))
                        .background(barColor)
                )
            }
        }
        // Spent
        Text(
            formatKobo(summary.spentAmountKobo),
            modifier = Modifier.width(70.dp),
            fontSize = ClearrTextSizes.sp13,
            fontWeight = FontWeight.Bold,
            color = barColor,
            textAlign = TextAlign.End
        )
        // Left
        Text(
            leftText,
            modifier = Modifier.width(56.dp),
            fontSize = ClearrTextSizes.sp12,
            fontWeight = FontWeight.Bold,
            color = leftColor,
            textAlign = TextAlign.End
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LOG EXPENSE SHEET
//
// MIGRATION CHANGE: Signature updated.
// OLD: LogExpenseSheet(category: CategorySummary, onDismiss, onSave)
// NEW: LogExpenseSheet(allCategories, preselectedCategory?: CategorySummary, onDismiss, onSave)
//
// onSave now passes selected CategorySummary back to caller.
// OLD onSave: (amountNaira: Double, note: String?) -> Unit
// NEW onSave: (category: CategorySummary, amountNaira: Double, note: String?) -> Unit
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogExpenseSheet(
    allCategories: List<CategorySummary>,
    preselectedCategory: CategorySummary?,
    onDismiss: () -> Unit,
    onSave: (category: CategorySummary, amountNaira: Double, note: String?) -> Unit
) {
    val colors = LocalDuesColors.current
    var amount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(preselectedCategory) }
    val amountNaira = amount.toDoubleOrNull() ?: 0.0
    val canSave = amountNaira > 0.0 && selectedCategory != null

    BackHandler(onBack = onDismiss)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ClearrDimens.dp20)
                .padding(bottom = ClearrDimens.dp36)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel", color = colors.muted) }
                Text("Log Expense", fontWeight = FontWeight.Bold, fontSize = ClearrTextSizes.sp16, color = colors.text)
                TextButton(
                    onClick = { selectedCategory?.let { onSave(it, amountNaira, note.ifBlank { null }) } },
                    enabled = canSave
                ) {
                    Text("Save", color = if (canSave) colors.green else colors.muted, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(ClearrDimens.dp8))

            // Amount input
            Surface(
                color = colors.bg,
                shape = RoundedCornerShape(ClearrDimens.dp16),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(ClearrDimens.dp20),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "AMOUNT",
                        fontSize = ClearrTextSizes.sp11,
                        fontWeight = FontWeight.Bold,
                        color = colors.muted,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(ClearrDimens.dp12))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "₦",
                            fontSize = ClearrTextSizes.sp32,
                            fontWeight = FontWeight.Bold,
                            color = if (amount.isBlank()) colors.muted else colors.text
                        )
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it.filter { ch -> ch.isDigit() } },
                            modifier = Modifier.width(180.dp),
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                textAlign = TextAlign.Center,
                                color = colors.text,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1).sp
                            ),
                            placeholder = {
                                Text(
                                    "0",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontSize = 44.sp,
                                    color = colors.muted
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClearrColors.Transparent,
                                unfocusedBorderColor = ClearrColors.Transparent
                            )
                        )
                    }
                    // Context hint when category selected
                    selectedCategory?.let { cat ->
                        Spacer(Modifier.height(ClearrDimens.dp4))
                        Text(
                            text = buildString {
                                append("into ${cat.category.name}")
                                if (cat.remainingAmountKobo > 0) append(" · ${formatKobo(cat.remainingAmountKobo)} remaining")
                                else append(" · over budget")
                            },
                            fontSize = ClearrTextSizes.sp12,
                            color = colors.muted
                        )
                    }
                }
            }
            Spacer(Modifier.height(ClearrDimens.dp16))

            // Category chips
            Text(
                "CATEGORY",
                fontSize = ClearrTextSizes.sp11,
                fontWeight = FontWeight.Bold,
                color = colors.muted,
                letterSpacing = 0.7.sp
            )
            Spacer(Modifier.height(ClearrDimens.dp10))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)) {
                items(allCategories, key = { it.category.id }) { cat ->
                    val tk = ClearrColors.fromToken(cat.category.colorToken)
                    val active = selectedCategory?.category?.id == cat.category.id
                    Surface(
                        color = if (active) tk.color else tk.background,
                        shape = RoundedCornerShape(ClearrDimens.dp99),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = ClearrDimens.dp12, vertical = ClearrDimens.dp7),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp6)
                        ) {
                            Text(cat.category.icon, fontSize = ClearrTextSizes.sp14)
                            Text(
                                cat.category.name,
                                fontSize = ClearrTextSizes.sp13,
                                fontWeight = FontWeight.SemiBold,
                                color = if (active) ClearrColors.Surface else colors.text
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(ClearrDimens.dp16))

            // Note
            Text(
                "NOTE (OPTIONAL)",
                fontSize = ClearrTextSizes.sp11,
                fontWeight = FontWeight.Bold,
                color = colors.muted,
                letterSpacing = 0.7.sp
            )
            Spacer(Modifier.height(ClearrDimens.dp8))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Grocery run, fuel...", color = colors.muted) },
                singleLine = true,
                shape = RoundedCornerShape(ClearrDimens.dp10)
            )
            Spacer(Modifier.height(ClearrDimens.dp16))

            // Save CTA
            Surface(
                color = if (canSave) colors.green else colors.border,
                shape = RoundedCornerShape(ClearrDimens.dp14),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = canSave) {
                        selectedCategory?.let { onSave(it, amountNaira, note.ifBlank { null }) }
                    }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = ClearrDimens.dp15)) {
                    Text(
                        text = if (canSave) "Log ${formatKobo((amountNaira * 100).toLong())} to ${selectedCategory!!.category.name}"
                               else "Select a category to continue",
                        fontSize = ClearrTextSizes.sp15,
                        fontWeight = FontWeight.Bold,
                        color = if (canSave) ClearrColors.Surface else colors.muted
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ADD CATEGORY SHEET
// No contract changes. Visual treatment updated (preview card, Violet CTA).
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategorySheet(
    onDismiss: () -> Unit,
    onAdd: (name: String, icon: String, colorToken: String, plannedAmountNaira: Double) -> Unit
) {
    val colors = LocalDuesColors.current
    var step by rememberSaveable { mutableStateOf(0) }
    var selected by remember { mutableStateOf(categoryPresets.first()) }
    var name by rememberSaveable { mutableStateOf("") }
    var plannedAmount by rememberSaveable { mutableStateOf("") }
    val canAdd = step == 1 && name.isNotBlank() && (plannedAmount.toDoubleOrNull() ?: 0.0) > 0.0

    BackHandler(onBack = { if (step == 0) onDismiss() else step = 0 })
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ClearrDimens.dp16)
                .padding(bottom = ClearrDimens.dp36)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { if (step == 0) onDismiss() else step = 0 }) {
                    Text(if (step == 0) "Cancel" else "← Back", color = colors.muted)
                }
                Text(
                    if (step == 0) "New Category" else "Configure",
                    fontWeight = FontWeight.Bold,
                    fontSize = ClearrTextSizes.sp16,
                    color = colors.text
                )
                if (step == 1) {
                    TextButton(
                        onClick = { onAdd(name.trim(), selected.icon, selected.colorToken, plannedAmount.toDouble()) },
                        enabled = canAdd
                    ) {
                        Text("Add", color = if (canAdd) colors.accent else colors.muted, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(Modifier.width(ClearrDimens.dp48))
                }
            }

            if (step == 0) {
                // ── STEP 0: Preset picker ────────────────────────────────────
                Text(
                    "SELECT A PRESET",
                    fontSize = ClearrTextSizes.sp11,
                    fontWeight = FontWeight.Bold,
                    color = colors.muted,
                    letterSpacing = 0.7.sp,
                    modifier = Modifier.padding(bottom = ClearrDimens.dp10)
                )
                Surface(
                    color = colors.surface,
                    shape = RoundedCornerShape(ClearrDimens.dp14),
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                                    .padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp13),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp12)
                            ) {
                                Surface(
                                    color = token.background,
                                    shape = RoundedCornerShape(ClearrDimens.dp10),
                                    modifier = Modifier.size(ClearrDimens.dp36)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(preset.icon, fontSize = ClearrTextSizes.sp18)
                                    }
                                }
                                Text(
                                    preset.name,
                                    modifier = Modifier.weight(1f),
                                    fontSize = ClearrTextSizes.sp15,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.text
                                )
                                Text("›", color = colors.muted)
                            }
                            if (index < categoryPresets.lastIndex) {
                                HorizontalDivider(
                                    color = colors.border,
                                    modifier = Modifier.padding(start = ClearrDimens.dp64)
                                )
                            }
                        }
                    }
                }
            } else {
                // ── STEP 1: Configure ────────────────────────────────────────
                val token = ClearrColors.fromToken(selected.colorToken)
                // Preview card
                Surface(
                    color = token.background,
                    shape = RoundedCornerShape(ClearrDimens.dp16),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = ClearrDimens.dp20)
                ) {
                    Row(
                        modifier = Modifier.padding(ClearrDimens.dp16),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp12)
                    ) {
                        Surface(
                            color = colors.surface,
                            shape = RoundedCornerShape(ClearrDimens.dp12),
                            modifier = Modifier.size(ClearrDimens.dp44)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(selected.icon, fontSize = ClearrTextSizes.sp22)
                            }
                        }
                        Column {
                            Text(
                                name.ifBlank { "Category name" },
                                fontSize = ClearrTextSizes.sp16,
                                fontWeight = FontWeight.Bold,
                                color = colors.text
                            )
                            Text(
                                if ((plannedAmount.toDoubleOrNull() ?: 0.0) > 0.0)
                                    "Budget: ${formatKobo(((plannedAmount.toDoubleOrNull() ?: 0.0) * 100).toLong())}"
                                else "Set a monthly budget below",
                                fontSize = ClearrTextSizes.sp12,
                                color = colors.muted
                            )
                        }
                    }
                }
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ClearrDimens.dp10)
                )
                Spacer(Modifier.height(ClearrDimens.dp10))
                // Budget field
                OutlinedTextField(
                    value = plannedAmount,
                    onValueChange = { plannedAmount = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Monthly Budget (₦)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ClearrDimens.dp10),
                    leadingIcon = { Text("₦", fontWeight = FontWeight.Bold, color = colors.muted) }
                )
                Spacer(Modifier.height(ClearrDimens.dp20))
                // Add CTA
                Surface(
                    color = if (canAdd) colors.accent else colors.border,
                    shape = RoundedCornerShape(ClearrDimens.dp14),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = canAdd) {
                            onAdd(name.trim(), selected.icon, selected.colorToken, plannedAmount.toDouble())
                        }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = ClearrDimens.dp15)) {
                        Text(
                            text = if (canAdd) "Add ${name.trim()}" else "Fill in the details to continue",
                            fontSize = ClearrTextSizes.sp15,
                            fontWeight = FontWeight.Bold,
                            color = if (canAdd) ClearrColors.Surface else colors.muted
                        )
                    }
                }
            }
        }
    }
}

// ── PRIVATE HELPERS ───────────────────────────────────────────────────────────
// Unchanged from original file.
private fun formatKobo(kobo: Long): String {
    val naira = kobo / 100
    return "₦" + "%,d".format(naira)
}

private data class CategoryPreset(
    val name: String,
    val icon: String,
    val colorToken: String
)

private val categoryPresets = listOf(
    CategoryPreset("Housing",       "🏠", "Violet"),
    CategoryPreset("Food",          "🍔", "Orange"),
    CategoryPreset("Transport",     "🚗", "Blue"),
    CategoryPreset("Health",        "💊", "Teal"),
    CategoryPreset("Savings",       "💰", "Amber"),
    CategoryPreset("Entertainment", "🎬", "Purple"),
    CategoryPreset("Utilities",     "💡", "Violet"),
    CategoryPreset("Shopping",      "🛍", "Orange"),
    CategoryPreset("Education",     "📚", "Blue"),
    CategoryPreset("Custom",        "✦", "Teal")
)
