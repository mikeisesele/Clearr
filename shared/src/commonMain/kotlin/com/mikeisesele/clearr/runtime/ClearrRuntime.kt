package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.domain.repository.BudgetPreferencesRepository
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import com.mikeisesele.clearr.domain.repository.TodoPreferencesRepository
import com.mikeisesele.clearr.ui.feature.budget.BudgetAiService
import com.mikeisesele.clearr.ui.feature.goals.GoalsAiService
import com.mikeisesele.clearr.ui.feature.todo.TodoAiService

interface ClearrRuntime {
    val repository: ClearrRepository
    val budgetPreferencesRepository: BudgetPreferencesRepository
    val todoPreferencesRepository: TodoPreferencesRepository
    val budgetAiService: BudgetAiService
    val todoAiService: TodoAiService
    val goalsAiService: GoalsAiService
    val nowMillis: () -> Long
}
