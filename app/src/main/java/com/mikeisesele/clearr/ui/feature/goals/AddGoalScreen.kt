@file:JvmName("AndroidAddGoalScreenKt")

package com.mikeisesele.clearr.ui.feature.goals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors

@Composable
fun AddGoalScreen(
    trackerId: Long,
    onClose: () -> Unit,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalClearrUiColors.current
    val goalsAiService = remember { AndroidGoalsAiService() }
    if (state.trackerId != trackerId) return

    AddGoalScreen(
        state = state,
        colors = colors,
        onClose = onClose,
        onAddGoal = { title, emoji, colorToken, target, frequency ->
            viewModel.onAction(GoalsAction.AddGoal(title, emoji, colorToken, target, frequency))
        },
        inferGoalDraft = { title, target, frequency, emoji, colorToken ->
            goalsAiService.inferGoal(title, target, frequency, emoji, colorToken)
        }
    )
}
