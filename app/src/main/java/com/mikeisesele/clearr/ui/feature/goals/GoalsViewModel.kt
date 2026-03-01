package com.mikeisesele.clearr.ui.feature.goals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikeisesele.clearr.domain.repository.GoalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class GoalsViewModel @Inject constructor(
    repository: GoalsRepository,
    goalsAiService: GoalsAiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val store = GoalsStore(
        trackerId = checkNotNull(savedStateHandle.get<Long>("trackerId")),
        repository = repository,
        goalsAiService = goalsAiService,
        scope = viewModelScope,
        nowMillis = System::currentTimeMillis
    )

    val uiState: StateFlow<GoalsUiState> = store.uiState
    val events = store.events

    fun onAction(action: GoalsAction) {
        store.onAction(action)
    }
}
