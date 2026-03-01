package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.domain.repository.OnboardingStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

private const val IOS_ONBOARDING_COMPLETE_KEY = "clearr.onboarding.complete"

class IosOnboardingStatusRepository(
    private val store: KeyValueStoreDriver = NSUserDefaultsKeyValueStoreDriver()
) : OnboardingStatusRepository {
    private val completionFlow = MutableStateFlow(store.getBoolean(IOS_ONBOARDING_COMPLETE_KEY))

    override val isOnboardingComplete: Flow<Boolean> = completionFlow

    override suspend fun markComplete() {
        store.setBoolean(IOS_ONBOARDING_COMPLETE_KEY, true)
        completionFlow.value = true
    }
}
