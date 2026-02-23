package com.mikeisesele.clearr.ui.feature.setup

import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.TrackerType

data class SetupWizardState(
    val step: Int = 0,                      // 0–6 (7 steps total)
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
)
