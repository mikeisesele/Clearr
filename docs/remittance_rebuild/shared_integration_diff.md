# Remittance Shared Integration Diff

This document shows the current uncommitted diff for shared files that participated in remittance behavior before the feature was removed.

Use this alongside `shared_integration_pre_removal.md`:
- `shared_integration_pre_removal.md` gives the exact old source
- this file shows how the current stripped app differs

## `app/src/main/java/com/mikeisesele/clearr/core/ai/ClearrEdgeAi.kt`

```diff
diff --git a/app/src/main/java/com/mikeisesele/clearr/core/ai/ClearrEdgeAi.kt b/app/src/main/java/com/mikeisesele/clearr/core/ai/ClearrEdgeAi.kt
index 76dd485..8cbc03a 100644
--- a/app/src/main/java/com/mikeisesele/clearr/core/ai/ClearrEdgeAi.kt
+++ b/app/src/main/java/com/mikeisesele/clearr/core/ai/ClearrEdgeAi.kt
@@ -114,7 +114,7 @@ object ClearrEdgeAi {
             val context = runCatching { ClearrApplication.appContext }.getOrNull() ?: return fallback
             val prompt = """
                 Parse setup intent. Respond strictly as JSON with keys:
-                trackerType(DUES|GOALS|TODO|BUDGET|null),
+                trackerType(GOALS|TODO|BUDGET|null),
                 frequency(MONTHLY|WEEKLY|QUARTERLY|ANNUAL|null),
                 defaultAmount(number|null),
                 trackerName(string|null)
@@ -301,20 +301,6 @@ object ClearrEdgeAi {
         }
     }
 
-    fun remittanceRiskLabel(
-        memberName: String,
-        paidMonths: Int,
-        expectedMonths: Int
-    ): String? {
-        if (expectedMonths <= 0) return null
-        val ratio = paidMonths.toFloat() / expectedMonths
-        return when {
-            ratio < 0.5f -> "$memberName has high remittance risk."
-            ratio < 0.75f -> "$memberName may miss upcoming remittance."
-            else -> null
-        }
-    }
-
     fun prioritizeTrackers(list: List<TrackerSummary>): List<TrackerSummary> {
         return list.sortedWith(
             compareByDescending<TrackerSummary> { urgencyScore(it) }
@@ -325,7 +311,6 @@ object ClearrEdgeAi {
     fun parseSetupIntent(text: String): SetupAiResult {
         val lower = text.lowercase(Locale.getDefault())
         val type = when {
-            hasAny(lower, "remittance", "dues", "fees", "payment", "clients") -> TrackerType.DUES
             hasAny(lower, "goal", "habit", "streak") -> TrackerType.GOALS
             hasAny(lower, "todo", "task", "checklist") -> TrackerType.TODO
             hasAny(lower, "budget", "expense", "spend") -> TrackerType.BUDGET
@@ -346,8 +331,6 @@ object ClearrEdgeAi {
             ?.toDoubleOrNull()
 
         val trackerName = when (type) {
-            TrackerType.DUES -> "Remittance"
-            TrackerType.EXPENSES -> "Remittance"
             TrackerType.GOALS -> "Goals"
             TrackerType.TODO -> "Todos"
             TrackerType.BUDGET -> "Budget"
@@ -438,8 +421,6 @@ object ClearrEdgeAi {
         val incomplete = max(summary.totalMembers - summary.completedCount, 0)
         val incompleteScore = if (summary.totalMembers > 0) (100 - summary.completionPercent) else 20
         val typeBias = when (summary.type) {
-            TrackerType.DUES -> 35
-            TrackerType.EXPENSES -> 35
             TrackerType.TODO -> 25
             TrackerType.BUDGET -> 20
             TrackerType.GOALS -> 10
```


## `app/src/main/java/com/mikeisesele/clearr/data/dao/TrackerDao.kt`

```diff
diff --git a/app/src/main/java/com/mikeisesele/clearr/data/dao/TrackerDao.kt b/app/src/main/java/com/mikeisesele/clearr/data/dao/TrackerDao.kt
index bb96d95..8c29af7 100644
--- a/app/src/main/java/com/mikeisesele/clearr/data/dao/TrackerDao.kt
+++ b/app/src/main/java/com/mikeisesele/clearr/data/dao/TrackerDao.kt
@@ -1,18 +1,17 @@
 package com.mikeisesele.clearr.data.dao
 
-import androidx.room.*
-import com.mikeisesele.clearr.data.model.RecordStatus
+import androidx.room.Dao
+import androidx.room.Insert
+import androidx.room.OnConflictStrategy
+import androidx.room.Query
+import androidx.room.Transaction
+import androidx.room.Update
 import com.mikeisesele.clearr.data.model.Tracker
-import com.mikeisesele.clearr.data.model.TrackerMember
-import com.mikeisesele.clearr.data.model.TrackerPeriod
-import com.mikeisesele.clearr.data.model.TrackerRecord
 import kotlinx.coroutines.flow.Flow
 
 @Dao
 interface TrackerDao {
 
-    // ── Trackers ──────────────────────────────────────────────────────────────
-
     @Query("SELECT * FROM trackers ORDER BY createdAt ASC")
     fun getAllTrackers(): Flow<List<Tracker>>
 
@@ -31,18 +30,12 @@ interface TrackerDao {
     @Query("DELETE FROM trackers WHERE id = :id")
     suspend fun deleteTrackerRow(id: Long)
 
-    @Query("DELETE FROM tracker_members WHERE trackerId = :trackerId")
-    suspend fun deleteMembersForTracker(trackerId: Long)
-
-    @Query("DELETE FROM tracker_periods WHERE trackerId = :trackerId")
-    suspend fun deletePeriodsForTracker(trackerId: Long)
-
-    @Query("DELETE FROM tracker_records WHERE trackerId = :trackerId")
-    suspend fun deleteRecordsForTracker(trackerId: Long)
-
     @Query("DELETE FROM budget_entries WHERE trackerId = :trackerId")
     suspend fun deleteBudgetEntriesForTracker(trackerId: Long)
 
+    @Query("DELETE FROM budget_category_plans WHERE periodId IN (SELECT id FROM budget_periods WHERE trackerId = :trackerId)")
+    suspend fun deleteBudgetPlansForTracker(trackerId: Long)
+
     @Query("DELETE FROM budget_categories WHERE trackerId = :trackerId")
     suspend fun deleteBudgetCategoriesForTracker(trackerId: Long)
 
@@ -60,10 +53,8 @@ interface TrackerDao {
 
     @Transaction
     suspend fun deleteTracker(trackerId: Long) {
-        deleteRecordsForTracker(trackerId)
-        deleteMembersForTracker(trackerId)
-        deletePeriodsForTracker(trackerId)
         deleteBudgetEntriesForTracker(trackerId)
+        deleteBudgetPlansForTracker(trackerId)
         deleteBudgetCategoriesForTracker(trackerId)
         deleteBudgetPeriodsForTracker(trackerId)
         deleteTodosForTracker(trackerId)
@@ -72,92 +63,6 @@ interface TrackerDao {
         deleteTrackerRow(trackerId)
     }
 
-    /** Clear isNew flag after first open */
     @Query("UPDATE trackers SET isNew = 0 WHERE id = :id")
     suspend fun clearNewFlag(id: Long)
-
-    // ── TrackerMembers ────────────────────────────────────────────────────────
-
-    @Query("SELECT * FROM tracker_members WHERE trackerId = :trackerId AND isArchived = 0 ORDER BY name ASC")
-    fun getActiveMembers(trackerId: Long): Flow<List<TrackerMember>>
-
-    @Query("SELECT * FROM tracker_members WHERE trackerId = :trackerId ORDER BY name ASC")
-    fun getAllMembers(trackerId: Long): Flow<List<TrackerMember>>
-
-    @Query("SELECT COUNT(*) FROM tracker_members WHERE trackerId = :trackerId AND isArchived = 0")
-    fun getActiveMemberCount(trackerId: Long): Flow<Int>
-
-    @Insert(onConflict = OnConflictStrategy.REPLACE)
-    suspend fun insertMember(member: TrackerMember): Long
-
-    @Update
-    suspend fun updateMember(member: TrackerMember)
-
-    @Query("UPDATE tracker_members SET isArchived = :archived WHERE id = :id")
-    suspend fun setMemberArchived(id: Long, archived: Boolean)
-
-    @Query("DELETE FROM tracker_records WHERE trackerId = :trackerId AND memberId = :memberId")
-    suspend fun deleteRecordsForTrackerMember(trackerId: Long, memberId: Long)
-
-    @Query("DELETE FROM tracker_members WHERE trackerId = :trackerId AND id = :memberId")
-    suspend fun deleteTrackerMember(trackerId: Long, memberId: Long)
-
-    // ── TrackerPeriods ────────────────────────────────────────────────────────
-
-    @Query("SELECT * FROM tracker_periods WHERE trackerId = :trackerId ORDER BY startDate ASC")
-    fun getPeriodsForTracker(trackerId: Long): Flow<List<TrackerPeriod>>
-
-    @Query("SELECT * FROM tracker_periods WHERE trackerId = :trackerId AND isCurrent = 1 LIMIT 1")
-    suspend fun getCurrentPeriod(trackerId: Long): TrackerPeriod?
-
-    @Query("SELECT * FROM tracker_periods WHERE trackerId = :trackerId AND isCurrent = 1 LIMIT 1")
-    fun getCurrentPeriodFlow(trackerId: Long): Flow<TrackerPeriod?>
-
-    @Query("SELECT * FROM tracker_periods WHERE id = :id LIMIT 1")
-    suspend fun getPeriodById(id: Long): TrackerPeriod?
-
-    @Query("SELECT * FROM tracker_periods WHERE trackerId = :trackerId AND label = :label LIMIT 1")
-    suspend fun getPeriodByLabel(trackerId: Long, label: String): TrackerPeriod?
-
-    @Insert(onConflict = OnConflictStrategy.REPLACE)
-    suspend fun insertPeriod(period: TrackerPeriod): Long
-
-    @Update
-    suspend fun updatePeriod(period: TrackerPeriod)
-
-    /** Mark all periods for a tracker as not current, then set the given one as current */
-    @Query("UPDATE tracker_periods SET isCurrent = 0 WHERE trackerId = :trackerId")
-    suspend fun clearCurrentPeriods(trackerId: Long)
-
-    @Query("UPDATE tracker_periods SET isCurrent = 1 WHERE id = :periodId")
-    suspend fun setCurrentPeriod(periodId: Long)
-
-    // ── TrackerRecords ────────────────────────────────────────────────────────
-
-    @Query("SELECT * FROM tracker_records WHERE trackerId = :trackerId AND periodId = :periodId")
-    fun getRecordsForPeriod(trackerId: Long, periodId: Long): Flow<List<TrackerRecord>>
-
-    @Query("SELECT * FROM tracker_records WHERE trackerId = :trackerId")
-    fun getRecordsForTracker(trackerId: Long): Flow<List<TrackerRecord>>
-
-    @Query("SELECT * FROM tracker_records WHERE trackerId = :trackerId AND periodId = :periodId AND memberId = :memberId LIMIT 1")
-    suspend fun getRecord(trackerId: Long, periodId: Long, memberId: Long): TrackerRecord?
-
-    @Insert(onConflict = OnConflictStrategy.REPLACE)
-    suspend fun insertRecord(record: TrackerRecord): Long
-
-    @Update
-    suspend fun updateRecord(record: TrackerRecord)
-
-    @Query("DELETE FROM tracker_records WHERE id = :id")
-    suspend fun deleteRecord(id: Long)
-
-    /** Count completed records for a period (status IN (PAID, PRESENT, DONE)) */
-    @Query("""
-        SELECT COUNT(*) FROM tracker_records
-        WHERE trackerId = :trackerId
-        AND periodId = :periodId
-        AND status IN ('PAID', 'PRESENT', 'DONE')
-    """)
-    suspend fun getCompletedCountForPeriod(trackerId: Long, periodId: Long): Int
 }
```


## `app/src/main/java/com/mikeisesele/clearr/data/model/AppConfig.kt`

```diff
diff --git a/app/src/main/java/com/mikeisesele/clearr/data/model/AppConfig.kt b/app/src/main/java/com/mikeisesele/clearr/data/model/AppConfig.kt
index 19d7463..ca7f89f 100644
--- a/app/src/main/java/com/mikeisesele/clearr/data/model/AppConfig.kt
+++ b/app/src/main/java/com/mikeisesele/clearr/data/model/AppConfig.kt
@@ -3,62 +3,42 @@ package com.mikeisesele.clearr.data.model
 import androidx.room.Entity
 import androidx.room.PrimaryKey
 
-/**
- * Singleton configuration table – always id = 1.
- * Stores all settings from the Setup Wizard and can be edited later via Settings.
- */
 @Entity(tableName = "app_config")
 data class AppConfig(
     @PrimaryKey val id: Int = 1,
-
-    // Group identity
-    val groupName: String = "JSS Durumi Brothers",
+    val groupName: String = "Clearr",
     val adminName: String = "",
     val adminPhone: String = "",
-
-    // Tracker behavior
-    val trackerType: TrackerType = TrackerType.DUES,
+    val trackerType: TrackerType = TrackerType.BUDGET,
     val frequency: Frequency = Frequency.MONTHLY,
-
-    // Amount
-    val defaultAmount: Double = 5000.0,
-
-    // Custom frequency config (JSON arrays stored as strings)
-    val customPeriodLabels: String = "[]",  // e.g. ["Term 1","Term 2","Term 3"]
-    val variableAmounts: String = "[]",     // e.g. [5000,7000,6000] per period
-
-    // UI Layout style
+    val defaultAmount: Double = 0.0,
+    val customPeriodLabels: String = "[]",
+    val variableAmounts: String = "[]",
     val layoutStyle: LayoutStyle = LayoutStyle.GRID,
-
-    // Reminder notifications
-    val remindersEnabled: Boolean = true,
-    val reminderDayOfPeriod: Int = 5,  // day number within period to send reminder
-
-    // Wizard
-    val setupComplete: Boolean = false
+    val remindersEnabled: Boolean = false,
+    val reminderDayOfPeriod: Int = 5,
+    val setupComplete: Boolean = true
 )
 
 enum class TrackerType {
-    DUES,       // Group financial obligations
-    EXPENSES,   // Legacy value kept for backward compatibility with old DB rows
-    GOALS,      // Personal goals / recurring habits
-    TODO,       // Personal to-do / task list
-    BUDGET      // Planned vs actual spending
+    GOALS,
+    TODO,
+    BUDGET
 }
 
 enum class Frequency {
     MONTHLY,
     WEEKLY,
-    QUARTERLY,   // 4 periods/year (Jan, Apr, Jul, Oct)
-    TERMLY,      // 3 periods/year (school terms)
-    BIANNUAL,    // 2 periods/year
-    ANNUAL,      // 1 period/year
-    CUSTOM       // user-defined period labels
+    QUARTERLY,
+    TERMLY,
+    BIANNUAL,
+    ANNUAL,
+    CUSTOM
 }
 
 enum class LayoutStyle {
-    GRID,     // default horizontal scrolling grid
-    KANBAN,   // columns per period, members as cards
-    CARDS,    // member cards with period chips
-    RECEIPT   // ledger / receipt style
+    GRID,
+    KANBAN,
+    CARDS,
+    RECEIPT
 }
```


## `app/src/main/java/com/mikeisesele/clearr/data/model/Tracker.kt`

```diff
diff --git a/app/src/main/java/com/mikeisesele/clearr/data/model/Tracker.kt b/app/src/main/java/com/mikeisesele/clearr/data/model/Tracker.kt
index b224e88..6a7764b 100644
--- a/app/src/main/java/com/mikeisesele/clearr/data/model/Tracker.kt
+++ b/app/src/main/java/com/mikeisesele/clearr/data/model/Tracker.kt
@@ -3,93 +3,19 @@ package com.mikeisesele.clearr.data.model
 import androidx.room.Entity
 import androidx.room.PrimaryKey
 
-/**
- * A Tracker is an independent tracking unit.
- * Each tracker owns its own member list (TrackerMember) and period records (TrackerPeriod + TrackerRecord).
- * It is completely isolated from other trackers.
- */
 @Entity(tableName = "trackers")
 data class Tracker(
     @PrimaryKey(autoGenerate = true)
     val id: Long = 0,
     val name: String,
-    val type: TrackerType = TrackerType.DUES,
+    val type: TrackerType = TrackerType.BUDGET,
     val frequency: Frequency = Frequency.MONTHLY,
-    /** Per-tracker layout style (independent from other trackers). */
     val layoutStyle: LayoutStyle = LayoutStyle.GRID,
-    /** Default amount per period (only relevant for DUES type) */
-    val defaultAmount: Double = 5000.0,
-    /** True until the tracker is first opened after creation */
-    val isNew: Boolean = true,
+    val defaultAmount: Double = 0.0,
+    val isNew: Boolean = false,
     val createdAt: Long = System.currentTimeMillis()
 )
 
-/**
- * A member belonging to exactly one tracker.
- * Member lists are per-tracker and do not share with other trackers.
- */
-@Entity(tableName = "tracker_members")
-data class TrackerMember(
-    @PrimaryKey(autoGenerate = true)
-    val id: Long = 0,
-    val trackerId: Long,
-    val name: String,
-    val phone: String? = null,
-    val isArchived: Boolean = false,
-    val createdAt: Long = System.currentTimeMillis()
-)
-
-/**
- * A Period represents one cycle of tracking for a given tracker.
- * e.g. "February 2026", "Week 8, 2026", "Q1 2026", "Term 1 2026"
- * Periods are generated automatically based on tracker frequency.
- */
-@Entity(tableName = "tracker_periods")
-data class TrackerPeriod(
-    @PrimaryKey(autoGenerate = true)
-    val id: Long = 0,
-    val trackerId: Long,
-    /** Human-readable label: "February 2026", "Week 8, 2026", etc. */
-    val label: String,
-    val startDate: Long,
-    val endDate: Long,
-    /** True = this is the period currently active based on the date */
-    val isCurrent: Boolean = false,
-    val createdAt: Long = System.currentTimeMillis()
-)
-
-/**
- * A Record is one member's status for one period.
- * status depends on tracker type:
- *  DUES       → PAID / PARTIAL / UNPAID
- *  ATTENDANCE → PRESENT / ABSENT
- *  TASKS      → DONE / PENDING
- *  EVENTS     → PRESENT / ABSENT
- */
-@Entity(tableName = "tracker_records")
-data class TrackerRecord(
-    @PrimaryKey(autoGenerate = true)
-    val id: Long = 0,
-    val trackerId: Long,
-    val periodId: Long,
-    val memberId: Long,
-    val status: RecordStatus = RecordStatus.UNPAID,
-    /** Amount paid — only meaningful for DUES type */
-    val amountPaid: Double = 0.0,
-    val note: String? = null,
-    val updatedAt: Long = System.currentTimeMillis()
-)
-
-enum class RecordStatus {
-    /** DUES */
-    PAID, PARTIAL, UNPAID,
-    /** ATTENDANCE / EVENTS */
-    PRESENT, ABSENT,
-    /** TASKS */
-    DONE, PENDING
-}
-
-/** Aggregated summary emitted by the DAO for the tracker list card */
 data class TrackerSummary(
     val trackerId: Long,
     val name: String,
```


## `app/src/main/java/com/mikeisesele/clearr/di/DatabaseModule.kt`

```diff
diff --git a/app/src/main/java/com/mikeisesele/clearr/di/DatabaseModule.kt b/app/src/main/java/com/mikeisesele/clearr/di/DatabaseModule.kt
index 7202fbd..32d5a67 100644
--- a/app/src/main/java/com/mikeisesele/clearr/di/DatabaseModule.kt
+++ b/app/src/main/java/com/mikeisesele/clearr/di/DatabaseModule.kt
@@ -2,94 +2,46 @@ package com.mikeisesele.clearr.di
 
 import android.content.Context
 import androidx.room.Room
-import androidx.room.RoomDatabase
-import androidx.sqlite.db.SupportSQLiteDatabase
 import com.mikeisesele.clearr.data.dao.AppConfigDao
 import com.mikeisesele.clearr.data.dao.BudgetDao
 import com.mikeisesele.clearr.data.dao.GoalsDao
-import com.mikeisesele.clearr.data.dao.MemberDao
-import com.mikeisesele.clearr.data.dao.PaymentRecordDao
 import com.mikeisesele.clearr.data.dao.TrackerDao
 import com.mikeisesele.clearr.data.dao.TodoDao
-import com.mikeisesele.clearr.data.dao.YearConfigDao
-import com.mikeisesele.clearr.data.database.DuesDatabase
+import com.mikeisesele.clearr.data.database.ClearrDatabase
 import dagger.Module
 import dagger.Provides
 import dagger.hilt.InstallIn
 import dagger.hilt.android.qualifiers.ApplicationContext
 import dagger.hilt.components.SingletonComponent
-import java.util.Calendar
 import javax.inject.Singleton
 
-private val SEED_MEMBERS = listOf(
-    "Henry Nwazuru",
-    "Chidubem",
-    "Simon Boniface",
-    "Ikechukwu Udeh",
-    "Oluwatobi Majekodunmi",
-    "Dare Oladunjoye",
-    "Michael Isesele",
-    "Faruk Umar"
-)
-
 @Module
 @InstallIn(SingletonComponent::class)
 object DatabaseModule {
 
     @Provides
     @Singleton
-    fun provideDatabase(@ApplicationContext context: Context): DuesDatabase {
-        return Room.databaseBuilder(
+    fun provideDatabase(@ApplicationContext context: Context): ClearrDatabase =
+        Room.databaseBuilder(
             context,
-            DuesDatabase::class.java,
-            "dues_database"
+            ClearrDatabase::class.java,
+            "clearr_database"
         )
             .fallbackToDestructiveMigration(dropAllTables = true)
-            .addCallback(object : RoomDatabase.Callback() {
-                override fun onCreate(db: SupportSQLiteDatabase) {
-                    super.onCreate(db)
-                    val now = System.currentTimeMillis()
-                    val year = Calendar.getInstance().get(Calendar.YEAR)
-                    // Seed default members
-                    SEED_MEMBERS.forEach { name ->
-                        db.execSQL(
-                            "INSERT INTO members (name, phone, isArchived, createdAt) VALUES (?, NULL, 0, ?)",
-                            arrayOf(name, now)
-                        )
-                    }
-                    // Seed current year config with default ₦5,000
-                    db.execSQL(
-                        "INSERT OR IGNORE INTO year_configs (year, dueAmountPerMonth, startedAt) VALUES (?, ?, ?)",
-                        arrayOf(year, 5000.0, now)
-                    )
-                    // Note: app_config row is NOT seeded here – the Setup Wizard
-                    // creates it on first launch so setupComplete = false triggers wizard.
-                }
-            })
             .build()
-    }
-
-    @Provides
-    fun provideMemberDao(db: DuesDatabase): MemberDao = db.memberDao()
-
-    @Provides
-    fun providePaymentRecordDao(db: DuesDatabase): PaymentRecordDao = db.paymentRecordDao()
-
-    @Provides
-    fun provideYearConfigDao(db: DuesDatabase): YearConfigDao = db.yearConfigDao()
 
     @Provides
-    fun provideAppConfigDao(db: DuesDatabase): AppConfigDao = db.appConfigDao()
+    fun provideAppConfigDao(db: ClearrDatabase): AppConfigDao = db.appConfigDao()
 
     @Provides
-    fun provideTrackerDao(db: DuesDatabase): TrackerDao = db.trackerDao()
+    fun provideTrackerDao(db: ClearrDatabase): TrackerDao = db.trackerDao()
 
     @Provides
-    fun provideBudgetDao(db: DuesDatabase): BudgetDao = db.budgetDao()
+    fun provideBudgetDao(db: ClearrDatabase): BudgetDao = db.budgetDao()
 
     @Provides
-    fun provideTodoDao(db: DuesDatabase): TodoDao = db.todoDao()
+    fun provideTodoDao(db: ClearrDatabase): TodoDao = db.todoDao()
 
     @Provides
-    fun provideGoalsDao(db: DuesDatabase): GoalsDao = db.goalsDao()
+    fun provideGoalsDao(db: ClearrDatabase): GoalsDao = db.goalsDao()
 }
```


## `app/src/main/java/com/mikeisesele/clearr/di/RepositoryModule.kt`

```diff
diff --git a/app/src/main/java/com/mikeisesele/clearr/di/RepositoryModule.kt b/app/src/main/java/com/mikeisesele/clearr/di/RepositoryModule.kt
index 7e48248..2d22881 100644
--- a/app/src/main/java/com/mikeisesele/clearr/di/RepositoryModule.kt
+++ b/app/src/main/java/com/mikeisesele/clearr/di/RepositoryModule.kt
@@ -1,7 +1,7 @@
 package com.mikeisesele.clearr.di
 
-import com.mikeisesele.clearr.data.repository.DuesRepositoryImpl
-import com.mikeisesele.clearr.domain.repository.DuesRepository
+import com.mikeisesele.clearr.data.repository.ClearrRepositoryImpl
+import com.mikeisesele.clearr.domain.repository.ClearrRepository
 import dagger.Binds
 import dagger.Module
 import dagger.hilt.InstallIn
@@ -14,5 +14,5 @@ abstract class RepositoryModule {
 
     @Binds
     @Singleton
-    abstract fun bindDuesRepository(impl: DuesRepositoryImpl): DuesRepository
+    abstract fun bindClearrRepository(impl: ClearrRepositoryImpl): ClearrRepository
 }
```


## `app/src/main/java/com/mikeisesele/clearr/domain/trackers/ObserveTrackerSummariesUseCase.kt`

```diff
diff --git a/app/src/main/java/com/mikeisesele/clearr/domain/trackers/ObserveTrackerSummariesUseCase.kt b/app/src/main/java/com/mikeisesele/clearr/domain/trackers/ObserveTrackerSummariesUseCase.kt
index dff5945..4165180 100644
--- a/app/src/main/java/com/mikeisesele/clearr/domain/trackers/ObserveTrackerSummariesUseCase.kt
+++ b/app/src/main/java/com/mikeisesele/clearr/domain/trackers/ObserveTrackerSummariesUseCase.kt
@@ -3,40 +3,33 @@ package com.mikeisesele.clearr.domain.trackers
 import com.mikeisesele.clearr.data.model.BudgetFrequency
 import com.mikeisesele.clearr.data.model.Frequency
 import com.mikeisesele.clearr.data.model.GoalPeriodKey
-import com.mikeisesele.clearr.data.model.LayoutStyle
 import com.mikeisesele.clearr.data.model.Tracker
-import com.mikeisesele.clearr.data.model.TrackerMember
-import com.mikeisesele.clearr.data.model.TrackerPeriod
-import com.mikeisesele.clearr.data.model.TrackerRecord
 import com.mikeisesele.clearr.data.model.TrackerSummary
 import com.mikeisesele.clearr.data.model.TrackerType
 import com.mikeisesele.clearr.data.model.TodoStatus
 import com.mikeisesele.clearr.data.model.derivedStatus
-import com.mikeisesele.clearr.domain.repository.DuesRepository
+import com.mikeisesele.clearr.domain.repository.ClearrRepository
+import java.text.SimpleDateFormat
+import java.util.Calendar
+import java.util.Locale
+import javax.inject.Inject
+import javax.inject.Singleton
 import kotlinx.coroutines.ExperimentalCoroutinesApi
 import kotlinx.coroutines.flow.Flow
 import kotlinx.coroutines.flow.combine
 import kotlinx.coroutines.flow.flatMapLatest
 import kotlinx.coroutines.flow.flowOf
 import kotlinx.coroutines.flow.map
-import java.text.SimpleDateFormat
-import java.util.Calendar
-import java.util.Locale
-import javax.inject.Inject
-import javax.inject.Singleton
 
 @OptIn(ExperimentalCoroutinesApi::class)
 @Singleton
 class ObserveTrackerSummariesUseCase @Inject constructor(
-    private val repository: DuesRepository
+    private val repository: ClearrRepository
 ) {
     operator fun invoke(): Flow<List<TrackerSummary>> =
         repository.getAllTrackers().flatMapLatest { trackers ->
-            if (trackers.isEmpty()) {
-                flowOf(emptyList())
-            } else {
-                combine(trackers.map(::summaryFlow)) { summaries -> summaries.toList() }
-            }
+            if (trackers.isEmpty()) flowOf(emptyList())
+            else combine(trackers.map(::summaryFlow)) { it.toList() }
         }
 
     private fun summaryFlow(tracker: Tracker): Flow<TrackerSummary> = when (tracker.type) {
@@ -55,38 +48,22 @@ class ObserveTrackerSummariesUseCase @Inject constructor(
                                         .map { plans ->
                                             val latestPeriod = periods.lastOrNull()
                                             val periodEntries = entries.filter { it.periodId == latestPeriod?.id }
-                                            val periodPlans = plans.filter { it.periodId == latestPeriod?.id }
-                                                .associateBy { it.categoryId }
+                                            val periodPlans = plans.filter { it.periodId == latestPeriod?.id }.associateBy { it.categoryId }
                                             val totalPlannedKobo = categories.sumOf { category ->
                                                 periodPlans[category.id]?.plannedAmountKobo ?: category.plannedAmountKobo
                                             }
                                             val totalSpentKobo = periodEntries.sumOf { it.amountKobo }
                                             val clearedCount = categories.count { category ->
-                                                val spent = periodEntries
-                                                    .asSequence()
-                                                    .filter { it.categoryId == category.id }
-                                                    .sumOf { it.amountKobo }
-                                                val planned = periodPlans[category.id]?.plannedAmountKobo
-                                                    ?: category.plannedAmountKobo
+                                                val spent = periodEntries.filter { it.categoryId == category.id }.sumOf { it.amountKobo }
+                                                val planned = periodPlans[category.id]?.plannedAmountKobo ?: category.plannedAmountKobo
                                                 planned > 0L && spent >= planned
                                             }
-                                            TrackerSummary(
-                                                trackerId = tracker.id,
-                                                name = tracker.name,
-                                                type = tracker.type,
-                                                frequency = tracker.frequency,
+                                            tracker.toSummary(
                                                 currentPeriodLabel = latestPeriod?.label ?: currentPeriodLabel(tracker.frequency),
-                                                totalMembers = categories.size,
-                                                completedCount = clearedCount,
-                                                completionPercent = if (categories.isNotEmpty()) {
-                                                    ((clearedCount.toDouble() / categories.size) * 100).toInt().coerceIn(0, 100)
-                                                } else {
-                                                    0
-                                                },
+                                                total = categories.size,
+                                                completed = clearedCount,
                                                 amountCompletedKobo = totalSpentKobo,
-                                                amountTargetKobo = totalPlannedKobo,
-                                                isNew = tracker.isNew,
-                                                createdAt = tracker.createdAt
+                                                amountTargetKobo = totalPlannedKobo
                                             )
                                         }
                                 }
@@ -96,114 +73,49 @@ class ObserveTrackerSummariesUseCase @Inject constructor(
 
         TrackerType.TODO -> repository.getTodosForTracker(tracker.id).map { todos ->
             val doneCount = todos.count { it.derivedStatus() == TodoStatus.DONE }
-            TrackerSummary(
-                trackerId = tracker.id,
-                name = tracker.name,
-                type = tracker.type,
-                frequency = tracker.frequency,
+            tracker.toSummary(
                 currentPeriodLabel = "Todo List",
-                totalMembers = todos.size,
-                completedCount = doneCount,
-                completionPercent = if (todos.isNotEmpty()) {
-                    ((doneCount.toDouble() / todos.size) * 100).toInt().coerceIn(0, 100)
-                } else {
-                    0
-                },
-                isNew = tracker.isNew,
-                createdAt = tracker.createdAt
+                total = todos.size,
+                completed = doneCount
             )
         }
 
         TrackerType.GOALS -> repository.getGoalsForTracker(tracker.id)
             .flatMapLatest { goals ->
-                repository.getGoalCompletionsForTracker(tracker.id)
-                    .map { completions ->
-                        val doneCount = goals.count { goal ->
-                            val currentKey = GoalPeriodKey.currentKey(goal.frequency)
-                            completions.any { it.goalId == goal.id && it.periodKey == currentKey }
-                        }
-                        TrackerSummary(
-                            trackerId = tracker.id,
-                            name = tracker.name,
-                            type = tracker.type,
-                            frequency = tracker.frequency,
-                            currentPeriodLabel = "Today",
-                            totalMembers = goals.size,
-                            completedCount = doneCount,
-                            completionPercent = if (goals.isNotEmpty()) {
-                                ((doneCount.toDouble() / goals.size) * 100).toInt().coerceIn(0, 100)
-                            } else {
-                                0
-                            },
-                            amountCompletedKobo = 0L,
-                            amountTargetKobo = 0L,
-                            isNew = tracker.isNew,
-                            createdAt = tracker.createdAt
-                        )
+                repository.getGoalCompletionsForTracker(tracker.id).map { completions ->
+                    val doneCount = goals.count { goal ->
+                        val currentKey = GoalPeriodKey.currentKey(goal.frequency)
+                        completions.any { it.goalId == goal.id && it.periodKey == currentKey }
                     }
-            }
-
-        else -> repository.getActiveMembersForTracker(tracker.id)
-            .flatMapLatest { members ->
-                    repository.getCurrentPeriodFlow(tracker.id)
-                        .flatMapLatest { period ->
-                            if (period == null) {
-                                flowOf(buildSummary(tracker, members, period, emptyList()))
-                            } else {
-                                repository.getRecordsForPeriod(tracker.id, period.id)
-                                    .map { records -> buildSummary(tracker, members, period, records) }
-                            }
-                        }
+                    tracker.toSummary(
+                        currentPeriodLabel = "Today",
+                        total = goals.size,
+                        completed = doneCount
+                    )
+                }
             }
     }
 
-    private fun buildSummary(
-        tracker: Tracker,
-        members: List<TrackerMember>,
-        period: TrackerPeriod?,
-        records: List<TrackerRecord>
-    ): TrackerSummary {
-        val total = members.size
-        val completedCount = records.count { record ->
-            record.status.name in completedStatuses(tracker.type)
-        }
-        val amountTargetKobo = when (tracker.type) {
-            TrackerType.DUES,
-            TrackerType.EXPENSES -> (tracker.defaultAmount * 100).toLong().coerceAtLeast(0L) * total
-            else -> 0L
-        }
-        val amountCompletedKobo = when (tracker.type) {
-            TrackerType.DUES,
-            TrackerType.EXPENSES -> records.sumOf { (it.amountPaid * 100).toLong().coerceAtLeast(0L) }
-            else -> 0L
-        }
-        val percent = when {
-            total > 0 -> ((completedCount.toDouble() / total) * 100).toInt().coerceIn(0, 100)
-            else -> 0
-        }
-        return TrackerSummary(
-            trackerId = tracker.id,
-            name = tracker.name,
-            type = tracker.type,
-            frequency = tracker.frequency,
-            currentPeriodLabel = period?.label ?: currentPeriodLabel(tracker.frequency),
-            totalMembers = total,
-            completedCount = completedCount,
-            completionPercent = percent,
-            amountCompletedKobo = amountCompletedKobo,
-            amountTargetKobo = amountTargetKobo,
-            isNew = tracker.isNew,
-            createdAt = tracker.createdAt
-        )
-    }
-
-    private fun completedStatuses(type: TrackerType): Set<String> = when (type) {
-        TrackerType.DUES -> setOf("PAID")
-        TrackerType.EXPENSES -> setOf("PAID")
-        TrackerType.GOALS -> setOf("DONE")
-        TrackerType.TODO -> setOf("DONE")
-        TrackerType.BUDGET -> emptySet()
-    }
+    private fun Tracker.toSummary(
+        currentPeriodLabel: String,
+        total: Int,
+        completed: Int,
+        amountCompletedKobo: Long = 0L,
+        amountTargetKobo: Long = 0L
+    ) = TrackerSummary(
+        trackerId = id,
+        name = name,
+        type = type,
+        frequency = frequency,
+        currentPeriodLabel = currentPeriodLabel,
+        totalMembers = total,
+        completedCount = completed,
+        completionPercent = if (total > 0) ((completed.toDouble() / total) * 100).toInt().coerceIn(0, 100) else 0,
+        amountCompletedKobo = amountCompletedKobo,
+        amountTargetKobo = amountTargetKobo,
+        isNew = isNew,
+        createdAt = createdAt
+    )
 
     private fun currentPeriodLabel(frequency: Frequency): String {
         val calendar = Calendar.getInstance()
```


## `app/src/main/java/com/mikeisesele/clearr/domain/trackers/TrackerBootstrapper.kt`

```diff
diff --git a/app/src/main/java/com/mikeisesele/clearr/domain/trackers/TrackerBootstrapper.kt b/app/src/main/java/com/mikeisesele/clearr/domain/trackers/TrackerBootstrapper.kt
index c0d51c0..7f7d144 100644
--- a/app/src/main/java/com/mikeisesele/clearr/domain/trackers/TrackerBootstrapper.kt
+++ b/app/src/main/java/com/mikeisesele/clearr/domain/trackers/TrackerBootstrapper.kt
@@ -5,14 +5,14 @@ import com.mikeisesele.clearr.data.model.Frequency
 import com.mikeisesele.clearr.data.model.LayoutStyle
 import com.mikeisesele.clearr.data.model.Tracker
 import com.mikeisesele.clearr.data.model.TrackerType
-import com.mikeisesele.clearr.domain.repository.DuesRepository
+import com.mikeisesele.clearr.domain.repository.ClearrRepository
 import kotlinx.coroutines.flow.first
 import javax.inject.Inject
 import javax.inject.Singleton
 
 @Singleton
 class TrackerBootstrapper @Inject constructor(
-    private val repository: DuesRepository
+    private val repository: ClearrRepository
 ) {
     suspend fun ensureStaticTrackers() {
         val now = System.currentTimeMillis()
```


## `app/src/main/java/com/mikeisesele/clearr/ui/feature/onboarding/CompletionScreen.kt`

```diff
diff --git a/app/src/main/java/com/mikeisesele/clearr/ui/feature/onboarding/CompletionScreen.kt b/app/src/main/java/com/mikeisesele/clearr/ui/feature/onboarding/CompletionScreen.kt
index 0717111..76b962c 100644
--- a/app/src/main/java/com/mikeisesele/clearr/ui/feature/onboarding/CompletionScreen.kt
+++ b/app/src/main/java/com/mikeisesele/clearr/ui/feature/onboarding/CompletionScreen.kt
@@ -1,111 +1,82 @@
 package com.mikeisesele.clearr.ui.feature.onboarding
 
-import androidx.compose.animation.core.*
+import androidx.compose.animation.core.FastOutSlowInEasing
+import androidx.compose.animation.core.animateDpAsState
+import androidx.compose.animation.core.animateFloatAsState
+import androidx.compose.animation.core.tween
 import androidx.compose.foundation.background
-import androidx.compose.foundation.layout.*
+import androidx.compose.foundation.layout.Box
+import androidx.compose.foundation.layout.Column
+import androidx.compose.foundation.layout.PaddingValues
+import androidx.compose.foundation.layout.Spacer
+import androidx.compose.foundation.layout.fillMaxSize
+import androidx.compose.foundation.layout.fillMaxWidth
+import androidx.compose.foundation.layout.height
+import androidx.compose.foundation.layout.offset
+import androidx.compose.foundation.layout.padding
+import androidx.compose.foundation.layout.size
 import androidx.compose.foundation.shape.RoundedCornerShape
 import androidx.compose.material3.Button
 import androidx.compose.material3.ButtonDefaults
 import androidx.compose.material3.Text
-import androidx.compose.runtime.*
+import androidx.compose.runtime.Composable
+import androidx.compose.runtime.LaunchedEffect
+import androidx.compose.runtime.getValue
+import androidx.compose.runtime.mutableStateOf
+import androidx.compose.runtime.remember
+import androidx.compose.runtime.setValue
 import androidx.compose.ui.Alignment
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.draw.alpha
 import androidx.compose.ui.draw.clip
-import androidx.compose.ui.graphics.Color
 import androidx.compose.ui.text.font.FontWeight
 import androidx.compose.ui.text.style.TextAlign
 import androidx.compose.ui.tooling.preview.Preview
-import androidx.compose.ui.unit.dp
 import androidx.compose.ui.unit.sp
 import com.mikeisesele.clearr.ui.theme.ClearrColors
+import com.mikeisesele.clearr.ui.theme.ClearrDimens
+import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
 import com.mikeisesele.clearr.ui.theme.ClearrTheme
 
-/**
- * Completion Screen — shown after the last slide or skip.
- * Animates in on mount. Single CTA → SetupWizardScreen.
- * No back navigation (back-stack cleared before arriving here).
- */
 @Composable
-fun CompletionScreen(onCreateTracker: () -> Unit) {
-
+fun CompletionScreen(onOpenApp: () -> Unit) {
     var visible by remember { mutableStateOf(false) }
-
-    val alpha by animateFloatAsState(
-        targetValue = if (visible) 1f else 0f,
-        animationSpec = tween(400, easing = FastOutSlowInEasing),
-        label = "completion_alpha"
-    )
-    val offsetY by animateDpAsState(
-        targetValue = if (visible) com.mikeisesele.clearr.ui.theme.ClearrDimens.dp0 else com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12,
-        animationSpec = tween(400, easing = FastOutSlowInEasing),
-        label = "completion_offset"
-    )
+    val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(400, easing = FastOutSlowInEasing), label = "completion_alpha")
+    val offsetY by animateDpAsState(targetValue = if (visible) ClearrDimens.dp0 else ClearrDimens.dp12, animationSpec = tween(400, easing = FastOutSlowInEasing), label = "completion_offset")
 
     LaunchedEffect(Unit) { visible = true }
 
-    Box(
-        modifier = Modifier
-            .fillMaxSize()
-            .background(ClearrColors.Background),
-        contentAlignment = Alignment.Center
-    ) {
+    Box(modifier = Modifier.fillMaxSize().background(ClearrColors.Background), contentAlignment = Alignment.Center) {
         Column(
-            modifier = Modifier
-                .alpha(alpha)
-                .offset(y = offsetY)
-                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp36),
+            modifier = Modifier.alpha(alpha).offset(y = offsetY).padding(horizontal = ClearrDimens.dp36),
             horizontalAlignment = Alignment.CenterHorizontally
         ) {
-            // Icon container
             Box(
-                modifier = Modifier
-                    .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp80)
-                    .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24))
-                    .background(ClearrColors.EmeraldBg),
+                modifier = Modifier.size(ClearrDimens.dp80).clip(RoundedCornerShape(ClearrDimens.dp24)).background(ClearrColors.EmeraldBg),
                 contentAlignment = Alignment.Center
             ) {
-                Text("✓", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp36, color = ClearrColors.Emerald, fontWeight = FontWeight.ExtraBold)
+                Text("✓", fontSize = ClearrTextSizes.sp36, color = ClearrColors.Emerald, fontWeight = FontWeight.ExtraBold)
             }
 
-            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp28))
-
-            Text(
-                "You're all set.",
-                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp24,
-                fontWeight = FontWeight.Black,
-                color = ClearrColors.TextPrimary,
-                textAlign = TextAlign.Center
-            )
-
-            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10))
-
+            Spacer(Modifier.height(ClearrDimens.dp28))
+            Text("You're all set.", fontSize = ClearrTextSizes.sp24, fontWeight = FontWeight.Black, color = ClearrColors.TextPrimary, textAlign = TextAlign.Center)
+            Spacer(Modifier.height(ClearrDimens.dp10))
             Text(
-                "Let's create your first tracker. It only takes a minute.",
-                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14,
+                "Budget, goals, and todos are ready. Open Clearr and start tracking.",
+                fontSize = ClearrTextSizes.sp14,
                 color = ClearrColors.TextSecondary,
                 textAlign = TextAlign.Center,
                 lineHeight = (14 * 1.7).sp
             )
-
-            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp40))
-
+            Spacer(Modifier.height(ClearrDimens.dp40))
             Button(
-                onClick = onCreateTracker,
+                onClick = onOpenApp,
                 modifier = Modifier.fillMaxWidth(),
-                shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
-                colors = ButtonDefaults.buttonColors(
-                    containerColor = ClearrColors.Violet,
-                    contentColor = ClearrColors.Surface
-                ),
-                contentPadding = PaddingValues(vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
+                shape = RoundedCornerShape(ClearrDimens.dp16),
+                colors = ButtonDefaults.buttonColors(containerColor = ClearrColors.Violet, contentColor = ClearrColors.Surface),
+                contentPadding = PaddingValues(vertical = ClearrDimens.dp16)
             ) {
-                Text(
-                    "Create First Tracker →",
-                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15,
-                    fontWeight = FontWeight.ExtraBold,
-                    color = ClearrColors.Surface
-                )
+                Text("Open Clearr", fontSize = ClearrTextSizes.sp15, fontWeight = FontWeight.ExtraBold, color = ClearrColors.Surface)
             }
         }
     }
@@ -114,7 +85,5 @@ fun CompletionScreen(onCreateTracker: () -> Unit) {
 @Preview(showBackground = true)
 @Composable
 private fun CompletionScreenPreview() {
-    ClearrTheme {
-        CompletionScreen(onCreateTracker = {})
-    }
+    ClearrTheme { CompletionScreen(onOpenApp = {}) }
 }
```


## `app/src/main/java/com/mikeisesele/clearr/ui/feature/onboarding/components/OnboardingSlides.kt`

```diff
diff --git a/app/src/main/java/com/mikeisesele/clearr/ui/feature/onboarding/components/OnboardingSlides.kt b/app/src/main/java/com/mikeisesele/clearr/ui/feature/onboarding/components/OnboardingSlides.kt
index 703dd04..8b8e8b4 100644
--- a/app/src/main/java/com/mikeisesele/clearr/ui/feature/onboarding/components/OnboardingSlides.kt
+++ b/app/src/main/java/com/mikeisesele/clearr/ui/feature/onboarding/components/OnboardingSlides.kt
@@ -50,49 +50,49 @@ internal data class SlideData(
 )
 
 internal val slides = listOf(
-    SlideData("◎", ClearrColors.Violet, ClearrColors.VioletBg, "Clear your obligations.", "with clarity and proof, clear remittance, goals, todos, and budget tracking in one app."),
-    SlideData("◈", ClearrColors.Emerald, ClearrColors.EmeraldBg, "Every tracker,\nEvery period.", "Create independent trackers for remittance, goals, todos, or budget — with weekly, monthly, quarterly, or custom periods."),
-    SlideData("⬡", ClearrColors.Amber, ClearrColors.AmberBg, "At a glance, always.", "See what’s cleared, pending, or overdue across your obligations — without guesswork.")
+    SlideData("◎", ClearrColors.Blue, ClearrColors.BlueBg, "Plan, do, improve.", "Track budgets, goals, and todos in one focused personal workspace."),
+    SlideData("◈", ClearrColors.Emerald, ClearrColors.EmeraldBg, "Every tracker,\nEvery period.", "See what is planned, pending, done, or overdue across the parts of life you are actively managing."),
+    SlideData("⬡", ClearrColors.Amber, ClearrColors.AmberBg, "At a glance, always.", "Open Clearr and know what needs attention without digging through notes, apps, or memory.")
 )
 
-private val memberNames = listOf("Henry", "Simon", "Dare", "Tobi", "Michael")
-private data class MockTracker(val name: String, val color: Color, val bg: Color, val icon: String, val paid: Int, val total: Int)
+private val budgetCategories = listOf("Housing", "Food", "Transport", "Savings")
+private data class MockTracker(val name: String, val color: Color, val bg: Color, val icon: String, val done: Int, val total: Int)
 private val mockTrackers = listOf(
-    MockTracker("Client Remittance Status", ClearrColors.Violet, ClearrColors.VioletBg, "₦", 7, 12),
-    MockTracker("Weekly Goals Progress", ClearrColors.Emerald, ClearrColors.EmeraldBg, "✓", 18, 23),
-    MockTracker("Todo Completion Tracker", ClearrColors.Amber, ClearrColors.AmberBg, "⬡", 4, 9)
+    MockTracker("Monthly Budget", ClearrColors.Blue, ClearrColors.BlueBg, "💳", 2, 5),
+    MockTracker("Weekly Goals", ClearrColors.Emerald, ClearrColors.EmeraldBg, "✓", 3, 4),
+    MockTracker("Today Todos", ClearrColors.Amber, ClearrColors.AmberBg, "☑", 4, 7)
 )
-private val slide3Names = listOf("John", "Simon", "Jessy", "Chelsea", "Mike", "Ola.")
-private val slide3Cleared = setOf(0, 1, 3, 5)
+private val slide3Names = listOf("Budget", "Goals", "Todos")
+private val slide3Cleared = setOf(0, 1)
 
 @Composable
 internal fun Slide1Visual() {
-    var clearedIndices by remember { mutableStateOf(setOf(0, 2)) }
+    var activeIndices by remember { mutableStateOf(setOf(0, 2)) }
 
     LaunchedEffect(Unit) {
         while (true) {
             delay(1_200)
-            clearedIndices = buildSet {
-                memberNames.indices.forEach { i -> if ((i + clearedIndices.size) % 2 == 0) add(i) }
+            activeIndices = buildSet {
+                budgetCategories.indices.forEach { i -> if ((i + activeIndices.size) % 2 == 0) add(i) }
                 if (size < 2) add(0)
             }
         }
     }
 
     Column(verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp6), modifier = Modifier.fillMaxWidth()) {
-        memberNames.forEachIndexed { i, name ->
-            val cleared = i in clearedIndices
-            val rowOffset by animateDpAsState(targetValue = if (cleared) (-2).dp else ClearrDimens.dp2, animationSpec = tween(400), label = "row_offset_$i")
-            val avatarBg = if (cleared) ClearrColors.EmeraldBg else ClearrColors.Border
-            val statusColor = if (cleared) ClearrColors.Emerald else ClearrColors.TextMuted
+        budgetCategories.forEachIndexed { i, name ->
+            val active = i in activeIndices
+            val rowOffset by animateDpAsState(targetValue = if (active) (-2).dp else ClearrDimens.dp2, animationSpec = tween(400), label = "row_offset_$i")
+            val avatarBg = if (active) ClearrColors.BlueBg else ClearrColors.Border
+            val statusColor = if (active) ClearrColors.Blue else ClearrColors.TextMuted
 
             Surface(color = ClearrColors.Surface, shape = RoundedCornerShape(ClearrDimens.dp14), shadowElevation = ClearrDimens.dp1, modifier = Modifier.fillMaxWidth().offset(x = rowOffset)) {
                 Row(modifier = Modifier.padding(horizontal = ClearrDimens.dp14, vertical = ClearrDimens.dp10), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp10)) {
                     Box(modifier = Modifier.size(ClearrDimens.dp30).clip(CircleShape).background(avatarBg), contentAlignment = Alignment.Center) {
-                        Text(name.first().toString(), fontSize = ClearrTextSizes.sp13, color = if (cleared) ClearrColors.Emerald else ClearrColors.TextSecondary)
+                        Text(name.first().toString(), fontSize = ClearrTextSizes.sp13, color = if (active) ClearrColors.Blue else ClearrColors.TextSecondary)
                     }
                     Text(name, fontSize = ClearrTextSizes.sp13, color = ClearrColors.TextPrimary, modifier = Modifier.weight(1f))
-                    Text(if (cleared) "Cleared ✓" else "Pending...", fontSize = ClearrTextSizes.sp11, color = statusColor)
+                    Text(if (active) "Tracked" else "Waiting", fontSize = ClearrTextSizes.sp11, color = statusColor)
                     Box(modifier = Modifier.size(ClearrDimens.dp7).clip(CircleShape).background(statusColor))
                 }
             }
@@ -107,7 +107,7 @@ internal fun Slide2Visual() {
 
     Column(verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp8), modifier = Modifier.fillMaxWidth()) {
         mockTrackers.forEachIndexed { i, tracker ->
-            val pct = tracker.paid.toFloat() / tracker.total
+            val pct = tracker.done.toFloat() / tracker.total
             val animatedPct by animateFloatAsState(targetValue = if (animated) pct else 0f, animationSpec = tween(1000, delayMillis = i * 100, easing = FastOutSlowInEasing), label = "bar_$i")
             val cardAlpha by animateFloatAsState(targetValue = if (animated) 1f else 0f, animationSpec = tween(300, delayMillis = i * 100), label = "card_alpha_$i")
             val cardOffset by animateDpAsState(targetValue = if (animated) ClearrDimens.dp0 else ClearrDimens.dp12, animationSpec = tween(300, delayMillis = i * 100), label = "card_offset_$i")
@@ -120,7 +120,7 @@ internal fun Slide2Visual() {
                     Column(modifier = Modifier.weight(1f)) {
                         Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                             Text(tracker.name, fontSize = ClearrTextSizes.sp12, color = ClearrColors.TextPrimary)
-                            Text("${tracker.paid}/${tracker.total}", fontSize = ClearrTextSizes.sp11, color = tracker.color)
+                            Text("${tracker.done}/${tracker.total}", fontSize = ClearrTextSizes.sp11, color = tracker.color)
                         }
                         Spacer(Modifier.height(ClearrDimens.dp5))
                         Box(modifier = Modifier.fillMaxWidth().height(ClearrDimens.dp5).clip(RoundedCornerShape(ClearrDimens.dp99)).background(ClearrColors.Border)) {
@@ -147,26 +147,26 @@ internal fun Slide3Visual() {
     Surface(color = ClearrColors.Surface, shape = RoundedCornerShape(ClearrDimens.dp16), shadowElevation = ClearrDimens.dp2, modifier = Modifier.fillMaxWidth()) {
         Column(modifier = Modifier.padding(ClearrDimens.dp16)) {
             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
-                Text("February 2026", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = ClearrTextSizes.sp14, color = ClearrColors.TextPrimary)
-                Text("${(pct * 100).toInt()}%", fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, fontSize = ClearrTextSizes.sp14, color = ClearrColors.Violet)
+                Text("This week", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = ClearrTextSizes.sp14, color = ClearrColors.TextPrimary)
+                Text("${(pct * 100).toInt()}%", fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, fontSize = ClearrTextSizes.sp14, color = ClearrColors.Blue)
             }
             Spacer(Modifier.height(ClearrDimens.dp8))
             Box(modifier = Modifier.fillMaxWidth().height(ClearrDimens.dp6).clip(RoundedCornerShape(ClearrDimens.dp99)).background(ClearrColors.Border)) {
-                Box(modifier = Modifier.fillMaxWidth(animatedPct).height(ClearrDimens.dp6).clip(RoundedCornerShape(ClearrDimens.dp99)).background(ClearrColors.Violet))
+                Box(modifier = Modifier.fillMaxWidth(animatedPct).height(ClearrDimens.dp6).clip(RoundedCornerShape(ClearrDimens.dp99)).background(ClearrColors.Blue))
             }
             Spacer(Modifier.height(ClearrDimens.dp12))
             FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp6), verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp5)) {
                 slide3Names.forEachIndexed { index, name ->
                     val cleared = index in slide3Cleared
-                    Box(modifier = Modifier.clip(RoundedCornerShape(ClearrDimens.dp20)).background(if (cleared) ClearrColors.EmeraldBg else ClearrColors.CoralBg).padding(horizontal = ClearrDimens.dp10, vertical = ClearrDimens.dp4)) {
-                        Text(name, fontSize = ClearrTextSizes.sp10, color = if (cleared) ClearrColors.Emerald else ClearrColors.Coral)
+                    Box(modifier = Modifier.clip(RoundedCornerShape(ClearrDimens.dp20)).background(if (cleared) ClearrColors.EmeraldBg else ClearrColors.AmberBg).padding(horizontal = ClearrDimens.dp10, vertical = ClearrDimens.dp4)) {
+                        Text(name, fontSize = ClearrTextSizes.sp10, color = if (cleared) ClearrColors.Emerald else ClearrColors.Amber)
                     }
                 }
             }
             Spacer(Modifier.height(ClearrDimens.dp10))
             Row(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)) {
-                OnboardingStatTile("Cleared", "$clearedCount", ClearrColors.Emerald, ClearrColors.EmeraldBg, Modifier.weight(1f))
-                OnboardingStatTile("Pending", "${totalCount - clearedCount}", ClearrColors.Amber, ClearrColors.AmberBg, Modifier.weight(1f))
+                OnboardingStatTile("Stable", "$clearedCount", ClearrColors.Emerald, ClearrColors.EmeraldBg, Modifier.weight(1f))
+                OnboardingStatTile("Needs work", "${totalCount - clearedCount}", ClearrColors.Amber, ClearrColors.AmberBg, Modifier.weight(1f))
             }
         }
     }
```


## `app/src/main/java/com/mikeisesele/clearr/ui/navigation/AppShellViewModel.kt`

```diff
diff --git a/app/src/main/java/com/mikeisesele/clearr/ui/navigation/AppShellViewModel.kt b/app/src/main/java/com/mikeisesele/clearr/ui/navigation/AppShellViewModel.kt
index ea331e3..4920324 100644
--- a/app/src/main/java/com/mikeisesele/clearr/ui/navigation/AppShellViewModel.kt
+++ b/app/src/main/java/com/mikeisesele/clearr/ui/navigation/AppShellViewModel.kt
@@ -3,14 +3,13 @@ package com.mikeisesele.clearr.ui.navigation
 import com.mikeisesele.clearr.core.base.BaseViewModel
 import com.mikeisesele.clearr.core.base.contract.BaseState
 import com.mikeisesele.clearr.core.base.contract.ViewEvent
-import com.mikeisesele.clearr.data.model.TrackerSummary
 import com.mikeisesele.clearr.data.model.TrackerType
 import com.mikeisesele.clearr.domain.trackers.ObserveTrackerSummariesUseCase
 import com.mikeisesele.clearr.domain.trackers.TrackerBootstrapper
 import com.mikeisesele.clearr.ui.feature.dashboard.utils.primarySummaryOf
 import dagger.hilt.android.lifecycle.HiltViewModel
-import kotlinx.coroutines.flow.collectLatest
 import javax.inject.Inject
+import kotlinx.coroutines.flow.collectLatest
 
 @HiltViewModel
 class AppShellViewModel @Inject constructor(
@@ -39,9 +38,6 @@ class AppShellViewModel @Inject constructor(
                         budgetTrackerId = summaries.primarySummaryOf(TrackerType.BUDGET)?.trackerId,
                         todoTrackerId = summaries.primarySummaryOf(TrackerType.TODO)?.trackerId,
                         goalsTrackerId = summaries.primarySummaryOf(TrackerType.GOALS)?.trackerId,
-                        remittanceCount = summaries.count { summary ->
-                            summary.type == TrackerType.DUES || summary.type == TrackerType.EXPENSES
-                        },
                         isLoading = false
                     )
                 }
@@ -54,7 +50,6 @@ data class AppShellUiState(
     val budgetTrackerId: Long? = null,
     val todoTrackerId: Long? = null,
     val goalsTrackerId: Long? = null,
-    val remittanceCount: Int = 0,
     val isLoading: Boolean = true
 ) : BaseState
 
```


## `app/src/main/java/com/mikeisesele/clearr/ui/navigation/NavRoutes.kt`

```diff
diff --git a/app/src/main/java/com/mikeisesele/clearr/ui/navigation/NavRoutes.kt b/app/src/main/java/com/mikeisesele/clearr/ui/navigation/NavRoutes.kt
index e5bc6ab..98e05c4 100644
--- a/app/src/main/java/com/mikeisesele/clearr/ui/navigation/NavRoutes.kt
+++ b/app/src/main/java/com/mikeisesele/clearr/ui/navigation/NavRoutes.kt
@@ -1,9 +1,7 @@
 package com.mikeisesele.clearr.ui.navigation
 
 sealed class NavRoutes(val route: String) {
-    object Setup : NavRoutes("setup")
     object Dashboard : NavRoutes("dashboard")
-    object RemittanceHome : NavRoutes("remittance_home")
     object BudgetRoot : NavRoutes("budget_root/{trackerId}") {
         fun createRoute(trackerId: Long) = "budget_root/$trackerId"
         const val baseRoute = "budget_root"
@@ -16,9 +14,6 @@ sealed class NavRoutes(val route: String) {
         fun createRoute(trackerId: Long) = "goals_root/$trackerId"
         const val baseRoute = "goals_root"
     }
-    object TrackerDetail : NavRoutes("tracker_detail/{trackerId}") {
-        fun createRoute(trackerId: Long) = "tracker_detail/$trackerId"
-    }
     object TodoAdd : NavRoutes("todo_add/{trackerId}") {
         fun createRoute(trackerId: Long) = "todo_add/$trackerId"
     }
@@ -28,6 +23,5 @@ sealed class NavRoutes(val route: String) {
     object BudgetAddCategory : NavRoutes("budget_add_category/{trackerId}") {
         fun createRoute(trackerId: Long) = "budget_add_category/$trackerId"
     }
-    object Settings : NavRoutes("settings")
     object Home : NavRoutes("dashboard")
 }
```


## `app/src/main/java/com/mikeisesele/clearr/ui/navigation/components/AppBottomNav.kt`

```diff
diff --git a/app/src/main/java/com/mikeisesele/clearr/ui/navigation/components/AppBottomNav.kt b/app/src/main/java/com/mikeisesele/clearr/ui/navigation/components/AppBottomNav.kt
index 93ce777..4a3549c 100644
--- a/app/src/main/java/com/mikeisesele/clearr/ui/navigation/components/AppBottomNav.kt
+++ b/app/src/main/java/com/mikeisesele/clearr/ui/navigation/components/AppBottomNav.kt
@@ -6,7 +6,6 @@ import androidx.compose.material.icons.filled.AccountBalanceWallet
 import androidx.compose.material.icons.filled.CheckCircle
 import androidx.compose.material.icons.filled.Checklist
 import androidx.compose.material.icons.filled.Home
-import androidx.compose.material.icons.filled.Payments
 import androidx.compose.material3.Icon
 import androidx.compose.material3.NavigationBar
 import androidx.compose.material3.NavigationBarItem
@@ -16,7 +15,7 @@ import androidx.compose.runtime.Composable
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.graphics.vector.ImageVector
 import com.mikeisesele.clearr.ui.theme.ClearrColors
-import com.mikeisesele.clearr.ui.theme.LocalDuesColors
+import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors
 
 internal enum class AppBottomNavItem(
     val label: String,
@@ -25,8 +24,7 @@ internal enum class AppBottomNavItem(
     HOME("Home", Icons.Filled.Home),
     BUDGET("Budget", Icons.Filled.AccountBalanceWallet),
     TODOS("Todos", Icons.Filled.Checklist),
-    GOALS("Goals", Icons.Filled.CheckCircle),
-    REMITTANCE("Remittance", Icons.Filled.Payments)
+    GOALS("Goals", Icons.Filled.CheckCircle)
 }
 
 @Composable
@@ -34,7 +32,7 @@ internal fun AppBottomNav(
     selectedItem: AppBottomNavItem?,
     onSelect: (AppBottomNavItem) -> Unit
 ) {
-    val colors = LocalDuesColors.current
+    val colors = LocalClearrUiColors.current
     NavigationBar(
         containerColor = colors.surface,
         contentColor = colors.text,
```


## `app/src/main/java/com/mikeisesele/clearr/ui/theme/Color.kt`

```diff
diff --git a/app/src/main/java/com/mikeisesele/clearr/ui/theme/Color.kt b/app/src/main/java/com/mikeisesele/clearr/ui/theme/Color.kt
index 350db88..1771e35 100644
--- a/app/src/main/java/com/mikeisesele/clearr/ui/theme/Color.kt
+++ b/app/src/main/java/com/mikeisesele/clearr/ui/theme/Color.kt
@@ -1,102 +1,78 @@
 package com.mikeisesele.clearr.ui.theme
 
 import androidx.compose.ui.graphics.Color
-import com.mikeisesele.clearr.data.model.RecordStatus
 import com.mikeisesele.clearr.data.model.TrackerType
 
-/**
- * Clearr Brand Color System
- * Four semantic primaries — every color carries meaning, never decorative.
- * Rule: Never hardcode a hex anywhere in the UI. Always reference ClearrColors.*
- */
 object ClearrColors {
-
-    // ── BRAND PRIMARIES ───────────────────────────────────────────────────────
-    val Violet  = Color(0xFF6C63FF)  // Primary / Dues
-    val Emerald = Color(0xFF00A67E)  // Success / Attendance / Cleared
-    val Amber   = Color(0xFFF59E0B)  // Caution / Tasks / Pending
-    val Blue    = Color(0xFF3B82F6)  // Info / Budget
-    val Coral   = Color(0xFFEF4444)  // Danger / Events / Unpaid / Absent
-    val Orange  = Color(0xFFFF9500)  // Emphasis for due-today / medium priority
-
-    // ── BRAND TOKENS (for logo/marketing + app-level theming) ───────────────
-    val BrandPrimary   = Violet
+    val Violet = Color(0xFF6C63FF)
+    val Emerald = Color(0xFF00A67E)
+    val Amber = Color(0xFFF59E0B)
+    val Blue = Color(0xFF3B82F6)
+    val Coral = Color(0xFFEF4444)
+    val Orange = Color(0xFFFF9500)
+
+    val BrandPrimary = Violet
     val BrandSecondary = Emerald
-    val BrandAccent    = Amber
-    val BrandDanger    = Coral
+    val BrandAccent = Amber
+    val BrandDanger = Coral
     val BrandBackground = Color(0xFFF7F7FB)
-    val BrandText      = Color(0xFF1A1A2E)
+    val BrandText = Color(0xFF1A1A2E)
 
-    // ── TINTED BACKGROUNDS (12% opacity on white) ─────────────────────────────
-    val VioletBg  = Color(0xFFEEF0FF)
+    val VioletBg = Color(0xFFEEF0FF)
     val EmeraldBg = Color(0xFFE6F7F3)
-    val AmberBg   = Color(0xFFFEF3C7)
-    val BlueBg    = Color(0xFFEFF6FF)
-    val CoralBg   = Color(0xFFFEE2E2)
+    val AmberBg = Color(0xFFFEF3C7)
+    val BlueBg = Color(0xFFEFF6FF)
+    val CoralBg = Color(0xFFFEE2E2)
 
-    // ── TINTED SURFACES (18% opacity — chips, badges, icon containers) ────────
-    val VioletSurface  = Color(0xFFE8E6FF)
+    val VioletSurface = Color(0xFFE8E6FF)
     val EmeraldSurface = Color(0xFFD1F5EA)
-    val AmberSurface   = Color(0xFFFEF3C7)
-    val BlueSurface    = Color(0xFFDCEEFF)
-    val CoralSurface   = Color(0xFFFFE4E4)
-
-    // ── NEUTRALS ──────────────────────────────────────────────────────────────
-    val Background   = Color(0xFFF7F7FB)   // App background
-    val Surface      = Color(0xFFFFFFFF)   // Cards, sheets
-    val Border       = Color(0xFFF0F0F0)   // Dividers, inactive progress track
-    val TextPrimary  = Color(0xFF1A1A2E)   // Headlines, primary text
-    val TextSecondary= Color(0xFF888888)   // Subtitles, hints
-    val TextMuted    = Color(0xFFBBBBBB)   // Placeholders, disabled
-    val Inactive     = Color(0xFFDDDDDD)   // Inactive dots, empty bars
-    val NavBg        = Color(0xFFEBEBF0)   // Back button background
-    val Transparent  = Color.Transparent
-
-    // ── DARK MODE VARIANTS ────────────────────────────────────────────────────
-    val DarkBackground  = Color(0xFF0F0F1A)
-    val DarkSurface     = Color(0xFF1A1A2E)
-    val DarkCard        = Color(0xFF242438)
-    val DarkBorder      = Color(0xFF2A2A3E)
+    val AmberSurface = Color(0xFFFEF3C7)
+    val BlueSurface = Color(0xFFDCEEFF)
+    val CoralSurface = Color(0xFFFFE4E4)
+
+    val Background = Color(0xFFF7F7FB)
+    val Surface = Color(0xFFFFFFFF)
+    val Border = Color(0xFFF0F0F0)
+    val TextPrimary = Color(0xFF1A1A2E)
+    val TextSecondary = Color(0xFF888888)
+    val TextMuted = Color(0xFFBBBBBB)
+    val Inactive = Color(0xFFDDDDDD)
+    val NavBg = Color(0xFFEBEBF0)
+    val Transparent = Color.Transparent
+
+    val DarkBackground = Color(0xFF0F0F1A)
+    val DarkSurface = Color(0xFF1A1A2E)
+    val DarkCard = Color(0xFF242438)
+    val DarkBorder = Color(0xFF2A2A3E)
     val DarkTextPrimary = Color(0xFFF0F0F8)
-    val DarkTextMuted   = Color(0xFF888888)
-    val DarkInactive    = Color(0xFF3A3A50)
+    val DarkTextMuted = Color(0xFF888888)
+    val DarkInactive = Color(0xFF3A3A50)
 
-    // ── MISC LEGACY (kept for WhatsApp share button, not brand palette) ───────
     val WhatsAppGreen = Color(0xFF25D366)
 }
 
-// ── TrackerType extensions ────────────────────────────────────────────────────
-
 fun TrackerType.brandColor(): Color = when (this) {
-    TrackerType.DUES     -> ClearrColors.Violet
-    TrackerType.EXPENSES -> ClearrColors.Violet
-    TrackerType.GOALS    -> ClearrColors.Emerald
-    TrackerType.TODO     -> ClearrColors.Amber
-    TrackerType.BUDGET   -> ClearrColors.Blue
+    TrackerType.GOALS -> ClearrColors.Emerald
+    TrackerType.TODO -> ClearrColors.Amber
+    TrackerType.BUDGET -> ClearrColors.Blue
 }
 
 fun TrackerType.brandBackground(): Color = when (this) {
-    TrackerType.DUES     -> ClearrColors.VioletBg
-    TrackerType.EXPENSES -> ClearrColors.VioletBg
-    TrackerType.GOALS    -> ClearrColors.EmeraldBg
-    TrackerType.TODO     -> ClearrColors.AmberBg
-    TrackerType.BUDGET   -> ClearrColors.BlueBg
+    TrackerType.GOALS -> ClearrColors.EmeraldBg
+    TrackerType.TODO -> ClearrColors.AmberBg
+    TrackerType.BUDGET -> ClearrColors.BlueBg
 }
 
 fun TrackerType.brandSurface(): Color = when (this) {
-    TrackerType.DUES     -> ClearrColors.VioletSurface
-    TrackerType.EXPENSES -> ClearrColors.VioletSurface
-    TrackerType.GOALS    -> ClearrColors.EmeraldSurface
-    TrackerType.TODO     -> ClearrColors.AmberSurface
-    TrackerType.BUDGET   -> ClearrColors.BlueSurface
+    TrackerType.GOALS -> ClearrColors.EmeraldSurface
+    TrackerType.TODO -> ClearrColors.AmberSurface
+    TrackerType.BUDGET -> ClearrColors.BlueSurface
 }
 
 fun TrackerType.brandIcon(): String = when (this) {
-    TrackerType.DUES     -> "₦"
-    TrackerType.EXPENSES -> "₦"
-    TrackerType.GOALS    -> "🎯"
-    TrackerType.TODO     -> "☑"
-    TrackerType.BUDGET   -> "💳"
+    TrackerType.GOALS -> "🎯"
+    TrackerType.TODO -> "☑"
+    TrackerType.BUDGET -> "💳"
 }
 
 data class BudgetColorScheme(
@@ -105,104 +81,37 @@ data class BudgetColorScheme(
 )
 
 fun ClearrColors.fromToken(token: String): BudgetColorScheme = when (token.lowercase()) {
-    "teal" -> BudgetColorScheme(Emerald, EmeraldBg)
-    "emerald" -> BudgetColorScheme(Emerald, EmeraldBg)
+    "teal", "emerald" -> BudgetColorScheme(Emerald, EmeraldBg)
     "coral" -> BudgetColorScheme(Coral, CoralBg)
     "amber" -> BudgetColorScheme(Amber, AmberBg)
-    "violet" -> BudgetColorScheme(Violet, VioletBg)
+    "violet", "purple" -> BudgetColorScheme(Violet, VioletBg)
     "blue" -> BudgetColorScheme(Blue, BlueBg)
-    "purple" -> BudgetColorScheme(Violet, VioletBg)
     "orange" -> BudgetColorScheme(Color(0xFFF97316), Color(0xFFFFF3E8))
     else -> BudgetColorScheme(Violet, VioletBg)
 }
 
-// ── RecordStatus extensions ───────────────────────────────────────────────────
-
-fun RecordStatus.brandColor(): Color = when (this) {
-    RecordStatus.PAID,
-    RecordStatus.PRESENT,
-    RecordStatus.DONE    -> ClearrColors.Emerald
-    RecordStatus.PARTIAL,
-    RecordStatus.PENDING -> ClearrColors.Amber
-    RecordStatus.UNPAID,
-    RecordStatus.ABSENT  -> ClearrColors.Coral
-}
-
-fun RecordStatus.brandBackground(): Color = when (this) {
-    RecordStatus.PAID,
-    RecordStatus.PRESENT,
-    RecordStatus.DONE    -> ClearrColors.EmeraldBg
-    RecordStatus.PARTIAL,
-    RecordStatus.PENDING -> ClearrColors.AmberBg
-    RecordStatus.UNPAID,
-    RecordStatus.ABSENT  -> ClearrColors.CoralBg
-}
-
-fun RecordStatus.brandLabel(type: TrackerType): String = when (type) {
-    TrackerType.DUES -> when (this) {
-        RecordStatus.PAID    -> "Paid"
-        RecordStatus.UNPAID  -> "Unpaid"
-        RecordStatus.PARTIAL -> "Partial"
-        else -> name.lowercase().replaceFirstChar { it.uppercase() }
-    }
-    TrackerType.EXPENSES -> when (this) {
-        RecordStatus.PAID    -> "Paid"
-        RecordStatus.UNPAID  -> "Unpaid"
-        RecordStatus.PARTIAL -> "Partial"
-        else -> name.lowercase().replaceFirstChar { it.uppercase() }
-    }
-    TrackerType.GOALS -> when (this) {
-        RecordStatus.DONE    -> "Done"
-        RecordStatus.PENDING -> "Pending"
-        else -> name.lowercase().replaceFirstChar { it.uppercase() }
-    }
-    TrackerType.TODO -> when (this) {
-        RecordStatus.DONE    -> "Done"
-        RecordStatus.PENDING -> "Pending"
-        else -> name.lowercase().replaceFirstChar { it.uppercase() }
-    }
-    TrackerType.BUDGET -> when (this) {
-        RecordStatus.PAID -> "On Track"
-        RecordStatus.PARTIAL -> "Near Limit"
-        RecordStatus.UNPAID -> "Over"
-        else -> name.lowercase().replaceFirstChar { it.uppercase() }
-    }
-}
-
-// ── Legacy aliases kept for backward compatibility with existing screens ───────
-// These are the old Indigo/Green/Amber/Red tokens referenced in Theme.kt.
-// Gradually migrate all call-sites to ClearrColors.*
-val Violet  = ClearrColors.Violet
+val Violet = ClearrColors.Violet
 val Emerald = ClearrColors.Emerald
-val Amber   = ClearrColors.Amber
-val Coral   = ClearrColors.Coral
-
-// Old Indigo aliases (map to Clearr Violet)
+val Amber = ClearrColors.Amber
+val Coral = ClearrColors.Coral
 val Indigo400 = ClearrColors.Violet
 val Indigo500 = ClearrColors.Violet
-val Indigo600 = Color(0xFF5652D6)  // slightly darker shade, used in dark scheme
-
-// Old semantic aliases
+val Indigo600 = Color(0xFF5652D6)
 val Green400 = ClearrColors.Emerald
 val Amber400 = ClearrColors.Amber
-val Red400   = ClearrColors.Coral
-
-// Old dark palette
-val DarkBg      = ClearrColors.DarkBackground
+val Red400 = ClearrColors.Coral
+val DarkBg = ClearrColors.DarkBackground
 val DarkSurface = ClearrColors.DarkSurface
-val DarkCard    = ClearrColors.DarkCard
-val DarkBorder  = ClearrColors.DarkBorder
-val DarkText    = ClearrColors.DarkTextPrimary
-val DarkMuted   = ClearrColors.DarkTextMuted
-val DarkDim     = ClearrColors.DarkInactive
-
-// Old light palette
-val LightBg      = ClearrColors.Background
+val DarkCard = ClearrColors.DarkCard
+val DarkBorder = ClearrColors.DarkBorder
+val DarkText = ClearrColors.DarkTextPrimary
+val DarkMuted = ClearrColors.DarkTextMuted
+val DarkDim = ClearrColors.DarkInactive
+val LightBg = ClearrColors.Background
 val LightSurface = ClearrColors.Surface
-val LightCard    = Color(0xFFF1F5F9)
-val LightBorder  = ClearrColors.Border
-val LightText    = ClearrColors.TextPrimary
-val LightMuted   = ClearrColors.TextSecondary
-val LightDim     = ClearrColors.Inactive
-
+val LightCard = Color(0xFFF1F5F9)
+val LightBorder = ClearrColors.Border
+val LightText = ClearrColors.TextPrimary
+val LightMuted = ClearrColors.TextSecondary
+val LightDim = ClearrColors.Inactive
 val WhatsAppGreen = ClearrColors.WhatsAppGreen
```


## `app/src/main/java/com/mikeisesele/clearr/ui/theme/Theme.kt`

```diff
diff --git a/app/src/main/java/com/mikeisesele/clearr/ui/theme/Theme.kt b/app/src/main/java/com/mikeisesele/clearr/ui/theme/Theme.kt
index 094954a..3ddbed4 100644
--- a/app/src/main/java/com/mikeisesele/clearr/ui/theme/Theme.kt
+++ b/app/src/main/java/com/mikeisesele/clearr/ui/theme/Theme.kt
@@ -47,10 +47,10 @@ private val LightColorScheme = lightColorScheme(
 )
 
 /**
- * Theme-aware color bag used throughout the app via LocalDuesColors.current.
+ * Theme-aware color bag used throughout the app via LocalClearrUiColors.current.
  * Maps Clearr brand tokens to semantic slots used by existing composables.
  */
-data class DuesColors(
+data class ClearrUiColors(
     val bg: Color,
     val surface: Color,
     val card: Color,
@@ -61,7 +61,7 @@ data class DuesColors(
     val green: Color,
     /** Caution / pending — Clearr Amber */
     val amber: Color,
-    /** Danger / unpaid — Clearr Coral */
+    /** Danger — Clearr Coral */
     val red: Color,
     val text: Color,
     val muted: Color,
@@ -69,8 +69,8 @@ data class DuesColors(
     val isDark: Boolean
 )
 
-val LocalDuesColors = staticCompositionLocalOf {
-    DuesColors(
+val LocalClearrUiColors = staticCompositionLocalOf {
+    ClearrUiColors(
         bg      = ClearrColors.DarkBackground,
         surface = ClearrColors.DarkSurface,
         card    = ClearrColors.DarkCard,
@@ -86,8 +86,8 @@ val LocalDuesColors = staticCompositionLocalOf {
     )
 }
 
-/** Light-mode DuesColors instance using Clearr tokens */
-private fun lightDuesColors() = DuesColors(
+/** Light-mode ClearrUiColors instance using Clearr tokens */
+private fun lightClearrUiColors() = ClearrUiColors(
     bg      = ClearrColors.BrandBackground,
     surface = ClearrColors.Surface,
     card    = LightCard,
@@ -102,8 +102,8 @@ private fun lightDuesColors() = DuesColors(
     isDark  = false
 )
 
-/** Dark-mode DuesColors instance using Clearr tokens */
-private fun darkDuesColors() = DuesColors(
+/** Dark-mode ClearrUiColors instance using Clearr tokens */
+private fun darkClearrUiColors() = ClearrUiColors(
     bg      = ClearrColors.DarkBackground,
     surface = ClearrColors.DarkSurface,
     card    = ClearrColors.DarkCard,
@@ -139,10 +139,10 @@ fun ClearrTheme(
         else      -> LightColorScheme
     }
 
-    val duesColors = if (darkTheme) darkDuesColors() else lightDuesColors()
+    val uiColors = if (darkTheme) darkClearrUiColors() else lightClearrUiColors()
 
     CompositionLocalProvider(
-        LocalDuesColors provides duesColors,
+        LocalClearrUiColors provides uiColors,
         LocalClearrSpacing provides ClearrSpacing(),
         LocalClearrRadii provides ClearrRadii(),
         LocalClearrSizes provides ClearrSizes()
```

