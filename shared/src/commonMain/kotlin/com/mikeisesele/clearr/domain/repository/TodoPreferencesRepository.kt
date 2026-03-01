package com.mikeisesele.clearr.domain.repository

import kotlinx.coroutines.flow.Flow

interface TodoPreferencesRepository {
    val swipeHintSeen: Flow<Boolean>

    suspend fun markSwipeHintSeen()
}
