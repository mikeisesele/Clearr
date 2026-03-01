package com.mikeisesele.clearr.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerRoomDao {
    @Query("SELECT * FROM trackers ORDER BY createdAt ASC")
    fun getAllTrackers(): Flow<List<TrackerRoomEntity>>

    @Query("SELECT * FROM trackers WHERE id = :id")
    suspend fun getTrackerById(id: Long): TrackerRoomEntity?

    @Query("SELECT * FROM trackers WHERE id = :id")
    fun getTrackerByIdFlow(id: Long): Flow<TrackerRoomEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracker(tracker: TrackerRoomEntity): Long

    @Update
    suspend fun updateTracker(tracker: TrackerRoomEntity)

    @Query("DELETE FROM trackers WHERE id = :id")
    suspend fun deleteTracker(id: Long)

    @Query("UPDATE trackers SET isNew = 0 WHERE id = :id")
    suspend fun clearNewFlag(id: Long)
}
