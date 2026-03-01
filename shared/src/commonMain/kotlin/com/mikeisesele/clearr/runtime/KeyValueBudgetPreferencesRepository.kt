package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.domain.repository.BudgetPreferencesRepository

private const val BUDGET_SWIPE_HINT_SHOWN_KEY = "clearr.budget.swipeHintShownAt"

class KeyValueBudgetPreferencesRepository(
    private val store: KeyValueStoreDriver
) : BudgetPreferencesRepository {
    override suspend fun shouldShowSwipeHint(now: Long): Boolean =
        store.getLong(BUDGET_SWIPE_HINT_SHOWN_KEY) == null

    override suspend fun markSwipeHintShown(now: Long) {
        store.setLong(BUDGET_SWIPE_HINT_SHOWN_KEY, now)
    }
}
