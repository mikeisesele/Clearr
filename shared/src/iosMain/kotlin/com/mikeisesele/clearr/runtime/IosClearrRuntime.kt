package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.data.local.room.RoomAppConfigTrackerRepository
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
    val goalsRepository = RoomGoalsRepository(roomDatabase.goalDao())
    val todoRepository = RoomTodoRepository(roomDatabase.todoDao())
    runBlocking {
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
        goalsRepository = goalsRepository,
        todoRepository = todoRepository,
        featureRepository = featureRepository
    )
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
