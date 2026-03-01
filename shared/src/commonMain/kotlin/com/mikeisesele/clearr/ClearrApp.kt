package com.mikeisesele.clearr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
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
import com.mikeisesele.clearr.ui.navigation.rememberAppNavigator
import com.mikeisesele.clearr.ui.theme.ClearrSharedTheme

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
            AppDestination.Dashboard -> DashboardScreen(
                state = iosPreviewDashboardModel(),
                isLoading = false,
                onDismissUrgency = {},
                onQuickAction = {}
            )
        }
    }
}

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
