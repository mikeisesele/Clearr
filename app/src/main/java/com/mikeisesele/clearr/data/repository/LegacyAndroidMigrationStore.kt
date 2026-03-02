package com.mikeisesele.clearr.data.repository

import com.mikeisesele.clearr.data.database.ClearrDatabase
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetCategoryPlan
import com.mikeisesele.clearr.data.model.BudgetEntry
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetPeriod
import com.mikeisesele.clearr.data.model.Goal
import com.mikeisesele.clearr.data.model.GoalCompletion
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.toDomain
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class LegacyAndroidMigrationStore @Inject constructor(
    database: ClearrDatabase
) {
    private val appConfigDao = database.appConfigDao()
    private val trackerDao = database.trackerDao()
    private val budgetDao = database.budgetDao()
    private val todoDao = database.todoDao()
    private val goalsDao = database.goalsDao()

    suspend fun getAppConfig(): AppConfig? = appConfigDao.getConfig()?.toDomain()

    fun getAllTrackers(): Flow<List<Tracker>> =
        trackerDao.getAllTrackers().map { trackers -> trackers.map { tracker -> tracker.toDomain() } }

    fun getBudgetPeriods(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetPeriod>> =
        budgetDao.getPeriods(trackerId, frequency).map { periods -> periods.map { period -> period.toDomain() } }

    fun getBudgetCategories(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetCategory>> =
        budgetDao.getCategories(trackerId, frequency).map { categories -> categories.map { category -> category.toDomain() } }

    fun getBudgetCategoryPlansForTracker(trackerId: Long): Flow<List<BudgetCategoryPlan>> =
        budgetDao.getCategoryPlansForTracker(trackerId).map { plans -> plans.map { plan -> plan.toDomain() } }

    fun getBudgetEntriesForTracker(trackerId: Long): Flow<List<BudgetEntry>> =
        budgetDao.getEntriesForTracker(trackerId).map { entries -> entries.map { entry -> entry.toDomain() } }

    fun getTodosForTracker(trackerId: Long): Flow<List<TodoItem>> =
        todoDao.getTodos(trackerId).map { todos -> todos.map { todo -> todo.toDomain() } }

    fun getGoalsForTracker(trackerId: Long): Flow<List<Goal>> =
        goalsDao.getGoals(trackerId).map { goals -> goals.map { goal -> goal.toDomain() } }

    fun getGoalCompletionsForTracker(trackerId: Long): Flow<List<GoalCompletion>> =
        goalsDao.getAllCompletions(trackerId).map { completions ->
            completions.map { completion -> completion.toDomain() }
        }
}
