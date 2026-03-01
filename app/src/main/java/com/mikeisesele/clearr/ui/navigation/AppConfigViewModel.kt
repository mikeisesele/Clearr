package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.di.AppStateHolder
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

/**
 * Root-level ViewModel that exposes app config/loading state for nav decisions.
 */
@HiltViewModel
class AppConfigViewModel @Inject constructor(
    private val repository: ClearrRepository,
    private val appState: AppStateHolder
) : BaseViewModel<AppConfigUiState, AppConfigAction, AppConfigEvent>(
    initialState = AppConfigUiState()
) {

    init {
        onAction(AppConfigAction.Observe)
    }

    override fun onAction(action: AppConfigAction) {
        when (action) {
            AppConfigAction.Observe -> observeConfig()
        }
    }

    private fun observeConfig() {
        launch {
            repository.getAppConfigFlow().collectLatest { config ->
                appState.setAppConfig(config)
                updateState {
                    it.copy(
                        appConfig = config,
                        isLoading = false
                    )
                }
            }
        }
    }
}
