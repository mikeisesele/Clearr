package com.mikeisesele.clearr.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mikeisesele.clearr.data.dao.AppConfigDao
import com.mikeisesele.clearr.data.dao.BudgetDao
import com.mikeisesele.clearr.data.dao.GoalsDao
import com.mikeisesele.clearr.data.dao.TrackerDao
import com.mikeisesele.clearr.data.dao.TodoDao
import com.mikeisesele.clearr.data.model.AppConfigEntity
import com.mikeisesele.clearr.data.model.BudgetCategoryEntity
import com.mikeisesele.clearr.data.model.BudgetCategoryPlanEntity
import com.mikeisesele.clearr.data.model.BudgetEntryEntity
import com.mikeisesele.clearr.data.model.BudgetPeriodEntity
import com.mikeisesele.clearr.data.model.GoalCompletionEntity
import com.mikeisesele.clearr.data.model.GoalEntity
import com.mikeisesele.clearr.data.model.TodoEntity
import com.mikeisesele.clearr.data.model.TrackerEntity

@Database(
    entities = [
        AppConfigEntity::class,
        TrackerEntity::class,
        BudgetPeriodEntity::class,
        BudgetCategoryEntity::class,
        BudgetCategoryPlanEntity::class,
        BudgetEntryEntity::class,
        TodoEntity::class,
        GoalEntity::class,
        GoalCompletionEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class ClearrDatabase : RoomDatabase() {
    abstract fun appConfigDao(): AppConfigDao
    abstract fun trackerDao(): TrackerDao
    abstract fun budgetDao(): BudgetDao
    abstract fun todoDao(): TodoDao
    abstract fun goalsDao(): GoalsDao
}
