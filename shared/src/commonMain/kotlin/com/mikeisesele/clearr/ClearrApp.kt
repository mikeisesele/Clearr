package com.mikeisesele.clearr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.mikeisesele.clearr.ui.feature.dashboard.DashboardScreen
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardClearanceScore
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerHealth
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerType
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardUiModel
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardUrgencyItem
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardUrgencySeverity
import com.mikeisesele.clearr.ui.feature.onboarding.CompletionScreen
import com.mikeisesele.clearr.ui.feature.onboarding.OnboardingScreen
import com.mikeisesele.clearr.ui.feature.onboarding.SplashScreen
import com.mikeisesele.clearr.ui.navigation.AppDestination
import com.mikeisesele.clearr.ui.navigation.AppShellDestination
import com.mikeisesele.clearr.ui.navigation.rememberAppNavigator
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
import com.mikeisesele.clearr.ui.theme.ClearrSharedTheme
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors

@Composable
fun ClearrApp() {
    ClearrSharedTheme {
        val navigator = rememberAppNavigator()
        val navigationState by navigator.state.collectAsState()

        when (navigationState.current) {
            AppDestination.Splash -> SplashScreen(onGetStarted = navigator::openOnboarding)
            AppDestination.Onboarding -> OnboardingScreen(
                onComplete = navigator::completeOnboarding,
                onSkip = navigator::completeOnboarding
            )
            AppDestination.Completion -> CompletionScreen(onOpenApp = navigator::openDashboard)
            is AppDestination.MainShell -> MainShellPreview(
                destination = (navigationState.current as AppDestination.MainShell).destination,
                onNavigate = navigator::openShellDestination
            )
        }
    }
}

@Composable
private fun MainShellPreview(
    destination: AppShellDestination,
    onNavigate: (AppShellDestination) -> Unit
) {
    when (destination) {
        AppShellDestination.Dashboard -> DashboardScreen(
            state = iosPreviewDashboardModel(),
            isLoading = false,
            onDismissUrgency = {},
            onQuickAction = { trackerType ->
                onNavigate(
                    when (trackerType) {
                        DashboardTrackerType.BUDGET -> AppShellDestination.BudgetRoot(PREVIEW_BUDGET_TRACKER_ID)
                        DashboardTrackerType.GOALS -> AppShellDestination.GoalsRoot(PREVIEW_GOALS_TRACKER_ID)
                        DashboardTrackerType.TODOS -> AppShellDestination.TodoRoot(PREVIEW_TODO_TRACKER_ID)
                    }
                )
            }
        )

        is AppShellDestination.BudgetRoot -> ShellPlaceholderScreen(
            title = "Budget",
            message = "Shared shell navigation is now driving this flow. Next step is replacing placeholders with live shared feature stores on iOS.",
            primaryAction = "Add Category",
            onPrimaryAction = {
                onNavigate(AppShellDestination.BudgetAddCategory(destination.trackerId))
            },
            onBack = { onNavigate(AppShellDestination.Dashboard) }
        )

        is AppShellDestination.GoalsRoot -> ShellPlaceholderScreen(
            title = "Goals",
            message = "This screen is now reachable through the shared navigator. The Android nav graph is still acting as the platform adapter.",
            primaryAction = "Add Goal",
            onPrimaryAction = {
                onNavigate(AppShellDestination.GoalAdd(destination.trackerId))
            },
            onBack = { onNavigate(AppShellDestination.Dashboard) }
        )

        is AppShellDestination.TodoRoot -> ShellPlaceholderScreen(
            title = "Todos",
            message = "The shared app shell can now move into tracker flows without depending on Android route strings.",
            primaryAction = "Add Todo",
            onPrimaryAction = {
                onNavigate(AppShellDestination.TodoAdd(destination.trackerId))
            },
            onBack = { onNavigate(AppShellDestination.Dashboard) }
        )

        is AppShellDestination.TodoAdd -> ShellPlaceholderScreen(
            title = "Add Todo",
            message = "Add-todo flow is now part of the shared shell backstack model.",
            primaryAction = "Back To Todos",
            onPrimaryAction = { onNavigate(AppShellDestination.TodoRoot(destination.trackerId)) },
            onBack = { onNavigate(AppShellDestination.TodoRoot(destination.trackerId)) }
        )

        is AppShellDestination.GoalAdd -> ShellPlaceholderScreen(
            title = "Add Goal",
            message = "Add-goal flow is now part of the shared shell backstack model.",
            primaryAction = "Back To Goals",
            onPrimaryAction = { onNavigate(AppShellDestination.GoalsRoot(destination.trackerId)) },
            onBack = { onNavigate(AppShellDestination.GoalsRoot(destination.trackerId)) }
        )

        is AppShellDestination.BudgetAddCategory -> ShellPlaceholderScreen(
            title = "Add Budget Category",
            message = "Add-category flow is now part of the shared shell backstack model.",
            primaryAction = "Back To Budget",
            onPrimaryAction = { onNavigate(AppShellDestination.BudgetRoot(destination.trackerId)) },
            onBack = { onNavigate(AppShellDestination.BudgetRoot(destination.trackerId)) }
        )
    }
}

@Composable
private fun ShellPlaceholderScreen(
    title: String,
    message: String,
    primaryAction: String,
    onPrimaryAction: () -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalClearrUiColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        ClearrTopBar(
            title = title,
            showLeading = true,
            leadingIcon = "←",
            onLeadingClick = onBack
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(ClearrDimens.dp20),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ClearrDimens.dp20),
                color = colors.surface,
                tonalElevation = ClearrDimens.dp4
            ) {
                Column(
                    modifier = Modifier.padding(ClearrDimens.dp20),
                    verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp16)
                ) {
                    Text(
                        text = title,
                        color = colors.text,
                        fontSize = ClearrTextSizes.sp20,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = message,
                        color = colors.muted,
                        fontSize = ClearrTextSizes.sp14
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onPrimaryAction),
                        shape = RoundedCornerShape(ClearrDimens.dp14),
                        color = ClearrColors.Violet
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = ClearrDimens.dp16),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = primaryAction,
                                color = ClearrColors.Surface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val PREVIEW_BUDGET_TRACKER_ID = 1001L
private const val PREVIEW_GOALS_TRACKER_ID = 1002L
private const val PREVIEW_TODO_TRACKER_ID = 1003L

private fun iosPreviewDashboardModel(): DashboardUiModel = DashboardUiModel(
    periodLabel = "March 2026",
    daysLabel = "31 days in view",
    score = DashboardClearanceScore(
        overall = 71,
        budget = DashboardTrackerHealth(
            trackerType = DashboardTrackerType.BUDGET,
            percent = 82,
            detail = "N82k / N100k spent",
            statusLabel = "Looking good"
        ),
        goals = DashboardTrackerHealth(
            trackerType = DashboardTrackerType.GOALS,
            percent = 66,
            detail = "2 / 3 done",
            statusLabel = "In progress"
        ),
        todos = DashboardTrackerHealth(
            trackerType = DashboardTrackerType.TODOS,
            percent = 54,
            detail = "7 / 13 done",
            statusLabel = "In progress"
        )
    ),
    urgencyItems = listOf(
        DashboardUrgencyItem(
            id = "ios-todos",
            message = "6 todos still need attention",
            severity = DashboardUrgencySeverity.WARNING,
            trackerType = DashboardTrackerType.TODOS,
            actionLabel = "Review todos"
        )
    ),
    visibleTiles = listOf(
        DashboardTrackerType.BUDGET,
        DashboardTrackerType.GOALS,
        DashboardTrackerType.TODOS
    ),
    hasTrackers = true
)
