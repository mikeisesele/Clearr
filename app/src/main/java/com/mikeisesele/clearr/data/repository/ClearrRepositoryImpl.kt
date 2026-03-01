package com.mikeisesele.clearr.data.repository

import com.mikeisesele.clearr.data.dao.AppConfigDao
import com.mikeisesele.clearr.data.dao.BudgetDao
import com.mikeisesele.clearr.data.dao.GoalsDao
import com.mikeisesele.clearr.data.dao.TrackerDao
import com.mikeisesele.clearr.data.dao.TodoDao
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
import com.mikeisesele.clearr.data.model.toEntity
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ClearrRepositoryImpl @Inject constructor(
    private val appConfigDao: AppConfigDao,
    private val trackerDao: TrackerDao,
    private val budgetDao: BudgetDao,
    private val todoDao: TodoDao,
    private val goalsDao: GoalsDao,
) : ClearrRepository {

    override fun getAppConfigFlow(): Flow<AppConfig?> = appConfigDao.getConfigFlow().map { it?.toDomain() }
    override suspend fun getAppConfig(): AppConfig? = appConfigDao.getConfig()?.toDomain()
    override suspend fun upsertAppConfig(config: AppConfig) = appConfigDao.upsertConfig(config.toEntity())

    override fun getAllTrackers(): Flow<List<Tracker>> = trackerDao.getAllTrackers().map { list -> list.map { it.toDomain() } }
    override suspend fun getTrackerById(id: Long): Tracker? = trackerDao.getTrackerById(id)?.toDomain()
    override fun getTrackerByIdFlow(id: Long): Flow<Tracker?> = trackerDao.getTrackerByIdFlow(id).map { it?.toDomain() }
    override suspend fun insertTracker(tracker: Tracker): Long = trackerDao.insertTracker(tracker.toEntity())
    override suspend fun updateTracker(tracker: Tracker) = trackerDao.updateTracker(tracker.toEntity())
    override suspend fun deleteTracker(id: Long) = trackerDao.deleteTracker(id)
    override suspend fun clearTrackerNewFlag(id: Long) = trackerDao.clearNewFlag(id)

    override fun getBudgetPeriods(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetPeriod>> =
        budgetDao.getPeriods(trackerId, frequency).map { list -> list.map { it.toDomain() } }

    override suspend fun ensureBudgetPeriods(trackerId: Long, frequency: BudgetFrequency) {
        val existing = budgetDao.getLatestPeriod(trackerId, frequency)?.toDomain()
        val periods = when {
            existing == null -> when (frequency) {
                BudgetFrequency.MONTHLY -> generateMonthlyPeriods(trackerId)
                BudgetFrequency.WEEKLY -> generateWeeklyPeriods(trackerId)
            }
            else -> when (frequency) {
                BudgetFrequency.MONTHLY -> generateMissingMonthlyPeriods(trackerId, existing)
                BudgetFrequency.WEEKLY -> generateMissingWeeklyPeriods(trackerId, existing)
            }
        }
        if (periods.isNotEmpty()) budgetDao.insertPeriods(periods.map { it.toEntity() })
    }

    override fun getBudgetCategories(trackerId: Long, frequency: BudgetFrequency): Flow<List<BudgetCategory>> =
        budgetDao.getCategories(trackerId, frequency).map { list -> list.map { it.toDomain() } }

    override suspend fun getBudgetMaxSortOrder(trackerId: Long, frequency: BudgetFrequency): Int =
        budgetDao.getMaxSortOrder(trackerId, frequency)

    override suspend fun addBudgetCategory(category: BudgetCategory): Long = budgetDao.insertCategory(category.toEntity())
    override suspend fun updateBudgetCategory(category: BudgetCategory) = budgetDao.updateCategory(category.toEntity())

    override suspend fun deleteBudgetCategory(categoryId: Long) {
        budgetDao.deleteEntriesByCategory(categoryId)
        budgetDao.deleteCategoryPlansByCategory(categoryId)
        budgetDao.deleteCategory(categoryId)
    }

    override suspend fun reorderBudgetCategories(
        trackerId: Long,
        frequency: BudgetFrequency,
        orderedIds: List<Long>
    ) {
        orderedIds.forEachIndexed { index, id ->
            budgetDao.updateCategorySortOrder(id, index)
        }
    }

    override fun getBudgetCategoryPlansForTracker(trackerId: Long): Flow<List<BudgetCategoryPlan>> =
        budgetDao.getCategoryPlansForTracker(trackerId).map { list -> list.map { it.toDomain() } }

    override suspend fun getBudgetCategoryPlansForPeriod(periodId: Long): List<BudgetCategoryPlan> =
        budgetDao.getCategoryPlansForPeriod(periodId).map { it.toDomain() }

    override suspend fun saveBudgetCategoryPlans(periodId: Long, plans: List<BudgetCategoryPlan>) {
        budgetDao.deleteCategoryPlansForPeriod(periodId)
        if (plans.isNotEmpty()) budgetDao.insertCategoryPlans(plans.map { it.toEntity() })
    }

    override fun getBudgetEntriesForTracker(trackerId: Long): Flow<List<BudgetEntry>> =
        budgetDao.getEntriesForTracker(trackerId).map { list -> list.map { it.toDomain() } }

    override suspend fun addBudgetEntry(entry: BudgetEntry): Long = budgetDao.insertEntry(entry.toEntity())

    override fun getTodosForTracker(trackerId: Long): Flow<List<TodoItem>> =
        todoDao.getTodos(trackerId).map { list -> list.map { it.toDomain() } }

    override suspend fun getTodoById(id: String): TodoItem? = todoDao.getTodoById(id)?.toDomain()
    override suspend fun insertTodo(todo: TodoItem) = todoDao.insert(todo.toEntity())
    override suspend fun updateTodo(todo: TodoItem) = todoDao.insert(todo.toEntity())
    override suspend fun markTodoDone(id: String, completedAt: Long) = todoDao.markDone(id, completedAt = completedAt)
    override suspend fun deleteTodo(id: String) = todoDao.delete(id)

    override fun getGoalsForTracker(trackerId: Long): Flow<List<Goal>> =
        goalsDao.getGoals(trackerId).map { list -> list.map { it.toDomain() } }

    override fun getGoalCompletionsForTracker(trackerId: Long): Flow<List<GoalCompletion>> =
        goalsDao.getAllCompletions(trackerId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertGoal(goal: Goal) = goalsDao.insertGoal(goal.toEntity())
    override suspend fun addGoalCompletion(completion: GoalCompletion) = goalsDao.insertCompletion(completion.toEntity())
    override suspend fun deleteGoal(goalId: String) = goalsDao.deleteGoal(goalId)

    private fun generateMonthlyPeriods(trackerId: Long): List<BudgetPeriod> {
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -4) }
        return (0 until 5).map {
            val periodCal = cal.clone() as Calendar
            periodCal.add(Calendar.MONTH, it)
            buildMonthlyPeriod(trackerId, periodCal)
        }
    }

    private fun generateWeeklyPeriods(trackerId: Long): List<BudgetPeriod> {
        val base = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            add(Calendar.WEEK_OF_YEAR, -4)
        }
        return (0 until 5).map {
            val periodCal = base.clone() as Calendar
            periodCal.add(Calendar.WEEK_OF_YEAR, it)
            buildWeeklyPeriod(trackerId, periodCal)
        }
    }

    private fun generateMissingMonthlyPeriods(trackerId: Long, latest: BudgetPeriod): List<BudgetPeriod> {
        val latestCal = Calendar.getInstance().apply { timeInMillis = latest.startDate }
        val currentCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!latestCal.before(currentCal)) return emptyList()

        val periods = mutableListOf<BudgetPeriod>()
        val nextCal = (latestCal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
        while (!nextCal.after(currentCal)) {
            periods += buildMonthlyPeriod(trackerId, nextCal)
            nextCal.add(Calendar.MONTH, 1)
        }
        return periods
    }

    private fun generateMissingWeeklyPeriods(trackerId: Long, latest: BudgetPeriod): List<BudgetPeriod> {
        val latestCal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            timeInMillis = latest.startDate
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentCal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!latestCal.before(currentCal)) return emptyList()

        val periods = mutableListOf<BudgetPeriod>()
        val nextCal = (latestCal.clone() as Calendar).apply { add(Calendar.WEEK_OF_YEAR, 1) }
        while (!nextCal.after(currentCal)) {
            periods += buildWeeklyPeriod(trackerId, nextCal)
            nextCal.add(Calendar.WEEK_OF_YEAR, 1)
        }
        return periods
    }

    private fun buildMonthlyPeriod(trackerId: Long, periodCal: Calendar): BudgetPeriod {
        val label = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(periodCal.time)
        val start = (periodCal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = (periodCal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        return BudgetPeriod(
            trackerId = trackerId,
            frequency = BudgetFrequency.MONTHLY,
            label = label,
            startDate = start,
            endDate = end
        )
    }

    private fun buildWeeklyPeriod(trackerId: Long, periodCal: Calendar): BudgetPeriod {
        val week = periodCal.get(Calendar.WEEK_OF_YEAR)
        val label = "Week $week"
        val start = (periodCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = (periodCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        return BudgetPeriod(
            trackerId = trackerId,
            frequency = BudgetFrequency.WEEKLY,
            label = label,
            startDate = start,
            endDate = end
        )
    }
}
