package com.mikeisesele.clearr.ui.feature.settings

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.data.model.YearConfig
import com.mikeisesele.clearr.ui.commons.state.ThemeMode

data class SettingsUiState(
    val selectedYear: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
    val allMembers: List<Member> = emptyList(),
    val yearConfigs: List<YearConfig> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val layoutStyle: LayoutStyle = LayoutStyle.GRID,
    val currentTrackerType: TrackerType? = null,
    val currentTrackerDueAmount: Double? = null
) : BaseState

sealed interface SettingsAction {
    data class SelectYear(val year: Int) : SettingsAction
    data class SetThemeMode(val mode: ThemeMode) : SettingsAction
    data class UpdateDueAmount(val year: Int, val amount: Double) : SettingsAction
    data class SetMemberArchived(val id: Long, val archived: Boolean) : SettingsAction
    data class StartNewYear(val fromYear: Int) : SettingsAction
    data class SetLayoutStyle(val style: LayoutStyle) : SettingsAction
    data object ResetSetup : SettingsAction
}

sealed interface SettingsEvent : ViewEvent
