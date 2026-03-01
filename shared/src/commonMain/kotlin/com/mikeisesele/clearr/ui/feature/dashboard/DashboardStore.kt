package com.mikeisesele.clearr.ui.feature.dashboard

import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.trackers.ObserveTrackerSummariesUseCase
import com.mikeisesele.clearr.domain.trackers.TrackerBootstrapper
import com.mikeisesele.clearr.ui.feature.dashboard.utils.emptyDashboardUiModel
import com.mikeisesele.clearr.ui.feature.dashboard.utils.primarySummaryOf
import com.mikeisesele.clearr.ui.feature.dashboard.utils.toDashboardUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardStore(
    private val trackerBootstrapper: TrackerBootstrapper,
    private val observeTrackerSummaries: ObserveTrackerSummariesUseCase,
    private val scope: CoroutineScope
) {
    private val mutableState = MutableStateFlow(DashboardState(model = emptyDashboardUiModel(), isLoading = true))
    val uiState: StateFlow<DashboardState> = mutableState.asStateFlow()

    private val eventChannel = Channel<DashboardEvent>(capacity = Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        scope.launch {
            trackerBootstrapper.ensureStaticTrackers()
            observeTrackerSummaries().collect { summaries ->
                mutableState.update { state ->
                    val model = listOfNotNull(
                        summaries.primarySummaryOf(TrackerType.BUDGET),
                        summaries.primarySummaryOf(TrackerType.GOALS),
                        summaries.primarySummaryOf(TrackerType.TODO)
                    ).toDashboardUiModel()
                    state.copy(
                        model = model.copy(urgencyItems = model.urgencyItems.filterNot { it.id in state.dismissedUrgencyIds }),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.DismissUrgency -> {
                mutableState.update { state ->
                    val dismissed = state.dismissedUrgencyIds + action.id
                    state.copy(
                        dismissedUrgencyIds = dismissed,
                        model = state.model.copy(urgencyItems = state.model.urgencyItems.filterNot { it.id == action.id })
                    )
                }
            }
            is DashboardAction.QuickAction -> {
                scope.launch { eventChannel.send(DashboardEvent.OpenTracker(action.trackerType)) }
            }
        }
    }
}
