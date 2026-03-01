package com.mikeisesele.clearr.preview

import com.mikeisesele.clearr.domain.repository.OnboardingStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryOnboardingStatusRepository(
    initialComplete: Boolean = false
) : OnboardingStatusRepository {
    private val completionFlow = MutableStateFlow(initialComplete)

    override val isOnboardingComplete: Flow<Boolean> = completionFlow

    override suspend fun markComplete() {
        completionFlow.value = true
    }
}
