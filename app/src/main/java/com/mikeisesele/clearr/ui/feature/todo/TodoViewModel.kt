package com.mikeisesele.clearr.ui.feature.todo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikeisesele.clearr.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class TodoViewModel @Inject constructor(
    repository: TodoRepository,
    todoPreferencesRepository: com.mikeisesele.clearr.domain.repository.TodoPreferencesRepository,
    todoAiService: TodoAiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val store = TodoStore(
        trackerId = checkNotNull(savedStateHandle.get<Long>("trackerId")),
        repository = repository,
        todoPreferencesRepository = todoPreferencesRepository,
        todoAiService = todoAiService,
        scope = viewModelScope,
        nowMillis = System::currentTimeMillis
    )

    val uiState: StateFlow<TodoUiState> = store.uiState
    val events = store.events

    fun onAction(action: TodoAction) {
        store.onAction(action)
    }
}
