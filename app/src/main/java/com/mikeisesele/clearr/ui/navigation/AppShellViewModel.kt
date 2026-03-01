package com.mikeisesele.clearr.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikeisesele.clearr.domain.trackers.ObserveTrackerSummariesUseCase
import com.mikeisesele.clearr.domain.trackers.TrackerBootstrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class AppShellViewModel @Inject constructor(
    trackerBootstrapper: TrackerBootstrapper,
    observeTrackerSummaries: ObserveTrackerSummariesUseCase
) : ViewModel() {

    private val store = AppShellStore(
        trackerBootstrapper = trackerBootstrapper,
        observeTrackerSummaries = observeTrackerSummaries,
        scope = viewModelScope
    )

    val uiState: StateFlow<AppShellUiState> = store.uiState
    val events = store.events

    fun onAction(action: AppShellAction) {
        store.onAction(action)
    }
}
