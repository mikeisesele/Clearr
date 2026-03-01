package com.mikeisesele.clearr.domain.repository

interface BudgetPreferencesRepository {
    suspend fun shouldShowSwipeHint(now: Long): Boolean
    suspend fun markSwipeHintShown(now: Long)
}
