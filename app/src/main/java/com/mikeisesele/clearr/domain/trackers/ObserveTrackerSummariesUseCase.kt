package com.mikeisesele.clearr.domain.trackers

import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.GoalPeriodKey
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.data.model.TrackerPeriod
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.data.model.TodoStatus
import com.mikeisesele.clearr.data.model.derivedStatus
import com.mikeisesele.clearr.domain.repository.DuesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ObserveTrackerSummariesUseCase @Inject constructor(
    private val repository: DuesRepository
) {
    operator fun invoke(): Flow<List<TrackerSummary>> =
        repository.getAllTrackers().flatMapLatest { trackers ->
            if (trackers.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(trackers.map(::summaryFlow)) { summaries -> summaries.toList() }
            }
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
                                            val periodPlans = plans.filter { it.periodId == latestPeriod?.id }
                                                .associateBy { it.categoryId }
                                            val clearedCount = categories.count { category ->
                                                val spent = periodEntries
                                                    .asSequence()
                                                    .filter { it.categoryId == category.id }
                                                    .sumOf { it.amountKobo }
                                                val planned = periodPlans[category.id]?.plannedAmountKobo
                                                    ?: category.plannedAmountKobo
                                                planned > 0L && spent >= planned
                                            }
                                            TrackerSummary(
                                                trackerId = tracker.id,
                                                name = tracker.name,
                                                type = tracker.type,
                                                frequency = tracker.frequency,
                                                currentPeriodLabel = latestPeriod?.label ?: currentPeriodLabel(tracker.frequency),
                                                totalMembers = categories.size,
                                                completedCount = clearedCount,
                                                completionPercent = if (categories.isNotEmpty()) {
                                                    ((clearedCount.toDouble() / categories.size) * 100).toInt().coerceIn(0, 100)
                                                } else {
                                                    0
                                                },
                                                isNew = tracker.isNew,
                                                createdAt = tracker.createdAt
                                            )
                                        }
                                }
                        }
                }
        }

        TrackerType.TODO -> repository.getTodosForTracker(tracker.id).map { todos ->
            val doneCount = todos.count { it.derivedStatus() == TodoStatus.DONE }
            TrackerSummary(
                trackerId = tracker.id,
                name = tracker.name,
                type = tracker.type,
                frequency = tracker.frequency,
                currentPeriodLabel = "Todo List",
                totalMembers = todos.size,
                completedCount = doneCount,
                completionPercent = if (todos.isNotEmpty()) {
                    ((doneCount.toDouble() / todos.size) * 100).toInt().coerceIn(0, 100)
                } else {
                    0
                },
                isNew = tracker.isNew,
                createdAt = tracker.createdAt
            )
        }

        TrackerType.GOALS -> repository.getGoalsForTracker(tracker.id)
            .flatMapLatest { goals ->
                repository.getGoalCompletionsForTracker(tracker.id)
                    .map { completions ->
                        val doneCount = goals.count { goal ->
                            val currentKey = GoalPeriodKey.currentKey(goal.frequency)
                            completions.any { it.goalId == goal.id && it.periodKey == currentKey }
                        }
                        TrackerSummary(
                            trackerId = tracker.id,
                            name = tracker.name,
                            type = tracker.type,
                            frequency = tracker.frequency,
                            currentPeriodLabel = "Today",
                            totalMembers = goals.size,
                            completedCount = doneCount,
                            completionPercent = if (goals.isNotEmpty()) {
                                ((doneCount.toDouble() / goals.size) * 100).toInt().coerceIn(0, 100)
                            } else {
                                0
                            },
                            isNew = tracker.isNew,
                            createdAt = tracker.createdAt
                        )
                    }
            }

        else -> repository.getActiveMembersForTracker(tracker.id)
            .flatMapLatest { members ->
                repository.getCurrentPeriodFlow(tracker.id)
                    .flatMapLatest { period ->
                        if (period == null) {
                            flowOf(buildSummary(tracker, members, period, 0))
                        } else {
                            repository.getRecordsForPeriod(tracker.id, period.id)
                                .map { records ->
                                    val completedCount = records.count { record ->
                                        record.status.name in completedStatuses(tracker.type)
                                    }
                                    buildSummary(tracker, members, period, completedCount)
                                }
                        }
                    }
            }
    }

    private fun buildSummary(
        tracker: Tracker,
        members: List<TrackerMember>,
        period: TrackerPeriod?,
        completedCount: Int
    ): TrackerSummary {
        val total = members.size
        val percent = if (total > 0) ((completedCount.toDouble() / total) * 100).toInt().coerceIn(0, 100) else 0
        return TrackerSummary(
            trackerId = tracker.id,
            name = tracker.name,
            type = tracker.type,
            frequency = tracker.frequency,
            currentPeriodLabel = period?.label ?: currentPeriodLabel(tracker.frequency),
            totalMembers = total,
            completedCount = completedCount,
            completionPercent = percent,
            isNew = tracker.isNew,
            createdAt = tracker.createdAt
        )
    }

    private fun completedStatuses(type: TrackerType): Set<String> = when (type) {
        TrackerType.DUES -> setOf("PAID")
        TrackerType.EXPENSES -> setOf("PAID")
        TrackerType.GOALS -> setOf("DONE")
        TrackerType.TODO -> setOf("DONE")
        TrackerType.BUDGET -> emptySet()
    }

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
