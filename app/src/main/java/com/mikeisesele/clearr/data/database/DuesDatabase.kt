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
