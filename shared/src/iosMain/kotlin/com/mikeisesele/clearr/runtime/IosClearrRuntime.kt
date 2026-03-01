package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.core.time.nowEpochMillis
import com.mikeisesele.clearr.preview.InMemoryBudgetPreferencesRepository
import com.mikeisesele.clearr.preview.InMemoryClearrRepository
import com.mikeisesele.clearr.preview.InMemoryTodoPreferencesRepository
import com.mikeisesele.clearr.preview.PreviewBudgetAiService
import com.mikeisesele.clearr.preview.PreviewGoalsAiService
import com.mikeisesele.clearr.preview.PreviewTodoAiService

class IosClearrRuntime(
    override val repository: InMemoryClearrRepository = InMemoryClearrRepository.sample(),
    override val budgetPreferencesRepository: InMemoryBudgetPreferencesRepository = InMemoryBudgetPreferencesRepository(),
    override val todoPreferencesRepository: InMemoryTodoPreferencesRepository = InMemoryTodoPreferencesRepository(),
    override val budgetAiService: PreviewBudgetAiService = PreviewBudgetAiService(),
    override val todoAiService: PreviewTodoAiService = PreviewTodoAiService(),
    override val goalsAiService: PreviewGoalsAiService = PreviewGoalsAiService(),
    override val nowMillis: () -> Long = ::nowEpochMillis
) : ClearrRuntime
