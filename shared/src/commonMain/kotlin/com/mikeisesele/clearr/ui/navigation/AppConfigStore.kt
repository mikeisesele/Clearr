package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppConfigStore(
    private val repository: ClearrRepository,
    private val scope: CoroutineScope,
    private val onConfigChanged: (AppConfig?) -> Unit = {}
) {
    private val mutableState = MutableStateFlow(AppConfigUiState())
    val uiState: StateFlow<AppConfigUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<AppConfigEvent>(capacity = Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        onAction(AppConfigAction.Observe)
    }

    fun onAction(action: AppConfigAction) {
        when (action) {
            AppConfigAction.Observe -> observeConfig()
        }
    }

    private fun observeConfig() {
        scope.launch {
            repository.getAppConfigFlow().collect { config ->
                onConfigChanged(config)
                mutableState.update {
                    it.copy(
                        appConfig = config,
                        isLoading = false
                    )
                }
            }
        }
    }
}
