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

enum class DashboardUrgencySeverity { CRITICAL, WARNING, INFO }

enum class DashboardTrackerType(
    val label: String,
    val icon: String,
    val accentColor: Color
) {
    BUDGET("Budget", "💳", TrackerType.BUDGET.brandColor()),
    GOALS("Goals", "🎯", TrackerType.GOALS.brandColor()),
    TODOS("Todos", "☑", TrackerType.TODO.brandColor())
}

internal fun List<TrackerSummary>.toDashboardUiModel(today: LocalDate = LocalDate.now()): DashboardUiModel {
    val prioritized = ClearrEdgeAi.prioritizeTrackers(this)
    val budgetSummary = prioritized.primarySummaryOf(TrackerType.BUDGET)
    val goalsSummary = prioritized.primarySummaryOf(TrackerType.GOALS)
    val todoSummary = prioritized.primarySummaryOf(TrackerType.TODO)

    val budgetHealth = budgetSummary.toBudgetHealth()
    val goalsHealth = goalsSummary.toCountHealth(DashboardTrackerType.GOALS)
    val todosHealth = todoSummary.toCountHealth(DashboardTrackerType.TODOS)

    val visibleTiles = buildList {
        if (budgetSummary.hasBudgetData()) add(DashboardTrackerType.BUDGET)
        if (goalsSummary.hasCountData()) add(DashboardTrackerType.GOALS)
        if (todoSummary.hasCountData()) add(DashboardTrackerType.TODOS)
    }

    val score = DashboardClearanceScore(
        overall = calculateOverall(
            budget = budgetHealth.percent,
            goals = goalsHealth.percent,
            todos = todosHealth.percent
        ),
        budget = budgetHealth,
        goals = goalsHealth,
        todos = todosHealth
    )

    val urgencyItems = buildList {
        if (budgetSummary != null) {
            when {
                budgetSummary.hasBudgetData() && budgetSummary.amountTargetKobo > 0L && budgetSummary.amountCompletedKobo == 0L -> add(
                    DashboardUrgencyItem(
                        id = "budget-empty",
                        message = "Budget has no logged spend yet",
                        severity = DashboardUrgencySeverity.WARNING,
                        trackerType = DashboardTrackerType.BUDGET,
                        actionLabel = "Log spend"
                    )
                )
                budgetHealth.percent < 35 && budgetSummary.amountTargetKobo > 0L -> add(
                    DashboardUrgencyItem(
                        id = "budget-low",
                        message = "Budget needs attention this period",
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
                        actionLabel = "Review todos"
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

        if (score.overall >= 75 && visibleTiles.isNotEmpty()) {
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
    }.sortedWith(compareBy<DashboardUrgencyItem> { it.severity.rank }.thenBy { it.message }).take(5)

    return DashboardUiModel(
        periodLabel = currentMonthLabel(today),
        daysLabel = periodContextLabel(today),
        score = score,
        urgencyItems = urgencyItems,
        visibleTiles = visibleTiles,
        hasTrackers = visibleTiles.isNotEmpty()
    )
}

private fun TrackerSummary?.hasBudgetData(): Boolean = this != null && (amountTargetKobo > 0L || amountCompletedKobo > 0L)
private fun TrackerSummary?.hasCountData(): Boolean = this != null && totalMembers > 0

private fun TrackerSummary?.toBudgetHealth(): DashboardTrackerHealth {
    val spent = this?.amountCompletedKobo ?: 0L
    val planned = this?.amountTargetKobo ?: 0L
    val percent = if (planned > 0L) ((spent.toDouble() / planned) * 100).roundToInt().coerceIn(0, 100) else 0
    return DashboardTrackerHealth(
        trackerType = DashboardTrackerType.BUDGET,
        percent = percent,
        detail = "${formatCompactKobo(spent)} / ${formatCompactKobo(planned)} spent",
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

internal fun List<TrackerSummary>.primarySummaryOf(type: TrackerType): TrackerSummary? =
    filter { it.type == type }.maxWithOrNull(
        compareBy<TrackerSummary> { it.totalMembers > 0 || it.amountTargetKobo > 0L || it.amountCompletedKobo > 0L }
            .thenBy { it.createdAt }
    )

private val DashboardUrgencySeverity.rank: Int
    get() = when (this) {
        DashboardUrgencySeverity.CRITICAL -> 0
        DashboardUrgencySeverity.WARNING -> 1
        DashboardUrgencySeverity.INFO -> 2
    }

private fun calculateOverall(budget: Int, goals: Int, todos: Int): Int =
    (budget * 0.45 + goals * 0.30 + todos * 0.25).roundToInt().coerceIn(0, 100)

private fun Int.toHealthLabel(): String = when {
    this >= 90 -> "Cleared"
    this >= 70 -> "Looking good"
    this >= 35 -> "In progress"
    this > 0 -> "Needs work"
    else -> "Not started"
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

private fun currentMonthLabel(today: LocalDate): String =
    today.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault()))

private fun periodContextLabel(today: LocalDate): String {
    val currentMonth = YearMonth.from(today)
    val remaining = currentMonth.lengthOfMonth() - today.dayOfMonth
    return when {
        remaining <= 0 -> "Last day"
        remaining == 1 -> "1 day left"
        else -> "$remaining days left"
    }
}

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

fun DashboardTrackerType.backgroundColor(): Color = when (this) {
    DashboardTrackerType.BUDGET -> ClearrColors.BlueBg
    DashboardTrackerType.GOALS -> ClearrColors.EmeraldBg
    DashboardTrackerType.TODOS -> ClearrColors.AmberBg
}
