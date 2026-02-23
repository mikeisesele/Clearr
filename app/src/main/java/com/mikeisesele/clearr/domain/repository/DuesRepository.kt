package com.mikeisesele.clearr.domain.repository

import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.data.model.PaymentRecord
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.data.model.TrackerPeriod
import com.mikeisesele.clearr.data.model.TrackerRecord
import com.mikeisesele.clearr.data.model.YearConfig
import kotlinx.coroutines.flow.Flow

interface DuesRepository {

    // App Config
    fun getAppConfigFlow(): Flow<AppConfig?>
    suspend fun getAppConfig(): AppConfig?
    suspend fun upsertAppConfig(config: AppConfig)

    // Members (legacy global list — kept for existing HomeScreen dues tracker)
    fun getAllMembers(): Flow<List<Member>>
    fun getActiveMembers(): Flow<List<Member>>
    suspend fun getMemberById(id: Long): Member?
    suspend fun insertMember(member: Member): Long
    suspend fun updateMember(member: Member)
    suspend fun setMemberArchived(id: Long, archived: Boolean)
    suspend fun deleteMember(id: Long)

    // Payments (legacy — kept for existing HomeScreen dues tracker)
    fun getPaymentsForYear(year: Int): Flow<List<PaymentRecord>>
    fun getPaymentsForMemberYear(memberId: Long, year: Int): Flow<List<PaymentRecord>>
    suspend fun getLatestPayment(memberId: Long, year: Int, monthIndex: Int): PaymentRecord?
    suspend fun getTotalPaidForMonth(memberId: Long, year: Int, monthIndex: Int): Double
    fun getTotalCollectedForYear(year: Int): Flow<Double>
    suspend fun insertPayment(record: PaymentRecord): Long
    suspend fun undoPayment(id: Long)
    suspend fun deletePaymentsForMonth(memberId: Long, year: Int, monthIndex: Int)

    // Year Configs (legacy — kept for existing HomeScreen dues tracker)
    fun getAllYearConfigs(): Flow<List<YearConfig>>
    suspend fun getYearConfig(year: Int): YearConfig?
    fun getYearConfigFlow(year: Int): Flow<YearConfig?>
    suspend fun insertYearConfig(config: YearConfig)
    suspend fun updateDueAmount(year: Int, amount: Double)
    suspend fun ensureYearConfig(year: Int, defaultAmount: Double = 5000.0)

    // ── Multi-tracker: Trackers ───────────────────────────────────────────────
    fun getAllTrackers(): Flow<List<Tracker>>
    suspend fun getTrackerById(id: Long): Tracker?
    fun getTrackerByIdFlow(id: Long): Flow<Tracker?>
    suspend fun insertTracker(tracker: Tracker): Long
    suspend fun updateTracker(tracker: Tracker)
    suspend fun deleteTracker(id: Long)
    suspend fun clearTrackerNewFlag(id: Long)

    // ── Multi-tracker: TrackerMembers ─────────────────────────────────────────
    fun getActiveMembersForTracker(trackerId: Long): Flow<List<TrackerMember>>
    fun getAllMembersForTracker(trackerId: Long): Flow<List<TrackerMember>>
    suspend fun insertTrackerMember(member: TrackerMember): Long
    suspend fun updateTrackerMember(member: TrackerMember)
    suspend fun setTrackerMemberArchived(id: Long, archived: Boolean)
    suspend fun deleteTrackerMember(trackerId: Long, memberId: Long)

    // ── Multi-tracker: TrackerPeriods ─────────────────────────────────────────
    fun getPeriodsForTracker(trackerId: Long): Flow<List<TrackerPeriod>>
    suspend fun getCurrentPeriod(trackerId: Long): TrackerPeriod?
    fun getCurrentPeriodFlow(trackerId: Long): Flow<TrackerPeriod?>
    suspend fun getPeriodByLabel(trackerId: Long, label: String): TrackerPeriod?
    suspend fun insertPeriod(period: TrackerPeriod): Long
    suspend fun updatePeriod(period: TrackerPeriod)
    suspend fun setCurrentPeriod(trackerId: Long, periodId: Long)

    // ── Multi-tracker: TrackerRecords ─────────────────────────────────────────
    fun getRecordsForPeriod(trackerId: Long, periodId: Long): Flow<List<TrackerRecord>>
    fun getRecordsForTracker(trackerId: Long): Flow<List<TrackerRecord>>
    suspend fun getRecord(trackerId: Long, periodId: Long, memberId: Long): TrackerRecord?
    suspend fun insertRecord(record: TrackerRecord): Long
    suspend fun updateRecord(record: TrackerRecord)
    suspend fun deleteRecord(id: Long)
    suspend fun getCompletedCountForPeriod(trackerId: Long, periodId: Long): Int
}
