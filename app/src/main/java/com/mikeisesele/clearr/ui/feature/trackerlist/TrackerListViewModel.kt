package com.mikeisesele.clearr.ui.feature.trackerlist

import com.mikeisesele.clearr.core.base.BaseViewModel
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TrackerListViewModel @Inject constructor(
    private val repository: DuesRepository
) : BaseViewModel<TrackerListUiState, TrackerListAction, TrackerListEvent>(
    initialState = TrackerListUiState(isLoading = true)
) {

    private val refreshSignal = MutableStateFlow(0)

    init {
        launch {
            ensureStaticTrackers()
            refreshSignal
                .flatMapLatest { repository.getAllTrackers() }
                .flatMapLatest { trackers ->
                    if (trackers.isEmpty()) {
                        flowOf(TrackerListUiState(summaries = emptyList(), isLoading = false))
                    } else {
                        val summaryFlows = trackers.map { tracker ->
                            if (tracker.type == TrackerType.BUDGET) {
                                val budgetFrequency = when (tracker.frequency) {
                                    Frequency.WEEKLY -> BudgetFrequency.WEEKLY
                                    else -> BudgetFrequency.MONTHLY
                                }
                                repository.getBudgetPeriods(tracker.id, budgetFrequency)
                                    .flatMapLatest { periods ->
                                        repository.getBudgetCategories(tracker.id, budgetFrequency)
                                            .flatMapLatest { categories ->
                                                repository.getBudgetEntriesForTracker(tracker.id)
                                                    .map { entries ->
                                                        val latestPeriod = periods.lastOrNull()
                                                        val periodEntries = entries.filter { it.periodId == latestPeriod?.id }
                                                        val clearedCount = categories.count { category ->
                                                            val spent = periodEntries
                                                                .asSequence()
                                                                .filter { it.categoryId == category.id }
                                                                .sumOf { it.amountKobo }
                                                            spent >= category.plannedAmountKobo
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
                            } else if (tracker.type == TrackerType.TODO) {
                                repository.getTodosForTracker(tracker.id).map { todos ->
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
                                        } else 0,
                                        isNew = tracker.isNew,
                                        createdAt = tracker.createdAt
                                    )
                                }
                            } else if (tracker.type == TrackerType.GOALS) {
                                repository.getGoalsForTracker(tracker.id)
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
                                                    } else 0,
                                                    isNew = tracker.isNew,
                                                    createdAt = tracker.createdAt
                                                )
                                            }
                                    }
                            } else {
                                repository.getActiveMembersForTracker(tracker.id)
                                    .flatMapLatest { members ->
                                        repository.getCurrentPeriodFlow(tracker.id)
                                            .flatMapLatest { period ->
                                                if (period == null) {
                                                    flowOf(buildSummary(tracker, members, period, 0))
                                                } else {
                                                    repository.getRecordsForPeriod(tracker.id, period.id)
                                                        .map { records ->
                                                            val completedCount = records.count { r ->
                                                                r.status.name in completedStatuses(tracker.type)
                                                            }
                                                            buildSummary(tracker, members, period, completedCount)
                                                        }
                                                }
                                            }
                                    }
                            }
                        }
                        combine(summaryFlows) { arr ->
                            TrackerListUiState(
                                summaries = arr.toList(),
                                isLoading = false
                            )
                        }
                    }
                }
                .collectLatest { newState -> updateState { newState } }
        }
    }

    private suspend fun ensureStaticTrackers() {
        val now = System.currentTimeMillis()
        val existing = repository.getAllTrackers().first()
        val existingTypes = existing.mapTo(mutableSetOf()) { it.type }

        suspend fun createIfMissing(type: TrackerType, name: String) {
            if (type in existingTypes) return
            val trackerId = repository.insertTracker(
                Tracker(
                    name = name,
                    type = type,
                    frequency = Frequency.MONTHLY,
                    layoutStyle = LayoutStyle.GRID,
                    defaultAmount = 0.0,
                    isNew = false,
                    createdAt = now
                )
            )
            if (type == TrackerType.BUDGET) {
                listOf(BudgetFrequency.MONTHLY, BudgetFrequency.WEEKLY).forEach { budgetFrequency ->
                    repository.ensureBudgetPeriods(trackerId, budgetFrequency)
                }
            }
            existingTypes += type
        }

        createIfMissing(TrackerType.GOALS, "Goals")
        createIfMissing(TrackerType.TODO, "Todos")
        createIfMissing(TrackerType.BUDGET, "Budget")
    }

    override fun onAction(action: TrackerListAction) {
        when (action) {
            is TrackerListAction.CreateTracker -> handleCreateTracker(
                name = action.name,
                type = action.type,
                frequency = action.frequency,
                defaultAmount = action.defaultAmount,
                initialMembers = action.initialMembers
            )
            is TrackerListAction.ClearNewFlag -> handleClearNewFlag(action.trackerId)
            is TrackerListAction.DeleteTracker -> handleDeleteTracker(action.trackerId)
            is TrackerListAction.RenameTracker -> handleRenameTracker(action.trackerId, action.newName)
            TrackerListAction.Refresh -> handleRefresh()
        }
    }

    private fun handleCreateTracker(
        name: String,
        type: TrackerType,
        frequency: Frequency,
        defaultAmount: Double,
        initialMembers: List<String>
    ) {
        launch {
            val now = System.currentTimeMillis()
            val trackerId = repository.insertTracker(
                Tracker(
                    name = name,
                    type = type,
                    frequency = frequency,
                    layoutStyle = LayoutStyle.GRID,
                    defaultAmount = defaultAmount,
                    isNew = true,
                    createdAt = now
                )
            )
            initialMembers.forEach { memberName ->
                if (memberName.isNotBlank()) {
                    repository.insertTrackerMember(
                        TrackerMember(
                            trackerId = trackerId,
                            name = memberName.trim(),
                            createdAt = now
                        )
                    )
                }
            }
            val period = buildCurrentPeriod(trackerId, frequency, now)
            val periodId = repository.insertPeriod(period)
            repository.setCurrentPeriod(trackerId, periodId)
            if (type == TrackerType.BUDGET) {
                listOf(BudgetFrequency.MONTHLY, BudgetFrequency.WEEKLY).forEach { budgetFrequency ->
                    repository.ensureBudgetPeriods(trackerId, budgetFrequency)
                }
            }
        }
    }

    private fun handleClearNewFlag(trackerId: Long) {
        launch { repository.clearTrackerNewFlag(trackerId) }
    }

    private fun handleDeleteTracker(trackerId: Long) {
        launch { repository.deleteTracker(trackerId) }
    }

    private fun handleRenameTracker(trackerId: Long, newName: String) {
        launch {
            val tracker = repository.getTrackerById(trackerId) ?: return@launch
            repository.updateTracker(tracker.copy(name = newName.trim()))
        }
    }

    private fun handleRefresh() {
        refreshSignal.update { it + 1 }
    }
    private fun buildSummary(
        tracker: Tracker,
        members: List<TrackerMember>,
        period: TrackerPeriod?,
        completedCount: Int
    ): TrackerSummary {
        val total = members.size
        val pct = if (total > 0) ((completedCount.toDouble() / total) * 100).toInt().coerceIn(0, 100) else 0
        return TrackerSummary(
            trackerId = tracker.id,
            name = tracker.name,
            type = tracker.type,
            frequency = tracker.frequency,
            currentPeriodLabel = period?.label ?: currentPeriodLabel(tracker.frequency),
            totalMembers = total,
            completedCount = completedCount,
            completionPercent = pct,
            isNew = tracker.isNew,
            createdAt = tracker.createdAt
        )
    }

    private fun completedStatuses(type: TrackerType): Set<String> = when (type) {
        TrackerType.DUES -> setOf("PAID")
        TrackerType.GOALS -> setOf("DONE")
        TrackerType.TODO -> setOf("DONE")
        TrackerType.BUDGET -> emptySet()
    }

    private fun currentPeriodLabel(frequency: Frequency): String {
        val cal = Calendar.getInstance()
        return when (frequency) {
            Frequency.MONTHLY -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
            Frequency.WEEKLY -> "Week ${cal.get(Calendar.WEEK_OF_YEAR)}, ${cal.get(Calendar.YEAR)}"
            Frequency.QUARTERLY -> "Q${(cal.get(Calendar.MONTH) / 3) + 1} ${cal.get(Calendar.YEAR)}"
            Frequency.TERMLY -> "Term ${(cal.get(Calendar.MONTH) / 4) + 1} ${cal.get(Calendar.YEAR)}"
            Frequency.BIANNUAL -> "H${if (cal.get(Calendar.MONTH) < 6) 1 else 2} ${cal.get(Calendar.YEAR)}"
            Frequency.ANNUAL -> "${cal.get(Calendar.YEAR)}"
            Frequency.CUSTOM -> "Current Period"
        }
    }

    private fun buildCurrentPeriod(trackerId: Long, frequency: Frequency, now: Long): TrackerPeriod {
        val cal = Calendar.getInstance()
        val (start, end) = when (frequency) {
            Frequency.MONTHLY -> {
                val s = cal.apply { set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                val e = cal.apply { set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH)); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                s to e
            }
            Frequency.WEEKLY -> {
                val s = cal.apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                val e = cal.apply { set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                s to e
            }
            Frequency.QUARTERLY -> {
                val quarter = cal.get(Calendar.MONTH) / 3
                val startMonth = quarter * 3
                val s = cal.apply { set(Calendar.MONTH, startMonth); set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                val e = cal.apply { set(Calendar.MONTH, startMonth + 2); set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH)); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                s to e
            }
            else -> {
                val yearStart = cal.apply { set(Calendar.MONTH, 0); set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                val yearEnd = cal.apply { set(Calendar.MONTH, 11); set(Calendar.DAY_OF_MONTH, 31); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                yearStart to yearEnd
            }
        }
        return TrackerPeriod(
            trackerId = trackerId,
            label = currentPeriodLabel(frequency),
            startDate = start,
            endDate = end,
            isCurrent = true,
            createdAt = now
        )
    }
}
