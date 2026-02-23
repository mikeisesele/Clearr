package com.mikeisesele.clearr.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mikeisesele.clearr.data.dao.AppConfigDao
import com.mikeisesele.clearr.data.dao.BudgetDao
import com.mikeisesele.clearr.data.dao.MemberDao
import com.mikeisesele.clearr.data.dao.PaymentRecordDao
import com.mikeisesele.clearr.data.dao.TrackerDao
import com.mikeisesele.clearr.data.dao.TodoDao
import com.mikeisesele.clearr.data.dao.YearConfigDao
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetEntry
import com.mikeisesele.clearr.data.model.BudgetPeriod
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.data.model.PaymentRecord
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
        BudgetEntry::class,
        TodoEntity::class,
    ],
    version = 6,
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

    companion object {
        /**
         * Migration 1 → 2: adds the app_config singleton table.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `app_config` (
                        `id` INTEGER NOT NULL DEFAULT 1,
                        `groupName` TEXT NOT NULL DEFAULT 'JSS Durumi Brothers',
                        `adminName` TEXT NOT NULL DEFAULT '',
                        `adminPhone` TEXT NOT NULL DEFAULT '',
                        `trackerType` TEXT NOT NULL DEFAULT 'DUES',
                        `frequency` TEXT NOT NULL DEFAULT 'MONTHLY',
                        `defaultAmount` REAL NOT NULL DEFAULT 5000.0,
                        `customPeriodLabels` TEXT NOT NULL DEFAULT '[]',
                        `variableAmounts` TEXT NOT NULL DEFAULT '[]',
                        `layoutStyle` TEXT NOT NULL DEFAULT 'GRID',
                        `remindersEnabled` INTEGER NOT NULL DEFAULT 1,
                        `reminderDayOfPeriod` INTEGER NOT NULL DEFAULT 5,
                        `setupComplete` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        /**
         * Migration 2 → 3: adds multi-tracker tables.
         * trackers       — top-level tracker registry
         * tracker_members — per-tracker member list (isolated from global members)
         * tracker_periods — period records per tracker
         * tracker_records — per-member per-period status entries
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `trackers` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `type` TEXT NOT NULL DEFAULT 'DUES',
                        `frequency` TEXT NOT NULL DEFAULT 'MONTHLY',
                        `defaultAmount` REAL NOT NULL DEFAULT 5000.0,
                        `isNew` INTEGER NOT NULL DEFAULT 1,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tracker_members` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `trackerId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `phone` TEXT,
                        `isArchived` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tracker_periods` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `trackerId` INTEGER NOT NULL,
                        `label` TEXT NOT NULL,
                        `startDate` INTEGER NOT NULL,
                        `endDate` INTEGER NOT NULL,
                        `isCurrent` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tracker_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `trackerId` INTEGER NOT NULL,
                        `periodId` INTEGER NOT NULL,
                        `memberId` INTEGER NOT NULL,
                        `status` TEXT NOT NULL DEFAULT 'UNPAID',
                        `amountPaid` REAL NOT NULL DEFAULT 0.0,
                        `note` TEXT,
                        `updatedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

    }
}
