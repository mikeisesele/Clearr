package com.mikeisesele.clearr.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.di.AppStateHolder
import com.mikeisesele.clearr.domain.repository.DuesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Lightweight ViewModel that lives at the root NavHost level.
 * It watches the DB's app_config row so the nav graph can switch
 * between the Setup Wizard and the main bottom-nav app.
 */
@HiltViewModel
class AppConfigViewModel @Inject constructor(
    private val repository: DuesRepository,
    private val appState: AppStateHolder
) : ViewModel() {

    /** null = loading, AppConfig = loaded (may have setupComplete = false) */
    val appConfig: StateFlow<AppConfig?> = repository
        .getAppConfigFlow()
        .onEach { config -> appState.setAppConfig(config) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    /**
     * True only during the very first DB emit (null).
     * Once the flow emits (even null from an empty table) this flips to false.
     */
    val isLoading: StateFlow<Boolean> = appConfig
        .map { false }     // any emission means we're done loading
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true   // start as loading
        )
}
