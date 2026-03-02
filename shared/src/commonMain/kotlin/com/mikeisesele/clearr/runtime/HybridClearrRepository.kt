package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.data.local.room.RoomAppConfigTrackerRepository
import com.mikeisesele.clearr.data.local.room.RoomBudgetRepository
import com.mikeisesele.clearr.data.local.room.RoomGoalsRepository
import com.mikeisesele.clearr.data.local.room.RoomTodoRepository
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
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import kotlinx.coroutines.flow.Flow

class HybridClearrRepository(
    private val trackerRepository: RoomAppConfigTrackerRepository,
    private val budgetRepository: RoomBudgetRepository,
    private val goalsRepository: RoomGoalsRepository,
    private val todoRepository: RoomTodoRepository
) : ClearrRepository {
    override fun getAppConfigFlow(): Flow<AppConfig?> = trackerRepository.getAppConfigFlow()

    override suspend fun getAppConfig(): AppConfig? = trackerRepository.getAppConfig()

    override suspend fun upsertAppConfig(config: AppConfig) {
        trackerRepository.upsertAppConfig(config)
    }

    override fun getAllTrackers(): Flow<List<Tracker>> = trackerRepository.getAllTrackers()

    override suspend fun getTrackerById(id: Long): Tracker? = trackerRepository.getTrackerById(id)

    override fun getTrackerByIdFlow(id: Long): Flow<Tracker?> = trackerRepository.getTrackerByIdFlow(id)

    override suspend fun insertTracker(tracker: Tracker): Long = trackerRepository.insertTracker(tracker)

    override suspend fun updateTracker(tracker: Tracker) {
        trackerRepository.updateTracker(tracker)
    }

    override suspend fun deleteTracker(id: Long) {
        budgetRepository.deleteBudgetDataForTracker(id)
        goalsRepository.deleteGoalsForTracker(id)
        todoRepository.deleteTodosForTracker(id)
        trackerRepository.deleteTracker(id)
    }

    override suspend fun clearTrackerNewFlag(id: Long) {
        trackerRepository.clearTrackerNewFlag(id)
    }

    override fun getBudgetPeriods(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetPeriod>> =
        budgetRepository.getBudgetPeriods(trackerId, frequency)

    override suspend fun ensureBudgetPeriods(trackerId: Long, frequency: BudgetFrequency) {
        budgetRepository.ensureBudgetPeriods(trackerId, frequency)
    }

    override fun getBudgetCategories(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetCategory>> =
        budgetRepository.getBudgetCategories(trackerId, frequency)

    override suspend fun getBudgetMaxSortOrder(trackerId: Long, frequency: BudgetFrequency): Int =
        budgetRepository.getBudgetMaxSortOrder(trackerId, frequency)

    override suspend fun addBudgetCategory(category: BudgetCategory): Long =
        budgetRepository.addBudgetCategory(category)

    override suspend fun updateBudgetCategory(category: BudgetCategory) {
        budgetRepository.updateBudgetCategory(category)
    }

    override suspend fun deleteBudgetCategory(categoryId: Long) {
        budgetRepository.deleteBudgetCategory(categoryId)
    }

    override suspend fun reorderBudgetCategories(trackerId: Long, frequency: BudgetFrequency, orderedIds: List<Long>) {
        budgetRepository.reorderBudgetCategories(trackerId, frequency, orderedIds)
    }

    override fun getBudgetCategoryPlansForTracker(trackerId: Long): Flow<List<BudgetCategoryPlan>> =
        budgetRepository.getBudgetCategoryPlansForTracker(trackerId)

    override suspend fun getBudgetCategoryPlansForPeriod(periodId: Long): List<BudgetCategoryPlan> =
        budgetRepository.getBudgetCategoryPlansForPeriod(periodId)

    override suspend fun saveBudgetCategoryPlans(periodId: Long, plans: List<BudgetCategoryPlan>) {
        budgetRepository.saveBudgetCategoryPlans(periodId, plans)
    }

    override fun getBudgetEntriesForTracker(trackerId: Long): Flow<List<BudgetEntry>> =
        budgetRepository.getBudgetEntriesForTracker(trackerId)

    override suspend fun addBudgetEntry(entry: BudgetEntry): Long = budgetRepository.addBudgetEntry(entry)

    override fun getGoalsForTracker(trackerId: Long): Flow<List<Goal>> =
        goalsRepository.getGoalsForTracker(trackerId)

    override fun getGoalCompletionsForTracker(trackerId: Long): Flow<List<GoalCompletion>> =
        goalsRepository.getGoalCompletionsForTracker(trackerId)

    override suspend fun insertGoal(goal: Goal) {
        goalsRepository.insertGoal(goal)
    }

    override suspend fun addGoalCompletion(completion: GoalCompletion) {
        goalsRepository.addGoalCompletion(completion)
    }

    override suspend fun deleteGoal(goalId: String) {
        goalsRepository.deleteGoal(goalId)
    }

    override fun getTodosForTracker(trackerId: Long): Flow<List<TodoItem>> =
        todoRepository.getTodosForTracker(trackerId)

    override suspend fun getTodoById(id: String): TodoItem? = todoRepository.getTodoById(id)

    override suspend fun insertTodo(todo: TodoItem) {
        todoRepository.insertTodo(todo)
    }

    override suspend fun updateTodo(todo: TodoItem) {
        todoRepository.updateTodo(todo)
    }

    override suspend fun markTodoDone(id: String, completedAt: Long) {
        todoRepository.markTodoDone(id, completedAt)
    }

    override suspend fun deleteTodo(id: String) {
        todoRepository.deleteTodo(id)
    }
}
