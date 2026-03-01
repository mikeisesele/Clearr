package com.mikeisesele.clearr.ui.feature.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerType

@Composable
fun DashboardRoute(
    onOpenBudget: () -> Unit,
    onOpenTodos: () -> Unit,
    onOpenGoals: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DashboardEvent.OpenTracker -> when (event.trackerType) {
                    DashboardTrackerType.BUDGET -> onOpenBudget()
                    DashboardTrackerType.GOALS -> onOpenGoals()
                    DashboardTrackerType.TODOS -> onOpenTodos()
                }
            }
        }
    }

    DashboardScreen(
        state = state.model,
        isLoading = state.isLoading,
        onDismissUrgency = { viewModel.onAction(DashboardAction.DismissUrgency(it)) },
        onQuickAction = { viewModel.onAction(DashboardAction.QuickAction(it)) }
    )
}
