package com.mikeisesele.clearr.ui.feature.settings

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
)
