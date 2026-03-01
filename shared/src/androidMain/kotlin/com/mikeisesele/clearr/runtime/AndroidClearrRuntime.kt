package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.core.time.nowEpochMillis
import com.mikeisesele.clearr.preview.InMemoryClearrRepository
import com.mikeisesele.clearr.preview.InMemoryOnboardingStatusRepository

class AndroidClearrRuntime(
    private val delegate: ClearrRuntime = InMemoryClearrRuntime(
        repository = InMemoryClearrRepository.sample(),
        onboardingStatusRepository = InMemoryOnboardingStatusRepository(),
        budgetPreferencesRepository = AndroidBudgetPreferencesRepository(),
        todoPreferencesRepository = AndroidTodoPreferencesRepository(),
        budgetAiService = AndroidBudgetAiService(),
        todoAiService = AndroidTodoAiService(),
        goalsAiService = AndroidGoalsAiService(),
        nowMillis = ::nowEpochMillis
    )
) : ClearrRuntime by delegate
