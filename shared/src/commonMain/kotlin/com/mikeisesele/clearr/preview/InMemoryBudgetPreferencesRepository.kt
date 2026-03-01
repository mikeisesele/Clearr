package com.mikeisesele.clearr.preview

import com.mikeisesele.clearr.domain.repository.BudgetPreferencesRepository

class InMemoryBudgetPreferencesRepository(
    private var lastShownAt: Long? = null
) : BudgetPreferencesRepository {
    override suspend fun shouldShowSwipeHint(now: Long): Boolean = lastShownAt == null

    override suspend fun markSwipeHintShown(now: Long) {
        lastShownAt = now
    }
}
