package com.mikeisesele.clearr.ui.feature.trackerlist

import com.mikeisesele.clearr.data.model.TrackerSummary

data class TrackerListUiState(
    val summaries: List<TrackerSummary> = emptyList(),
    val isLoading: Boolean = true
)
