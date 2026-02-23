package com.mikeisesele.clearr.ui.feature.goals

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.data.model.GoalSummary

data class GoalsUiState(
    val trackerId: Long = -1L,
    val trackerName: String = "My Goals",
    val summaries: List<GoalSummary> = emptyList(),
    val doneCount: Int = 0,
    val totalCount: Int = 0,
    val allDoneThisPeriod: Boolean = false,
    val isLoading: Boolean = true
) : BaseState

sealed interface GoalsAction {
    data class MarkDone(val goalId: String) : GoalsAction
    data class AddGoal(
        val title: String,
        val emoji: String,
        val colorToken: String,
        val target: String?,
        val frequency: GoalFrequency
    ) : GoalsAction
}

sealed interface GoalsEvent : ViewEvent
