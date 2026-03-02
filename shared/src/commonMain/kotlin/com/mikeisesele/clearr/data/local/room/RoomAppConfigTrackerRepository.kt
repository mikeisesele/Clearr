package com.mikeisesele.clearr.data.local.room

import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.Tracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomAppConfigTrackerRepository(
    private val appConfigDao: AppConfigRoomDao,
    private val trackerDao: TrackerRoomDao
) {
    fun getAppConfigFlow(): Flow<AppConfig?> = appConfigDao.getConfigFlow().map { it?.toDomain() }

    suspend fun getAppConfig(): AppConfig? = appConfigDao.getConfig()?.toDomain()

    suspend fun upsertAppConfig(config: AppConfig) {
        appConfigDao.upsertConfig(config.toRoomEntity())
    }

    fun getAllTrackers(): Flow<List<Tracker>> = trackerDao.getAllTrackers().map { list -> list.map { it.toDomain() } }

    suspend fun getTrackerById(id: Long): Tracker? = trackerDao.getTrackerById(id)?.toDomain()

    fun getTrackerByIdFlow(id: Long): Flow<Tracker?> = trackerDao.getTrackerByIdFlow(id).map { it?.toDomain() }

    suspend fun insertTracker(tracker: Tracker): Long = trackerDao.insertTracker(tracker.toRoomEntity())

    suspend fun updateTracker(tracker: Tracker) {
        trackerDao.updateTracker(tracker.toRoomEntity())
    }

    suspend fun deleteTracker(id: Long) {
        trackerDao.deleteTracker(id)
    }

    suspend fun clearTrackerNewFlag(id: Long) {
        trackerDao.clearNewFlag(id)
    }
}
