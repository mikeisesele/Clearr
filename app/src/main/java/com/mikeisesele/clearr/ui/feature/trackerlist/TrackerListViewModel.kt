package com.mikeisesele.clearr.ui.feature.trackerlist

import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.domain.repository.DuesRepository
import com.mikeisesele.clearr.domain.trackers.ObserveTrackerSummariesUseCase
import com.mikeisesele.clearr.domain.trackers.TrackerBootstrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TrackerListViewModel @Inject constructor(
    private val repository: DuesRepository,
    private val trackerBootstrapper: TrackerBootstrapper,
    private val observeTrackerSummaries: ObserveTrackerSummariesUseCase
) : BaseViewModel<TrackerListUiState, TrackerListAction, TrackerListEvent>(
    initialState = TrackerListUiState(isLoading = true)
) {

    private val refreshSignal = MutableStateFlow(0)

    init {
        launch {
            trackerBootstrapper.ensureStaticTrackers()
            observeTrackerSummaries().collectLatest { summaries ->
                updateState {
                    TrackerListUiState(
                        summaries = ClearrEdgeAi.prioritizeTrackers(summaries),
                        isLoading = false
                    )
                }
            }
        }
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
        type: com.mikeisesele.clearr.data.model.TrackerType,
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
            if (type == com.mikeisesele.clearr.data.model.TrackerType.BUDGET) {
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
        updateState { state ->
            state.copy(
                summaries = state.summaries.filterNot { it.trackerId == trackerId }
            )
        }
        launch {
            repository.deleteTracker(trackerId)
            refreshSignal.update { it + 1 }
        }
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

    private fun buildCurrentPeriod(trackerId: Long, frequency: Frequency, now: Long): com.mikeisesele.clearr.data.model.TrackerPeriod {
        val cal = java.util.Calendar.getInstance()
        val (start, end) = when (frequency) {
            Frequency.MONTHLY -> {
                val s = cal.apply { set(java.util.Calendar.DAY_OF_MONTH, 1); set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis
                val e = cal.apply { set(java.util.Calendar.DAY_OF_MONTH, getActualMaximum(java.util.Calendar.DAY_OF_MONTH)); set(java.util.Calendar.HOUR_OF_DAY, 23); set(java.util.Calendar.MINUTE, 59); set(java.util.Calendar.SECOND, 59) }.timeInMillis
                s to e
            }
            Frequency.WEEKLY -> {
                val s = cal.apply { set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY); set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis
                val e = cal.apply { set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SUNDAY); set(java.util.Calendar.HOUR_OF_DAY, 23); set(java.util.Calendar.MINUTE, 59); set(java.util.Calendar.SECOND, 59) }.timeInMillis
                s to e
            }
            Frequency.QUARTERLY -> {
                val quarter = cal.get(java.util.Calendar.MONTH) / 3
                val startMonth = quarter * 3
                val s = cal.apply { set(java.util.Calendar.MONTH, startMonth); set(java.util.Calendar.DAY_OF_MONTH, 1); set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis
                val e = cal.apply { set(java.util.Calendar.MONTH, startMonth + 2); set(java.util.Calendar.DAY_OF_MONTH, getActualMaximum(java.util.Calendar.DAY_OF_MONTH)); set(java.util.Calendar.HOUR_OF_DAY, 23); set(java.util.Calendar.MINUTE, 59); set(java.util.Calendar.SECOND, 59) }.timeInMillis
                s to e
            }
            else -> {
                val yearStart = cal.apply { set(java.util.Calendar.MONTH, 0); set(java.util.Calendar.DAY_OF_MONTH, 1); set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis
                val yearEnd = cal.apply { set(java.util.Calendar.MONTH, 11); set(java.util.Calendar.DAY_OF_MONTH, 31); set(java.util.Calendar.HOUR_OF_DAY, 23); set(java.util.Calendar.MINUTE, 59); set(java.util.Calendar.SECOND, 59) }.timeInMillis
                yearStart to yearEnd
            }
        }
        return com.mikeisesele.clearr.data.model.TrackerPeriod(
            trackerId = trackerId,
            label = currentPeriodLabel(frequency),
            startDate = start,
            endDate = end,
            isCurrent = true,
            createdAt = now
        )
    }

    private fun currentPeriodLabel(frequency: Frequency): String {
        val cal = java.util.Calendar.getInstance()
        return when (frequency) {
            Frequency.MONTHLY -> java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(cal.time)
            Frequency.WEEKLY -> "Week ${cal.get(java.util.Calendar.WEEK_OF_YEAR)}, ${cal.get(java.util.Calendar.YEAR)}"
            Frequency.QUARTERLY -> "Q${(cal.get(java.util.Calendar.MONTH) / 3) + 1} ${cal.get(java.util.Calendar.YEAR)}"
            Frequency.TERMLY -> "Term ${(cal.get(java.util.Calendar.MONTH) / 4) + 1} ${cal.get(java.util.Calendar.YEAR)}"
            Frequency.BIANNUAL -> "H${if (cal.get(java.util.Calendar.MONTH) < 6) 1 else 2} ${cal.get(java.util.Calendar.YEAR)}"
            Frequency.ANNUAL -> "${cal.get(java.util.Calendar.YEAR)}"
            Frequency.CUSTOM -> "Current Period"
        }
    }
}
