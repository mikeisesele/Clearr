package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.trackers.ObserveTrackerSummariesUseCase
import com.mikeisesele.clearr.domain.trackers.TrackerBootstrapper
import com.mikeisesele.clearr.ui.feature.dashboard.utils.primarySummaryOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

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
                        remittanceCount = summaries.count { summary ->
                            summary.type == TrackerType.DUES || summary.type == TrackerType.EXPENSES
                        },
                        isLoading = false
                    )
                }
            }
        }
    }
}

data class AppShellUiState(
    val budgetTrackerId: Long? = null,
    val todoTrackerId: Long? = null,
    val goalsTrackerId: Long? = null,
    val remittanceCount: Int = 0,
    val isLoading: Boolean = true
) : BaseState

sealed interface AppShellAction {
    data object Observe : AppShellAction
}

sealed interface AppShellEvent : ViewEvent
