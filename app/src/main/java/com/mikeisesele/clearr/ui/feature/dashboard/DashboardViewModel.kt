package com.mikeisesele.clearr.ui.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class DashboardViewModel @Inject constructor(
    trackerBootstrapper: com.mikeisesele.clearr.domain.trackers.TrackerBootstrapper,
    observeTrackerSummaries: com.mikeisesele.clearr.domain.trackers.ObserveTrackerSummariesUseCase
) : ViewModel() {

    private val store = DashboardStore(
        trackerBootstrapper = trackerBootstrapper,
        observeTrackerSummaries = observeTrackerSummaries,
        scope = viewModelScope
    )

    val uiState: StateFlow<DashboardState> = store.uiState
    val events = store.events

    fun onAction(action: DashboardAction) {
        store.onAction(action)
    }
}
