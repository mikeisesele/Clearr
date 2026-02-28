package com.mikeisesele.clearr.ui.feature.dashboard.utils

import androidx.compose.ui.graphics.Color
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.brandColor
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

data class DashboardUiModel(
    val periodLabel: String,
    val daysLabel: String,
    val score: DashboardClearanceScore,
    val urgencyItems: List<DashboardUrgencyItem>,
    val hasTrackers: Boolean
)

data class DashboardClearanceScore(
    val overall: Int,
    val budget: Int,
    val goals: Int,
    val dues: Int,
    val todos: Int
)

data class DashboardUrgencyItem(
    val id: String,
    val message: String,
    val severity: DashboardUrgencySeverity,
    val trackerType: DashboardTrackerType,
    val actionLabel: String
)

enum class DashboardUrgencySeverity {
    CRITICAL,
    WARNING,
    INFO
}

enum class DashboardTrackerType(
    val label: String,
    val icon: String,
    val accentColor: Color
) {
    BUDGET("Budget", "💳", TrackerType.BUDGET.brandColor()),
    GOALS("Goals", "🎯", TrackerType.GOALS.brandColor()),
    DUES("Remittance", "₦", TrackerType.DUES.brandColor()),
    TODOS("Todos", "☑", TrackerType.TODO.brandColor())
}

internal fun List<TrackerSummary>.toDashboardUiModel(today: LocalDate = LocalDate.now()): DashboardUiModel {
    val budget = firstOrNull { it.type == TrackerType.BUDGET }
    val goals = firstOrNull { it.type == TrackerType.GOALS }
    val todos = firstOrNull { it.type == TrackerType.TODO }
    val remittances = filter { it.type == TrackerType.DUES || it.type == TrackerType.EXPENSES }

    val duesScore = if (remittances.isEmpty()) 0 else {
        val completed = remittances.sumOf { it.completedCount }
        val total = remittances.sumOf { it.totalMembers }
        if (total > 0) ((completed.toDouble() / total) * 100).roundToInt().coerceIn(0, 100) else 0
    }

    val score = DashboardClearanceScore(
        budget = budget?.completionPercent ?: 0,
        goals = goals?.completionPercent ?: 0,
        dues = duesScore,
        todos = todos?.completionPercent ?: 0,
        overall = calculateOverall(
            budget = budget?.completionPercent ?: 0,
            dues = duesScore,
            goals = goals?.completionPercent ?: 0,
            todos = todos?.completionPercent ?: 0
        )
    )

    val urgencyItems = buildList {
        if (budget != null) {
            when {
                budget.totalMembers > 0 && budget.completedCount == 0 -> add(
                    DashboardUrgencyItem(
                        id = "budget-empty",
                        message = "Budget needs attention this period",
                        severity = DashboardUrgencySeverity.CRITICAL,
                        trackerType = DashboardTrackerType.BUDGET,
                        actionLabel = "Log spend"
                    )
                )
                budget.totalMembers > 0 && budget.completionPercent < 35 -> add(
                    DashboardUrgencyItem(
                        id = "budget-low",
                        message = "Budget progress is behind this period",
                        severity = DashboardUrgencySeverity.WARNING,
                        trackerType = DashboardTrackerType.BUDGET,
                        actionLabel = "Log spend"
                    )
                )
            }
        }

        if (goals != null && goals.totalMembers > 0 && goals.completedCount == 0) {
            add(
                DashboardUrgencyItem(
                    id = "goals-idle",
                    message = "No goals logged this period",
                    severity = DashboardUrgencySeverity.WARNING,
                    trackerType = DashboardTrackerType.GOALS,
                    actionLabel = "Mark done"
                )
            )
        }

        if (todos != null && todos.totalMembers > 0) {
            val remaining = (todos.totalMembers - todos.completedCount).coerceAtLeast(0)
            when {
                todos.completedCount == 0 -> add(
                    DashboardUrgencyItem(
                        id = "todos-pending",
                        message = "$remaining todos still open",
                        severity = DashboardUrgencySeverity.CRITICAL,
                        trackerType = DashboardTrackerType.TODOS,
                        actionLabel = "Mark done"
                    )
                )
                todos.completionPercent < 50 -> add(
                    DashboardUrgencyItem(
                        id = "todos-low",
                        message = "$remaining todos still need attention",
                        severity = DashboardUrgencySeverity.WARNING,
                        trackerType = DashboardTrackerType.TODOS,
                        actionLabel = "Review todos"
                    )
                )
            }
        }

        if (remittances.isNotEmpty()) {
            val total = remittances.sumOf { it.totalMembers }
            val completed = remittances.sumOf { it.completedCount }
            val remaining = (total - completed).coerceAtLeast(0)
            if (remaining > 0) {
                add(
                    DashboardUrgencyItem(
                        id = "remittance-open",
                        message = "$remaining remittance records still uncleared",
                        severity = if (remaining >= 3) DashboardUrgencySeverity.CRITICAL else DashboardUrgencySeverity.WARNING,
                        trackerType = DashboardTrackerType.DUES,
                        actionLabel = "Record"
                    )
                )
            }
        }

        if (score.overall >= 75) {
            add(
                DashboardUrgencyItem(
                    id = "score-positive",
                    message = "Clearance score is improving",
                    severity = DashboardUrgencySeverity.INFO,
                    trackerType = DashboardTrackerType.GOALS,
                    actionLabel = "Keep going"
                )
            )
        }
    }
        .sortedWith(compareBy<DashboardUrgencyItem> { it.severity.rank }.thenBy { it.message })
        .take(5)

    return DashboardUiModel(
        periodLabel = budget?.currentPeriodLabel ?: currentMonthLabel(today),
        daysLabel = periodContextLabel(today),
        score = score,
        urgencyItems = urgencyItems,
        hasTrackers = isNotEmpty()
    )
}

private val DashboardUrgencySeverity.rank: Int
    get() = when (this) {
        DashboardUrgencySeverity.CRITICAL -> 0
        DashboardUrgencySeverity.WARNING -> 1
        DashboardUrgencySeverity.INFO -> 2
    }

private fun calculateOverall(
    budget: Int,
    dues: Int,
    goals: Int,
    todos: Int
): Int = (
    budget * 0.35 +
        dues * 0.30 +
        goals * 0.20 +
        todos * 0.15
    ).roundToInt().coerceIn(0, 100)

private fun currentMonthLabel(today: LocalDate): String =
    today.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault()))

private fun periodContextLabel(today: LocalDate): String {
    val month = YearMonth.from(today)
    val remaining = month.lengthOfMonth() - today.dayOfMonth
    return when {
        remaining <= 0 -> "Last day"
        remaining == 1 -> "1 day left"
        else -> "$remaining days left"
    }
}

internal val previewDashboardUi = DashboardUiModel(
    periodLabel = "Feb 2026",
    daysLabel = "3 days left",
    score = DashboardClearanceScore(
        overall = 69,
        budget = 62,
        goals = 80,
        dues = 90,
        todos = 65
    ),
    urgencyItems = listOf(
        DashboardUrgencyItem(
            id = "1",
            message = "Budget needs attention this period",
            severity = DashboardUrgencySeverity.CRITICAL,
            trackerType = DashboardTrackerType.BUDGET,
            actionLabel = "Log spend"
        ),
        DashboardUrgencyItem(
            id = "2",
            message = "No goals logged this period",
            severity = DashboardUrgencySeverity.WARNING,
            trackerType = DashboardTrackerType.GOALS,
            actionLabel = "Mark done"
        ),
        DashboardUrgencyItem(
            id = "3",
            message = "4 remittance records still uncleared",
            severity = DashboardUrgencySeverity.WARNING,
            trackerType = DashboardTrackerType.DUES,
            actionLabel = "Record"
        )
    ),
    hasTrackers = true
)

internal val previewEmptyDashboardUi = DashboardUiModel(
    periodLabel = "Feb 2026",
    daysLabel = "3 days left",
    score = DashboardClearanceScore(0, 0, 0, 0, 0),
    urgencyItems = emptyList(),
    hasTrackers = false
)

internal fun DashboardUrgencySeverity.toColor(baseText: Color, warning: Color, critical: Color, info: Color): Color = when (this) {
    DashboardUrgencySeverity.CRITICAL -> critical
    DashboardUrgencySeverity.WARNING -> warning
    DashboardUrgencySeverity.INFO -> info
}

internal fun DashboardTrackerType.surfaceTint(): Color = when (this) {
    DashboardTrackerType.BUDGET -> ClearrColors.BlueSurface
    DashboardTrackerType.GOALS -> ClearrColors.EmeraldSurface
    DashboardTrackerType.DUES -> ClearrColors.VioletSurface
    DashboardTrackerType.TODOS -> ClearrColors.AmberSurface
}
