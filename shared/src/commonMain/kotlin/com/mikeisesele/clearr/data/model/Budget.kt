package com.mikeisesele.clearr.data.model

enum class BudgetFrequency {
    MONTHLY,
    WEEKLY
}

data class BudgetPeriod(
    val id: Long = 0,
    val trackerId: Long,
    val frequency: BudgetFrequency,
    val label: String,
    val startDate: Long,
    val endDate: Long
)

data class BudgetCategory(
    val id: Long = 0,
    val trackerId: Long,
    val frequency: BudgetFrequency,
    val name: String,
    val icon: String,
    val colorToken: String,
    val plannedAmountKobo: Long,
    val sortOrder: Int,
    val createdAt: Long = System.currentTimeMillis()
)

data class BudgetEntry(
    val id: Long = 0,
    val trackerId: Long,
    val categoryId: Long,
    val periodId: Long,
    val amountKobo: Long,
    val note: String? = null,
    val loggedAt: Long = System.currentTimeMillis()
)

data class BudgetCategoryPlan(
    val id: Long = 0,
    val trackerId: Long,
    val categoryId: Long,
    val periodId: Long,
    val plannedAmountKobo: Long,
    val createdAt: Long = System.currentTimeMillis()
)

enum class BudgetStatus {
    ON_TRACK,
    NEAR_LIMIT,
    OVER_BUDGET,
    CLEARED
}

data class CategorySummary(
    val category: BudgetCategory,
    val plannedAmountKobo: Long,
    val spentAmountKobo: Long,
    val remainingAmountKobo: Long,
    val percentUsed: Float,
    val status: BudgetStatus
)

data class BudgetSummary(
    val totalPlannedKobo: Long,
    val totalSpentKobo: Long,
    val totalRemainingKobo: Long,
    val percentUsed: Float,
    val isOverBudget: Boolean,
    val overBudgetCategories: List<CategorySummary>
)
