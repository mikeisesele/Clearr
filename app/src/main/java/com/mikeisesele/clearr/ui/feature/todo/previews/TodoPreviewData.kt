package com.mikeisesele.clearr.ui.feature.todo.previews

import com.mikeisesele.clearr.core.time.plusDays
import com.mikeisesele.clearr.core.time.todayLocalDate
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import com.mikeisesele.clearr.data.model.TodoStatus

internal val previewTodoItem = TodoItem(
    id = "todo-1",
    trackerId = 1L,
    title = "Pay rent",
    note = "Send before noon",
    priority = TodoPriority.HIGH,
    dueDate = todayLocalDate().plusDays(1),
    status = TodoStatus.PENDING,
    createdAt = 0L,
    completedAt = null
)
