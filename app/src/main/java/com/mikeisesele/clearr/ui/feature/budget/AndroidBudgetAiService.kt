package com.mikeisesele.clearr.ui.feature.budget

import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.CategorySummary
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidBudgetAiService @Inject constructor() : BudgetAiService {
    override suspend fun inferBudgetCategoryId(note: String?, categories: List<BudgetCategory>): Long? =
        ClearrEdgeAi.inferBudgetCategoryIdNanoAware(note = note, categories = categories)

    override suspend fun budgetInsight(summaries: List<CategorySummary>): String? =
        ClearrEdgeAi.budgetInsightNanoAware(summaries)
}
