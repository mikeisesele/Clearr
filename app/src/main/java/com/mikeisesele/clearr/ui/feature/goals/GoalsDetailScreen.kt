package com.mikeisesele.clearr.ui.feature.goals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors

@Composable
fun GoalsDetailScreen(
    trackerId: Long,
    onNavigateBack: (() -> Unit)? = null,
    onAddGoal: () -> Unit,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalClearrUiColors.current

    if (state.trackerId != trackerId) return

    GoalsScreen(
        state = state,
        colors = colors,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        onAddGoal = onAddGoal
    )
}
