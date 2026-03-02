package com.mikeisesele.clearr.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalRoomDao {
    @Query("SELECT * FROM goals WHERE trackerId = :trackerId ORDER BY createdAt DESC")
    fun getGoalsForTracker(trackerId: Long): Flow<List<GoalRoomEntity>>

    @Query(
        """
        SELECT gc.* FROM goal_completions gc
        INNER JOIN goals g ON g.id = gc.goalId
        WHERE g.trackerId = :trackerId
        ORDER BY gc.completedAt DESC
        """
    )
    fun getGoalCompletionsForTracker(trackerId: Long): Flow<List<GoalCompletionRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: GoalRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoals(goals: List<GoalRoomEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: GoalCompletionRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletions(completions: List<GoalCompletionRoomEntity>)

    @Query("DELETE FROM goal_completions WHERE goalId = :goalId")
    suspend fun deleteCompletionsForGoal(goalId: String)

    @Query("DELETE FROM goal_completions WHERE goalId IN (SELECT id FROM goals WHERE trackerId = :trackerId)")
    suspend fun deleteCompletionsForTracker(trackerId: Long)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoal(goalId: String)

    @Query("DELETE FROM goals WHERE trackerId = :trackerId")
    suspend fun deleteGoalsForTracker(trackerId: Long)

    @Query("SELECT COUNT(*) FROM goals")
    suspend fun countGoals(): Int
}
