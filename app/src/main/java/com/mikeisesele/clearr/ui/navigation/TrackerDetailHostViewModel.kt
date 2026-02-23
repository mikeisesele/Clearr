package com.mikeisesele.clearr.ui.navigation

import androidx.lifecycle.SavedStateHandle
import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.domain.repository.DuesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class TrackerDetailHostViewModel @Inject constructor(
    private val repository: DuesRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<TrackerDetailHostState, TrackerDetailHostAction, TrackerDetailHostEvent>(
    initialState = TrackerDetailHostState()
) {

    private val trackerId = checkNotNull(savedStateHandle.get<Long>("trackerId"))

    init {
        launch {
            repository.getTrackerByIdFlow(trackerId).collectLatest { tracker ->
                if (tracker == null) {
                    updateState { it.copy(isLoading = false) }
                    return@collectLatest
                }
                updateState {
                    it.copy(
                        isLoading = false,
                        trackerType = tracker.type
                    )
                }
            }
        }
    }

    override fun onAction(action: TrackerDetailHostAction) = Unit
}
