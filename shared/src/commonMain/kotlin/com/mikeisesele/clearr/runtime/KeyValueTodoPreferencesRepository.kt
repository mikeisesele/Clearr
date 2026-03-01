package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.domain.repository.TodoPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

private const val TODO_SWIPE_HINT_SEEN_KEY = "clearr.todo.swipeHintSeen"

class KeyValueTodoPreferencesRepository(
    private val store: KeyValueStoreDriver
) : TodoPreferencesRepository {
    private val mutableSwipeHintSeen = MutableStateFlow(store.getBoolean(TODO_SWIPE_HINT_SEEN_KEY))

    override val swipeHintSeen: Flow<Boolean> = mutableSwipeHintSeen

    override suspend fun markSwipeHintSeen() {
        store.setBoolean(TODO_SWIPE_HINT_SEEN_KEY, true)
        mutableSwipeHintSeen.value = true
    }
}
