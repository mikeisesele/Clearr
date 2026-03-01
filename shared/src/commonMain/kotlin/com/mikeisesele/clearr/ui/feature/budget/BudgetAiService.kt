package com.mikeisesele.clearr.ui.feature.budget

import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.CategorySummary

interface BudgetAiService {
    suspend fun inferBudgetCategoryId(note: String?, categories: List<BudgetCategory>): Long?

    suspend fun budgetInsight(summaries: List<CategorySummary>): String?
}
