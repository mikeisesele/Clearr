package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.domain.repository.TodoPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class AndroidTodoPreferencesRepository(
    initialSwipeHintSeen: Boolean = false
) : TodoPreferencesRepository {
    private val mutableSwipeHintSeen = MutableStateFlow(initialSwipeHintSeen)

    override val swipeHintSeen: Flow<Boolean> = mutableSwipeHintSeen

    override suspend fun markSwipeHintSeen() {
        mutableSwipeHintSeen.value = true
    }
}
