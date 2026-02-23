package com.mikeisesele.clearr.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.data.model.TrackerPeriod
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.di.AppStateHolder
import com.mikeisesele.clearr.domain.repository.DuesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class SetupWizardState(
    val step: Int = 0,                      // 0–6 (7 steps total)
    val groupName: String = "JSS Durumi Brothers",
    val trackerName: String = "Dues Tracker",
    val adminName: String = "",
    val adminPhone: String = "",
    val trackerType: TrackerType = TrackerType.DUES,
    val frequency: Frequency = Frequency.MONTHLY,
    val defaultAmount: String = "5000",
    val layoutStyle: LayoutStyle = LayoutStyle.GRID,
    val loadSampleMembers: Boolean = true,
    val isSaving: Boolean = false
)

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val repository: DuesRepository,
    private val appState: AppStateHolder
) : ViewModel() {

    private val _state = MutableStateFlow(SetupWizardState())
    val state: StateFlow<SetupWizardState> = _state

    init {
        viewModelScope.launch {
            val hasTrackers = repository.getAllTrackers().first().isNotEmpty()
            if (hasTrackers) {
                // Skip the intro-only quick setup step for subsequent tracker creation.
                _state.update { it.copy(step = 1) }
            }
        }
    }

    fun nextStep() = _state.update { s ->
        val next = when {
            s.step == 3 && s.trackerType != TrackerType.DUES -> 5
            else -> s.step + 1
        }.coerceAtMost(6)
        s.copy(step = next)
    }
    fun prevStep() = _state.update { s ->
        val prev = when {
            s.step == 5 && s.trackerType != TrackerType.DUES -> 3
            else -> s.step - 1
        }.coerceAtLeast(0)
        s.copy(step = prev)
    }

    fun setGroupName(v: String) = _state.update { it.copy(groupName = v) }
    fun setTrackerName(v: String) = _state.update { it.copy(trackerName = v) }
    fun setAdminName(v: String) = _state.update { it.copy(adminName = v) }
    fun setAdminPhone(v: String) = _state.update { it.copy(adminPhone = v) }
    fun setTrackerType(v: TrackerType) = _state.update { s ->
        val oldDefault = defaultTrackerName(s.trackerType)
        val shouldAutoRename = s.trackerName.isBlank() || s.trackerName == oldDefault
        s.copy(
            trackerType = v,
            trackerName = if (shouldAutoRename) defaultTrackerName(v) else s.trackerName
        )
    }
    fun setFrequency(v: Frequency) = _state.update { it.copy(frequency = v) }
    fun setDefaultAmount(v: String) = _state.update { it.copy(defaultAmount = v) }
    fun setLayoutStyle(v: LayoutStyle) = _state.update { it.copy(layoutStyle = v) }
    fun setLoadSampleMembers(v: Boolean) = _state.update { it.copy(loadSampleMembers = v) }

    fun goToStep(step: Int) = _state.update { it.copy(step = step.coerceIn(0, 6)) }

    /** Called from the Review step – saves AppConfig and creates a tracker from wizard selections. */
    fun finishSetup(onDone: () -> Unit) {
        val s = _state.value
        val amount = s.defaultAmount.toDoubleOrNull()?.takeIf { it > 0 } ?: 5000.0
        val now = System.currentTimeMillis()
        val config = AppConfig(
            id = 1,
            groupName = s.groupName.trim().ifBlank { "JSS Durumi Brothers" },
            adminName = s.adminName.trim(),
            adminPhone = s.adminPhone.trim(),
            trackerType = s.trackerType,
            frequency = s.frequency,
            defaultAmount = amount,
            layoutStyle = s.layoutStyle,
            remindersEnabled = false,
            setupComplete = true
        )
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val trackerId = repository.insertTracker(
                Tracker(
                    name = s.trackerName.trim().ifBlank { defaultTrackerName(s.trackerType) },
                    type = s.trackerType,
                    frequency = s.frequency,
                    layoutStyle = s.layoutStyle,
                    defaultAmount = amount,
                    isNew = true,
                    createdAt = now
                )
            )
            val periodId = repository.insertPeriod(buildCurrentPeriod(trackerId, s.frequency, now))
            repository.setCurrentPeriod(trackerId, periodId)

            if (s.trackerType == TrackerType.DUES && s.loadSampleMembers) {
                SAMPLE_MEMBERS.forEach { name ->
                    repository.insertTrackerMember(
                        TrackerMember(
                            trackerId = trackerId,
                            name = name,
                            createdAt = now
                        )
                    )
                }
            }

            // Persist setupComplete only after tracker creation so the app doesn't
            // navigate to TrackerList before data is ready.
            repeat(6) {
                if (repository.getTrackerById(trackerId) != null) return@repeat
                delay(50)
            }
            repository.upsertAppConfig(config)
            // Flip app state after tracker creation so TrackerList can render fresh data immediately.
            appState.setAppConfig(config)
            _state.update { it.copy(isSaving = false) }
            onDone()
        }
    }

    /** Loads existing config into the wizard state (for "Edit Settings" use-case). */
    fun loadExistingConfig(config: AppConfig) {
        _state.update {
            it.copy(
                groupName = config.groupName,
                trackerName = defaultTrackerName(config.trackerType),
                adminName = config.adminName,
                adminPhone = config.adminPhone,
                trackerType = config.trackerType,
                frequency = config.frequency,
                defaultAmount = config.defaultAmount.toInt().toString(),
                layoutStyle = config.layoutStyle,
                loadSampleMembers = true
            )
        }
    }

    private fun defaultTrackerName(type: TrackerType): String = when (type) {
        TrackerType.DUES -> "Dues Tracker"
        TrackerType.ATTENDANCE -> "Attendance Tracker"
        TrackerType.TASKS -> "Task Tracker"
        TrackerType.EVENTS -> "Event Tracker"
        TrackerType.CUSTOM -> "Custom Tracker"
    }

    private companion object {
        val SAMPLE_MEMBERS = listOf(
            "Henry Nwazuru",
            "Chidubem",
            "Simon Boniface",
            "Ikechukwu Udeh",
            "Oluwatobi Majekodunmi",
            "Dare Oladunjoye",
            "Michael Isesele",
            "Faruk Umar"
        )
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
                val s = cal.apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val e = cal.apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis
                s to e
            }
            Frequency.WEEKLY -> {
                val s = cal.apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val e = cal.apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis
                s to e
            }
            Frequency.QUARTERLY -> {
                val quarter = cal.get(Calendar.MONTH) / 3
                val startMonth = quarter * 3
                val s = cal.apply {
                    set(Calendar.MONTH, startMonth)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val e = cal.apply {
                    set(Calendar.MONTH, startMonth + 2)
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis
                s to e
            }
            else -> {
                val yearStart = cal.apply {
                    set(Calendar.MONTH, 0)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val yearEnd = cal.apply {
                    set(Calendar.MONTH, 11)
                    set(Calendar.DAY_OF_MONTH, 31)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis
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
