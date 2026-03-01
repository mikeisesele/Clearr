package com.mikeisesele.clearr.ui.feature.todo

import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidTodoAiService @Inject constructor() : TodoAiService {
    override suspend fun inferTodo(
        title: String,
        note: String?,
        selectedPriority: com.mikeisesele.clearr.data.model.TodoPriority,
        selectedDueDate: java.time.LocalDate?
    ): TodoAiResult {
        val result = ClearrEdgeAi.inferTodoNanoAware(
            title = title,
            note = note,
            selectedPriority = selectedPriority,
            selectedDueDate = selectedDueDate
        )
        return TodoAiResult(
            normalizedTitle = result.normalizedTitle,
            normalizedNote = result.normalizedNote,
            suggestedPriority = result.suggestedPriority,
            suggestedDueDate = result.suggestedDueDate
        )
    }

    override suspend fun todoInsight(todos: List<com.mikeisesele.clearr.data.model.TodoItem>): String? =
        ClearrEdgeAi.todoInsightNanoAware(todos)

    override fun normalizeTitle(input: String): String = ClearrEdgeAi.normalizeTitle(input)
}
