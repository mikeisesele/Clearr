package com.mikeisesele.clearr.domain.trackers

import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.GoalPeriodKey
import com.mikeisesele.clearr.data.model.TodoStatus
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.data.model.derivedStatus
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveTrackerSummariesUseCase(
    private val repository: ClearrRepository
) {
    operator fun invoke(): Flow<List<TrackerSummary>> =
        repository.getAllTrackers().flatMapLatest { trackers ->
            if (trackers.isEmpty()) flowOf(emptyList())
            else combine(trackers.map(::summaryFlow)) { it.toList() }
        }

    private fun summaryFlow(tracker: Tracker): Flow<TrackerSummary> = when (tracker.type) {
        TrackerType.BUDGET -> {
            val budgetFrequency = when (tracker.frequency) {
                Frequency.WEEKLY -> BudgetFrequency.WEEKLY
                else -> BudgetFrequency.MONTHLY
            }
            repository.getBudgetPeriods(tracker.id, budgetFrequency)
                .flatMapLatest { periods ->
                    repository.getBudgetCategories(tracker.id, budgetFrequency)
                        .flatMapLatest { categories ->
                            repository.getBudgetEntriesForTracker(tracker.id)
                                .flatMapLatest { entries ->
                                    repository.getBudgetCategoryPlansForTracker(tracker.id)
                                        .map { plans ->
                                            val latestPeriod = periods.lastOrNull()
                                            val periodEntries = entries.filter { it.periodId == latestPeriod?.id }
                                            val periodPlans = plans.filter { it.periodId == latestPeriod?.id }.associateBy { it.categoryId }
                                            val totalPlannedKobo = categories.sumOf { category ->
                                                periodPlans[category.id]?.plannedAmountKobo ?: category.plannedAmountKobo
                                            }
                                            val totalSpentKobo = periodEntries.sumOf { it.amountKobo }
                                            val clearedCount = categories.count { category ->
                                                val spent = periodEntries.filter { it.categoryId == category.id }.sumOf { it.amountKobo }
                                                val planned = periodPlans[category.id]?.plannedAmountKobo ?: category.plannedAmountKobo
                                                planned > 0L && spent >= planned
                                            }
                                            tracker.toSummary(
                                                currentPeriodLabel = latestPeriod?.label ?: currentPeriodLabel(tracker.frequency),
                                                total = categories.size,
                                                completed = clearedCount,
                                                amountCompletedKobo = totalSpentKobo,
                                                amountTargetKobo = totalPlannedKobo
                                            )
                                        }
                                }
                        }
                }
        }

        TrackerType.TODO -> repository.getTodosForTracker(tracker.id).map { todos ->
            val doneCount = todos.count { it.derivedStatus() == TodoStatus.DONE }
            tracker.toSummary(
                currentPeriodLabel = "Todo List",
                total = todos.size,
                completed = doneCount
            )
        }

        TrackerType.GOALS -> repository.getGoalsForTracker(tracker.id)
            .flatMapLatest { goals ->
                repository.getGoalCompletionsForTracker(tracker.id).map { completions ->
                    val doneCount = goals.count { goal ->
                        val currentKey = GoalPeriodKey.currentKey(goal.frequency)
                        completions.any { it.goalId == goal.id && it.periodKey == currentKey }
                    }
                    tracker.toSummary(
                        currentPeriodLabel = "Today",
                        total = goals.size,
                        completed = doneCount
                    )
                }
            }
    }

    private fun Tracker.toSummary(
        currentPeriodLabel: String,
        total: Int,
        completed: Int,
        amountCompletedKobo: Long = 0L,
        amountTargetKobo: Long = 0L
    ) = TrackerSummary(
        trackerId = id,
        name = name,
        type = type,
        frequency = frequency,
        currentPeriodLabel = currentPeriodLabel,
        totalMembers = total,
        completedCount = completed,
        completionPercent = if (total > 0) ((completed.toDouble() / total) * 100).toInt().coerceIn(0, 100) else 0,
        amountCompletedKobo = amountCompletedKobo,
        amountTargetKobo = amountTargetKobo,
        isNew = isNew,
        createdAt = createdAt
    )

    private fun currentPeriodLabel(frequency: Frequency): String {
        val calendar = Calendar.getInstance()
        return when (frequency) {
            Frequency.MONTHLY -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
            Frequency.WEEKLY -> "Week ${calendar.get(Calendar.WEEK_OF_YEAR)}, ${calendar.get(Calendar.YEAR)}"
            Frequency.QUARTERLY -> "Q${(calendar.get(Calendar.MONTH) / 3) + 1} ${calendar.get(Calendar.YEAR)}"
            Frequency.TERMLY -> "Term ${(calendar.get(Calendar.MONTH) / 4) + 1} ${calendar.get(Calendar.YEAR)}"
            Frequency.BIANNUAL -> "H${if (calendar.get(Calendar.MONTH) < 6) 1 else 2} ${calendar.get(Calendar.YEAR)}"
            Frequency.ANNUAL -> "${calendar.get(Calendar.YEAR)}"
            Frequency.CUSTOM -> "Current Period"
        }
    }
}
