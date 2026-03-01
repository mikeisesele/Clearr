package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent

data class AppShellUiState(
    val budgetTrackerId: Long? = null,
    val todoTrackerId: Long? = null,
    val goalsTrackerId: Long? = null,
    val isLoading: Boolean = true
) : BaseState

sealed interface AppShellAction {
    data object Observe : AppShellAction
}

sealed interface AppShellEvent : ViewEvent
