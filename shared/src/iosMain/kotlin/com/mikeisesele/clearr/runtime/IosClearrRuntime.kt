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
    migrationStore: LegacyIosMigrationStore = LegacyIosMigrationStore(store),
    roomDatabase: com.mikeisesele.clearr.data.local.room.ClearrSharedDatabase = createIosClearrSharedDatabase(),
    override val repository: HybridClearrRepository = createIosHybridRepository(roomDatabase, migrationStore),
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
    migrationStore: LegacyIosMigrationStore
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
            migrationStore = migrationStore
        )
        migrateLegacyTodosIfNeeded(
            trackerRepository = trackerRepository,
            todoRepository = todoRepository,
            migrationStore = migrationStore
        )
        migrateLegacyGoalsIfNeeded(
            trackerRepository = trackerRepository,
            goalsRepository = goalsRepository,
            migrationStore = migrationStore
        )
    }
    return HybridClearrRepository(
        trackerRepository = trackerRepository,
        budgetRepository = budgetRepository,
        goalsRepository = goalsRepository,
        todoRepository = todoRepository
    )
}

private suspend fun migrateLegacyBudgetIfNeeded(
    trackerRepository: RoomAppConfigTrackerRepository,
    budgetRepository: RoomBudgetRepository,
    migrationStore: LegacyIosMigrationStore
) {
    if (!budgetRepository.isEmpty()) return
    val budgetTrackers = trackerRepository.getAllTrackers().first()
        .filter { it.type == com.mikeisesele.clearr.data.model.TrackerType.BUDGET }
    val periods = budgetTrackers.flatMap { tracker ->
        migrationStore.loadBudgetPeriods(tracker.id, com.mikeisesele.clearr.data.model.BudgetFrequency.MONTHLY) +
            migrationStore.loadBudgetPeriods(tracker.id, com.mikeisesele.clearr.data.model.BudgetFrequency.WEEKLY)
    }
    val categories = budgetTrackers.flatMap { tracker ->
        migrationStore.loadBudgetCategories(tracker.id, com.mikeisesele.clearr.data.model.BudgetFrequency.MONTHLY) +
            migrationStore.loadBudgetCategories(tracker.id, com.mikeisesele.clearr.data.model.BudgetFrequency.WEEKLY)
    }
    val plans = budgetTrackers.flatMap { tracker ->
        migrationStore.loadBudgetCategoryPlans(tracker.id)
    }
    val entries = budgetTrackers.flatMap { tracker ->
        migrationStore.loadBudgetEntries(tracker.id)
    }
    budgetRepository.seedBudgetData(periods, categories, plans, entries)
}

private suspend fun migrateLegacyTodosIfNeeded(
    trackerRepository: RoomAppConfigTrackerRepository,
    todoRepository: RoomTodoRepository,
    migrationStore: LegacyIosMigrationStore
) {
    if (!todoRepository.isEmpty()) return
    val legacyTodos = trackerRepository.getAllTrackers().first()
        .flatMap { tracker -> migrationStore.loadTodos(tracker.id) }
    todoRepository.seedTodos(legacyTodos)
}

private suspend fun migrateLegacyGoalsIfNeeded(
    trackerRepository: RoomAppConfigTrackerRepository,
    goalsRepository: RoomGoalsRepository,
    migrationStore: LegacyIosMigrationStore
) {
    if (!goalsRepository.isEmpty()) return
    val trackers = trackerRepository.getAllTrackers().first()
    val legacyGoals = trackers.flatMap { tracker -> migrationStore.loadGoals(tracker.id) }
    val legacyCompletions = trackers.flatMap { tracker -> migrationStore.loadGoalCompletionsForTracker(tracker.id) }
    goalsRepository.seedGoals(legacyGoals, legacyCompletions)
}
