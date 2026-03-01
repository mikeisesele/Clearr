package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.core.time.nowEpochMillis
import com.mikeisesele.clearr.preview.InMemoryClearrRepository
import com.mikeisesele.clearr.preview.InMemoryOnboardingStatusRepository

class AndroidClearrRuntime(
    override val repository: InMemoryClearrRepository = InMemoryClearrRepository.sample(),
    override val onboardingStatusRepository: InMemoryOnboardingStatusRepository = InMemoryOnboardingStatusRepository(),
    override val budgetPreferencesRepository: AndroidBudgetPreferencesRepository = AndroidBudgetPreferencesRepository(),
    override val todoPreferencesRepository: AndroidTodoPreferencesRepository = AndroidTodoPreferencesRepository(),
    override val budgetAiService: AndroidBudgetAiService = AndroidBudgetAiService(),
    override val todoAiService: AndroidTodoAiService = AndroidTodoAiService(),
    override val goalsAiService: AndroidGoalsAiService = AndroidGoalsAiService(),
    override val nowMillis: () -> Long = ::nowEpochMillis
) : ClearrRuntime
