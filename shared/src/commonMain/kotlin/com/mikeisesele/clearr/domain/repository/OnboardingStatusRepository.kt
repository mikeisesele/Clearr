package com.mikeisesele.clearr.domain.repository

import kotlinx.coroutines.flow.Flow

interface OnboardingStatusRepository {
    val isOnboardingComplete: Flow<Boolean>

    suspend fun markComplete()
}
