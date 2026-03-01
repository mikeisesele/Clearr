package com.mikeisesele.clearr.ui.feature.dashboard

import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.trackers.ObserveTrackerSummariesUseCase
import com.mikeisesele.clearr.domain.trackers.TrackerBootstrapper
import com.mikeisesele.clearr.ui.feature.dashboard.utils.primarySummaryOf
import com.mikeisesele.clearr.ui.feature.dashboard.utils.previewEmptyDashboardUi
import com.mikeisesele.clearr.ui.feature.dashboard.utils.toDashboardUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val trackerBootstrapper: TrackerBootstrapper,
    private val observeTrackerSummaries: ObserveTrackerSummariesUseCase
) : BaseViewModel<DashboardState, DashboardAction, DashboardEvent>(
    initialState = DashboardState(model = previewEmptyDashboardUi, isLoading = true)
) {

    init {
        launch {
            trackerBootstrapper.ensureStaticTrackers()
            observeTrackerSummaries().collectLatest { summaries ->
                updateState { state ->
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

    override fun onAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.DismissUrgency -> updateState { state ->
                val dismissed = state.dismissedUrgencyIds + action.id
                state.copy(
                    dismissedUrgencyIds = dismissed,
                    model = state.model.copy(urgencyItems = state.model.urgencyItems.filterNot { it.id == action.id })
                )
            }
            is DashboardAction.QuickAction -> sendEvent(DashboardEvent.OpenTracker(action.trackerType))
        }
    }
}
