package com.mikeisesele.clearr.ui.feature.budget

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.ui.feature.budget.BudgetPlanDraft
import com.mikeisesele.clearr.data.model.CategorySummary
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
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
                actionText = "Edit month",
                onActionClick = { viewModel.onAction(BudgetAction.OpenBudgetSetup) },
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
                        showSwipeHint = state.showSwipeHint,
                        onCategoryTap = { cat ->
                            loggingCategory = cat
                            showLogDialog = true
                        },
                        onCategoryDelete = { categoryId ->
                            viewModel.onAction(BudgetAction.DeleteCategory(categoryId))
                        },
                        onSwipeHintDisplayed = { viewModel.onAction(BudgetAction.OnSwipeHintDisplayed) },
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

    if (state.showBudgetSetup) {
        BudgetPlanSetupDialog(
            periodLabel = state.budgetSetupPeriodLabel,
            sourceLabel = state.budgetSetupSourceLabel,
            drafts = state.budgetSetupDrafts,
            onDismiss = { viewModel.onAction(BudgetAction.DismissBudgetSetup) },
            onAmountChange = { categoryId, amountNaira ->
                viewModel.onAction(BudgetAction.UpdateBudgetDraft(categoryId, amountNaira))
            },
            onConfirm = { viewModel.onAction(BudgetAction.ConfirmBudgetSetup) }
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
    val monthsListState = rememberLazyListState()

    LaunchedEffect(periods, selectedPeriodId) {
        val selectedIndex = periods.indexOfFirst { it.id == selectedPeriodId }
        if (selectedIndex >= 0) {
            monthsListState.animateScrollToItem(selectedIndex)
        }
    }

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

        LazyRow(
            state = monthsListState,
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

@Composable
private fun BudgetPlanSetupDialog(
    periodLabel: String?,
    sourceLabel: String?,
    drafts: List<BudgetPlanDraft>,
    onDismiss: () -> Unit,
    onAmountChange: (Long, Double) -> Unit,
    onConfirm: () -> Unit
) {
    val colors = LocalDuesColors.current
    val totalPlanned = drafts.sumOf { it.plannedAmountKobo }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.size(ClearrDimens.dp34))
                        Text(
                            text = periodLabel?.let { "Set $it budget" } ?: "Set month budget",
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
                    Text(
                        text = sourceLabel?.let { "Copied from $it. Adjust any amount for this period." }
                            ?: "Set how much you plan to spend across categories for this period.",
                        fontSize = ClearrTextSizes.sp12,
                        color = colors.muted
                    )
                    Spacer(Modifier.height(ClearrDimens.dp14))

                    drafts.forEach { draft ->
                        val token = ClearrColors.fromToken(draft.colorToken)
                        var amountInput by rememberSaveable(draft.categoryId, draft.plannedAmountKobo) {
                            mutableStateOf(
                                if (draft.plannedAmountKobo == 0L) "" else (draft.plannedAmountKobo / 100).toString()
                            )
                        }

                        Surface(
                            color = colors.card,
                            shape = RoundedCornerShape(ClearrDimens.dp14),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = ClearrDimens.dp12, vertical = ClearrDimens.dp10),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp12)
                            ) {
                                Surface(
                                    color = token.background,
                                    shape = RoundedCornerShape(ClearrDimens.dp10),
                                    modifier = Modifier.size(ClearrDimens.dp36)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(draft.icon, fontSize = ClearrTextSizes.sp18)
                                    }
                                }
                                Text(
                                    text = draft.name,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = ClearrTextSizes.sp12,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.text
                                )
                                Surface(
                                    color = colors.surface,
                                    shape = RoundedCornerShape(ClearrDimens.dp10),
                                    border = BorderStroke(1.dp, colors.border),
                                    modifier = Modifier.width(112.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = ClearrDimens.dp10, vertical = ClearrDimens.dp8),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp4)
                                    ) {
                                        Text(
                                            text = "₦",
                                            color = colors.muted,
                                            fontSize = ClearrTextSizes.sp13,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        BasicTextField(
                                            value = amountInput,
                                            onValueChange = { next ->
                                                amountInput = next.filter { it.isDigit() }
                                                onAmountChange(draft.categoryId, amountInput.toDoubleOrNull() ?: 0.0)
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                color = colors.text,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = ClearrTextSizes.sp13
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            cursorBrush = SolidColor(colors.accent),
                                            decorationBox = { innerTextField ->
                                                if (amountInput.isBlank()) {
                                                    Text(
                                                        text = "0",
                                                        color = colors.muted.copy(alpha = 0.7f),
                                                        fontSize = ClearrTextSizes.sp13,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(ClearrDimens.dp10))
                    }

                    Surface(
                        color = colors.card,
                        shape = RoundedCornerShape(ClearrDimens.dp14),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = ClearrDimens.dp14, vertical = ClearrDimens.dp12),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total planned", color = colors.muted, fontSize = ClearrTextSizes.sp12)
                            Text(
                                formatKobo(totalPlanned),
                                color = colors.text,
                                fontSize = ClearrTextSizes.sp16,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(ClearrDimens.dp16))
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ClearrDimens.dp14),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                        contentPadding = PaddingValues(vertical = ClearrDimens.dp16)
                    ) {
                        Text(
                            "Save Month Budget",
                            color = ClearrColors.Surface,
                            fontSize = ClearrTextSizes.sp15,
                            fontWeight = FontWeight.Bold
                        )
                    }
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

        Column(
            verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp10),
            modifier = Modifier.fillMaxWidth()
        ) {
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
                            modifier = Modifier
                                .size(56.dp)
                                .clickable { onCategoryTap(summary) },
                            shape = RoundedCornerShape(ClearrDimens.dp16),
                            color = ClearrColors.fromToken(summary.category.colorToken).background
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "+",
                                    fontSize = ClearrTextSizes.sp22,
                                    fontWeight = FontWeight.SemiBold,
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
private fun SwipeableBudgetCategoryRow(
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
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
            }
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
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cardShape)
                    .background(colors.red),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    "Delete",
                    modifier = Modifier.padding(end = ClearrDimens.dp16),
                    color = Color.White,
                    fontSize = ClearrTextSizes.sp13,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    ) {
        Surface(
            color = colors.surface,
            shape = cardShape,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = hintOffset.value }
        ) {
            BudgetCategoryRow(
                summary = summary,
                colors = colors
            )
        }
    }
}

@Composable
private fun BudgetCategoryRow(
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
            Text(
                summary.category.icon,
                fontSize = ClearrTextSizes.sp18,
                modifier = Modifier.width(ClearrDimens.dp32)
            )
            Text(
                summary.category.name,
                fontSize = ClearrTextSizes.sp14,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
                modifier = Modifier.weight(1f)
            )
            Text(
                leftText,
                fontSize = ClearrTextSizes.sp12,
                fontWeight = FontWeight.Medium,
                color = leftColor
            )
            Text(
                if (expanded) "▲" else "▼",
                fontSize = ClearrTextSizes.sp9,
                color = colors.muted
            )
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
                        .drawBehind {
                            drawRect(
                                color = glow,
                                blendMode = BlendMode.Screen
                            )
                        }
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = ClearrDimens.dp40, top = ClearrDimens.dp10),
                horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp20)
            ) {
                Column {
                    Text(
                        "SPENT",
                        fontSize = ClearrTextSizes.sp9,
                        color = colors.muted,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        formatKoboFull(summary.spentAmountKobo),
                        fontSize = ClearrTextSizes.sp13,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                }
                Column {
                    Text(
                        "PLANNED",
                        fontSize = ClearrTextSizes.sp9,
                        color = colors.muted,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        formatKoboFull(summary.plannedAmountKobo),
                        fontSize = ClearrTextSizes.sp13,
                        fontWeight = FontWeight.Bold,
                        color = colors.muted
                    )
                }
            }
        }
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
    val amountKobo = (amountNaira * 100).toLong()
    val canSave = amountNaira > 0.0 && selectedCategory != null
    val hasAmount = amountNaira > 0.0
    val amountFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        amountFocusRequester.requestFocus()
    }

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

                                BasicTextField(
                                    value = amount,
                                    onValueChange = { amount = it.filter { ch -> ch.isDigit() } },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(amountFocusRequester),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    cursorBrush = SolidColor(colors.text),
                                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                                        color = colors.text,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-1).sp
                                    ),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier.padding(vertical = ClearrDimens.dp2),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (amount.isBlank()) {
                                                Text(
                                                    "0",
                                                    fontSize = 44.sp,
                                                    color = colors.muted,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }

                            // Over-budget banner — only shown when category selected

                            selectedCategory?.let { cat ->
                                if (hasAmount) {
                                    val projectedRemainingKobo = cat.remainingAmountKobo - amountKobo
                                    val isOverBudget = projectedRemainingKobo < 0L
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
                                                text = if (isOverBudget) {
                                                    "Over budget in ${cat.category.name}"
                                                } else {
                                                    "${formatKobo(projectedRemainingKobo.coerceAtLeast(0L))} remaining in ${cat.category.name}"
                                                },
                                                fontSize = ClearrTextSizes.sp12,
                                                color = if (isOverBudget) ClearrColors.BrandDanger else colors.green
                                            )
                                        }
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
    val naira = kobo / 100.0
    return when {
        naira >= 1_000_000 -> "₦" + "%.1f".format(naira / 1_000_000).trimEnd('0').trimEnd('.') + "M"
        naira >= 100_000 -> "₦" + "%.0f".format(naira / 1_000) + "k"
        naira >= 10_000 -> "₦" + "%.1f".format(naira / 1_000).trimEnd('0').trimEnd('.') + "k"
        else -> "₦" + "%,d".format(naira.toLong())
    }
}

private fun formatKoboFull(kobo: Long): String {
    return "₦" + "%,d".format(kobo / 100)
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
    CategoryPreset("Savings", "💰", "Amber"),
    CategoryPreset("Entertainment", "🎬", "Purple"),
    CategoryPreset("Utilities", "💡", "Violet"),
    CategoryPreset("Shopping", "🛍", "Orange"),
    CategoryPreset("Education", "📚", "Blue"),
    CategoryPreset("Custom", "✦", "Teal")
)

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
fun BudgetDetailScreenPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        val periods = listOf(
            BudgetPeriod(
                id = 1L,
                trackerId = 1L,
                frequency = com.mikeisesele.clearr.data.model.BudgetFrequency.MONTHLY,
                label = "Nov 2025",
                startDate = 0L,
                endDate = 0L
            ),
            BudgetPeriod(
                id = 2L,
                trackerId = 1L,
                frequency = com.mikeisesele.clearr.data.model.BudgetFrequency.MONTHLY,
                label = "Dec 2025",
                startDate = 0L,
                endDate = 0L
            ),
            BudgetPeriod(
                id = 3L,
                trackerId = 1L,
                frequency = com.mikeisesele.clearr.data.model.BudgetFrequency.MONTHLY,
                label = "Jan 2026",
                startDate = 0L,
                endDate = 0L
            ),
            BudgetPeriod(
                id = 4L,
                trackerId = 1L,
                frequency = com.mikeisesele.clearr.data.model.BudgetFrequency.MONTHLY,
                label = "Feb 2026",
                startDate = 0L,
                endDate = 0L
            )
        )
        val summaries = listOf(
            CategorySummary(
                category = BudgetCategory(
                    id = 1L,
                    trackerId = 1L,
                    frequency = com.mikeisesele.clearr.data.model.BudgetFrequency.MONTHLY,
                    name = "Entertainment",
                    icon = "🎬",
                    colorToken = "Purple",
                    plannedAmountKobo = 20_000_00L,
                    sortOrder = 0
                ),
                plannedAmountKobo = 20_000_00L,
                spentAmountKobo = 7_500_00L,
                remainingAmountKobo = 12_500_00L,
                percentUsed = 0.375f,
                status = BudgetStatus.ON_TRACK
            ),
            CategorySummary(
                category = BudgetCategory(
                    id = 2L,
                    trackerId = 1L,
                    frequency = com.mikeisesele.clearr.data.model.BudgetFrequency.MONTHLY,
                    name = "Food",
                    icon = "🍔",
                    colorToken = "Orange",
                    plannedAmountKobo = 25_000_00L,
                    sortOrder = 1
                ),
                plannedAmountKobo = 25_000_00L,
                spentAmountKobo = 0L,
                remainingAmountKobo = 25_000_00L,
                percentUsed = 0f,
                status = BudgetStatus.ON_TRACK
            ),
            CategorySummary(
                category = BudgetCategory(
                    id = 3L,
                    trackerId = 1L,
                    frequency = com.mikeisesele.clearr.data.model.BudgetFrequency.MONTHLY,
                    name = "Housing",
                    icon = "🏠",
                    colorToken = "Violet",
                    plannedAmountKobo = 50_000_00L,
                    sortOrder = 2
                ),
                plannedAmountKobo = 50_000_00L,
                spentAmountKobo = 5_000_00L,
                remainingAmountKobo = 45_000_00L,
                percentUsed = 0.10f,
                status = BudgetStatus.ON_TRACK
            ),
            CategorySummary(
                category = BudgetCategory(
                    id = 4L,
                    trackerId = 1L,
                    frequency = com.mikeisesele.clearr.data.model.BudgetFrequency.MONTHLY,
                    name = "Savings",
                    icon = "💰",
                    colorToken = "Teal",
                    plannedAmountKobo = 15_000_00L,
                    sortOrder = 3
                ),
                plannedAmountKobo = 15_000_00L,
                spentAmountKobo = 0L,
                remainingAmountKobo = 15_000_00L,
                percentUsed = 0f,
                status = BudgetStatus.ON_TRACK
            ),
            CategorySummary(
                category = BudgetCategory(
                    id = 5L,
                    trackerId = 1L,
                    frequency = com.mikeisesele.clearr.data.model.BudgetFrequency.MONTHLY,
                    name = "Transport",
                    icon = "🚗",
                    colorToken = "Blue",
                    plannedAmountKobo = 15_000_00L,
                    sortOrder = 4
                ),
                plannedAmountKobo = 15_000_00L,
                spentAmountKobo = 0L,
                remainingAmountKobo = 15_000_00L,
                percentUsed = 0f,
                status = BudgetStatus.ON_TRACK
            ),
            CategorySummary(
                category = BudgetCategory(
                    id = 6L,
                    trackerId = 1L,
                    frequency = com.mikeisesele.clearr.data.model.BudgetFrequency.MONTHLY,
                    name = "Utilities",
                    icon = "💡",
                    colorToken = "Violet",
                    plannedAmountKobo = 15_000_00L,
                    sortOrder = 5
                ),
                plannedAmountKobo = 15_000_00L,
                spentAmountKobo = 0L,
                remainingAmountKobo = 15_000_00L,
                percentUsed = 0f,
                status = BudgetStatus.ON_TRACK
            )
        )
        val summary = BudgetSummary(
            totalPlannedKobo = summaries.sumOf { it.plannedAmountKobo },
            totalSpentKobo = summaries.sumOf { it.spentAmountKobo },
            totalRemainingKobo = summaries.sumOf { it.remainingAmountKobo },
            percentUsed = summaries.sumOf { it.spentAmountKobo }.toFloat() / summaries.sumOf { it.plannedAmountKobo },
            isOverBudget = false,
            overBudgetCategories = emptyList()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
        ) {
            ClearrTopBar(
                title = "Budget",
                leadingIcon = "←",
                onLeadingClick = {},
                actionIcon = "✎",
                onActionClick = {},
                leadingContainerColor = ClearrColors.Transparent
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    BudgetHeroSection(
                        summary = summary,
                        periods = periods,
                        selectedPeriodId = 4L,
                        onPeriodSelect = {},
                        colors = colors
                    )
                }
                item {
                    BudgetCategoryTable(
                        summaries = summaries,
                        overBudgetNames = emptyList(),
                        aiInsight = null,
                        showSwipeHint = true,
                        onCategoryTap = {},
                        onCategoryDelete = {},
                        onSwipeHintDisplayed = {},
                        colors = colors
                    )
                }
            }
        }
    }
}
