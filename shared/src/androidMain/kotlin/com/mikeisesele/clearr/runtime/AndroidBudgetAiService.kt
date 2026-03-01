package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.CategorySummary
import com.mikeisesele.clearr.ui.feature.budget.BudgetAiService

class AndroidBudgetAiService : BudgetAiService {
    override suspend fun inferBudgetCategoryId(note: String?, categories: List<BudgetCategory>): Long? {
        val query = note?.lowercase()?.trim().orEmpty()
        if (query.isBlank()) return null
        return categories.firstOrNull { category ->
            category.name.lowercase() in query || query in category.name.lowercase()
        }?.id
    }

    override suspend fun budgetInsight(summaries: List<CategorySummary>): String? =
        when {
            summaries.isEmpty() -> "No budget categories yet."
            summaries.any { it.remainingAmountKobo < 0 } -> "Some categories are already over budget."
            else -> "Budget is still within the planned range."
        }
}
