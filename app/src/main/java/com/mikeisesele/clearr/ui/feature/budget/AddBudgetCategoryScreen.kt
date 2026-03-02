@file:JvmName("AndroidAddBudgetCategoryScreenKt")

package com.mikeisesele.clearr.ui.feature.budget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors

@Composable
fun AddBudgetCategoryScreen(
    trackerId: Long,
    onClose: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalClearrUiColors.current
    if (state.trackerId != trackerId) return

    AddBudgetCategoryScreen(
        state = state,
        colors = colors,
        onClose = onClose,
        onAddCategory = { name, icon, colorToken, plannedAmountNaira ->
            viewModel.onAction(BudgetAction.AddCategory(name, icon, colorToken, plannedAmountNaira))
        }
    )
}
