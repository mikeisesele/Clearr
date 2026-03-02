package com.mikeisesele.clearr.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import com.mikeisesele.clearr.data.model.TodoStatus
import kotlinx.datetime.LocalDate

@Entity(tableName = "todos")
data class TodoRoomEntity(
    @PrimaryKey val id: String,
    val trackerId: Long,
    val title: String,
    val note: String? = null,
    val priority: TodoPriority,
    val dueDateIso: String? = null,
    val status: TodoStatus,
    val createdAt: Long,
    val completedAt: Long? = null
)

fun TodoRoomEntity.toDomain(): TodoItem = TodoItem(
    id = id,
    trackerId = trackerId,
    title = title,
    note = note,
    priority = priority,
    dueDate = dueDateIso?.let(LocalDate::parse),
    status = status,
    createdAt = createdAt,
    completedAt = completedAt
)

fun TodoItem.toRoomEntity(): TodoRoomEntity = TodoRoomEntity(
    id = id,
    trackerId = trackerId,
    title = title,
    note = note,
    priority = priority,
    dueDateIso = dueDate?.toString(),
    status = status,
    createdAt = createdAt,
    completedAt = completedAt
)
