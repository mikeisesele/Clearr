package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.core.time.nowEpochMillis
import com.mikeisesele.clearr.domain.repository.BudgetPreferencesRepository
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import com.mikeisesele.clearr.domain.repository.OnboardingStatusRepository
import com.mikeisesele.clearr.domain.repository.TodoPreferencesRepository
import com.mikeisesele.clearr.preview.InMemoryBudgetPreferencesRepository
import com.mikeisesele.clearr.preview.InMemoryClearrRepository
import com.mikeisesele.clearr.preview.InMemoryOnboardingStatusRepository
import com.mikeisesele.clearr.preview.InMemoryTodoPreferencesRepository
import com.mikeisesele.clearr.preview.PreviewBudgetAiService
import com.mikeisesele.clearr.preview.PreviewGoalsAiService
import com.mikeisesele.clearr.preview.PreviewTodoAiService
import com.mikeisesele.clearr.ui.feature.budget.BudgetAiService
import com.mikeisesele.clearr.ui.feature.goals.GoalsAiService
import com.mikeisesele.clearr.ui.feature.todo.TodoAiService

class InMemoryClearrRuntime(
    override val repository: ClearrRepository = InMemoryClearrRepository.sample(),
    override val onboardingStatusRepository: OnboardingStatusRepository =
        InMemoryOnboardingStatusRepository(
            initialComplete = (repository as? InMemoryClearrRepository)?.snapshotAppConfig()?.setupComplete == true
        ),
    override val budgetPreferencesRepository: BudgetPreferencesRepository = InMemoryBudgetPreferencesRepository(),
    override val todoPreferencesRepository: TodoPreferencesRepository = InMemoryTodoPreferencesRepository(),
    override val budgetAiService: BudgetAiService = PreviewBudgetAiService(),
    override val todoAiService: TodoAiService = PreviewTodoAiService(),
    override val goalsAiService: GoalsAiService = PreviewGoalsAiService(),
    override val nowMillis: () -> Long = ::nowEpochMillis
) : ClearrRuntime
