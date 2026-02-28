package com.mikeisesele.clearr.ui.feature.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.CategorySummary
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
import com.mikeisesele.clearr.ui.feature.budget.components.BudgetCategoryTable
import com.mikeisesele.clearr.ui.feature.budget.components.BudgetHeroSection
import com.mikeisesele.clearr.ui.feature.budget.components.BudgetPlanSetupDialog
import com.mikeisesele.clearr.ui.feature.budget.components.LogExpenseDialog
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
fun BudgetDetailScreen(
    trackerId: Long,
    onNavigateBack: (() -> Unit)? = null,
    onAddCategory: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalDuesColors.current
    var loggingCategory by remember { mutableStateOf<CategorySummary?>(null) }
    var showLogDialog by remember { mutableStateOf(false) }

    if (state.trackerId != trackerId) return

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            ClearrTopBar(
                title = state.trackerName,
                showLeading = onNavigateBack != null,
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
                        showSwipeHint = state.showSwipeHint,
                        onCategoryTap = { category ->
                            loggingCategory = category
                            showLogDialog = true
                        },
                        onCategoryDelete = { categoryId -> viewModel.onAction(BudgetAction.DeleteCategory(categoryId)) },
                        onSwipeHintDisplayed = { viewModel.onAction(BudgetAction.OnSwipeHintDisplayed) },
                        colors = colors
                    )
                }
                item { Spacer(Modifier.size(ClearrDimens.dp96)) }
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
            Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable(onClick = onAddCategory)) {
                Text("⊞", color = ClearrColors.Surface, fontSize = ClearrTextSizes.sp22)
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
            onAmountChange = { categoryId, amountNaira -> viewModel.onAction(BudgetAction.UpdateBudgetDraft(categoryId, amountNaira)) },
            onConfirm = { viewModel.onAction(BudgetAction.ConfirmBudgetSetup) }
        )
    }
}
