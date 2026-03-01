package com.mikeisesele.clearr.data.model

import com.mikeisesele.clearr.core.time.todayLocalDate
import kotlinx.datetime.LocalDate

data class TodoItem(
    val id: String,
    val trackerId: Long,
    val title: String,
    val note: String? = null,
    val priority: TodoPriority,
    val dueDate: LocalDate? = null,
    val status: TodoStatus,
    val createdAt: Long,
    val completedAt: Long? = null
)

enum class TodoPriority { HIGH, MEDIUM, LOW }

enum class TodoStatus { PENDING, OVERDUE, DONE }

fun TodoItem.derivedStatus(today: LocalDate = todayLocalDate()): TodoStatus = when {
    status == TodoStatus.DONE -> TodoStatus.DONE
    dueDate != null && dueDate < today -> TodoStatus.OVERDUE
    else -> TodoStatus.PENDING
}
