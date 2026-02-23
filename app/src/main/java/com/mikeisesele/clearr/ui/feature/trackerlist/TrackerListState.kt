package com.mikeisesele.clearr.ui.feature.trackerlist

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType

data class TrackerListUiState(
    val summaries: List<TrackerSummary> = emptyList(),
    val isLoading: Boolean = true
) : BaseState

sealed interface TrackerListAction {
    data class CreateTracker(
        val name: String,
        val type: TrackerType,
        val frequency: Frequency,
        val defaultAmount: Double,
        val initialMembers: List<String>
    ) : TrackerListAction

    data class ClearNewFlag(val trackerId: Long) : TrackerListAction
    data class DeleteTracker(val trackerId: Long) : TrackerListAction
    data class RenameTracker(val trackerId: Long, val newName: String) : TrackerListAction
    data object Refresh : TrackerListAction
}

sealed interface TrackerListEvent : ViewEvent
