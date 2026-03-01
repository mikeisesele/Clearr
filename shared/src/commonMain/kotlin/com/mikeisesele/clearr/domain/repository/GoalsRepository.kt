package com.mikeisesele.clearr.domain.repository

import com.mikeisesele.clearr.data.model.Goal
import com.mikeisesele.clearr.data.model.GoalCompletion
import com.mikeisesele.clearr.data.model.Tracker
import kotlinx.coroutines.flow.Flow

interface GoalsRepository {
    fun getTrackerByIdFlow(id: Long): Flow<Tracker?>
    fun getGoalsForTracker(trackerId: Long): Flow<List<Goal>>
    fun getGoalCompletionsForTracker(trackerId: Long): Flow<List<GoalCompletion>>
    suspend fun insertGoal(goal: Goal)
    suspend fun addGoalCompletion(completion: GoalCompletion)
    suspend fun deleteGoal(goalId: String)
}
