## `app/src/main/java/com/mikeisesele/clearr/data/dao/MemberDao.kt`

```kotlin
package com.mikeisesele.clearr.data.dao

import androidx.room.*
import com.mikeisesele.clearr.data.model.Member
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {

    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE isArchived = 0 ORDER BY name ASC")
    fun getActiveMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMemberById(id: Long): Member?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Update
    suspend fun updateMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)

    @Query("DELETE FROM members WHERE id = :id")
    suspend fun deleteMemberById(id: Long)

    @Query("UPDATE members SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean)
}
```

## `app/src/main/java/com/mikeisesele/clearr/data/dao/PaymentRecordDao.kt`

```kotlin
package com.mikeisesele.clearr.data.dao

import androidx.room.*
import com.mikeisesele.clearr.data.model.PaymentRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentRecordDao {

    @Query("SELECT * FROM payment_records WHERE memberId = :memberId AND year = :year AND monthIndex = :monthIndex AND isUndone = 0")
    fun getPaymentsForMonth(memberId: Long, year: Int, monthIndex: Int): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records WHERE memberId = :memberId AND year = :year AND isUndone = 0 ORDER BY monthIndex ASC, paidAt ASC")
    fun getPaymentsForMemberYear(memberId: Long, year: Int): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records WHERE year = :year AND isUndone = 0")
    fun getPaymentsForYear(year: Int): Flow<List<PaymentRecord>>

    @Query("SELECT * FROM payment_records WHERE memberId = :memberId AND year = :year AND monthIndex = :monthIndex AND isUndone = 0 ORDER BY paidAt DESC LIMIT 1")
    suspend fun getLatestPayment(memberId: Long, year: Int, monthIndex: Int): PaymentRecord?

    @Query("SELECT COALESCE(SUM(amountPaid), 0.0) FROM payment_records WHERE memberId = :memberId AND year = :year AND monthIndex = :monthIndex AND isUndone = 0")
    suspend fun getTotalPaidForMonth(memberId: Long, year: Int, monthIndex: Int): Double

    @Query("SELECT COALESCE(SUM(amountPaid), 0.0) FROM payment_records WHERE year = :year AND isUndone = 0")
    fun getTotalCollectedForYear(year: Int): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(record: PaymentRecord): Long

    @Update
    suspend fun updatePayment(record: PaymentRecord)

    @Query("UPDATE payment_records SET isUndone = 1 WHERE id = :id")
    suspend fun undoPayment(id: Long)

    @Query("DELETE FROM payment_records WHERE memberId = :memberId AND year = :year AND monthIndex = :monthIndex")
    suspend fun deletePaymentsForMonth(memberId: Long, year: Int, monthIndex: Int)

    @Query("DELETE FROM payment_records WHERE memberId = :memberId")
    suspend fun deletePaymentsForMember(memberId: Long)

    @Query("SELECT * FROM payment_records WHERE isUndone = 0 ORDER BY paidAt DESC")
    fun getAllPayments(): Flow<List<PaymentRecord>>
}
```

## `app/src/main/java/com/mikeisesele/clearr/data/dao/YearConfigDao.kt`

```kotlin
package com.mikeisesele.clearr.data.dao

import androidx.room.*
import com.mikeisesele.clearr.data.model.YearConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface YearConfigDao {

    @Query("SELECT * FROM year_configs ORDER BY year DESC")
    fun getAllYearConfigs(): Flow<List<YearConfig>>

    @Query("SELECT * FROM year_configs WHERE year = :year")
    suspend fun getYearConfig(year: Int): YearConfig?

    @Query("SELECT * FROM year_configs WHERE year = :year")
    fun getYearConfigFlow(year: Int): Flow<YearConfig?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertYearConfig(config: YearConfig)

    @Update
    suspend fun updateYearConfig(config: YearConfig)

    @Query("UPDATE year_configs SET dueAmountPerMonth = :amount WHERE year = :year")
    suspend fun updateDueAmount(year: Int, amount: Double)
}
```

## `app/src/main/java/com/mikeisesele/clearr/data/database/DuesDatabase.kt`

```kotlin
package com.mikeisesele.clearr.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mikeisesele.clearr.data.dao.AppConfigDao
import com.mikeisesele.clearr.data.dao.BudgetDao
import com.mikeisesele.clearr.data.dao.GoalsDao
import com.mikeisesele.clearr.data.dao.MemberDao
import com.mikeisesele.clearr.data.dao.PaymentRecordDao
import com.mikeisesele.clearr.data.dao.TrackerDao
import com.mikeisesele.clearr.data.dao.TodoDao
import com.mikeisesele.clearr.data.dao.YearConfigDao
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetEntry
import com.mikeisesele.clearr.data.model.BudgetCategoryPlan
import com.mikeisesele.clearr.data.model.BudgetPeriod
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.data.model.PaymentRecord
import com.mikeisesele.clearr.data.model.GoalCompletionEntity
import com.mikeisesele.clearr.data.model.GoalEntity
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.data.model.TrackerPeriod
import com.mikeisesele.clearr.data.model.TrackerRecord
import com.mikeisesele.clearr.data.model.TodoEntity
import com.mikeisesele.clearr.data.model.YearConfig

@Database(
    entities = [
        Member::class,
        PaymentRecord::class,
        YearConfig::class,
        AppConfig::class,
        Tracker::class,
        TrackerMember::class,
        TrackerPeriod::class,
        TrackerRecord::class,
        BudgetPeriod::class,
        BudgetCategory::class,
        BudgetCategoryPlan::class,
        BudgetEntry::class,
        TodoEntity::class,
        GoalEntity::class,
        GoalCompletionEntity::class,
    ],
    version = 2,
    exportSchema = false
)
abstract class DuesDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun paymentRecordDao(): PaymentRecordDao
    abstract fun yearConfigDao(): YearConfigDao
    abstract fun appConfigDao(): AppConfigDao
    abstract fun trackerDao(): TrackerDao
    abstract fun budgetDao(): BudgetDao
    abstract fun todoDao(): TodoDao
    abstract fun goalsDao(): GoalsDao
}
```

## `app/src/main/java/com/mikeisesele/clearr/data/model/Member.kt`

```kotlin
package com.mikeisesele.clearr.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
```

## `app/src/main/java/com/mikeisesele/clearr/data/model/PaymentRecord.kt`

```kotlin
package com.mikeisesele.clearr.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_records",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("memberId")]
)
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val year: Int,
    val monthIndex: Int,
    val amountPaid: Double,
    val expectedAmount: Double,
    val paidAt: Long = System.currentTimeMillis(),
    val note: String? = null,
    val isUndone: Boolean = false
)
```

## `app/src/main/java/com/mikeisesele/clearr/data/model/YearConfig.kt`

```kotlin
package com.mikeisesele.clearr.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "year_configs")
data class YearConfig(
    @PrimaryKey
    val year: Int,
    val dueAmountPerMonth: Double = 5000.0,
    val startedAt: Long = System.currentTimeMillis()
)
```

## `app/src/main/java/com/mikeisesele/clearr/data/repository/DuesRepositoryImpl.kt`

```kotlin
package com.mikeisesele.clearr.data.repository

import com.mikeisesele.clearr.data.dao.AppConfigDao
import com.mikeisesele.clearr.data.dao.BudgetDao
import com.mikeisesele.clearr.data.dao.GoalsDao
import com.mikeisesele.clearr.data.dao.MemberDao
import com.mikeisesele.clearr.data.dao.PaymentRecordDao
import com.mikeisesele.clearr.data.dao.TrackerDao
import com.mikeisesele.clearr.data.dao.TodoDao
import com.mikeisesele.clearr.data.dao.YearConfigDao
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetCategoryPlan
import com.mikeisesele.clearr.data.model.BudgetEntry
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetPeriod
import com.mikeisesele.clearr.data.model.Goal
import com.mikeisesele.clearr.data.model.GoalCompletion
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.data.model.PaymentRecord
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.data.model.TrackerPeriod
import com.mikeisesele.clearr.data.model.TrackerRecord
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.YearConfig
import com.mikeisesele.clearr.data.model.toDomain
import com.mikeisesele.clearr.data.model.toEntity
import com.mikeisesele.clearr.domain.repository.DuesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuesRepositoryImpl @Inject constructor(
    private val memberDao: MemberDao,
    private val paymentRecordDao: PaymentRecordDao,
    private val yearConfigDao: YearConfigDao,
    private val appConfigDao: AppConfigDao,
    private val trackerDao: TrackerDao,
    private val budgetDao: BudgetDao,
    private val todoDao: TodoDao,
    private val goalsDao: GoalsDao
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

    // ── Budget tracker ────────────────────────────────────────────────────────
    override fun getBudgetPeriods(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetPeriod>> =
        budgetDao.getPeriods(trackerId, frequency)

    override suspend fun ensureBudgetPeriods(trackerId: Long, frequency: BudgetFrequency) {
        val existing = budgetDao.getLatestPeriod(trackerId, frequency)
        val periods = when {
            existing == null -> when (frequency) {
                BudgetFrequency.MONTHLY -> generateMonthlyPeriods(trackerId)
                BudgetFrequency.WEEKLY -> generateWeeklyPeriods(trackerId)
            }

            else -> when (frequency) {
                BudgetFrequency.MONTHLY -> generateMissingMonthlyPeriods(trackerId, existing)
                BudgetFrequency.WEEKLY -> generateMissingWeeklyPeriods(trackerId, existing)
            }
        }
        if (periods.isNotEmpty()) {
            budgetDao.insertPeriods(periods)
        }
    }

    override fun getBudgetCategories(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetCategory>> =
        budgetDao.getCategories(trackerId, frequency)

    override suspend fun getBudgetMaxSortOrder(trackerId: Long, frequency: BudgetFrequency): Int =
        budgetDao.getMaxSortOrder(trackerId, frequency)

    override suspend fun addBudgetCategory(category: BudgetCategory): Long =
        budgetDao.insertCategory(category)

    override suspend fun updateBudgetCategory(category: BudgetCategory) =
        budgetDao.updateCategory(category)

    override suspend fun deleteBudgetCategory(categoryId: Long) {
        budgetDao.deleteEntriesByCategory(categoryId)
        budgetDao.deleteCategoryPlansByCategory(categoryId)
        budgetDao.deleteCategory(categoryId)
    }

    override suspend fun reorderBudgetCategories(
        trackerId: Long,
        frequency: BudgetFrequency,
        orderedIds: List<Long>
    ) {
        orderedIds.forEachIndexed { index, id ->
            budgetDao.updateCategorySortOrder(id, index)
        }
    }

    override fun getBudgetCategoryPlansForTracker(trackerId: Long): Flow<List<BudgetCategoryPlan>> =
        budgetDao.getCategoryPlansForTracker(trackerId)

    override suspend fun getBudgetCategoryPlansForPeriod(periodId: Long): List<BudgetCategoryPlan> =
        budgetDao.getCategoryPlansForPeriod(periodId)

    override suspend fun saveBudgetCategoryPlans(periodId: Long, plans: List<BudgetCategoryPlan>) {
        budgetDao.deleteCategoryPlansForPeriod(periodId)
        if (plans.isNotEmpty()) {
            budgetDao.insertCategoryPlans(plans)
        }
    }

    override fun getBudgetEntriesForTracker(trackerId: Long): Flow<List<BudgetEntry>> =
        budgetDao.getEntriesForTracker(trackerId)

    override suspend fun addBudgetEntry(entry: BudgetEntry): Long =
        budgetDao.insertEntry(entry)

    // ── Todo tracker ─────────────────────────────────────────────────────────
    override fun getTodosForTracker(trackerId: Long): Flow<List<TodoItem>> =
        todoDao.getTodos(trackerId).map { list -> list.map { it.toDomain() } }

    override suspend fun getTodoById(id: String): TodoItem? =
        todoDao.getTodoById(id)?.toDomain()

    override suspend fun insertTodo(todo: TodoItem) =
        todoDao.insert(todo.toEntity())

    override suspend fun updateTodo(todo: TodoItem) =
        todoDao.insert(todo.toEntity())

    override suspend fun markTodoDone(id: String, completedAt: Long) =
        todoDao.markDone(id = id, completedAt = completedAt)

    override suspend fun deleteTodo(id: String) =
        todoDao.delete(id)

    // ── Goals tracker ────────────────────────────────────────────────────────
    override fun getGoalsForTracker(trackerId: Long): Flow<List<Goal>> =
        goalsDao.getGoals(trackerId).map { list -> list.map { it.toDomain() } }

    override fun getGoalCompletionsForTracker(trackerId: Long): Flow<List<GoalCompletion>> =
        goalsDao.getAllCompletions(trackerId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertGoal(goal: Goal) =
        goalsDao.insertGoal(goal.toEntity())

    override suspend fun addGoalCompletion(completion: GoalCompletion) =
        goalsDao.insertCompletion(completion.toEntity())

    override suspend fun deleteGoal(goalId: String) =
        goalsDao.deleteGoal(goalId)

    private fun generateMonthlyPeriods(trackerId: Long): List<BudgetPeriod> {
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -4) }
        return (0 until 5).map {
            val periodCal = cal.clone() as Calendar
            periodCal.add(Calendar.MONTH, it)
            val label = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(periodCal.time)
            val start = (periodCal.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val end = (periodCal.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            BudgetPeriod(
                trackerId = trackerId,
                frequency = BudgetFrequency.MONTHLY,
                label = label,
                startDate = start,
                endDate = end
            )
        }
    }

    private fun generateWeeklyPeriods(trackerId: Long): List<BudgetPeriod> {
        val base = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            add(Calendar.WEEK_OF_YEAR, -4)
        }
        return (0 until 5).map {
            val periodCal = base.clone() as Calendar
            periodCal.add(Calendar.WEEK_OF_YEAR, it)
            val week = periodCal.get(Calendar.WEEK_OF_YEAR)
            val label = "Week $week"
            val start = (periodCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val end = (periodCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 6)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            BudgetPeriod(
                trackerId = trackerId,
                frequency = BudgetFrequency.WEEKLY,
                label = label,
                startDate = start,
                endDate = end
            )
        }
    }

    private fun generateMissingMonthlyPeriods(
        trackerId: Long,
        latest: BudgetPeriod
    ): List<BudgetPeriod> {
        val latestCal = Calendar.getInstance().apply { timeInMillis = latest.startDate }
        val currentCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!latestCal.before(currentCal)) return emptyList()

        val periods = mutableListOf<BudgetPeriod>()
        val nextCal = (latestCal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
        while (!nextCal.after(currentCal)) {
            periods += buildMonthlyPeriod(trackerId, nextCal)
            nextCal.add(Calendar.MONTH, 1)
        }
        return periods
    }

    private fun generateMissingWeeklyPeriods(
        trackerId: Long,
        latest: BudgetPeriod
    ): List<BudgetPeriod> {
        val latestCal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            timeInMillis = latest.startDate
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentCal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!latestCal.before(currentCal)) return emptyList()

        val periods = mutableListOf<BudgetPeriod>()
        val nextCal = (latestCal.clone() as Calendar).apply { add(Calendar.WEEK_OF_YEAR, 1) }
        while (!nextCal.after(currentCal)) {
            periods += buildWeeklyPeriod(trackerId, nextCal)
            nextCal.add(Calendar.WEEK_OF_YEAR, 1)
        }
        return periods
    }

    private fun buildMonthlyPeriod(
        trackerId: Long,
        periodCal: Calendar
    ): BudgetPeriod {
        val label = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(periodCal.time)
        val start = (periodCal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = (periodCal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        return BudgetPeriod(
            trackerId = trackerId,
            frequency = BudgetFrequency.MONTHLY,
            label = label,
            startDate = start,
            endDate = end
        )
    }

    private fun buildWeeklyPeriod(
        trackerId: Long,
        periodCal: Calendar
    ): BudgetPeriod {
        val week = periodCal.get(Calendar.WEEK_OF_YEAR)
        val label = "Week $week"
        val start = (periodCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = (periodCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        return BudgetPeriod(
            trackerId = trackerId,
            frequency = BudgetFrequency.WEEKLY,
            label = label,
            startDate = start,
            endDate = end
        )
    }
}
```

