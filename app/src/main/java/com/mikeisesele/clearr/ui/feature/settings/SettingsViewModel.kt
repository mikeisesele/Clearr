package com.mikeisesele.clearr.ui.feature.settings

import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.di.AppStateHolder
import com.mikeisesele.clearr.domain.repository.DuesRepository
import com.mikeisesele.clearr.ui.commons.state.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: DuesRepository,
    private val appState: AppStateHolder
) : BaseViewModel<SettingsUiState, SettingsAction, SettingsEvent>(
    initialState = SettingsUiState()
) {

    private val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)

    init {
        launch {
            combine(
                appState.selectedYear,
                repository.getAllMembers(),
                repository.getAllYearConfigs(),
                themeModeFlow,
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
                        currentTrackerType = tracker?.type,
                        currentTrackerDueAmount = tracker?.defaultAmount
                    )
                }
            }.collectLatest { newState ->
                updateState { newState }
            }
        }
    }

    override fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SelectYear -> handleSelectYear(action.year)
            is SettingsAction.SetThemeMode -> handleSetThemeMode(action.mode)
            is SettingsAction.UpdateDueAmount -> handleUpdateDueAmount(action.amount)
            is SettingsAction.SetMemberArchived -> handleSetMemberArchived(action.id, action.archived)
            is SettingsAction.StartNewYear -> handleStartNewYear(action.fromYear)
            SettingsAction.ResetSetup -> handleResetSetup()
        }
    }

    private fun handleSelectYear(year: Int) {
        appState.setYear(year)
        launch { repository.ensureYearConfig(year) }
    }

    private fun handleSetThemeMode(mode: ThemeMode) {
        themeModeFlow.value = mode
    }

    private fun handleUpdateDueAmount(amount: Double) {
        launch {
            val trackerId = appState.currentTrackerId.value
            if (trackerId != null) {
                val tracker = repository.getTrackerById(trackerId) ?: return@launch
                if (tracker.type == TrackerType.DUES) {
                    repository.updateTracker(tracker.copy(defaultAmount = amount))
                }
            }
        }
    }

    private fun handleSetMemberArchived(id: Long, archived: Boolean) {
        launch { repository.setMemberArchived(id, archived) }
    }

    private fun handleStartNewYear(fromYear: Int) {
        launch {
            val currentConfig = repository.getYearConfig(fromYear)
            val nextYear = fromYear + 1
            repository.ensureYearConfig(nextYear, currentConfig?.dueAmountPerMonth ?: 5000.0)
            appState.setYear(nextYear)
        }
    }

    private fun handleResetSetup() {
        launch {
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
