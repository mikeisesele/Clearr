package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.trackers.ObserveTrackerSummariesUseCase
import com.mikeisesele.clearr.domain.trackers.TrackerBootstrapper
import com.mikeisesele.clearr.ui.feature.dashboard.utils.primarySummaryOf
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest

@HiltViewModel
class AppShellViewModel @Inject constructor(
    private val trackerBootstrapper: TrackerBootstrapper,
    private val observeTrackerSummaries: ObserveTrackerSummariesUseCase
) : BaseViewModel<AppShellUiState, AppShellAction, AppShellEvent>(
    initialState = AppShellUiState(isLoading = true)
) {

    init {
        onAction(AppShellAction.Observe)
    }

    override fun onAction(action: AppShellAction) {
        when (action) {
            AppShellAction.Observe -> observeTrackers()
        }
    }

    private fun observeTrackers() {
        launch {
            trackerBootstrapper.ensureStaticTrackers()
            observeTrackerSummaries().collectLatest { summaries ->
                updateState {
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
