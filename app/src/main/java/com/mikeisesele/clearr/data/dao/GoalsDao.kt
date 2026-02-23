package com.mikeisesele.clearr.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mikeisesele.clearr.data.model.GoalCompletionEntity
import com.mikeisesele.clearr.data.model.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalsDao {

    @Query("SELECT * FROM goals WHERE trackerId = :trackerId ORDER BY createdAt ASC")
    fun getGoals(trackerId: Long): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goal_completions WHERE goalId IN (SELECT id FROM goals WHERE trackerId = :trackerId)")
    fun getAllCompletions(trackerId: Long): Flow<List<GoalCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: GoalCompletionEntity)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoalRow(goalId: String)

    @Query("DELETE FROM goal_completions WHERE goalId = :goalId")
    suspend fun deleteCompletionsForGoal(goalId: String)

    @Transaction
    suspend fun deleteGoal(goalId: String) {
        deleteCompletionsForGoal(goalId)
        deleteGoalRow(goalId)
    }

    @Query("DELETE FROM goal_completions WHERE goalId IN (SELECT id FROM goals WHERE trackerId = :trackerId)")
    suspend fun deleteCompletionsForTracker(trackerId: Long)

    @Query("DELETE FROM goals WHERE trackerId = :trackerId")
    suspend fun deleteGoalsForTracker(trackerId: Long)

    @Transaction
    suspend fun deleteAllForTracker(trackerId: Long) {
        deleteCompletionsForTracker(trackerId)
        deleteGoalsForTracker(trackerId)
    }
}
