package com.mikeisesele.clearr.data.local.room

import com.mikeisesele.clearr.data.model.Goal
import com.mikeisesele.clearr.data.model.GoalCompletion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomGoalsRepository(
    private val goalDao: GoalRoomDao
) {
    fun getGoalsForTracker(trackerId: Long): Flow<List<Goal>> =
        goalDao.getGoalsForTracker(trackerId).map { list -> list.map(GoalRoomEntity::toDomain) }

    fun getGoalCompletionsForTracker(trackerId: Long): Flow<List<GoalCompletion>> =
        goalDao.getGoalCompletionsForTracker(trackerId).map { list -> list.map(GoalCompletionRoomEntity::toDomain) }

    suspend fun insertGoal(goal: Goal) {
        goalDao.upsertGoal(goal.toRoomEntity())
    }

    suspend fun addGoalCompletion(completion: GoalCompletion) {
        goalDao.insertCompletion(completion.toRoomEntity())
    }

    suspend fun deleteGoal(goalId: String) {
        goalDao.deleteCompletionsForGoal(goalId)
        goalDao.deleteGoal(goalId)
    }

    suspend fun deleteGoalsForTracker(trackerId: Long) {
        goalDao.deleteCompletionsForTracker(trackerId)
        goalDao.deleteGoalsForTracker(trackerId)
    }

    suspend fun isEmpty(): Boolean = goalDao.countGoals() == 0

    suspend fun seedGoals(goals: List<Goal>, completions: List<GoalCompletion>) {
        if (goals.isNotEmpty()) {
            goalDao.upsertGoals(goals.map(Goal::toRoomEntity))
        }
        if (completions.isNotEmpty()) {
            goalDao.insertCompletions(completions.map(GoalCompletion::toRoomEntity))
        }
    }
}
