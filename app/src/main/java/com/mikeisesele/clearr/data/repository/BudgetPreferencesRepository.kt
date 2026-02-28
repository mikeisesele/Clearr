package com.mikeisesele.clearr.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private val Context.budgetDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "clearr_budget")

@Singleton
class BudgetPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val swipeHintSeen = booleanPreferencesKey("budget_swipe_hint_seen")
    private val swipeHintLastShownAt = longPreferencesKey("budget_swipe_hint_last_shown_at")

    suspend fun shouldShowSwipeHint(now: Long = System.currentTimeMillis()): Boolean {
        val prefs = context.budgetDataStore.data.first()
        val hasSeen = prefs[swipeHintSeen] ?: false
        if (!hasSeen) return true

        val lastShownAt = prefs[swipeHintLastShownAt] ?: 0L
        val cooldownMs = 7L * 24L * 60L * 60L * 1000L
        if (now - lastShownAt < cooldownMs) return false

        return Random.nextFloat() < 0.18f
    }

    suspend fun markSwipeHintShown(now: Long = System.currentTimeMillis()) {
        context.budgetDataStore.edit { prefs ->
            prefs[swipeHintSeen] = true
            prefs[swipeHintLastShownAt] = now
        }
    }
}
