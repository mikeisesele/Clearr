package com.mikeisesele.clearr.ui.feature.dashboard

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerType
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardUiModel

data class DashboardState(
    val model: DashboardUiModel,
    val isLoading: Boolean = true,
    val dismissedUrgencyIds: Set<String> = emptySet()
) : BaseState

sealed interface DashboardAction {
    data class DismissUrgency(val id: String) : DashboardAction
    data class QuickAction(val trackerType: DashboardTrackerType) : DashboardAction
}

sealed interface DashboardEvent : ViewEvent {
    data class OpenTracker(val trackerType: DashboardTrackerType) : DashboardEvent
}
