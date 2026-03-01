package com.mikeisesele.clearr.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mikeisesele.clearr.data.model.Tracker
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {

    @Query("SELECT * FROM trackers ORDER BY createdAt ASC")
    fun getAllTrackers(): Flow<List<Tracker>>

    @Query("SELECT * FROM trackers WHERE id = :id")
    suspend fun getTrackerById(id: Long): Tracker?

    @Query("SELECT * FROM trackers WHERE id = :id")
    fun getTrackerByIdFlow(id: Long): Flow<Tracker?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracker(tracker: Tracker): Long

    @Update
    suspend fun updateTracker(tracker: Tracker)

    @Query("DELETE FROM trackers WHERE id = :id")
    suspend fun deleteTrackerRow(id: Long)

    @Query("DELETE FROM budget_entries WHERE trackerId = :trackerId")
    suspend fun deleteBudgetEntriesForTracker(trackerId: Long)

    @Query("DELETE FROM budget_category_plans WHERE periodId IN (SELECT id FROM budget_periods WHERE trackerId = :trackerId)")
    suspend fun deleteBudgetPlansForTracker(trackerId: Long)

    @Query("DELETE FROM budget_categories WHERE trackerId = :trackerId")
    suspend fun deleteBudgetCategoriesForTracker(trackerId: Long)

    @Query("DELETE FROM budget_periods WHERE trackerId = :trackerId")
    suspend fun deleteBudgetPeriodsForTracker(trackerId: Long)

    @Query("DELETE FROM todos WHERE trackerId = :trackerId")
    suspend fun deleteTodosForTracker(trackerId: Long)

    @Query("DELETE FROM goal_completions WHERE goalId IN (SELECT id FROM goals WHERE trackerId = :trackerId)")
    suspend fun deleteGoalCompletionsForTracker(trackerId: Long)

    @Query("DELETE FROM goals WHERE trackerId = :trackerId")
    suspend fun deleteGoalsForTracker(trackerId: Long)

    @Transaction
    suspend fun deleteTracker(trackerId: Long) {
        deleteBudgetEntriesForTracker(trackerId)
        deleteBudgetPlansForTracker(trackerId)
        deleteBudgetCategoriesForTracker(trackerId)
        deleteBudgetPeriodsForTracker(trackerId)
        deleteTodosForTracker(trackerId)
        deleteGoalCompletionsForTracker(trackerId)
        deleteGoalsForTracker(trackerId)
        deleteTrackerRow(trackerId)
    }

    @Query("UPDATE trackers SET isNew = 0 WHERE id = :id")
    suspend fun clearNewFlag(id: Long)
}
