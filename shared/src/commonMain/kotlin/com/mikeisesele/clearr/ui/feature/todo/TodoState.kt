package com.mikeisesele.clearr.ui.feature.todo

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import kotlinx.datetime.LocalDate

data class TodoUiState(
    val trackerId: Long = -1,
    val trackerName: String = "My Todos",
    val filter: TodoFilter = TodoFilter.ALL,
    val todos: List<TodoItem> = emptyList(),
    val displayedTodos: List<TodoItem> = emptyList(),
    val counts: TodoCounts = TodoCounts(),
    val aiInsight: String? = null,
    val showSwipeHint: Boolean = true,
    val isLoading: Boolean = true
) : BaseState

enum class TodoFilter { ALL, PENDING, DONE }

data class TodoCounts(
    val pending: Int = 0,
    val overdue: Int = 0,
    val done: Int = 0
)

sealed interface TodoAction {
    data class SetFilter(val filter: TodoFilter) : TodoAction
    data class AddTodo(
        val title: String,
        val note: String?,
        val priority: TodoPriority,
        val dueDate: LocalDate?
    ) : TodoAction
    data class Rename(val id: String, val title: String) : TodoAction
    data class MarkDone(val id: String) : TodoAction
    data object MarkAllDone : TodoAction
    data class Delete(val id: String) : TodoAction
    data object ClearCompleted : TodoAction
    data object OnFirstSwipeAction : TodoAction
}

sealed interface TodoEvent : ViewEvent
