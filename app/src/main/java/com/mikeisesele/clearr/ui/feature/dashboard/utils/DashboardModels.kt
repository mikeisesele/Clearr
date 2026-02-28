package com.mikeisesele.clearr.ui.feature.dashboard.utils

import androidx.compose.ui.graphics.Color
import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
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
    val visibleTiles: List<DashboardTrackerType>,
    val hasTrackers: Boolean
)

data class DashboardClearanceScore(
    val overall: Int,
    val budget: DashboardTrackerHealth,
    val goals: DashboardTrackerHealth,
    val dues: DashboardTrackerHealth,
    val todos: DashboardTrackerHealth
)

data class DashboardTrackerHealth(
    val trackerType: DashboardTrackerType,
    val percent: Int,
    val detail: String,
    val statusLabel: String
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
    DUES("Remit", "₦", TrackerType.DUES.brandColor()),
    TODOS("Todos", "☑", TrackerType.TODO.brandColor())
}

internal fun List<TrackerSummary>.toDashboardUiModel(today: LocalDate = LocalDate.now()): DashboardUiModel {
    val prioritized = ClearrEdgeAi.prioritizeTrackers(this)
    val budgetSummary = prioritized.primarySummaryOf(TrackerType.BUDGET)
    val goalsSummary = prioritized.primarySummaryOf(TrackerType.GOALS)
    val todoSummary = prioritized.primarySummaryOf(TrackerType.TODO)
    val remittanceSummary = prioritized
        .filter { it.type == TrackerType.DUES || it.type == TrackerType.EXPENSES }
        .mergeRemittanceSummary()

    val budgetHealth = budgetSummary.toBudgetHealth()
    val goalsHealth = goalsSummary.toCountHealth(DashboardTrackerType.GOALS)
    val todosHealth = todoSummary.toCountHealth(DashboardTrackerType.TODOS)
    val remitHealth = remittanceSummary.toRemittanceHealth()

    val score = DashboardClearanceScore(
        overall = calculateOverall(
            budget = budgetHealth.percent,
            dues = remitHealth.percent,
            goals = goalsHealth.percent,
            todos = todosHealth.percent
        ),
        budget = budgetHealth,
        goals = goalsHealth,
        dues = remitHealth,
        todos = todosHealth
    )

    val visibleTiles = buildList {
        if (budgetSummary.hasBudgetData()) add(DashboardTrackerType.BUDGET)
        if (goalsSummary.hasCountData()) add(DashboardTrackerType.GOALS)
        if (remittanceSummary.hasRemittanceData()) add(DashboardTrackerType.DUES)
        if (todoSummary.hasCountData()) add(DashboardTrackerType.TODOS)
    }

    val urgencyItems = buildList {
        if (budgetSummary != null) {
            when {
                budgetSummary.amountTargetKobo > 0L && budgetSummary.amountCompletedKobo == 0L -> add(
                    DashboardUrgencyItem(
                        id = "budget-empty",
                        message = "Budget needs attention this period",
                        severity = DashboardUrgencySeverity.CRITICAL,
                        trackerType = DashboardTrackerType.BUDGET,
                        actionLabel = "Log spend"
                    )
                )

                budgetHealth.percent < 35 && budgetSummary.amountTargetKobo > 0L -> add(
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

        if (goalsSummary != null && goalsSummary.totalMembers > 0 && goalsSummary.completedCount == 0) {
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

        if (todoSummary != null && todoSummary.totalMembers > 0) {
            val remaining = (todoSummary.totalMembers - todoSummary.completedCount).coerceAtLeast(0)
            when {
                todoSummary.completedCount == 0 -> add(
                    DashboardUrgencyItem(
                        id = "todos-pending",
                        message = "$remaining todos still open",
                        severity = DashboardUrgencySeverity.CRITICAL,
                        trackerType = DashboardTrackerType.TODOS,
                        actionLabel = "Mark done"
                    )
                )

                todoSummary.completionPercent < 50 -> add(
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

        if (remittanceSummary != null) {
            val remaining = (remittanceSummary.totalMembers - remittanceSummary.completedCount).coerceAtLeast(0)
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
        periodLabel = budgetSummary?.currentPeriodLabel
            ?: remittanceSummary?.currentPeriodLabel
            ?: currentMonthLabel(today),
        daysLabel = periodContextLabel(today),
        score = score,
        urgencyItems = urgencyItems,
        visibleTiles = visibleTiles,
        hasTrackers = isNotEmpty()
    )
}

private fun TrackerSummary?.hasBudgetData(): Boolean =
    this != null && (amountTargetKobo > 0L || amountCompletedKobo > 0L)

private fun TrackerSummary?.hasCountData(): Boolean =
    this != null && totalMembers > 0

private fun TrackerSummary?.hasRemittanceData(): Boolean =
    this != null && (totalMembers > 0 || amountTargetKobo > 0L || amountCompletedKobo > 0L)

private fun TrackerSummary?.toBudgetHealth(): DashboardTrackerHealth {
    val spent = this?.amountCompletedKobo ?: 0L
    val planned = this?.amountTargetKobo ?: 0L
    val percent = when {
        planned > 0L -> ((spent.toDouble() / planned) * 100).roundToInt().coerceIn(0, 100)
        else -> 0
    }
    return DashboardTrackerHealth(
        trackerType = DashboardTrackerType.BUDGET,
        percent = percent,
        detail = "${formatCompactKobo(spent)} / ${formatCompactKobo(planned)} spent",
        statusLabel = percent.toHealthLabel()
    )
}

private fun TrackerSummary?.toRemittanceHealth(): DashboardTrackerHealth {
    val collected = this?.amountCompletedKobo ?: 0L
    val target = this?.amountTargetKobo ?: 0L
    val percent = when {
        target > 0L -> ((collected.toDouble() / target) * 100).roundToInt().coerceIn(0, 100)
        else -> 0
    }
    return DashboardTrackerHealth(
        trackerType = DashboardTrackerType.DUES,
        percent = percent,
        detail = "${formatCompactKobo(collected)} / ${formatCompactKobo(target)} collected",
        statusLabel = percent.toHealthLabel()
    )
}

private fun TrackerSummary?.toCountHealth(type: DashboardTrackerType): DashboardTrackerHealth {
    val completed = this?.completedCount ?: 0
    val total = this?.totalMembers ?: 0
    val percent = this?.completionPercent ?: 0
    return DashboardTrackerHealth(
        trackerType = type,
        percent = percent,
        detail = "$completed / $total done",
        statusLabel = percent.toHealthLabel()
    )
}

private fun List<TrackerSummary>.mergeRemittanceSummary(): TrackerSummary? {
    if (isEmpty()) return null
    val totalMembers = sumOf { it.totalMembers }
    val completedCount = sumOf { it.completedCount }
    val amountCompletedKobo = sumOf { it.amountCompletedKobo }
    val amountTargetKobo = sumOf { it.amountTargetKobo }
    val completionPercent = when {
        amountTargetKobo > 0L -> ((amountCompletedKobo.toDouble() / amountTargetKobo) * 100).roundToInt().coerceIn(0, 100)
        totalMembers > 0 -> ((completedCount.toDouble() / totalMembers) * 100).roundToInt().coerceIn(0, 100)
        else -> 0
    }
    return TrackerSummary(
        trackerId = first().trackerId,
        name = first().name,
        type = TrackerType.DUES,
        frequency = first().frequency,
        currentPeriodLabel = first().currentPeriodLabel,
        totalMembers = totalMembers,
        completedCount = completedCount,
        completionPercent = completionPercent,
        amountCompletedKobo = amountCompletedKobo,
        amountTargetKobo = amountTargetKobo,
        isNew = any { it.isNew },
        createdAt = maxOf { it.createdAt }
    )
}

private fun Int.toHealthLabel(): String = when {
    this == 0 -> "Not started"
    this < 35 -> "Needs work"
    this < 70 -> "In progress"
    this < 90 -> "Looking good"
    else -> "Cleared ✓"
}

private fun formatCompactKobo(kobo: Long): String {
    val naira = kobo / 100.0
    return when {
        naira >= 1_000_000 -> "₦" + "%.1f".format(naira / 1_000_000).trimEnd('0').trimEnd('.') + "M"
        naira >= 100_000 -> "₦" + "%.0f".format(naira / 1_000) + "k"
        naira >= 10_000 -> "₦" + "%.1f".format(naira / 1_000).trimEnd('0').trimEnd('.') + "k"
        else -> "₦" + "%,d".format(naira.toLong())
    }
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
    daysLabel = "1 day left",
    score = DashboardClearanceScore(
        overall = 38,
        budget = DashboardTrackerHealth(
            trackerType = DashboardTrackerType.BUDGET,
            percent = 0,
            detail = "₦0 / ₦170k spent",
            statusLabel = "Not started"
        ),
        goals = DashboardTrackerHealth(
            trackerType = DashboardTrackerType.GOALS,
            percent = 100,
            detail = "4 / 4 done",
            statusLabel = "Cleared ✓"
        ),
        dues = DashboardTrackerHealth(
            trackerType = DashboardTrackerType.DUES,
            percent = 0,
            detail = "₦0 / ₦50k collected",
            statusLabel = "Not started"
        ),
        todos = DashboardTrackerHealth(
            trackerType = DashboardTrackerType.TODOS,
            percent = 50,
            detail = "1 / 2 done",
            statusLabel = "In progress"
        )
    ),
    urgencyItems = listOf(
        DashboardUrgencyItem(
            id = "1",
            message = "Budget needs attention this period",
            severity = DashboardUrgencySeverity.CRITICAL,
            trackerType = DashboardTrackerType.BUDGET,
            actionLabel = "Log spend"
        )
    ),
    visibleTiles = listOf(
        DashboardTrackerType.BUDGET,
        DashboardTrackerType.GOALS,
        DashboardTrackerType.DUES,
        DashboardTrackerType.TODOS
    ),
    hasTrackers = true
)

internal val previewEmptyDashboardUi = DashboardUiModel(
    periodLabel = "Feb 2026",
    daysLabel = "3 days left",
    score = DashboardClearanceScore(
        overall = 0,
        budget = DashboardTrackerHealth(DashboardTrackerType.BUDGET, 0, "₦0 / ₦0 spent", "Not started"),
        goals = DashboardTrackerHealth(DashboardTrackerType.GOALS, 0, "0 / 0 done", "Not started"),
        dues = DashboardTrackerHealth(DashboardTrackerType.DUES, 0, "₦0 / ₦0 collected", "Not started"),
        todos = DashboardTrackerHealth(DashboardTrackerType.TODOS, 0, "0 / 0 done", "Not started")
    ),
    urgencyItems = emptyList(),
    visibleTiles = emptyList(),
    hasTrackers = false
)

internal fun DashboardUrgencySeverity.toColor(baseText: Color, warning: Color, critical: Color, info: Color): Color = when (this) {
    DashboardUrgencySeverity.CRITICAL -> critical
    DashboardUrgencySeverity.WARNING -> warning
    DashboardUrgencySeverity.INFO -> info
}

internal fun List<TrackerSummary>.primarySummaryOf(type: TrackerType): TrackerSummary? =
    filter { it.type == type }
        .sortedWith(
            compareByDescending<TrackerSummary> { it.totalMembers > 0 }
                .thenByDescending { it.amountTargetKobo > 0L || it.amountCompletedKobo > 0L }
                .thenByDescending { it.totalMembers }
                .thenByDescending { it.amountTargetKobo }
                .thenByDescending { it.completedCount }
                .thenByDescending { it.createdAt }
        )
        .firstOrNull()

internal fun DashboardTrackerType.surfaceTint(): Color = when (this) {
    DashboardTrackerType.BUDGET -> ClearrColors.BlueSurface
    DashboardTrackerType.GOALS -> ClearrColors.EmeraldSurface
    DashboardTrackerType.DUES -> ClearrColors.VioletSurface
    DashboardTrackerType.TODOS -> ClearrColors.AmberSurface
}
