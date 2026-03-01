package com.mikeisesele.clearr.ui.feature.todo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TodoRoute(
    trackerId: Long,
    onNavigateBack: (() -> Unit)? = null,
    onAddTodo: () -> Unit,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.trackerId != trackerId) return

    TodoDetailScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        onAddTodo = onAddTodo
    )
}
