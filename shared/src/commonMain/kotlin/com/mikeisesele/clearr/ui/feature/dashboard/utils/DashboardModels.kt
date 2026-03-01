package com.mikeisesele.clearr.ui.feature.dashboard.utils

import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
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
    val icon: String
) {
    BUDGET("Budget", "💳"),
    GOALS("Goals", "🎯"),
    TODOS("Todos", "☑")
}

fun List<TrackerSummary>.toDashboardUiModel(today: LocalDate = LocalDate.now()): DashboardUiModel {
    val prioritized = prioritizeTrackers(this)
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

fun List<TrackerSummary>.primarySummaryOf(type: TrackerType): TrackerSummary? =
    filter { it.type == type }.maxWithOrNull(
        compareBy<TrackerSummary> { it.totalMembers > 0 || it.amountTargetKobo > 0L || it.amountCompletedKobo > 0L }
            .thenBy { it.createdAt }
    )

private fun prioritizeTrackers(list: List<TrackerSummary>): List<TrackerSummary> {
    return list.sortedWith(
        compareByDescending<TrackerSummary> { urgencyScore(it) }
            .thenByDescending { it.createdAt }
    )
}

private fun urgencyScore(summary: TrackerSummary): Int {
    val incomplete = max(summary.totalMembers - summary.completedCount, 0)
    val incompleteScore = if (summary.totalMembers > 0) (100 - summary.completionPercent) else 20
    val typeBias = when (summary.type) {
        TrackerType.TODO -> 25
        TrackerType.BUDGET -> 20
        TrackerType.GOALS -> 10
    }
    val newBoost = if (summary.isNew) 5 else 0
    val loadScore = minOf(incomplete * 2, 30)
    return incompleteScore + typeBias + newBoost + loadScore
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
