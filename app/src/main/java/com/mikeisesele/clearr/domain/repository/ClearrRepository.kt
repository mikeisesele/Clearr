package com.mikeisesele.clearr.domain.repository

import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetCategoryPlan
import com.mikeisesele.clearr.data.model.BudgetEntry
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetPeriod
import com.mikeisesele.clearr.data.model.Goal
import com.mikeisesele.clearr.data.model.GoalCompletion
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TodoItem
import kotlinx.coroutines.flow.Flow

interface ClearrRepository {
    fun getAppConfigFlow(): Flow<AppConfig?>
    suspend fun getAppConfig(): AppConfig?
    suspend fun upsertAppConfig(config: AppConfig)

    fun getAllTrackers(): Flow<List<Tracker>>
    suspend fun getTrackerById(id: Long): Tracker?
    fun getTrackerByIdFlow(id: Long): Flow<Tracker?>
    suspend fun insertTracker(tracker: Tracker): Long
    suspend fun updateTracker(tracker: Tracker)
    suspend fun deleteTracker(id: Long)
    suspend fun clearTrackerNewFlag(id: Long)

    fun getBudgetPeriods(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetPeriod>>
    suspend fun ensureBudgetPeriods(trackerId: Long, frequency: BudgetFrequency)
    fun getBudgetCategories(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetCategory>>
    suspend fun getBudgetMaxSortOrder(trackerId: Long, frequency: BudgetFrequency): Int
    suspend fun addBudgetCategory(category: BudgetCategory): Long
    suspend fun updateBudgetCategory(category: BudgetCategory)
    suspend fun deleteBudgetCategory(categoryId: Long)
    suspend fun reorderBudgetCategories(trackerId: Long, frequency: BudgetFrequency, orderedIds: List<Long>)
    fun getBudgetCategoryPlansForTracker(trackerId: Long): Flow<List<BudgetCategoryPlan>>
    suspend fun getBudgetCategoryPlansForPeriod(periodId: Long): List<BudgetCategoryPlan>
    suspend fun saveBudgetCategoryPlans(periodId: Long, plans: List<BudgetCategoryPlan>)
    fun getBudgetEntriesForTracker(trackerId: Long): Flow<List<BudgetEntry>>
    suspend fun addBudgetEntry(entry: BudgetEntry): Long

    fun getTodosForTracker(trackerId: Long): Flow<List<TodoItem>>
    suspend fun getTodoById(id: String): TodoItem?
    suspend fun insertTodo(todo: TodoItem)
    suspend fun updateTodo(todo: TodoItem)
    suspend fun markTodoDone(id: String, completedAt: Long)
    suspend fun deleteTodo(id: String)

    fun getGoalsForTracker(trackerId: Long): Flow<List<Goal>>
    fun getGoalCompletionsForTracker(trackerId: Long): Flow<List<GoalCompletion>>
    suspend fun insertGoal(goal: Goal)
    suspend fun addGoalCompletion(completion: GoalCompletion)
    suspend fun deleteGoal(goalId: String)
}
