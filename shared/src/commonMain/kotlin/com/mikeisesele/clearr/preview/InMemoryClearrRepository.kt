package com.mikeisesele.clearr.preview

import com.mikeisesele.clearr.core.time.formatFullMonthYear
import com.mikeisesele.clearr.core.time.localDateAtEndOfDayEpochMillis
import com.mikeisesele.clearr.core.time.localDateAtStartOfDayEpochMillis
import com.mikeisesele.clearr.core.time.nowEpochMillis
import com.mikeisesele.clearr.core.time.plusDays
import com.mikeisesele.clearr.core.time.randomId
import com.mikeisesele.clearr.core.time.todayLocalDate
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetCategoryPlan
import com.mikeisesele.clearr.data.model.BudgetEntry
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetPeriod
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.Goal
import com.mikeisesele.clearr.data.model.GoalCompletion
import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import com.mikeisesele.clearr.data.model.TodoStatus
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryClearrRepository private constructor(
    trackers: List<Tracker>,
    budgetPeriods: List<BudgetPeriod>,
    budgetCategories: List<BudgetCategory>,
    budgetPlans: List<BudgetCategoryPlan>,
    budgetEntries: List<BudgetEntry>,
    goals: List<Goal>,
    goalCompletions: List<GoalCompletion>,
    todos: List<TodoItem>,
    appConfig: AppConfig? = null
) : ClearrRepository {
    private val appConfigFlow = MutableStateFlow(appConfig)
    private val trackersFlow = MutableStateFlow(trackers)
    private val budgetPeriodsFlow = MutableStateFlow(budgetPeriods)
    private val budgetCategoriesFlow = MutableStateFlow(budgetCategories)
    private val budgetPlansFlow = MutableStateFlow(budgetPlans)
    private val budgetEntriesFlow = MutableStateFlow(budgetEntries)
    private val goalsFlow = MutableStateFlow(goals)
    private val goalCompletionsFlow = MutableStateFlow(goalCompletions)
    private val todosFlow = MutableStateFlow(todos)

    private var nextTrackerId = (trackers.maxOfOrNull { it.id } ?: 0L) + 1
    private var nextBudgetPeriodId = (budgetPeriods.maxOfOrNull { it.id } ?: 0L) + 1
    private var nextBudgetCategoryId = (budgetCategories.maxOfOrNull { it.id } ?: 0L) + 1
    private var nextBudgetCategoryPlanId = (budgetPlans.maxOfOrNull { it.id } ?: 0L) + 1
    private var nextBudgetEntryId = (budgetEntries.maxOfOrNull { it.id } ?: 0L) + 1

    override fun getAppConfigFlow(): Flow<AppConfig?> = appConfigFlow

    override suspend fun getAppConfig(): AppConfig? = appConfigFlow.value

    override suspend fun upsertAppConfig(config: AppConfig) {
        appConfigFlow.value = config
    }

    override fun getAllTrackers(): Flow<List<Tracker>> = trackersFlow

    override suspend fun getTrackerById(id: Long): Tracker? = trackersFlow.value.firstOrNull { it.id == id }

    override fun getTrackerByIdFlow(id: Long): Flow<Tracker?> =
        trackersFlow.map { trackers -> trackers.firstOrNull { it.id == id } }

    override suspend fun insertTracker(tracker: Tracker): Long {
        val id = nextTrackerId++
        trackersFlow.value = trackersFlow.value + tracker.copy(id = id)
        return id
    }

    override suspend fun updateTracker(tracker: Tracker) {
        trackersFlow.value = trackersFlow.value.map { existing ->
            if (existing.id == tracker.id) tracker else existing
        }
    }

    override suspend fun deleteTracker(id: Long) {
        trackersFlow.value = trackersFlow.value.filterNot { it.id == id }
        budgetPeriodsFlow.value = budgetPeriodsFlow.value.filterNot { it.trackerId == id }
        budgetCategoriesFlow.value = budgetCategoriesFlow.value.filterNot { it.trackerId == id }
        budgetPlansFlow.value = budgetPlansFlow.value.filterNot { it.trackerId == id }
        budgetEntriesFlow.value = budgetEntriesFlow.value.filterNot { it.trackerId == id }
        goalsFlow.value = goalsFlow.value.filterNot { it.trackerId == id }
        val remainingGoalIds = goalsFlow.value.mapTo(mutableSetOf()) { it.id }
        goalCompletionsFlow.value = goalCompletionsFlow.value.filter { it.goalId in remainingGoalIds }
        todosFlow.value = todosFlow.value.filterNot { it.trackerId == id }
    }

    override suspend fun clearTrackerNewFlag(id: Long) {
        trackersFlow.value = trackersFlow.value.map { tracker ->
            if (tracker.id == id) tracker.copy(isNew = false) else tracker
        }
    }

    override fun getBudgetPeriods(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetPeriod>> =
        budgetPeriodsFlow.map { periods ->
            periods.filter { it.trackerId == trackerId && it.frequency == frequency }.sortedBy { it.startDate }
        }

    override suspend fun ensureBudgetPeriods(trackerId: Long, frequency: BudgetFrequency) {
        val exists = budgetPeriodsFlow.value.any { it.trackerId == trackerId && it.frequency == frequency }
        if (exists) return
        val today = todayLocalDate()
        val start = localDateAtStartOfDayEpochMillis(today)
        val end = localDateAtEndOfDayEpochMillis(
            when (frequency) {
                BudgetFrequency.MONTHLY -> today.plusDays(30)
                BudgetFrequency.WEEKLY -> today.plusDays(6)
            }
        )
        val label = when (frequency) {
            BudgetFrequency.MONTHLY -> formatFullMonthYear(today)
            BudgetFrequency.WEEKLY -> "Current Week"
        }
        budgetPeriodsFlow.value = budgetPeriodsFlow.value + BudgetPeriod(
            id = nextBudgetPeriodId++,
            trackerId = trackerId,
            frequency = frequency,
            label = label,
            startDate = start,
            endDate = end
        )
    }

    override fun getBudgetCategories(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetCategory>> =
        budgetCategoriesFlow.map { categories ->
            categories.filter { it.trackerId == trackerId && it.frequency == frequency }.sortedBy { it.sortOrder }
        }

    override suspend fun getBudgetMaxSortOrder(trackerId: Long, frequency: BudgetFrequency): Int =
        budgetCategoriesFlow.value
            .filter { it.trackerId == trackerId && it.frequency == frequency }
            .maxOfOrNull { it.sortOrder } ?: -1

    override suspend fun addBudgetCategory(category: BudgetCategory): Long {
        val id = nextBudgetCategoryId++
        budgetCategoriesFlow.value = budgetCategoriesFlow.value + category.copy(id = id)
        return id
    }

    override suspend fun updateBudgetCategory(category: BudgetCategory) {
        budgetCategoriesFlow.value = budgetCategoriesFlow.value.map { existing ->
            if (existing.id == category.id) category else existing
        }
    }

    override suspend fun deleteBudgetCategory(categoryId: Long) {
        budgetCategoriesFlow.value = budgetCategoriesFlow.value.filterNot { it.id == categoryId }
        budgetPlansFlow.value = budgetPlansFlow.value.filterNot { it.categoryId == categoryId }
        budgetEntriesFlow.value = budgetEntriesFlow.value.filterNot { it.categoryId == categoryId }
    }

    override suspend fun reorderBudgetCategories(trackerId: Long, frequency: BudgetFrequency, orderedIds: List<Long>) {
        val orderMap = orderedIds.withIndex().associate { it.value to it.index }
        budgetCategoriesFlow.value = budgetCategoriesFlow.value.map { category ->
            if (category.trackerId == trackerId && category.frequency == frequency && category.id in orderMap) {
                category.copy(sortOrder = orderMap.getValue(category.id))
            } else {
                category
            }
        }
    }

    override fun getBudgetCategoryPlansForTracker(trackerId: Long): Flow<List<BudgetCategoryPlan>> =
        budgetPlansFlow.map { plans -> plans.filter { it.trackerId == trackerId } }

    override suspend fun getBudgetCategoryPlansForPeriod(periodId: Long): List<BudgetCategoryPlan> =
        budgetPlansFlow.value.filter { it.periodId == periodId }

    override suspend fun saveBudgetCategoryPlans(periodId: Long, plans: List<BudgetCategoryPlan>) {
        budgetPlansFlow.value = budgetPlansFlow.value.filterNot { it.periodId == periodId } +
            plans.map { plan ->
                if (plan.id == 0L) plan.copy(id = nextBudgetCategoryPlanId++) else plan
            }
    }

    override fun getBudgetEntriesForTracker(trackerId: Long): Flow<List<BudgetEntry>> =
        budgetEntriesFlow.map { entries -> entries.filter { it.trackerId == trackerId } }

    override suspend fun addBudgetEntry(entry: BudgetEntry): Long {
        val id = nextBudgetEntryId++
        budgetEntriesFlow.value = budgetEntriesFlow.value + entry.copy(id = id)
        return id
    }

    override fun getGoalsForTracker(trackerId: Long): Flow<List<Goal>> =
        goalsFlow.map { goals -> goals.filter { it.trackerId == trackerId } }

    override fun getGoalCompletionsForTracker(trackerId: Long): Flow<List<GoalCompletion>> {
        val goalIds = goalsFlow.value.filter { it.trackerId == trackerId }.mapTo(mutableSetOf()) { it.id }
        return goalCompletionsFlow.map { completions -> completions.filter { it.goalId in goalIds } }
    }

    override suspend fun insertGoal(goal: Goal) {
        goalsFlow.value = goalsFlow.value + goal
    }

    override suspend fun addGoalCompletion(completion: GoalCompletion) {
        goalCompletionsFlow.value = goalCompletionsFlow.value + completion
    }

    override suspend fun deleteGoal(goalId: String) {
        goalsFlow.value = goalsFlow.value.filterNot { it.id == goalId }
        goalCompletionsFlow.value = goalCompletionsFlow.value.filterNot { it.goalId == goalId }
    }

    override fun getTodosForTracker(trackerId: Long): Flow<List<TodoItem>> =
        todosFlow.map { todos -> todos.filter { it.trackerId == trackerId } }

    override suspend fun getTodoById(id: String): TodoItem? = todosFlow.value.firstOrNull { it.id == id }

    override suspend fun insertTodo(todo: TodoItem) {
        todosFlow.value = todosFlow.value + todo
    }

    override suspend fun updateTodo(todo: TodoItem) {
        todosFlow.value = todosFlow.value.map { existing ->
            if (existing.id == todo.id) todo else existing
        }
    }

    override suspend fun markTodoDone(id: String, completedAt: Long) {
        todosFlow.value = todosFlow.value.map { todo ->
            if (todo.id == id) todo.copy(status = TodoStatus.DONE, completedAt = completedAt) else todo
        }
    }

    override suspend fun deleteTodo(id: String) {
        todosFlow.value = todosFlow.value.filterNot { it.id == id }
    }

    companion object {
        fun empty(setupComplete: Boolean = false): InMemoryClearrRepository = InMemoryClearrRepository(
            trackers = emptyList(),
            budgetPeriods = emptyList(),
            budgetCategories = emptyList(),
            budgetPlans = emptyList(),
            budgetEntries = emptyList(),
            goals = emptyList(),
            goalCompletions = emptyList(),
            todos = emptyList(),
            appConfig = AppConfig(setupComplete = setupComplete)
        )

        fun sample(): InMemoryClearrRepository {
            val now = nowEpochMillis()
            val today = todayLocalDate()
            val budgetTracker = Tracker(
                id = 1001L,
                name = "Monthly Budget",
                type = TrackerType.BUDGET,
                frequency = Frequency.MONTHLY,
                layoutStyle = LayoutStyle.GRID,
                createdAt = now
            )
            val goalsTracker = Tracker(
                id = 1002L,
                name = "Goals",
                type = TrackerType.GOALS,
                frequency = Frequency.MONTHLY,
                layoutStyle = LayoutStyle.GRID,
                createdAt = now
            )
            val todoTracker = Tracker(
                id = 1003L,
                name = "Todos",
                type = TrackerType.TODO,
                frequency = Frequency.MONTHLY,
                layoutStyle = LayoutStyle.GRID,
                createdAt = now
            )
            val period = BudgetPeriod(
                id = 5001L,
                trackerId = budgetTracker.id,
                frequency = BudgetFrequency.MONTHLY,
                label = "March 2026",
                startDate = localDateAtStartOfDayEpochMillis(today),
                endDate = localDateAtEndOfDayEpochMillis(today.plusDays(30))
            )
            val categories = listOf(
                BudgetCategory(6001L, budgetTracker.id, BudgetFrequency.MONTHLY, "Housing", "🏠", "Violet", 4_500_000, 0, now),
                BudgetCategory(6002L, budgetTracker.id, BudgetFrequency.MONTHLY, "Food", "🍔", "Orange", 1_800_000, 1, now),
                BudgetCategory(6003L, budgetTracker.id, BudgetFrequency.MONTHLY, "Transport", "🚗", "Blue", 900_000, 2, now)
            )
            val entries = listOf(
                BudgetEntry(7001L, budgetTracker.id, 6001L, period.id, 3_800_000, "Rent", now),
                BudgetEntry(7002L, budgetTracker.id, 6002L, period.id, 1_125_000, "Groceries", now),
                BudgetEntry(7003L, budgetTracker.id, 6003L, period.id, 960_000, "Fuel", now)
            )
            val goals = listOf(
                Goal(randomId(), goalsTracker.id, "Exercise", "💪", "Emerald", "30 mins", GoalFrequency.DAILY, now),
                Goal(randomId(), goalsTracker.id, "Read", "📚", "Blue", "20 pages", GoalFrequency.DAILY, now),
                Goal(randomId(), goalsTracker.id, "Save", "💰", "Amber", "₦10,000", GoalFrequency.WEEKLY, now)
            )
            val completions = listOf(
                GoalCompletion(randomId(), goals[0].id, today.toString(), now),
                GoalCompletion(randomId(), goals[2].id, "${today.year}-W10", now)
            )
            val todos = listOf(
                TodoItem(randomId(), todoTracker.id, "Pay rent", "Before evening", TodoPriority.HIGH, today, TodoStatus.PENDING, now),
                TodoItem(randomId(), todoTracker.id, "Review grocery list", null, TodoPriority.MEDIUM, today.plusDays(1), TodoStatus.PENDING, now),
                TodoItem(randomId(), todoTracker.id, "Submit report", "Email team copy", TodoPriority.HIGH, today.plusDays(-1), TodoStatus.OVERDUE, now),
                TodoItem(randomId(), todoTracker.id, "Book haircut", null, TodoPriority.LOW, null, TodoStatus.DONE, now, now)
            )
            return InMemoryClearrRepository(
                trackers = listOf(budgetTracker, goalsTracker, todoTracker),
                budgetPeriods = listOf(period),
                budgetCategories = categories,
                budgetPlans = categories.mapIndexed { index, category ->
                    BudgetCategoryPlan(
                        id = 8000L + index,
                        trackerId = budgetTracker.id,
                        categoryId = category.id,
                        periodId = period.id,
                        plannedAmountKobo = category.plannedAmountKobo,
                        createdAt = now
                    )
                },
                budgetEntries = entries,
                goals = goals,
                goalCompletions = completions,
                todos = todos,
                appConfig = AppConfig(setupComplete = true)
            )
        }
    }
}
