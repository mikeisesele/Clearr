package com.mikeisesele.clearr.domain.repository

import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.Tracker
import kotlinx.coroutines.flow.Flow

interface ClearrRepository : AppConfigRepository, GoalsRepository, TodoRepository, BudgetRepository {
    fun getAllTrackers(): Flow<List<Tracker>>
    suspend fun getTrackerById(id: Long): Tracker?
    override fun getTrackerByIdFlow(id: Long): Flow<Tracker?>
    suspend fun insertTracker(tracker: Tracker): Long
    suspend fun updateTracker(tracker: Tracker)
    suspend fun deleteTracker(id: Long)
    suspend fun clearTrackerNewFlag(id: Long)
}
