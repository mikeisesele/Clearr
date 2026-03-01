package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.domain.repository.TodoPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

private const val IOS_TODO_SWIPE_HINT_SEEN_KEY = "clearr.todo.swipeHintSeen"

class IosTodoPreferencesRepository(
    private val store: KeyValueStoreDriver = NSUserDefaultsKeyValueStoreDriver()
) : TodoPreferencesRepository {
    private val mutableSwipeHintSeen = MutableStateFlow(store.getBoolean(IOS_TODO_SWIPE_HINT_SEEN_KEY))

    override val swipeHintSeen: Flow<Boolean> = mutableSwipeHintSeen

    override suspend fun markSwipeHintSeen() {
        store.setBoolean(IOS_TODO_SWIPE_HINT_SEEN_KEY, true)
        mutableSwipeHintSeen.value = true
    }
}
