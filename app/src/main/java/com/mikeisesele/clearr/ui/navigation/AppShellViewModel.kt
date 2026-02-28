package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.repository.DuesRepository
import com.mikeisesele.clearr.domain.trackers.TrackerBootstrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class AppShellViewModel @Inject constructor(
    private val repository: DuesRepository,
    private val trackerBootstrapper: TrackerBootstrapper
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
            repository.getAllTrackers().collectLatest { trackers ->
                updateState {
                    it.copy(
                        budgetTrackerId = trackers.firstIdOf(TrackerType.BUDGET),
                        todoTrackerId = trackers.firstIdOf(TrackerType.TODO),
                        goalsTrackerId = trackers.firstIdOf(TrackerType.GOALS),
                        remittanceCount = trackers.count { tracker ->
                            tracker.type == TrackerType.DUES || tracker.type == TrackerType.EXPENSES
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

private fun List<Tracker>.firstIdOf(type: TrackerType): Long? =
    firstOrNull { tracker -> tracker.type == type }?.id
