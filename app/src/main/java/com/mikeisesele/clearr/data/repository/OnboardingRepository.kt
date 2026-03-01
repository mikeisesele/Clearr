package com.mikeisesele.clearr.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import com.mikeisesele.clearr.domain.repository.OnboardingStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onboardingDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "clearr_onboarding")

@Singleton
class OnboardingRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : OnboardingStatusRepository {
    private val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")

    /** Emits true once the user has completed or skipped onboarding. */
    override val isOnboardingComplete: Flow<Boolean> = context.onboardingDataStore.data
        .map { prefs -> prefs[ONBOARDING_COMPLETE] ?: false }

    /** Persists the onboarding-complete flag so onboarding never shows again. */
    override suspend fun markComplete() {
        context.onboardingDataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETE] = true
        }
    }
}
