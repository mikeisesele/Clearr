package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.core.time.nowEpochMillis
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import com.mikeisesele.clearr.domain.repository.OnboardingStatusRepository
import com.mikeisesele.clearr.preview.InMemoryBudgetPreferencesRepository
import com.mikeisesele.clearr.preview.InMemoryClearrRepository
import com.mikeisesele.clearr.preview.InMemoryOnboardingStatusRepository
import com.mikeisesele.clearr.preview.InMemoryTodoPreferencesRepository
import com.mikeisesele.clearr.preview.PreviewBudgetAiService
import com.mikeisesele.clearr.preview.PreviewGoalsAiService
import com.mikeisesele.clearr.preview.PreviewTodoAiService

class InMemoryClearrRuntime(
    override val repository: ClearrRepository = InMemoryClearrRepository.sample(),
    override val onboardingStatusRepository: OnboardingStatusRepository =
        InMemoryOnboardingStatusRepository(
            initialComplete = (repository as? InMemoryClearrRepository)?.snapshotAppConfig()?.setupComplete == true
        ),
    override val budgetPreferencesRepository: InMemoryBudgetPreferencesRepository = InMemoryBudgetPreferencesRepository(),
    override val todoPreferencesRepository: InMemoryTodoPreferencesRepository = InMemoryTodoPreferencesRepository(),
    override val budgetAiService: PreviewBudgetAiService = PreviewBudgetAiService(),
    override val todoAiService: PreviewTodoAiService = PreviewTodoAiService(),
    override val goalsAiService: PreviewGoalsAiService = PreviewGoalsAiService(),
    override val nowMillis: () -> Long = ::nowEpochMillis
) : ClearrRuntime
