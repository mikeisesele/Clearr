package com.mikeisesele.clearr.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.di.AppStateHolder
import com.mikeisesele.clearr.domain.repository.DuesRepository
import com.mikeisesele.clearr.ui.state.SettingsUiState
import com.mikeisesele.clearr.ui.state.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: DuesRepository,
    private val appState: AppStateHolder
) : ViewModel() {

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)

    val uiState: StateFlow<SettingsUiState> = combine(
        appState.selectedYear,
        repository.getAllMembers(),
        repository.getAllYearConfigs(),
        _themeMode,
        appState.appConfig,
        appState.currentTrackerId
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        Sextuple(
            selectedYear = arr[0] as Int,
            allMembers = arr[1] as List<com.mikeisesele.clearr.data.model.Member>,
            yearConfigs = arr[2] as List<com.mikeisesele.clearr.data.model.YearConfig>,
            themeMode = arr[3] as ThemeMode,
            appConfig = arr[4] as AppConfig?,
            trackerId = arr[5] as Long?
        )
    }.flatMapLatest { p ->
        val trackerFlow: Flow<Tracker?> = p.trackerId?.let { repository.getTrackerByIdFlow(it) } ?: flowOf(null)
        trackerFlow.map { tracker ->
            SettingsUiState(
                selectedYear = p.selectedYear,
                allMembers = p.allMembers,
                yearConfigs = p.yearConfigs,
                themeMode = p.themeMode,
                layoutStyle = tracker?.layoutStyle ?: p.appConfig?.layoutStyle ?: LayoutStyle.GRID,
                currentTrackerType = tracker?.type,
                currentTrackerDueAmount = tracker?.defaultAmount
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    val themeMode: StateFlow<ThemeMode> = _themeMode

    fun selectYear(year: Int) {
        appState.setYear(year)
        viewModelScope.launch { repository.ensureYearConfig(year) }
    }

    fun setThemeMode(mode: ThemeMode) { _themeMode.value = mode }

    fun updateDueAmount(year: Int, amount: Double) {
        viewModelScope.launch {
            val trackerId = appState.currentTrackerId.value
            if (trackerId != null) {
                val tracker = repository.getTrackerById(trackerId) ?: return@launch
                if (tracker.type == TrackerType.DUES) {
                    repository.updateTracker(tracker.copy(defaultAmount = amount))
                }
            }
        }
    }

    fun setMemberArchived(id: Long, archived: Boolean) {
        viewModelScope.launch { repository.setMemberArchived(id, archived) }
    }

    fun startNewYear(fromYear: Int) {
        viewModelScope.launch {
            val currentConfig = repository.getYearConfig(fromYear)
            val nextYear = fromYear + 1
            repository.ensureYearConfig(nextYear, currentConfig?.dueAmountPerMonth ?: 5000.0)
            appState.setYear(nextYear)
        }
    }

    /**
     * Saves the chosen layout style to DB and immediately pushes it to
     * AppStateHolder so HomeScreen reacts without any navigation needed.
     */
    fun setLayoutStyle(style: LayoutStyle) {
        viewModelScope.launch {
            val trackerId = appState.currentTrackerId.value
            if (trackerId != null) {
                val tracker = repository.getTrackerById(trackerId) ?: return@launch
                repository.updateTracker(tracker.copy(layoutStyle = style))
            } else {
                // Fallback default if no tracker is currently selected.
                val existing = repository.getAppConfig() ?: AppConfig()
                val updated = existing.copy(layoutStyle = style)
                repository.upsertAppConfig(updated)
                appState.setAppConfig(updated)
            }
        }
    }

    /** Marks setupComplete = false so the wizard shows on next recompose. */
    fun resetSetup() {
        viewModelScope.launch {
            val existing = repository.getAppConfig()
            val config = existing?.copy(setupComplete = false)
                ?: AppConfig(setupComplete = false)
            repository.upsertAppConfig(config)
            appState.setAppConfig(config)
        }
    }

    private data class Sextuple(
        val selectedYear: Int,
        val allMembers: List<com.mikeisesele.clearr.data.model.Member>,
        val yearConfigs: List<com.mikeisesele.clearr.data.model.YearConfig>,
        val themeMode: ThemeMode,
        val appConfig: AppConfig?,
        val trackerId: Long?
    )
}
