package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.domain.repository.TodoPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults

private const val IOS_TODO_SWIPE_HINT_SEEN_KEY = "clearr.todo.swipeHintSeen"

class IosTodoPreferencesRepository(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : TodoPreferencesRepository {
    private val mutableSwipeHintSeen = MutableStateFlow(defaults.boolForKey(IOS_TODO_SWIPE_HINT_SEEN_KEY))

    override val swipeHintSeen: Flow<Boolean> = mutableSwipeHintSeen

    override suspend fun markSwipeHintSeen() {
        defaults.setBool(true, forKey = IOS_TODO_SWIPE_HINT_SEEN_KEY)
        mutableSwipeHintSeen.value = true
    }
}
