package com.mikeisesele.clearr.ui.feature.dashboard.utils

import androidx.compose.ui.graphics.Color
import com.mikeisesele.clearr.ui.theme.ClearrColors

val previewDashboardUi = DashboardUiModel(
    periodLabel = "Mar 2026",
    daysLabel = "31 days left",
    score = DashboardClearanceScore(
        overall = 52,
        budget = DashboardTrackerHealth(DashboardTrackerType.BUDGET, 41, "₦63.8k / ₦155k spent", "In progress"),
        goals = DashboardTrackerHealth(DashboardTrackerType.GOALS, 50, "1 / 2 done", "In progress"),
        todos = DashboardTrackerHealth(DashboardTrackerType.TODOS, 33, "1 / 3 done", "Needs work")
    ),
    urgencyItems = listOf(
        DashboardUrgencyItem("todo-open", "2 todos still open", DashboardUrgencySeverity.CRITICAL, DashboardTrackerType.TODOS, "Review todos")
    ),
    visibleTiles = listOf(DashboardTrackerType.BUDGET, DashboardTrackerType.GOALS, DashboardTrackerType.TODOS),
    hasTrackers = true
)

val previewEmptyDashboardUi = DashboardUiModel(
    periodLabel = "Mar 2026",
    daysLabel = "31 days left",
    score = DashboardClearanceScore(
        overall = 0,
        budget = DashboardTrackerHealth(DashboardTrackerType.BUDGET, 0, "₦0 / ₦0 spent", "Not started"),
        goals = DashboardTrackerHealth(DashboardTrackerType.GOALS, 0, "0 / 0 done", "Not started"),
        todos = DashboardTrackerHealth(DashboardTrackerType.TODOS, 0, "0 / 0 done", "Not started")
    ),
    urgencyItems = emptyList(),
    visibleTiles = emptyList(),
    hasTrackers = false
)

val DashboardTrackerType.accentColor: Color
    get() = when (this) {
        DashboardTrackerType.BUDGET -> ClearrColors.Blue
        DashboardTrackerType.GOALS -> ClearrColors.Emerald
        DashboardTrackerType.TODOS -> ClearrColors.Amber
    }

fun DashboardTrackerType.backgroundColor(): Color = when (this) {
    DashboardTrackerType.BUDGET -> ClearrColors.BlueBg
    DashboardTrackerType.GOALS -> ClearrColors.EmeraldBg
    DashboardTrackerType.TODOS -> ClearrColors.AmberBg
}
