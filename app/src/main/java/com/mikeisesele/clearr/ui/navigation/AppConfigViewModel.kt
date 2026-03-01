package com.mikeisesele.clearr.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikeisesele.clearr.di.AppStateHolder
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/**
 * Root-level ViewModel that exposes app config/loading state for nav decisions.
 */
@HiltViewModel
class AppConfigViewModel @Inject constructor(
    repository: ClearrRepository,
    appState: AppStateHolder
) : ViewModel() {

    private val store = AppConfigStore(
        repository = repository,
        scope = viewModelScope,
        onConfigChanged = appState::setAppConfig
    )

    val uiState: StateFlow<AppConfigUiState> = store.uiState
    val events = store.events

    fun onAction(action: AppConfigAction) {
        store.onAction(action)
    }
}
