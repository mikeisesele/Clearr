package com.mikeisesele.clearr.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mikeisesele.clearr.data.model.GoalCompletion

@Entity(tableName = "goal_completions")
data class GoalCompletionRoomEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val periodKey: String,
    val completedAt: Long
)

fun GoalCompletionRoomEntity.toDomain(): GoalCompletion = GoalCompletion(
    id = id,
    goalId = goalId,
    periodKey = periodKey,
    completedAt = completedAt
)

fun GoalCompletion.toRoomEntity(): GoalCompletionRoomEntity = GoalCompletionRoomEntity(
    id = id,
    goalId = goalId,
    periodKey = periodKey,
    completedAt = completedAt
)
