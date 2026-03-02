package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.data.local.room.RoomAppConfigTrackerRepository
import com.mikeisesele.clearr.data.local.room.RoomBudgetRepository
import com.mikeisesele.clearr.data.local.room.RoomGoalsRepository
import com.mikeisesele.clearr.data.local.room.RoomTodoRepository
import com.mikeisesele.clearr.data.local.room.createIosClearrSharedDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.gettimeofday
import platform.posix.timeval

@OptIn(ExperimentalForeignApi::class)
class IosClearrRuntime(
    store: KeyValueStoreDriver = NSUserDefaultsKeyValueStoreDriver(),
    featureRepository: IosClearrRepository = IosClearrRepository(store),
    roomDatabase: com.mikeisesele.clearr.data.local.room.ClearrSharedDatabase = createIosClearrSharedDatabase(),
    override val repository: HybridClearrRepository = createIosHybridRepository(roomDatabase, featureRepository),
    override val onboardingStatusRepository: KeyValueOnboardingStatusRepository = KeyValueOnboardingStatusRepository(store),
    override val budgetPreferencesRepository: KeyValueBudgetPreferencesRepository = KeyValueBudgetPreferencesRepository(store),
    override val todoPreferencesRepository: KeyValueTodoPreferencesRepository = KeyValueTodoPreferencesRepository(store),
    override val budgetAiService: IosBudgetAiService = IosBudgetAiService(),
    override val todoAiService: IosTodoAiService = IosTodoAiService(),
    override val goalsAiService: IosGoalsAiService = IosGoalsAiService(),
    override val nowMillis: () -> Long = {
        memScoped {
            val tv = alloc<timeval>()
            gettimeofday(tv.ptr, null)
            (tv.tv_sec * 1000L) + (tv.tv_usec / 1000L)
        }
    }
) : ClearrRuntime

private fun createIosHybridRepository(
    roomDatabase: com.mikeisesele.clearr.data.local.room.ClearrSharedDatabase,
    featureRepository: IosClearrRepository
): HybridClearrRepository {
    val trackerRepository = RoomAppConfigTrackerRepository(
        appConfigDao = roomDatabase.appConfigDao(),
        trackerDao = roomDatabase.trackerDao()
    )
    val budgetRepository = RoomBudgetRepository(roomDatabase.budgetDao())
    val goalsRepository = RoomGoalsRepository(roomDatabase.goalDao())
    val todoRepository = RoomTodoRepository(roomDatabase.todoDao())
    runBlocking {
        migrateLegacyBudgetIfNeeded(
            trackerRepository = trackerRepository,
            budgetRepository = budgetRepository,
            featureRepository = featureRepository
        )
        migrateLegacyTodosIfNeeded(
            trackerRepository = trackerRepository,
            todoRepository = todoRepository,
            featureRepository = featureRepository
        )
        migrateLegacyGoalsIfNeeded(
            trackerRepository = trackerRepository,
            goalsRepository = goalsRepository,
            featureRepository = featureRepository
        )
    }
    return HybridClearrRepository(
        trackerRepository = trackerRepository,
        budgetRepository = budgetRepository,
        goalsRepository = goalsRepository,
        todoRepository = todoRepository,
        featureRepository = featureRepository
    )
}

private suspend fun migrateLegacyBudgetIfNeeded(
    trackerRepository: RoomAppConfigTrackerRepository,
    budgetRepository: RoomBudgetRepository,
    featureRepository: IosClearrRepository
) {
    if (!budgetRepository.isEmpty()) return
    val budgetTrackers = trackerRepository.getAllTrackers().first()
        .filter { it.type == com.mikeisesele.clearr.data.model.TrackerType.BUDGET }
    val periods = budgetTrackers.flatMap { tracker ->
        featureRepository.getBudgetPeriods(tracker.id, com.mikeisesele.clearr.data.model.BudgetFrequency.MONTHLY).first() +
            featureRepository.getBudgetPeriods(tracker.id, com.mikeisesele.clearr.data.model.BudgetFrequency.WEEKLY).first()
    }
    val categories = budgetTrackers.flatMap { tracker ->
        featureRepository.getBudgetCategories(tracker.id, com.mikeisesele.clearr.data.model.BudgetFrequency.MONTHLY).first() +
            featureRepository.getBudgetCategories(tracker.id, com.mikeisesele.clearr.data.model.BudgetFrequency.WEEKLY).first()
    }
    val plans = budgetTrackers.flatMap { tracker ->
        featureRepository.getBudgetCategoryPlansForTracker(tracker.id).first()
    }
    val entries = budgetTrackers.flatMap { tracker ->
        featureRepository.getBudgetEntriesForTracker(tracker.id).first()
    }
    budgetRepository.seedBudgetData(periods, categories, plans, entries)
}

private suspend fun migrateLegacyTodosIfNeeded(
    trackerRepository: RoomAppConfigTrackerRepository,
    todoRepository: RoomTodoRepository,
    featureRepository: IosClearrRepository
) {
    if (!todoRepository.isEmpty()) return
    val legacyTodos = trackerRepository.getAllTrackers().first()
        .flatMap { tracker -> featureRepository.getTodosForTracker(tracker.id).first() }
    todoRepository.seedTodos(legacyTodos)
}

private suspend fun migrateLegacyGoalsIfNeeded(
    trackerRepository: RoomAppConfigTrackerRepository,
    goalsRepository: RoomGoalsRepository,
    featureRepository: IosClearrRepository
) {
    if (!goalsRepository.isEmpty()) return
    val trackers = trackerRepository.getAllTrackers().first()
    val legacyGoals = trackers.flatMap { tracker -> featureRepository.getGoalsForTracker(tracker.id).first() }
    val legacyCompletions = trackers.flatMap { tracker -> featureRepository.getGoalCompletionsForTracker(tracker.id).first() }
    goalsRepository.seedGoals(legacyGoals, legacyCompletions)
}
