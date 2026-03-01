package com.mikeisesele.clearr.ui.feature.todo

import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import java.time.LocalDate

data class TodoAiResult(
    val normalizedTitle: String,
    val normalizedNote: String?,
    val suggestedPriority: TodoPriority,
    val suggestedDueDate: LocalDate?
)

interface TodoAiService {
    suspend fun inferTodo(
        title: String,
        note: String?,
        selectedPriority: TodoPriority,
        selectedDueDate: LocalDate?
    ): TodoAiResult

    suspend fun todoInsight(todos: List<TodoItem>): String?

    fun normalizeTitle(input: String): String
}
