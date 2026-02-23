package com.mikeisesele.clearr.ui.feature.trackerlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.data.model.TrackerPeriod
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.repository.DuesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TrackerListViewModel @Inject constructor(
    private val repository: DuesRepository
) : ViewModel() {

    private val refreshSignal = MutableStateFlow(0)

    val uiState: StateFlow<TrackerListUiState> = refreshSignal
        .flatMapLatest {
            repository.getAllTrackers()
        }
        .flatMapLatest { trackers ->
            if (trackers.isEmpty()) {
                flowOf(TrackerListUiState(summaries = emptyList(), isLoading = false))
            } else {
                // For each tracker, build a summary flow that reacts to member and period changes
                val summaryFlows: List<Flow<TrackerSummary>> = trackers.map { tracker ->
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
                combine(summaryFlows) { arr ->
                    TrackerListUiState(
                        summaries = arr.toList(),
                        isLoading = false
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = TrackerListUiState(isLoading = true)
        )

    /** Create a new tracker and immediately ensure a current period exists for it */
    fun createTracker(
        name: String,
        type: TrackerType,
        frequency: Frequency,
        defaultAmount: Double,
        initialMembers: List<String>
    ) {
        viewModelScope.launch {
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
            // Insert initial members
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
            // Create and mark the current period
            val period = buildCurrentPeriod(trackerId, frequency, now)
            val periodId = repository.insertPeriod(period)
            repository.setCurrentPeriod(trackerId, periodId)
        }
    }

    /** Called when the user taps a tracker card for the first time (clears NEW badge) */
    fun clearNewFlag(trackerId: Long) {
        viewModelScope.launch { repository.clearTrackerNewFlag(trackerId) }
    }

    fun deleteTracker(trackerId: Long) {
        viewModelScope.launch { repository.deleteTracker(trackerId) }
    }

    fun renameTracker(trackerId: Long, newName: String) {
        viewModelScope.launch {
            val tracker = repository.getTrackerById(trackerId) ?: return@launch
            repository.updateTracker(tracker.copy(name = newName.trim()))
        }
    }

    fun refresh() {
        refreshSignal.update { it + 1 }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
        TrackerType.DUES       -> setOf("PAID")
        TrackerType.ATTENDANCE -> setOf("PRESENT")
        TrackerType.TASKS      -> setOf("DONE")
        TrackerType.EVENTS     -> setOf("PRESENT")
        TrackerType.CUSTOM     -> setOf("PAID", "PRESENT", "DONE")
    }

    /** Generate a human-readable label for the current period based on frequency */
    private fun currentPeriodLabel(frequency: Frequency): String {
        val cal = Calendar.getInstance()
        return when (frequency) {
            Frequency.MONTHLY    -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
            Frequency.WEEKLY     -> "Week ${cal.get(Calendar.WEEK_OF_YEAR)}, ${cal.get(Calendar.YEAR)}"
            Frequency.QUARTERLY  -> "Q${(cal.get(Calendar.MONTH) / 3) + 1} ${cal.get(Calendar.YEAR)}"
            Frequency.TERMLY     -> "Term ${(cal.get(Calendar.MONTH) / 4) + 1} ${cal.get(Calendar.YEAR)}"
            Frequency.BIANNUAL   -> "H${if (cal.get(Calendar.MONTH) < 6) 1 else 2} ${cal.get(Calendar.YEAR)}"
            Frequency.ANNUAL     -> "${cal.get(Calendar.YEAR)}"
            Frequency.CUSTOM     -> "Current Period"
        }
    }

    /** Build a TrackerPeriod for the current cycle, not yet inserted */
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
