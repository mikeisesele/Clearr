package com.mikeisesele.clearr.ui.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.ui.feature.dashboard.components.AllClearCard
import com.mikeisesele.clearr.ui.feature.dashboard.components.ClearanceScoreSection
import com.mikeisesele.clearr.ui.feature.dashboard.components.PeriodContextBar
import com.mikeisesele.clearr.ui.feature.dashboard.components.QuickActionRow
import com.mikeisesele.clearr.ui.feature.dashboard.components.UrgencyHeader
import com.mikeisesele.clearr.ui.feature.dashboard.components.UrgencyStrip
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerType
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardUiModel
import com.mikeisesele.clearr.ui.feature.dashboard.utils.previewDashboardUi
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
fun DashboardScreen(
    onOpenBudget: () -> Unit,
    onOpenTodos: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenRemittance: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DashboardEvent.OpenTracker -> when (event.trackerType) {
                    DashboardTrackerType.BUDGET -> onOpenBudget()
                    DashboardTrackerType.GOALS -> onOpenGoals()
                    DashboardTrackerType.DUES -> onOpenRemittance()
                    DashboardTrackerType.TODOS -> onOpenTodos()
                }
            }
        }
    }

    DashboardContent(
        state = state.model,
        isLoading = state.isLoading,
        onDismissUrgency = { viewModel.onAction(DashboardAction.DismissUrgency(it)) },
        onQuickAction = { viewModel.onAction(DashboardAction.QuickAction(it)) }
    )
}

@Composable
internal fun DashboardContent(
    state: DashboardUiModel,
    isLoading: Boolean,
    onDismissUrgency: (String) -> Unit,
    onQuickAction: (DashboardTrackerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDuesColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        PeriodContextBar(
            period = state.periodLabel,
            days = state.daysLabel
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = ClearrDimens.dp20,
                end = ClearrDimens.dp20,
                top = ClearrDimens.dp8,
                bottom = ClearrDimens.dp12
            ),
            verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp20)
        ) {
            item {
                ClearanceScoreSection(score = state.score)
            }

            if (state.urgencyItems.isEmpty()) {
                item {
                    AllClearCard(
                        hasTrackers = state.hasTrackers,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                item {
                    UrgencyHeader(modifier = Modifier.fillMaxWidth())
                }
                item {
                    UrgencyStrip(
                        state = state,
                        onDismissUrgency = onDismissUrgency,
                        onQuickAction = onQuickAction,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        QuickActionRow(
            onLogSpend = { onQuickAction(DashboardTrackerType.BUDGET) },
            onMarkGoal = { onQuickAction(DashboardTrackerType.GOALS) },
            onRecordDue = { onQuickAction(DashboardTrackerType.DUES) },
            modifier = Modifier.navigationBarsPadding()
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun DashboardScreenPreview() {
    ClearrTheme {
        DashboardContent(
            state = previewDashboardUi,
            isLoading = false,
            onDismissUrgency = {},
            onQuickAction = {}
        )
    }
}
