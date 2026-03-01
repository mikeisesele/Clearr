package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import com.mikeisesele.clearr.ui.feature.todo.TodoAiResult
import com.mikeisesele.clearr.ui.feature.todo.TodoAiService
import kotlinx.datetime.LocalDate

class IosTodoAiService : TodoAiService {
    override suspend fun inferTodo(
        title: String,
        note: String?,
        selectedPriority: TodoPriority,
        selectedDueDate: LocalDate?
    ): TodoAiResult = TodoAiResult(
        normalizedTitle = normalizeTitle(title),
        normalizedNote = note?.trim()?.ifBlank { null },
        suggestedPriority = selectedPriority,
        suggestedDueDate = selectedDueDate
    )

    override suspend fun todoInsight(todos: List<TodoItem>): String? =
        when {
            todos.count { it.status.name == "OVERDUE" } > 0 -> "There are overdue todos that need immediate attention."
            todos.count { it.status.name == "PENDING" } > 3 -> "You have several pending todos queued up."
            else -> "Todo list looks manageable."
        }

    override fun normalizeTitle(input: String): String =
        input.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
