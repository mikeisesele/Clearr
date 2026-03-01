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
    override val repository: IosClearrRepository = IosClearrRepository(defaults),
    override val onboardingStatusRepository: IosOnboardingStatusRepository = IosOnboardingStatusRepository(defaults),
    override val budgetPreferencesRepository: IosBudgetPreferencesRepository = IosBudgetPreferencesRepository(defaults),
    override val todoPreferencesRepository: IosTodoPreferencesRepository = IosTodoPreferencesRepository(defaults),
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
