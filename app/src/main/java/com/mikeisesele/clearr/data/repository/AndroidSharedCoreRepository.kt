package com.mikeisesele.clearr.data.repository

import com.mikeisesele.clearr.data.local.room.ClearrSharedDatabase
import com.mikeisesele.clearr.data.local.room.RoomAppConfigTrackerRepository
import com.mikeisesele.clearr.data.local.room.RoomBudgetRepository
import com.mikeisesele.clearr.data.local.room.RoomGoalsRepository
import com.mikeisesele.clearr.data.local.room.RoomTodoRepository
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetCategoryPlan
import com.mikeisesele.clearr.data.model.BudgetEntry
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.Goal
import com.mikeisesele.clearr.data.model.GoalCompletion
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.domain.repository.AppConfigRepository
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class AndroidSharedCoreRepository @Inject constructor(
    sharedDatabase: ClearrSharedDatabase
) : ClearrRepository, AppConfigRepository {

    private val sharedRepository = RoomAppConfigTrackerRepository(
        appConfigDao = sharedDatabase.appConfigDao(),
        trackerDao = sharedDatabase.trackerDao()
    )
    private val sharedTodoRepository = RoomTodoRepository(
        todoDao = sharedDatabase.todoDao()
    )
    private val sharedGoalsRepository = RoomGoalsRepository(
        goalDao = sharedDatabase.goalDao()
    )
    private val sharedBudgetRepository = RoomBudgetRepository(
        budgetDao = sharedDatabase.budgetDao()
    )

    override fun getAppConfigFlow(): Flow<AppConfig?> = sharedRepository.getAppConfigFlow()

    override suspend fun getAppConfig(): AppConfig? = sharedRepository.getAppConfig()

    override suspend fun upsertAppConfig(config: AppConfig) = sharedRepository.upsertAppConfig(config)

    override fun getAllTrackers(): Flow<List<Tracker>> = sharedRepository.getAllTrackers()

    override suspend fun getTrackerById(id: Long): Tracker? = sharedRepository.getTrackerById(id)

    override fun getTrackerByIdFlow(id: Long): Flow<Tracker?> = sharedRepository.getTrackerByIdFlow(id)

    override suspend fun insertTracker(tracker: Tracker): Long = sharedRepository.insertTracker(tracker)

    override suspend fun updateTracker(tracker: Tracker) = sharedRepository.updateTracker(tracker)

    override suspend fun deleteTracker(id: Long) {
        sharedBudgetRepository.deleteBudgetDataForTracker(id)
        sharedGoalsRepository.deleteGoalsForTracker(id)
        sharedTodoRepository.deleteTodosForTracker(id)
        sharedRepository.deleteTracker(id)
    }

    override suspend fun clearTrackerNewFlag(id: Long) = sharedRepository.clearTrackerNewFlag(id)

    override fun getBudgetPeriods(trackerId: Long, frequency: BudgetFrequency) =
        sharedBudgetRepository.getBudgetPeriods(trackerId, frequency)

    override suspend fun ensureBudgetPeriods(trackerId: Long, frequency: BudgetFrequency) =
        sharedBudgetRepository.ensureBudgetPeriods(trackerId, frequency)

    override fun getBudgetCategories(trackerId: Long, frequency: BudgetFrequency) =
        sharedBudgetRepository.getBudgetCategories(trackerId, frequency)

    override suspend fun getBudgetMaxSortOrder(trackerId: Long, frequency: BudgetFrequency): Int =
        sharedBudgetRepository.getBudgetMaxSortOrder(trackerId, frequency)

    override suspend fun addBudgetCategory(category: BudgetCategory): Long {
        return sharedBudgetRepository.addBudgetCategory(category)
    }

    override suspend fun updateBudgetCategory(category: BudgetCategory) = sharedBudgetRepository.updateBudgetCategory(category)

    override suspend fun deleteBudgetCategory(categoryId: Long) = sharedBudgetRepository.deleteBudgetCategory(categoryId)

    override suspend fun reorderBudgetCategories(
        trackerId: Long,
        frequency: BudgetFrequency,
        orderedIds: List<Long>
    ) = sharedBudgetRepository.reorderBudgetCategories(trackerId, frequency, orderedIds)

    override fun getBudgetCategoryPlansForTracker(trackerId: Long) =
        sharedBudgetRepository.getBudgetCategoryPlansForTracker(trackerId)

    override suspend fun getBudgetCategoryPlansForPeriod(periodId: Long) =
        sharedBudgetRepository.getBudgetCategoryPlansForPeriod(periodId)

    override suspend fun saveBudgetCategoryPlans(
        periodId: Long,
        plans: List<BudgetCategoryPlan>
    ) = sharedBudgetRepository.saveBudgetCategoryPlans(periodId, plans)

    override fun getBudgetEntriesForTracker(trackerId: Long) =
        sharedBudgetRepository.getBudgetEntriesForTracker(trackerId)

    override suspend fun addBudgetEntry(entry: BudgetEntry): Long = sharedBudgetRepository.addBudgetEntry(entry)

    override fun getGoalsForTracker(trackerId: Long) = sharedGoalsRepository.getGoalsForTracker(trackerId)

    override fun getGoalCompletionsForTracker(trackerId: Long) =
        sharedGoalsRepository.getGoalCompletionsForTracker(trackerId)

    override suspend fun insertGoal(goal: Goal) = sharedGoalsRepository.insertGoal(goal)

    override suspend fun addGoalCompletion(completion: GoalCompletion) =
        sharedGoalsRepository.addGoalCompletion(completion)

    override suspend fun deleteGoal(goalId: String) = sharedGoalsRepository.deleteGoal(goalId)

    override fun getTodosForTracker(trackerId: Long) = sharedTodoRepository.getTodosForTracker(trackerId)

    override suspend fun getTodoById(id: String) = sharedTodoRepository.getTodoById(id)

    override suspend fun insertTodo(todo: com.mikeisesele.clearr.data.model.TodoItem) =
        sharedTodoRepository.insertTodo(todo)

    override suspend fun updateTodo(todo: com.mikeisesele.clearr.data.model.TodoItem) =
        sharedTodoRepository.updateTodo(todo)

    override suspend fun markTodoDone(id: String, completedAt: Long) =
        sharedTodoRepository.markTodoDone(id, completedAt)

    override suspend fun deleteTodo(id: String) = sharedTodoRepository.deleteTodo(id)
}
