package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.domain.repository.BudgetPreferencesRepository
import platform.Foundation.NSUserDefaults

private const val IOS_BUDGET_SWIPE_HINT_SHOWN_KEY = "clearr.budget.swipeHintShownAt"

class IosBudgetPreferencesRepository(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : BudgetPreferencesRepository {
    override suspend fun shouldShowSwipeHint(now: Long): Boolean =
        defaults.objectForKey(IOS_BUDGET_SWIPE_HINT_SHOWN_KEY) == null

    override suspend fun markSwipeHintShown(now: Long) {
        defaults.setObject(now, forKey = IOS_BUDGET_SWIPE_HINT_SHOWN_KEY)
    }
}
