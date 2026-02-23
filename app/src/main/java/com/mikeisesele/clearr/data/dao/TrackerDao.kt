package com.mikeisesele.clearr.data.dao

import androidx.room.*
import com.mikeisesele.clearr.data.model.RecordStatus
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.data.model.TrackerPeriod
import com.mikeisesele.clearr.data.model.TrackerRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {

    // ── Trackers ──────────────────────────────────────────────────────────────

    @Query("SELECT * FROM trackers ORDER BY createdAt DESC")
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

    @Query("DELETE FROM tracker_members WHERE trackerId = :trackerId")
    suspend fun deleteMembersForTracker(trackerId: Long)

    @Query("DELETE FROM tracker_periods WHERE trackerId = :trackerId")
    suspend fun deletePeriodsForTracker(trackerId: Long)

    @Query("DELETE FROM tracker_records WHERE trackerId = :trackerId")
    suspend fun deleteRecordsForTracker(trackerId: Long)

    @Query("DELETE FROM budget_entries WHERE trackerId = :trackerId")
    suspend fun deleteBudgetEntriesForTracker(trackerId: Long)

    @Query("DELETE FROM budget_categories WHERE trackerId = :trackerId")
    suspend fun deleteBudgetCategoriesForTracker(trackerId: Long)

    @Query("DELETE FROM budget_periods WHERE trackerId = :trackerId")
    suspend fun deleteBudgetPeriodsForTracker(trackerId: Long)

    @Transaction
    suspend fun deleteTracker(trackerId: Long) {
        deleteRecordsForTracker(trackerId)
        deleteMembersForTracker(trackerId)
        deletePeriodsForTracker(trackerId)
        deleteBudgetEntriesForTracker(trackerId)
        deleteBudgetCategoriesForTracker(trackerId)
        deleteBudgetPeriodsForTracker(trackerId)
        deleteTrackerRow(trackerId)
    }

    /** Clear isNew flag after first open */
    @Query("UPDATE trackers SET isNew = 0 WHERE id = :id")
    suspend fun clearNewFlag(id: Long)

    // ── TrackerMembers ────────────────────────────────────────────────────────

    @Query("SELECT * FROM tracker_members WHERE trackerId = :trackerId AND isArchived = 0 ORDER BY name ASC")
    fun getActiveMembers(trackerId: Long): Flow<List<TrackerMember>>

    @Query("SELECT * FROM tracker_members WHERE trackerId = :trackerId ORDER BY name ASC")
    fun getAllMembers(trackerId: Long): Flow<List<TrackerMember>>

    @Query("SELECT COUNT(*) FROM tracker_members WHERE trackerId = :trackerId AND isArchived = 0")
    fun getActiveMemberCount(trackerId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: TrackerMember): Long

    @Update
    suspend fun updateMember(member: TrackerMember)

    @Query("UPDATE tracker_members SET isArchived = :archived WHERE id = :id")
    suspend fun setMemberArchived(id: Long, archived: Boolean)

    @Query("DELETE FROM tracker_records WHERE trackerId = :trackerId AND memberId = :memberId")
    suspend fun deleteRecordsForTrackerMember(trackerId: Long, memberId: Long)

    @Query("DELETE FROM tracker_members WHERE trackerId = :trackerId AND id = :memberId")
    suspend fun deleteTrackerMember(trackerId: Long, memberId: Long)

    // ── TrackerPeriods ────────────────────────────────────────────────────────

    @Query("SELECT * FROM tracker_periods WHERE trackerId = :trackerId ORDER BY startDate ASC")
    fun getPeriodsForTracker(trackerId: Long): Flow<List<TrackerPeriod>>

    @Query("SELECT * FROM tracker_periods WHERE trackerId = :trackerId AND isCurrent = 1 LIMIT 1")
    suspend fun getCurrentPeriod(trackerId: Long): TrackerPeriod?

    @Query("SELECT * FROM tracker_periods WHERE trackerId = :trackerId AND isCurrent = 1 LIMIT 1")
    fun getCurrentPeriodFlow(trackerId: Long): Flow<TrackerPeriod?>

    @Query("SELECT * FROM tracker_periods WHERE id = :id LIMIT 1")
    suspend fun getPeriodById(id: Long): TrackerPeriod?

    @Query("SELECT * FROM tracker_periods WHERE trackerId = :trackerId AND label = :label LIMIT 1")
    suspend fun getPeriodByLabel(trackerId: Long, label: String): TrackerPeriod?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriod(period: TrackerPeriod): Long

    @Update
    suspend fun updatePeriod(period: TrackerPeriod)

    /** Mark all periods for a tracker as not current, then set the given one as current */
    @Query("UPDATE tracker_periods SET isCurrent = 0 WHERE trackerId = :trackerId")
    suspend fun clearCurrentPeriods(trackerId: Long)

    @Query("UPDATE tracker_periods SET isCurrent = 1 WHERE id = :periodId")
    suspend fun setCurrentPeriod(periodId: Long)

    // ── TrackerRecords ────────────────────────────────────────────────────────

    @Query("SELECT * FROM tracker_records WHERE trackerId = :trackerId AND periodId = :periodId")
    fun getRecordsForPeriod(trackerId: Long, periodId: Long): Flow<List<TrackerRecord>>

    @Query("SELECT * FROM tracker_records WHERE trackerId = :trackerId")
    fun getRecordsForTracker(trackerId: Long): Flow<List<TrackerRecord>>

    @Query("SELECT * FROM tracker_records WHERE trackerId = :trackerId AND periodId = :periodId AND memberId = :memberId LIMIT 1")
    suspend fun getRecord(trackerId: Long, periodId: Long, memberId: Long): TrackerRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TrackerRecord): Long

    @Update
    suspend fun updateRecord(record: TrackerRecord)

    @Query("DELETE FROM tracker_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)

    /** Count completed records for a period (status IN (PAID, PRESENT, DONE)) */
    @Query("""
        SELECT COUNT(*) FROM tracker_records
        WHERE trackerId = :trackerId
        AND periodId = :periodId
        AND status IN ('PAID', 'PRESENT', 'DONE')
    """)
    suspend fun getCompletedCountForPeriod(trackerId: Long, periodId: Long): Int
}
