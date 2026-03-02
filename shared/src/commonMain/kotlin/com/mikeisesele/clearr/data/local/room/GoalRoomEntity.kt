package com.mikeisesele.clearr.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mikeisesele.clearr.data.model.Goal
import com.mikeisesele.clearr.data.model.GoalFrequency

@Entity(tableName = "goals")
data class GoalRoomEntity(
    @PrimaryKey val id: String,
    val trackerId: Long,
    val title: String,
    val emoji: String,
    val colorToken: String,
    val target: String? = null,
    val frequency: GoalFrequency,
    val createdAt: Long
)

fun GoalRoomEntity.toDomain(): Goal = Goal(
    id = id,
    trackerId = trackerId,
    title = title,
    emoji = emoji,
    colorToken = colorToken,
    target = target,
    frequency = frequency,
    createdAt = createdAt
)

fun Goal.toRoomEntity(): GoalRoomEntity = GoalRoomEntity(
    id = id,
    trackerId = trackerId,
    title = title,
    emoji = emoji,
    colorToken = colorToken,
    target = target,
    frequency = frequency,
    createdAt = createdAt
)
