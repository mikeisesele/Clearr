package com.mikeisesele.clearr.ui.feature.budget

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.text.KeyboardOptions
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

@Composable
fun BudgetDetailScreen(
    trackerId: Long,
    onNavigateBack: () -> Unit,
    onAddCategory: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalDuesColors.current

    var loggingCategory by remember { mutableStateOf<CategorySummary?>(null) }
    var showLogDialog by remember { mutableStateOf(false) }

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
                item {
                    BudgetHeroSection(
                        summary = state.budgetSummary,
                        periods = state.periods,
                        selectedPeriodId = state.selectedPeriodId,
                        onPeriodSelect = { viewModel.onAction(BudgetAction.SelectPeriod(it)) },
                        colors = colors
                    )
                }
                item {
                    BudgetCategoryTable(
                        summaries = state.categorySummaries,
                        overBudgetNames = state.budgetSummary.overBudgetCategories.map { it.category.name },
                        aiInsight = state.aiInsight,
                        onCategoryTap = { cat ->
                            loggingCategory = cat
                            showLogDialog = true
                        },
                        onCategoryDelete = { categoryId ->
                            viewModel.onAction(BudgetAction.DeleteCategory(categoryId))
                        },
                        onLogExpenseTap = {
                            loggingCategory = null
                            showLogDialog = true
                        },
                        colors = colors
                    )
                }
                item { Spacer(Modifier.height(ClearrDimens.dp96)) }
                item { Spacer(Modifier.navigationBarsPadding()) }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = ClearrDimens.dp20, bottom = ClearrDimens.dp24)
                .size(ClearrDimens.dp52),
            color = colors.accent,
            shape = RoundedCornerShape(ClearrDimens.dp16),
            shadowElevation = ClearrDimens.dp10
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.clickable { onAddCategory() }
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

    if (showLogDialog) {
        LogExpenseDialog(
            allCategories = state.categorySummaries,
            preselectedCategory = loggingCategory,
            onDismiss = {
                showLogDialog = false
                loggingCategory = null
            },
            onSave = { category, amountNaira, note ->
                viewModel.onAction(BudgetAction.LogExpense(category.category.id, amountNaira, note))
                showLogDialog = false
                loggingCategory = null
            }
        )
    }
}

@Composable
private fun BudgetHeroSection(
    summary: BudgetSummary,
    periods: List<BudgetPeriod>,
    selectedPeriodId: Long?,
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

        BudgetHealthMeter(
            summary = summary,
            colors = colors
        )

        Spacer(Modifier.height(ClearrDimens.dp16))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp6)) {
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

@Composable
private fun BudgetHealthMeter(
    summary: BudgetSummary,
    colors: DuesColors
) {
    val pct = if (summary.totalPlannedKobo > 0L) {
        (summary.totalSpentKobo.toFloat() / summary.totalPlannedKobo).coerceIn(0f, 1f)
    } else {
        0f
    }
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

@Composable
private fun BudgetCategoryTable(
    summaries: List<CategorySummary>,
    overBudgetNames: List<String>,
    aiInsight: String?,
    onCategoryTap: (CategorySummary) -> Unit,
    onCategoryDelete: (Long) -> Unit,
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
        Box(
            modifier = Modifier
                .width(ClearrDimens.dp36)
                .height(ClearrDimens.dp4)
                .background(colors.border, RoundedCornerShape(ClearrDimens.dp99))
                .align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(ClearrDimens.dp16))

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

        // if (!aiInsight.isNullOrBlank()) {
        //     Surface(
        //         color = colors.amber.copy(alpha = if (colors.isDark) 0.22f else 0.16f),
        //         shape = RoundedCornerShape(ClearrDimens.dp10),
        //         modifier = Modifier.fillMaxWidth()
        //     ) {
        //         Row(
        //             modifier = Modifier.padding(ClearrDimens.dp12),
        //             horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)
        //         ) {
        //             Text("💡", fontSize = ClearrTextSizes.sp16)
        //             Text(
        //                 text = aiInsight,
        //                 fontSize = ClearrTextSizes.sp12,
        //                 color = colors.text,
        //                 lineHeight = 18.sp
        //             )
        //         }
        //     }
        //     Spacer(Modifier.height(ClearrDimens.dp12))
        // }

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

        Surface(
            color = colors.surface,
            shape = RoundedCornerShape(ClearrDimens.dp14),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                summaries.forEachIndexed { index, summary ->
                    key(summary.category.id) {
                        SwipeableBudgetCategoryRow(
                            summary = summary,
                            colors = colors,
                            onTap = { onCategoryTap(summary) },
                            onDelete = { onCategoryDelete(summary.category.id) },
                            isLast = index == summaries.lastIndex
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(ClearrDimens.dp12))

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableBudgetCategoryRow(
    summary: CategorySummary,
    colors: DuesColors,
    onTap: () -> Unit,
    onDelete: () -> Unit,
    isLast: Boolean
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.35f },
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.red)
            )
        }
    ) {
        BudgetCategoryRow(
            summary = summary,
            colors = colors,
            onClick = onTap
        )
    }

    if (!isLast) {
        HorizontalDivider(
            color = colors.border,
            modifier = Modifier.padding(start = 52.dp)
        )
    }
}

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
        BudgetStatus.CLEARED -> colors.green
        else -> token.color
    }
    val leftText = when (summary.status) {
        BudgetStatus.OVER_BUDGET -> "-${formatKobo(summary.remainingAmountKobo.absoluteValue)}"
        BudgetStatus.CLEARED -> "✓"
        else -> formatKobo(summary.remainingAmountKobo.coerceAtLeast(0L))
    }
    val leftColor = when (summary.status) {
        BudgetStatus.OVER_BUDGET -> colors.red
        BudgetStatus.CLEARED -> colors.green
        else -> colors.muted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .clickable { onClick() }
            .padding(horizontal = ClearrDimens.dp12, vertical = ClearrDimens.dp11),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)
    ) {
        Text(summary.category.icon, fontSize = ClearrTextSizes.sp18, modifier = Modifier.width(ClearrDimens.dp32))

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

        Text(
            formatKobo(summary.spentAmountKobo),
            modifier = Modifier.width(70.dp),
            fontSize = ClearrTextSizes.sp13,
            fontWeight = FontWeight.Bold,
            color = barColor,
            textAlign = TextAlign.End
        )
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

@Composable
fun LogExpenseDialog(
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
    val hasAmount = amountNaira > 0.0

    BackHandler(onBack = onDismiss)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.text.copy(alpha = 0.35f))
                .padding(horizontal = ClearrDimens.dp16),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ClearrDimens.dp20),
                color = colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp12)
                ) {
                    // ── Header ──────────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.size(ClearrDimens.dp34))
                        Text(
                            "Log Expense",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = ClearrTextSizes.sp16,
                            color = colors.text
                        )
                        Surface(
                            modifier = Modifier
                                .size(ClearrDimens.dp34)
                                .clickable { onDismiss() },
                            shape = RoundedCornerShape(ClearrDimens.dp10),
                            color = colors.card
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = colors.muted
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(ClearrDimens.dp10))

                    // ── THEO LAYOUT: Amount card ─────────────────────────────
                    // Selected category color for accent — falls back to muted
                    val tk = selectedCategory?.let { ClearrColors.fromToken(it.category.colorToken) }

                    Surface(
                        color = colors.card,
                        shape = RoundedCornerShape(ClearrDimens.dp16),
                        modifier = Modifier.fillMaxWidth(),
                        border = tk?.let {
                            BorderStroke(
                                width = 1.5.dp,
                                color = it.color.copy(alpha = 0.25f)
                            )
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(ClearrDimens.dp16)
                        ) {
                            // Row: "AMOUNT" label  ←→  selected category badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "AMOUNT",
                                    fontSize = ClearrTextSizes.sp11,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.muted,
                                    letterSpacing = 0.8.sp
                                )
                                // Category badge — only shown when one is selected
                                selectedCategory?.let { cat ->
                                    val catTk = ClearrColors.fromToken(cat.category.colorToken)
                                    Surface(
                                        color = catTk.background,
                                        shape = RoundedCornerShape(ClearrDimens.dp99)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                horizontal = ClearrDimens.dp10,
                                                vertical = ClearrDimens.dp5
                                            ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp5)
                                        ) {
                                            Text(cat.category.icon, fontSize = ClearrTextSizes.sp13)
                                            Text(
                                                cat.category.name,
                                                fontSize = ClearrTextSizes.sp12,
                                                fontWeight = FontWeight.SemiBold,
                                                color = catTk.color
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(ClearrDimens.dp14))

                            // Row: ₦ icon tile + amount input
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp10)
                            ) {
                                // Naira icon tile — lights up with category accent when amount entered
                                Surface(
                                    shape = RoundedCornerShape(ClearrDimens.dp14),
                                    color = if (hasAmount && tk != null)
                                        tk.color.copy(alpha = 0.15f)
                                    else
                                        colors.surface,
                                    modifier = Modifier.size(ClearrDimens.dp44)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "₦",
                                            fontSize = ClearrTextSizes.sp20,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasAmount && tk != null) tk.color else colors.muted
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = amount,
                                    onValueChange = { amount = it.filter { ch -> ch.isDigit() } },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                                        color = colors.text,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-1).sp
                                    ),
                                    placeholder = {
                                        Text(
                                            "0",
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

                            // Over-budget banner — only shown when category selected

                            selectedCategory?.let { cat ->
                                val isOverBudget = cat.remainingAmountKobo <= 0
                                Spacer(Modifier.height(ClearrDimens.dp12))
                                Surface(
                                    color = if (isOverBudget)
                                        ClearrColors.BrandDanger.copy(alpha = 0.12f)
                                    else
                                        colors.green.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(ClearrDimens.dp10),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = ClearrDimens.dp12,
                                            vertical = ClearrDimens.dp8
                                        ),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)
                                    ) {
                                        Text(
                                            if (isOverBudget) "⚠️" else "✅",
                                            fontSize = ClearrTextSizes.sp13
                                        )
                                        Text(
                                            text = if (isOverBudget)
                                                "over budget in ${cat.category.name}"
                                            else
                                                "${formatKobo(cat.remainingAmountKobo)} remaining in ${cat.category.name}",
                                            fontSize = ClearrTextSizes.sp12,
                                            color = if (isOverBudget) ClearrColors.BrandDanger else colors.green
                                        )
                                    }
                                }

                            }
                        }
                    }

                    Spacer(Modifier.height(ClearrDimens.dp16))

                    // ── CATEGORY chips (unchanged behaviour, same style) ──────
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
                            val catTk = ClearrColors.fromToken(cat.category.colorToken)
                            val active = selectedCategory?.category?.id == cat.category.id
                            Surface(
                                color = if (active) catTk.color else catTk.background,
                                shape = RoundedCornerShape(ClearrDimens.dp99),
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = ClearrDimens.dp12,
                                        vertical = ClearrDimens.dp7
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp6)
                                ) {
                                    Text(cat.category.icon, fontSize = ClearrTextSizes.sp14)
                                    Text(
                                        cat.category.name,
                                        fontSize = ClearrTextSizes.sp13,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (active) ClearrColors.Surface else catTk.color
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(ClearrDimens.dp16))

                    // ── Note ─────────────────────────────────────────────────
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

                    // ── CTA ──────────────────────────────────────────────────
                    Button(
                        onClick = {
                            selectedCategory?.let { onSave(it, amountNaira, note.ifBlank { null }) }
                        },
                        enabled = canSave,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ClearrDimens.dp14),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.green,
                            disabledContainerColor = colors.border
                        ),
                        contentPadding = PaddingValues(vertical = ClearrDimens.dp16)
                    ) {
                        Text(
                            text = if (canSave) {
                                "Log ${formatKobo((amountNaira * 100).toLong())} to ${selectedCategory!!.category.name}"
                            } else {
                                "Select a category to continue"
                            },
                            fontSize = ClearrTextSizes.sp15,
                            fontWeight = FontWeight.Bold,
                            color = if (canSave) ClearrColors.Surface else colors.muted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddBudgetCategoryScreen(
    trackerId: Long,
    onClose: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalDuesColors.current
    if (state.trackerId != trackerId) return

    var selectedPreset by remember { mutableStateOf<CategoryPreset?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp8)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(ClearrDimens.dp34)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.text
                )
            }
            Text(
                "Select Category",
                fontSize = ClearrTextSizes.sp16,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
            Spacer(modifier = Modifier.size(ClearrDimens.dp34))
        }

        Spacer(Modifier.height(ClearrDimens.dp12))

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
                            .clickable { selectedPreset = preset }
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
    }

    selectedPreset?.let { preset ->
        AddCategoryDetailDialog(
            preset = preset,
            onDismiss = { selectedPreset = null },
            onAdd = { name, amountNaira ->
                viewModel.onAction(BudgetAction.AddCategory(name, preset.icon, preset.colorToken, amountNaira))
                selectedPreset = null
                onClose()
            }
        )
    }
}

@Composable
private fun AddCategoryDetailDialog(
    preset: CategoryPreset,
    onDismiss: () -> Unit,
    onAdd: (name: String, plannedAmountNaira: Double) -> Unit
) {
    val colors = LocalDuesColors.current
    val token = ClearrColors.fromToken(preset.colorToken)
    var name by rememberSaveable(preset.name) { mutableStateOf(if (preset.name == "Custom") "" else preset.name) }
    var plannedAmount by rememberSaveable(preset.name) { mutableStateOf("") }
    val amountNaira = plannedAmount.toDoubleOrNull() ?: 0.0
    val canAdd = name.isNotBlank()

    BackHandler(onBack = onDismiss)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.text.copy(alpha = 0.35f))
                .padding(horizontal = ClearrDimens.dp16),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ClearrDimens.dp20),
                color = colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp12)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.size(ClearrDimens.dp34))
                        Text(
                            "Category Details",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = ClearrTextSizes.sp16,
                            color = colors.text
                        )
                        Surface(
                            modifier = Modifier
                                .size(ClearrDimens.dp34)
                                .clickable { onDismiss() },
                            shape = RoundedCornerShape(ClearrDimens.dp10),
                            color = colors.card
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = colors.muted
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(ClearrDimens.dp10))

                    Surface(
                        color = colors.card,
                        shape = RoundedCornerShape(ClearrDimens.dp16),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = ClearrDimens.dp16)
                    ) {
                        Row(
                            modifier = Modifier.padding(ClearrDimens.dp16),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp12)
                        ) {
                            Surface(
                                color = token.background,
                                shape = RoundedCornerShape(ClearrDimens.dp12),
                                modifier = Modifier.size(ClearrDimens.dp44)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(preset.icon, fontSize = ClearrTextSizes.sp22)
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
                                    if (plannedAmount.isBlank()) {
                                        "Starts at ₦0"
                                    } else {
                                        "Budget: ${formatKobo((amountNaira * 100).toLong())}"
                                    },
                                    fontSize = ClearrTextSizes.sp12,
                                    color = colors.muted
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ClearrDimens.dp10)
                    )
                    Spacer(Modifier.height(ClearrDimens.dp10))
                    OutlinedTextField(
                        value = plannedAmount,
                        onValueChange = { plannedAmount = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Monthly Budget (₦)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ClearrDimens.dp10),
                        leadingIcon = { Text("₦", fontWeight = FontWeight.Bold, color = colors.muted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(ClearrDimens.dp16))
                    Button(
                        onClick = { onAdd(name.trim(), amountNaira) },
                        enabled = canAdd,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ClearrDimens.dp14),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            disabledContainerColor = colors.border
                        ),
                        contentPadding = PaddingValues(vertical = ClearrDimens.dp16)
                    ) {
                        Text(
                            "Add",
                            color = ClearrColors.Surface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

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
