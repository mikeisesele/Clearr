package com.mikeisesele.clearr.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(
    tableName = "goals",
    indices = [Index(value = ["trackerId"])]
)
data class GoalEntity(
    @PrimaryKey
    val id: String,
    val trackerId: Long,
    val title: String,
    val emoji: String,
    val colorToken: String,
    val target: String?,
    val frequency: GoalFrequency,
    val createdAt: Long
)

@Entity(
    tableName = "goal_completions",
    indices = [
        Index(value = ["goalId"]),
        Index(value = ["goalId", "periodKey"], unique = true)
    ]
)
data class GoalCompletionEntity(
    @PrimaryKey
    val id: String,
    val goalId: String,
    val periodKey: String,
    val completedAt: Long
)

fun GoalEntity.toDomain(): Goal = Goal(
    id = id,
    trackerId = trackerId,
    title = title,
    emoji = emoji,
    colorToken = colorToken,
    target = target,
    frequency = frequency,
    createdAt = createdAt
)

fun Goal.toEntity(): GoalEntity = GoalEntity(
    id = id,
    trackerId = trackerId,
    title = title,
    emoji = emoji,
    colorToken = colorToken,
    target = target,
    frequency = frequency,
    createdAt = createdAt
)

fun GoalCompletionEntity.toDomain(): GoalCompletion = GoalCompletion(
    id = id,
    goalId = goalId,
    periodKey = periodKey,
    completedAt = completedAt
)

fun GoalCompletion.toEntity(): GoalCompletionEntity = GoalCompletionEntity(
    id = id,
    goalId = goalId,
    periodKey = periodKey,
    completedAt = completedAt
)
