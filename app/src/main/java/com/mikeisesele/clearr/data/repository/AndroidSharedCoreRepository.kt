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
    private val featureRepository: ClearrRepositoryImpl,
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
        runBlocking { synchronizeCoreDataIfNeeded() }
    }

    override fun getAppConfigFlow(): Flow<AppConfig?> = sharedRepository.getAppConfigFlow()

    override suspend fun getAppConfig(): AppConfig? = sharedRepository.getAppConfig()

    override suspend fun upsertAppConfig(config: AppConfig) {
        featureRepository.upsertAppConfig(config)
        sharedRepository.upsertAppConfig(config)
    }

    override fun getAllTrackers(): Flow<List<Tracker>> = sharedRepository.getAllTrackers()

    override suspend fun getTrackerById(id: Long): Tracker? = sharedRepository.getTrackerById(id)

    override fun getTrackerByIdFlow(id: Long): Flow<Tracker?> = sharedRepository.getTrackerByIdFlow(id)

    override suspend fun insertTracker(tracker: Tracker): Long {
        val id = featureRepository.insertTracker(tracker)
        sharedRepository.insertTracker(tracker.copy(id = id))
        return id
    }

    override suspend fun updateTracker(tracker: Tracker) {
        featureRepository.updateTracker(tracker)
        sharedRepository.updateTracker(tracker)
    }

    override suspend fun deleteTracker(id: Long) {
        featureRepository.deleteTracker(id)
        sharedBudgetRepository.deleteBudgetDataForTracker(id)
        sharedGoalsRepository.deleteGoalsForTracker(id)
        sharedTodoRepository.deleteTodosForTracker(id)
        sharedRepository.deleteTracker(id)
    }

    override suspend fun clearTrackerNewFlag(id: Long) {
        featureRepository.clearTrackerNewFlag(id)
        sharedRepository.clearTrackerNewFlag(id)
    }

    override fun getBudgetPeriods(trackerId: Long, frequency: BudgetFrequency) =
        sharedBudgetRepository.getBudgetPeriods(trackerId, frequency)

    override suspend fun ensureBudgetPeriods(trackerId: Long, frequency: BudgetFrequency) {
        featureRepository.ensureBudgetPeriods(trackerId, frequency)
        sharedBudgetRepository.ensureBudgetPeriods(trackerId, frequency)
    }

    override fun getBudgetCategories(trackerId: Long, frequency: BudgetFrequency) =
        sharedBudgetRepository.getBudgetCategories(trackerId, frequency)

    override suspend fun getBudgetMaxSortOrder(trackerId: Long, frequency: BudgetFrequency): Int =
        sharedBudgetRepository.getBudgetMaxSortOrder(trackerId, frequency)

    override suspend fun addBudgetCategory(category: BudgetCategory): Long {
        val id = featureRepository.addBudgetCategory(category)
        sharedBudgetRepository.addBudgetCategory(category.copy(id = id))
        return id
    }

    override suspend fun updateBudgetCategory(category: BudgetCategory) {
        featureRepository.updateBudgetCategory(category)
        sharedBudgetRepository.updateBudgetCategory(category)
    }

    override suspend fun deleteBudgetCategory(categoryId: Long) {
        featureRepository.deleteBudgetCategory(categoryId)
        sharedBudgetRepository.deleteBudgetCategory(categoryId)
    }

    override suspend fun reorderBudgetCategories(
        trackerId: Long,
        frequency: BudgetFrequency,
        orderedIds: List<Long>
    ) {
        featureRepository.reorderBudgetCategories(trackerId, frequency, orderedIds)
        sharedBudgetRepository.reorderBudgetCategories(trackerId, frequency, orderedIds)
    }

    override fun getBudgetCategoryPlansForTracker(trackerId: Long) =
        sharedBudgetRepository.getBudgetCategoryPlansForTracker(trackerId)

    override suspend fun getBudgetCategoryPlansForPeriod(periodId: Long) =
        sharedBudgetRepository.getBudgetCategoryPlansForPeriod(periodId)

    override suspend fun saveBudgetCategoryPlans(
        periodId: Long,
        plans: List<BudgetCategoryPlan>
    ) {
        featureRepository.saveBudgetCategoryPlans(periodId, plans)
        sharedBudgetRepository.saveBudgetCategoryPlans(periodId, plans)
    }

    override fun getBudgetEntriesForTracker(trackerId: Long) =
        sharedBudgetRepository.getBudgetEntriesForTracker(trackerId)

    override suspend fun addBudgetEntry(entry: BudgetEntry): Long {
        val id = featureRepository.addBudgetEntry(entry)
        sharedBudgetRepository.addBudgetEntry(entry.copy(id = id))
        return id
    }

    override fun getGoalsForTracker(trackerId: Long) = sharedGoalsRepository.getGoalsForTracker(trackerId)

    override fun getGoalCompletionsForTracker(trackerId: Long) =
        sharedGoalsRepository.getGoalCompletionsForTracker(trackerId)

    override suspend fun insertGoal(goal: com.mikeisesele.clearr.data.model.Goal) {
        featureRepository.insertGoal(goal)
        sharedGoalsRepository.insertGoal(goal)
    }

    override suspend fun addGoalCompletion(completion: com.mikeisesele.clearr.data.model.GoalCompletion) {
        featureRepository.addGoalCompletion(completion)
        sharedGoalsRepository.addGoalCompletion(completion)
    }

    override suspend fun deleteGoal(goalId: String) {
        featureRepository.deleteGoal(goalId)
        sharedGoalsRepository.deleteGoal(goalId)
    }

    override fun getTodosForTracker(trackerId: Long) = sharedTodoRepository.getTodosForTracker(trackerId)

    override suspend fun getTodoById(id: String) = sharedTodoRepository.getTodoById(id)

    override suspend fun insertTodo(todo: com.mikeisesele.clearr.data.model.TodoItem) {
        featureRepository.insertTodo(todo)
        sharedTodoRepository.insertTodo(todo)
    }

    override suspend fun updateTodo(todo: com.mikeisesele.clearr.data.model.TodoItem) {
        featureRepository.updateTodo(todo)
        sharedTodoRepository.updateTodo(todo)
    }

    override suspend fun markTodoDone(id: String, completedAt: Long) {
        featureRepository.markTodoDone(id, completedAt)
        sharedTodoRepository.markTodoDone(id, completedAt)
    }

    override suspend fun deleteTodo(id: String) {
        featureRepository.deleteTodo(id)
        sharedTodoRepository.deleteTodo(id)
    }

    private suspend fun synchronizeCoreDataIfNeeded() {
        val sharedConfig = sharedRepository.getAppConfig()
        val featureConfig = featureRepository.getAppConfig()
        when {
            sharedConfig == null && featureConfig != null -> sharedRepository.upsertAppConfig(featureConfig)
            sharedConfig != null && featureConfig == null -> featureRepository.upsertAppConfig(sharedConfig)
        }

        val sharedTrackers = sharedRepository.getAllTrackers().first()
        val featureTrackers = featureRepository.getAllTrackers().first()
        when {
            sharedTrackers.isEmpty() && featureTrackers.isNotEmpty() -> {
                featureTrackers.forEach { tracker -> sharedRepository.insertTracker(tracker) }
            }
            featureTrackers.isEmpty() && sharedTrackers.isNotEmpty() -> {
                sharedTrackers.forEach { tracker -> featureRepository.insertTracker(tracker) }
            }
        }

        synchronizeTodosIfNeeded(featureTrackers, sharedTrackers)
        synchronizeGoalsIfNeeded(featureTrackers, sharedTrackers)
        synchronizeBudgetIfNeeded(featureTrackers, sharedTrackers)
    }

    private suspend fun synchronizeTodosIfNeeded(
        featureTrackers: List<Tracker>,
        sharedTrackers: List<Tracker>
    ) {
        val featureTrackerIds = featureTrackers.map(Tracker::id).toSet()
        val sharedTrackerIds = sharedTrackers.map(Tracker::id).toSet()
        val trackerIds = (featureTrackerIds + sharedTrackerIds).filter { id -> id > 0L }

        trackerIds.forEach { trackerId ->
            val sharedTodos = sharedTodoRepository.getTodosForTracker(trackerId).first()
            val featureTodos = featureRepository.getTodosForTracker(trackerId).first()
            when {
                sharedTodos.isEmpty() && featureTodos.isNotEmpty() -> {
                    sharedTodoRepository.seedTodos(featureTodos)
                }
                featureTodos.isEmpty() && sharedTodos.isNotEmpty() -> {
                    sharedTodos.forEach { todo -> featureRepository.insertTodo(todo) }
                }
            }
        }
    }

    private suspend fun synchronizeGoalsIfNeeded(
        featureTrackers: List<Tracker>,
        sharedTrackers: List<Tracker>
    ) {
        val featureTrackerIds = featureTrackers.map(Tracker::id).toSet()
        val sharedTrackerIds = sharedTrackers.map(Tracker::id).toSet()
        val trackerIds = (featureTrackerIds + sharedTrackerIds).filter { id -> id > 0L }

        trackerIds.forEach { trackerId ->
            val sharedGoals = sharedGoalsRepository.getGoalsForTracker(trackerId).first()
            val sharedCompletions = sharedGoalsRepository.getGoalCompletionsForTracker(trackerId).first()
            val featureGoals = featureRepository.getGoalsForTracker(trackerId).first()
            val featureCompletions = featureRepository.getGoalCompletionsForTracker(trackerId).first()
            when {
                sharedGoals.isEmpty() && sharedCompletions.isEmpty() &&
                    (featureGoals.isNotEmpty() || featureCompletions.isNotEmpty()) -> {
                    sharedGoalsRepository.seedGoals(featureGoals, featureCompletions)
                }
                featureGoals.isEmpty() && featureCompletions.isEmpty() &&
                    (sharedGoals.isNotEmpty() || sharedCompletions.isNotEmpty()) -> {
                    seedFeatureGoals(sharedGoals, sharedCompletions)
                }
            }
        }
    }

    private suspend fun seedFeatureGoals(
        goals: List<Goal>,
        completions: List<GoalCompletion>
    ) {
        goals.forEach { goal -> featureRepository.insertGoal(goal) }
        completions.forEach { completion -> featureRepository.addGoalCompletion(completion) }
    }

    private suspend fun synchronizeBudgetIfNeeded(
        featureTrackers: List<Tracker>,
        sharedTrackers: List<Tracker>
    ) {
        val featureTrackerIds = featureTrackers.map(Tracker::id).toSet()
        val sharedTrackerIds = sharedTrackers.map(Tracker::id).toSet()
        val trackerIds = (featureTrackerIds + sharedTrackerIds).filter { id -> id > 0L }

        trackerIds.forEach { trackerId ->
            BudgetFrequency.entries.forEach { frequency ->
                val sharedPeriods = sharedBudgetRepository.getBudgetPeriods(trackerId, frequency).first()
                val sharedCategories = sharedBudgetRepository.getBudgetCategories(trackerId, frequency).first()
                val featurePeriods = featureRepository.getBudgetPeriods(trackerId, frequency).first()
                val featureCategories = featureRepository.getBudgetCategories(trackerId, frequency).first()
                if (
                    sharedPeriods.isEmpty() &&
                    sharedCategories.isEmpty() &&
                    (featurePeriods.isNotEmpty() || featureCategories.isNotEmpty())
                ) {
                    seedSharedBudgetForFrequency(
                        trackerId = trackerId,
                        frequency = frequency,
                        periods = featurePeriods,
                        categories = featureCategories
                    )
                }
            }
        }
    }

    private suspend fun seedSharedBudgetForFrequency(
        trackerId: Long,
        frequency: BudgetFrequency,
        periods: List<BudgetPeriod>,
        categories: List<BudgetCategory>
    ) {
        val periodIds = periods.map(BudgetPeriod::id).toSet()
        val categoryIds = categories.map(BudgetCategory::id).toSet()
        val plans = featureRepository.getBudgetCategoryPlansForTracker(trackerId).first()
            .filter { plan -> plan.periodId in periodIds && plan.categoryId in categoryIds }
        val entries = featureRepository.getBudgetEntriesForTracker(trackerId).first()
            .filter { entry -> entry.periodId in periodIds && entry.categoryId in categoryIds }

        sharedBudgetRepository.seedBudgetData(
            periods = periods.filter { period -> period.frequency == frequency },
            categories = categories.filter { category -> category.frequency == frequency },
            plans = plans,
            entries = entries
        )
    }
}
