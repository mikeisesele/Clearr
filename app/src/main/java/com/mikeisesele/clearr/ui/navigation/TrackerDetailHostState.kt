package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.TrackerType

data class TrackerDetailHostState(
    val isLoading: Boolean = true,
    val trackerType: TrackerType = TrackerType.DUES
) : BaseState

sealed interface TrackerDetailHostAction

sealed interface TrackerDetailHostEvent : ViewEvent
