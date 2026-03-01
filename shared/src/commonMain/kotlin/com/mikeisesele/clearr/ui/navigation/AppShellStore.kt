package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.trackers.ObserveTrackerSummariesUseCase
import com.mikeisesele.clearr.domain.trackers.TrackerBootstrapper
import com.mikeisesele.clearr.ui.feature.dashboard.utils.primarySummaryOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppShellStore(
    private val trackerBootstrapper: TrackerBootstrapper,
    private val observeTrackerSummaries: ObserveTrackerSummariesUseCase,
    private val scope: CoroutineScope
) {
    private val mutableState = MutableStateFlow(AppShellUiState(isLoading = true))
    val uiState: StateFlow<AppShellUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<AppShellEvent>(capacity = Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        onAction(AppShellAction.Observe)
    }

    fun onAction(action: AppShellAction) {
        when (action) {
            AppShellAction.Observe -> observeTrackers()
        }
    }

    private fun observeTrackers() {
        scope.launch {
            trackerBootstrapper.ensureStaticTrackers()
            observeTrackerSummaries().collect { summaries ->
                mutableState.update {
                    it.copy(
                        budgetTrackerId = summaries.primarySummaryOf(TrackerType.BUDGET)?.trackerId,
                        todoTrackerId = summaries.primarySummaryOf(TrackerType.TODO)?.trackerId,
                        goalsTrackerId = summaries.primarySummaryOf(TrackerType.GOALS)?.trackerId,
                        isLoading = false
                    )
                }
            }
        }
    }
}
