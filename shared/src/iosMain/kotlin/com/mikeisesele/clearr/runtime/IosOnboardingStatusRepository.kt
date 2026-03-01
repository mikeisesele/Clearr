package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.domain.repository.OnboardingStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults

private const val IOS_ONBOARDING_COMPLETE_KEY = "clearr.onboarding.complete"

class IosOnboardingStatusRepository(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : OnboardingStatusRepository {
    private val completionFlow = MutableStateFlow(defaults.boolForKey(IOS_ONBOARDING_COMPLETE_KEY))

    override val isOnboardingComplete: Flow<Boolean> = completionFlow

    override suspend fun markComplete() {
        defaults.setBool(true, forKey = IOS_ONBOARDING_COMPLETE_KEY)
        completionFlow.value = true
    }
}
