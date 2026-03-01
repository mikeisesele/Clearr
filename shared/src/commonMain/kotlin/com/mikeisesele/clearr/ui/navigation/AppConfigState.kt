package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.AppConfig

data class AppConfigUiState(
    val appConfig: AppConfig? = null,
    val isLoading: Boolean = true
) : BaseState

sealed interface AppConfigAction {
    data object Observe : AppConfigAction
}

sealed interface AppConfigEvent : ViewEvent
