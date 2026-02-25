package com.mikeisesele.clearr.ui.feature.budget

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetPeriod
import com.mikeisesele.clearr.data.model.BudgetSummary
import com.mikeisesele.clearr.data.model.CategorySummary

data class BudgetUiState(
    val trackerId: Long = -1,
    val trackerName: String = "Budget Tracker",
    val frequency: BudgetFrequency = BudgetFrequency.MONTHLY,
    val periods: List<BudgetPeriod> = emptyList(),
    val selectedPeriodId: Long? = null,
    val categorySummaries: List<CategorySummary> = emptyList(),
    val aiInsight: String? = null,
    val budgetSummary: BudgetSummary = BudgetSummary(
        totalPlannedKobo = 0,
        totalSpentKobo = 0,
        totalRemainingKobo = 0,
        percentUsed = 0f,
        isOverBudget = false,
        overBudgetCategories = emptyList()
    ),
    val isLoading: Boolean = true
) : BaseState

sealed interface BudgetAction {
    data class SetFrequency(val frequency: BudgetFrequency) : BudgetAction
    data class SelectPeriod(val periodId: Long) : BudgetAction
    data class LogExpense(val categoryId: Long, val amountNaira: Double, val note: String?) : BudgetAction
    data class AddCategory(
        val name: String,
        val icon: String,
        val colorToken: String,
        val plannedAmountNaira: Double
    ) : BudgetAction
}

sealed interface BudgetEvent : ViewEvent
