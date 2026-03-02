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
import com.mikeisesele.clearr.data.model.BudgetPeriod
import com.mikeisesele.clearr.data.model.Goal
import com.mikeisesele.clearr.data.model.GoalCompletion
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.domain.repository.AppConfigRepository
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Singleton
class AndroidSharedCoreRepository @Inject constructor(
    private val legacyMigrationStore: LegacyAndroidMigrationStore,
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

    init {
        runBlocking { migrateLegacyDataIfNeeded() }
    }

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

    private suspend fun migrateLegacyDataIfNeeded() {
        val sharedConfig = sharedRepository.getAppConfig()
        val legacyConfig = legacyMigrationStore.getAppConfig()
        if (sharedConfig == null && legacyConfig != null) {
            sharedRepository.upsertAppConfig(legacyConfig)
        }

        val sharedTrackers = sharedRepository.getAllTrackers().first()
        val legacyTrackers = legacyMigrationStore.getAllTrackers().first()
        if (sharedTrackers.isEmpty() && legacyTrackers.isNotEmpty()) {
            legacyTrackers.forEach { tracker -> sharedRepository.insertTracker(tracker) }
        }

        migrateLegacyTodosIfNeeded(legacyTrackers)
        migrateLegacyGoalsIfNeeded(legacyTrackers)
        migrateLegacyBudgetIfNeeded(legacyTrackers)
    }

    private suspend fun migrateLegacyTodosIfNeeded(legacyTrackers: List<Tracker>) {
        legacyTrackers.map(Tracker::id).filter { id -> id > 0L }.forEach { trackerId ->
            val sharedTodos = sharedTodoRepository.getTodosForTracker(trackerId).first()
            if (sharedTodos.isEmpty()) {
                val legacyTodos = legacyMigrationStore.getTodosForTracker(trackerId).first()
                sharedTodoRepository.seedTodos(legacyTodos)
            }
        }
    }

    private suspend fun migrateLegacyGoalsIfNeeded(legacyTrackers: List<Tracker>) {
        legacyTrackers.map(Tracker::id).filter { id -> id > 0L }.forEach { trackerId ->
            val sharedGoals = sharedGoalsRepository.getGoalsForTracker(trackerId).first()
            val sharedCompletions = sharedGoalsRepository.getGoalCompletionsForTracker(trackerId).first()
            if (sharedGoals.isEmpty() && sharedCompletions.isEmpty()) {
                val legacyGoals = legacyMigrationStore.getGoalsForTracker(trackerId).first()
                val legacyCompletions = legacyMigrationStore.getGoalCompletionsForTracker(trackerId).first()
                sharedGoalsRepository.seedGoals(legacyGoals, legacyCompletions)
            }
        }
    }

    private suspend fun migrateLegacyBudgetIfNeeded(legacyTrackers: List<Tracker>) {
        legacyTrackers.map(Tracker::id).filter { id -> id > 0L }.forEach { trackerId ->
            BudgetFrequency.entries.forEach { frequency ->
                val sharedPeriods = sharedBudgetRepository.getBudgetPeriods(trackerId, frequency).first()
                val sharedCategories = sharedBudgetRepository.getBudgetCategories(trackerId, frequency).first()
                if (
                    sharedPeriods.isEmpty() &&
                    sharedCategories.isEmpty()
                ) {
                    val legacyPeriods = legacyMigrationStore.getBudgetPeriods(trackerId, frequency).first()
                    val legacyCategories = legacyMigrationStore.getBudgetCategories(trackerId, frequency).first()
                    migrateLegacyBudgetFrequency(
                        trackerId = trackerId,
                        frequency = frequency,
                        periods = legacyPeriods,
                        categories = legacyCategories
                    )
                }
            }
        }
    }

    private suspend fun migrateLegacyBudgetFrequency(
        trackerId: Long,
        frequency: BudgetFrequency,
        periods: List<BudgetPeriod>,
        categories: List<BudgetCategory>
    ) {
        val periodIds = periods.map(BudgetPeriod::id).toSet()
        val categoryIds = categories.map(BudgetCategory::id).toSet()
        val plans = legacyMigrationStore.getBudgetCategoryPlansForTracker(trackerId).first()
            .filter { plan -> plan.periodId in periodIds && plan.categoryId in categoryIds }
        val entries = legacyMigrationStore.getBudgetEntriesForTracker(trackerId).first()
            .filter { entry -> entry.periodId in periodIds && entry.categoryId in categoryIds }

        sharedBudgetRepository.seedBudgetData(
            periods = periods.filter { period -> period.frequency == frequency },
            categories = categories.filter { category -> category.frequency == frequency },
            plans = plans,
            entries = entries
        )
    }
}
