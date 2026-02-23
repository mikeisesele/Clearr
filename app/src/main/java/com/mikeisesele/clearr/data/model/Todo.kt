package com.mikeisesele.clearr.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "todos",
    indices = [Index(value = ["trackerId"])]
)
data class TodoEntity(
    @PrimaryKey
    val id: String,
    val trackerId: Long,
    val title: String,
    val note: String?,
    val priority: TodoPriority,
    /** ISO-8601 date string (yyyy-MM-dd). Null means no due date. */
    val dueDate: String?,
    /** Persisted values: PENDING or DONE only. */
    val status: TodoStatus,
    val createdAt: Long,
    val completedAt: Long?
)

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

fun TodoItem.derivedStatus(today: LocalDate = LocalDate.now()): TodoStatus = when {
    status == TodoStatus.DONE -> TodoStatus.DONE
    dueDate != null && dueDate < today -> TodoStatus.OVERDUE
    else -> TodoStatus.PENDING
}

fun TodoEntity.toDomain(): TodoItem = TodoItem(
    id = id,
    trackerId = trackerId,
    title = title,
    note = note,
    priority = priority,
    dueDate = dueDate?.let { LocalDate.parse(it) },
    status = status,
    createdAt = createdAt,
    completedAt = completedAt
)

fun TodoItem.toEntity(): TodoEntity = TodoEntity(
    id = id,
    trackerId = trackerId,
    title = title,
    note = note,
    priority = priority,
    dueDate = dueDate?.toString(),
    status = when (status) {
        TodoStatus.DONE -> TodoStatus.DONE
        else -> TodoStatus.PENDING
    },
    createdAt = createdAt,
    completedAt = completedAt
)
