package com.mikeisesele.clearr.preview

import com.mikeisesele.clearr.domain.repository.TodoPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryTodoPreferencesRepository(
    initialSwipeHintSeen: Boolean = false
) : TodoPreferencesRepository {
    private val mutableSwipeHintSeen = MutableStateFlow(initialSwipeHintSeen)

    override val swipeHintSeen: Flow<Boolean> = mutableSwipeHintSeen

    override suspend fun markSwipeHintSeen() {
        mutableSwipeHintSeen.value = true
    }
}
