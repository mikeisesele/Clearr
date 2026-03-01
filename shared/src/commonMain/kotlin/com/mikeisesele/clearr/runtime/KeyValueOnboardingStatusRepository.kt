package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.domain.repository.OnboardingStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

private const val ONBOARDING_COMPLETE_KEY = "clearr.onboarding.complete"

class KeyValueOnboardingStatusRepository(
    private val store: KeyValueStoreDriver
) : OnboardingStatusRepository {
    private val completionFlow = MutableStateFlow(store.getBoolean(ONBOARDING_COMPLETE_KEY))

    override val isOnboardingComplete: Flow<Boolean> = completionFlow

    override suspend fun markComplete() {
        store.setBoolean(ONBOARDING_COMPLETE_KEY, true)
        completionFlow.value = true
    }
}
