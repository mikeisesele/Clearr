package com.mikeisesele.clearr.runtime

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.Foundation.NSUserDefaults
import platform.posix.gettimeofday
import platform.posix.timeval

@OptIn(ExperimentalForeignApi::class)
class IosClearrRuntime(
    defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
    private val delegate: ClearrRuntime = InMemoryClearrRuntime(
        repository = IosClearrRepository(defaults),
        onboardingStatusRepository = IosOnboardingStatusRepository(defaults),
        budgetPreferencesRepository = IosBudgetPreferencesRepository(defaults),
        todoPreferencesRepository = IosTodoPreferencesRepository(defaults),
        budgetAiService = IosBudgetAiService(),
        todoAiService = IosTodoAiService(),
        goalsAiService = IosGoalsAiService(),
        nowMillis = {
            memScoped {
                val tv = alloc<timeval>()
                gettimeofday(tv.ptr, null)
                (tv.tv_sec * 1000L) + (tv.tv_usec / 1000L)
            }
        }
    )
) : ClearrRuntime by delegate
