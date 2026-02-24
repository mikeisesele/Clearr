package com.mikeisesele.clearr.di

import com.mikeisesele.clearr.data.model.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton that holds app-wide UI state shared across ViewModels.
 * - selectedYear: which year the tracker screens display
 * - appConfig: the live AppConfig from the DB (wizard completion and defaults)
 * - currentTrackerId: the tracker currently open in detail view
 */
@Singleton
class AppStateHolder @Inject constructor() {

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear

    fun setYear(year: Int) { _selectedYear.value = year }

    /** Mirrors the DB AppConfig so all screens can react to wizard completion */
    private val _appConfig = MutableStateFlow<AppConfig?>(null)
    val appConfig: StateFlow<AppConfig?> = _appConfig

    fun setAppConfig(config: AppConfig?) { _appConfig.value = config }

    private val _currentTrackerId = MutableStateFlow<Long?>(null)
    val currentTrackerId: StateFlow<Long?> = _currentTrackerId

    fun setCurrentTrackerId(trackerId: Long?) { _currentTrackerId.value = trackerId }
}
