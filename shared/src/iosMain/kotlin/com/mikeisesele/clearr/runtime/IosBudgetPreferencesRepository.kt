package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.domain.repository.BudgetPreferencesRepository

private const val IOS_BUDGET_SWIPE_HINT_SHOWN_KEY = "clearr.budget.swipeHintShownAt"

class IosBudgetPreferencesRepository(
    private val store: KeyValueStoreDriver = NSUserDefaultsKeyValueStoreDriver()
) : BudgetPreferencesRepository {
    override suspend fun shouldShowSwipeHint(now: Long): Boolean =
        store.getLong(IOS_BUDGET_SWIPE_HINT_SHOWN_KEY) == null

    override suspend fun markSwipeHintShown(now: Long) {
        store.setLong(IOS_BUDGET_SWIPE_HINT_SHOWN_KEY, now)
    }
}
