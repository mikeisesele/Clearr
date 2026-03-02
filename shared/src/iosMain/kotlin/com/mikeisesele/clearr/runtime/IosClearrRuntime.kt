package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.data.local.room.RoomAppConfigTrackerRepository
import com.mikeisesele.clearr.data.local.room.createIosClearrSharedDatabase
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
    override val repository: HybridClearrRepository = HybridClearrRepository(
        trackerRepository = RoomAppConfigTrackerRepository(
            appConfigDao = roomDatabase.appConfigDao(),
            trackerDao = roomDatabase.trackerDao()
        ),
        featureRepository = featureRepository
    ),
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
