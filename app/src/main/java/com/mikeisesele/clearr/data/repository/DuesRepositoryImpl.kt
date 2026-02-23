package com.mikeisesele.clearr.data.repository

import com.mikeisesele.clearr.data.dao.AppConfigDao
import com.mikeisesele.clearr.data.dao.MemberDao
import com.mikeisesele.clearr.data.dao.PaymentRecordDao
import com.mikeisesele.clearr.data.dao.TrackerDao
import com.mikeisesele.clearr.data.dao.YearConfigDao
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.data.model.PaymentRecord
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.data.model.TrackerPeriod
import com.mikeisesele.clearr.data.model.TrackerRecord
import com.mikeisesele.clearr.data.model.YearConfig
import com.mikeisesele.clearr.domain.repository.DuesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuesRepositoryImpl @Inject constructor(
    private val memberDao: MemberDao,
    private val paymentRecordDao: PaymentRecordDao,
    private val yearConfigDao: YearConfigDao,
    private val appConfigDao: AppConfigDao,
    private val trackerDao: TrackerDao
) : DuesRepository {

    // ── App Config ────────────────────────────────────────────────────────────
    override fun getAppConfigFlow(): Flow<AppConfig?> = appConfigDao.getConfigFlow()
    override suspend fun getAppConfig(): AppConfig? = appConfigDao.getConfig()
    override suspend fun upsertAppConfig(config: AppConfig) = appConfigDao.upsertConfig(config)

    // ── Legacy global members ─────────────────────────────────────────────────
    override fun getAllMembers(): Flow<List<Member>> = memberDao.getAllMembers()
    override fun getActiveMembers(): Flow<List<Member>> = memberDao.getActiveMembers()
    override suspend fun getMemberById(id: Long): Member? = memberDao.getMemberById(id)
    override suspend fun insertMember(member: Member): Long = memberDao.insertMember(member)
    override suspend fun updateMember(member: Member) = memberDao.updateMember(member)
    override suspend fun setMemberArchived(id: Long, archived: Boolean) = memberDao.setArchived(id, archived)
    override suspend fun deleteMember(id: Long) {
        paymentRecordDao.deletePaymentsForMember(id)
        memberDao.deleteMemberById(id)
    }

    // ── Legacy payments ───────────────────────────────────────────────────────
    override fun getPaymentsForYear(year: Int): Flow<List<PaymentRecord>> =
        paymentRecordDao.getPaymentsForYear(year)

    override fun getPaymentsForMemberYear(memberId: Long, year: Int): Flow<List<PaymentRecord>> =
        paymentRecordDao.getPaymentsForMemberYear(memberId, year)

    override suspend fun getLatestPayment(memberId: Long, year: Int, monthIndex: Int): PaymentRecord? =
        paymentRecordDao.getLatestPayment(memberId, year, monthIndex)

    override suspend fun getTotalPaidForMonth(memberId: Long, year: Int, monthIndex: Int): Double =
        paymentRecordDao.getTotalPaidForMonth(memberId, year, monthIndex)

    override fun getTotalCollectedForYear(year: Int): Flow<Double> =
        paymentRecordDao.getTotalCollectedForYear(year)

    override suspend fun insertPayment(record: PaymentRecord): Long =
        paymentRecordDao.insertPayment(record)

    override suspend fun undoPayment(id: Long) = paymentRecordDao.undoPayment(id)

    override suspend fun deletePaymentsForMonth(memberId: Long, year: Int, monthIndex: Int) =
        paymentRecordDao.deletePaymentsForMonth(memberId, year, monthIndex)

    // ── Legacy year configs ───────────────────────────────────────────────────
    override fun getAllYearConfigs(): Flow<List<YearConfig>> = yearConfigDao.getAllYearConfigs()
    override suspend fun getYearConfig(year: Int): YearConfig? = yearConfigDao.getYearConfig(year)
    override fun getYearConfigFlow(year: Int): Flow<YearConfig?> = yearConfigDao.getYearConfigFlow(year)
    override suspend fun insertYearConfig(config: YearConfig) = yearConfigDao.insertYearConfig(config)
    override suspend fun updateDueAmount(year: Int, amount: Double) =
        yearConfigDao.updateDueAmount(year, amount)

    override suspend fun ensureYearConfig(year: Int, defaultAmount: Double) {
        if (yearConfigDao.getYearConfig(year) == null) {
            yearConfigDao.insertYearConfig(YearConfig(year = year, dueAmountPerMonth = defaultAmount))
        }
    }

    // ── Multi-tracker: Trackers ───────────────────────────────────────────────
    override fun getAllTrackers(): Flow<List<Tracker>> = trackerDao.getAllTrackers()
    override suspend fun getTrackerById(id: Long): Tracker? = trackerDao.getTrackerById(id)
    override fun getTrackerByIdFlow(id: Long): Flow<Tracker?> = trackerDao.getTrackerByIdFlow(id)
    override suspend fun insertTracker(tracker: Tracker): Long = trackerDao.insertTracker(tracker)
    override suspend fun updateTracker(tracker: Tracker) = trackerDao.updateTracker(tracker)
    override suspend fun deleteTracker(id: Long) = trackerDao.deleteTracker(id)
    override suspend fun clearTrackerNewFlag(id: Long) = trackerDao.clearNewFlag(id)

    // ── Multi-tracker: TrackerMembers ─────────────────────────────────────────
    override fun getActiveMembersForTracker(trackerId: Long): Flow<List<TrackerMember>> =
        trackerDao.getActiveMembers(trackerId)

    override fun getAllMembersForTracker(trackerId: Long): Flow<List<TrackerMember>> =
        trackerDao.getAllMembers(trackerId)

    override suspend fun insertTrackerMember(member: TrackerMember): Long =
        trackerDao.insertMember(member)

    override suspend fun updateTrackerMember(member: TrackerMember) =
        trackerDao.updateMember(member)

    override suspend fun setTrackerMemberArchived(id: Long, archived: Boolean) =
        trackerDao.setMemberArchived(id, archived)

    override suspend fun deleteTrackerMember(trackerId: Long, memberId: Long) {
        trackerDao.deleteRecordsForTrackerMember(trackerId, memberId)
        trackerDao.deleteTrackerMember(trackerId, memberId)
    }

    // ── Multi-tracker: TrackerPeriods ─────────────────────────────────────────
    override fun getPeriodsForTracker(trackerId: Long): Flow<List<TrackerPeriod>> =
        trackerDao.getPeriodsForTracker(trackerId)

    override suspend fun getCurrentPeriod(trackerId: Long): TrackerPeriod? =
        trackerDao.getCurrentPeriod(trackerId)

    override fun getCurrentPeriodFlow(trackerId: Long): Flow<TrackerPeriod?> =
        trackerDao.getCurrentPeriodFlow(trackerId)

    override suspend fun getPeriodByLabel(trackerId: Long, label: String): TrackerPeriod? =
        trackerDao.getPeriodByLabel(trackerId, label)

    override suspend fun insertPeriod(period: TrackerPeriod): Long =
        trackerDao.insertPeriod(period)

    override suspend fun updatePeriod(period: TrackerPeriod) =
        trackerDao.updatePeriod(period)

    override suspend fun setCurrentPeriod(trackerId: Long, periodId: Long) {
        trackerDao.clearCurrentPeriods(trackerId)
        trackerDao.setCurrentPeriod(periodId)
    }

    // ── Multi-tracker: TrackerRecords ─────────────────────────────────────────
    override fun getRecordsForPeriod(trackerId: Long, periodId: Long): Flow<List<TrackerRecord>> =
        trackerDao.getRecordsForPeriod(trackerId, periodId)

    override fun getRecordsForTracker(trackerId: Long): Flow<List<TrackerRecord>> =
        trackerDao.getRecordsForTracker(trackerId)

    override suspend fun getRecord(trackerId: Long, periodId: Long, memberId: Long): TrackerRecord? =
        trackerDao.getRecord(trackerId, periodId, memberId)

    override suspend fun insertRecord(record: TrackerRecord): Long =
        trackerDao.insertRecord(record)

    override suspend fun updateRecord(record: TrackerRecord) =
        trackerDao.updateRecord(record)

    override suspend fun deleteRecord(id: Long) =
        trackerDao.deleteRecord(id)

    override suspend fun getCompletedCountForPeriod(trackerId: Long, periodId: Long): Int =
        trackerDao.getCompletedCountForPeriod(trackerId, periodId)
}
