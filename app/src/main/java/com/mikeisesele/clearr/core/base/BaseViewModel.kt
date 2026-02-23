package com.mikeisesele.clearr.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Common MVI base ViewModel.
 * - [uiState] is the single source of truth for UI rendering.
 * - [events] emits one-off side effects (navigation, toasts, etc).
 */
abstract class BaseViewModel<ViewState : BaseState, Action, Event : ViewEvent>(
    initialState: ViewState
) : ViewModel() {

    private val mutableState = MutableStateFlow(initialState)
    val uiState: StateFlow<ViewState> = mutableState.asStateFlow()

    private val eventChannel = Channel<Event>(capacity = Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    protected val currentState: ViewState
        get() = uiState.value

    abstract fun onAction(action: Action)

    protected fun updateState(reducer: (ViewState) -> ViewState) {
        mutableState.update(reducer)
    }

    protected fun sendEvent(event: Event) {
        launch { eventChannel.send(event) }
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(block = block)
    }
}
