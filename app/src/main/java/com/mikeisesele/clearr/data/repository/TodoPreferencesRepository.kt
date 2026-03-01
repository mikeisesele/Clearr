package com.mikeisesele.clearr.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import com.mikeisesele.clearr.domain.repository.TodoPreferencesRepository as TodoPreferencesContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.todoDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "clearr_todo")

@Singleton
class TodoPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : TodoPreferencesContract {
    private val SWIPE_HINT_SEEN = booleanPreferencesKey("todo_swipe_hint_seen")

    override val swipeHintSeen: Flow<Boolean> = context.todoDataStore.data
        .map { prefs -> prefs[SWIPE_HINT_SEEN] ?: false }

    override suspend fun markSwipeHintSeen() {
        context.todoDataStore.edit { prefs -> prefs[SWIPE_HINT_SEEN] = true }
    }
}
