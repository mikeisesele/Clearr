package com.mikeisesele.clearr.ui.feature.setup

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.TrackerType

data class SetupWizardState(
    val step: Int = 0,                      // 0–5 (entry + 5 setup steps)
    val groupName: String = "JSS Durumi Brothers",
    val trackerName: String = "Dues Tracker",
    val adminName: String = "",
    val adminPhone: String = "",
    val trackerType: TrackerType = TrackerType.DUES,
    val frequency: Frequency = Frequency.MONTHLY,
    val defaultAmount: String = "5000",
    val layoutStyle: LayoutStyle = LayoutStyle.GRID,
    val loadSampleMembers: Boolean = true,
    val isSaving: Boolean = false
) : BaseState

sealed interface SetupAction {
    data object NextStep : SetupAction
    data object PrevStep : SetupAction
    data class SetGroupName(val value: String) : SetupAction
    data class SetTrackerName(val value: String) : SetupAction
    data class SetAdminName(val value: String) : SetupAction
    data class SetAdminPhone(val value: String) : SetupAction
    data class SetTrackerType(val value: TrackerType) : SetupAction
    data class SetFrequency(val value: Frequency) : SetupAction
    data class SetDefaultAmount(val value: String) : SetupAction
    data class SetLayoutStyle(val value: LayoutStyle) : SetupAction
    data class SetLoadSampleMembers(val value: Boolean) : SetupAction
    data class GoToStep(val step: Int) : SetupAction
    data class FinishSetup(val onDone: () -> Unit) : SetupAction
    data class LoadExistingConfig(val config: AppConfig) : SetupAction
}

sealed interface SetupEvent : ViewEvent
