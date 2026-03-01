package com.mikeisesele.clearr.preview

import com.mikeisesele.clearr.core.time.nowEpochMillis
import com.mikeisesele.clearr.runtime.ClearrRuntime

class SampleClearrRuntime(
    override val repository: InMemoryClearrRepository = InMemoryClearrRepository.sample(),
    override val budgetPreferencesRepository: InMemoryBudgetPreferencesRepository = InMemoryBudgetPreferencesRepository(),
    override val todoPreferencesRepository: InMemoryTodoPreferencesRepository = InMemoryTodoPreferencesRepository(),
    override val budgetAiService: PreviewBudgetAiService = PreviewBudgetAiService(),
    override val todoAiService: PreviewTodoAiService = PreviewTodoAiService(),
    override val goalsAiService: PreviewGoalsAiService = PreviewGoalsAiService(),
    override val nowMillis: () -> Long = ::nowEpochMillis
) : ClearrRuntime
