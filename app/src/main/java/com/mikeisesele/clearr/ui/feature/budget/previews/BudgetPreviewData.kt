package com.mikeisesele.clearr.ui.feature.budget

import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetPeriod
import com.mikeisesele.clearr.data.model.BudgetStatus
import com.mikeisesele.clearr.data.model.BudgetSummary
import com.mikeisesele.clearr.data.model.CategorySummary

internal val previewBudgetPeriods = listOf(
    BudgetPeriod(1L, 1L, BudgetFrequency.MONTHLY, "Nov 2025", 0L, 0L),
    BudgetPeriod(2L, 1L, BudgetFrequency.MONTHLY, "Dec 2025", 0L, 0L),
    BudgetPeriod(3L, 1L, BudgetFrequency.MONTHLY, "Jan 2026", 0L, 0L),
    BudgetPeriod(4L, 1L, BudgetFrequency.MONTHLY, "Feb 2026", 0L, 0L)
)

internal val previewBudgetSummaries = listOf(
    CategorySummary(
        category = BudgetCategory(1L, 1L, BudgetFrequency.MONTHLY, "Entertainment", "🎬", "Purple", 20_000_00L, 0),
        plannedAmountKobo = 20_000_00L,
        spentAmountKobo = 7_500_00L,
        remainingAmountKobo = 12_500_00L,
        percentUsed = 0.375f,
        status = BudgetStatus.ON_TRACK
    ),
    CategorySummary(
        category = BudgetCategory(2L, 1L, BudgetFrequency.MONTHLY, "Food", "🍔", "Orange", 25_000_00L, 1),
        plannedAmountKobo = 25_000_00L,
        spentAmountKobo = 0L,
        remainingAmountKobo = 25_000_00L,
        percentUsed = 0f,
        status = BudgetStatus.ON_TRACK
    ),
    CategorySummary(
        category = BudgetCategory(3L, 1L, BudgetFrequency.MONTHLY, "Housing", "🏠", "Violet", 50_000_00L, 2),
        plannedAmountKobo = 50_000_00L,
        spentAmountKobo = 5_000_00L,
        remainingAmountKobo = 45_000_00L,
        percentUsed = 0.1f,
        status = BudgetStatus.ON_TRACK
    ),
    CategorySummary(
        category = BudgetCategory(4L, 1L, BudgetFrequency.MONTHLY, "Savings", "💰", "Teal", 15_000_00L, 3),
        plannedAmountKobo = 15_000_00L,
        spentAmountKobo = 15_000_00L,
        remainingAmountKobo = 0L,
        percentUsed = 1f,
        status = BudgetStatus.CLEARED
    )
)

internal val previewBudgetSummary = BudgetSummary(
    totalPlannedKobo = 140_000_00L,
    totalSpentKobo = 27_500_00L,
    totalRemainingKobo = 112_500_00L,
    percentUsed = 0.1964f,
    isOverBudget = false,
    overBudgetCategories = emptyList()
)

internal val previewBudgetPlanDrafts = listOf(
    BudgetPlanDraft(1L, "Housing", "🏠", "Violet", 50_000_00L),
    BudgetPlanDraft(2L, "Food", "🍔", "Orange", 25_000_00L),
    BudgetPlanDraft(3L, "Transport", "🚗", "Blue", 15_000_00L),
    BudgetPlanDraft(4L, "Savings", "💰", "Amber", 15_000_00L)
)
