package com.mikeisesele.clearr.ui.feature.todo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AddTodoRoute(
    trackerId: Long,
    onClose: () -> Unit,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.trackerId != trackerId) return

    AddTodoScreen(
        onClose = onClose,
        onAddTodo = { title, note, priority, dueDate ->
            viewModel.onAction(TodoAction.AddTodo(title, note, priority, dueDate))
        }
    )
}
