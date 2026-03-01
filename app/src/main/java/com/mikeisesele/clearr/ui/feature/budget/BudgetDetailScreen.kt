package com.mikeisesele.clearr.ui.feature.budget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors

@Composable
fun BudgetDetailScreen(
    trackerId: Long,
    onNavigateBack: (() -> Unit)? = null,
    onAddCategory: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalClearrUiColors.current

    if (state.trackerId != trackerId) return

    BudgetScreen(
        state = state,
        colors = colors,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        onAddCategory = onAddCategory
    )
}
