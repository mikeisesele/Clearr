package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerType
import com.mikeisesele.clearr.ui.navigation.components.AppBottomNavItem

fun String?.isBottomNavRoute(): Boolean = toAppShellDestinationKind()?.bottomNavItem != null

fun String?.toBottomNavItem(): AppBottomNavItem? = toAppShellDestinationKind()?.bottomNavItem

fun AppShellDestination.toBottomNavItem(): AppBottomNavItem? = topLevelDestination().kind.bottomNavItem

fun AppShellUiState.destinationFor(item: AppBottomNavItem): AppShellDestination? = when (item) {
    AppBottomNavItem.HOME -> AppShellDestination.Dashboard
    AppBottomNavItem.BUDGET -> budgetTrackerId?.let { AppShellDestination.BudgetRoot(it) }
    AppBottomNavItem.TODOS -> todoTrackerId?.let { AppShellDestination.TodoRoot(it) }
    AppBottomNavItem.GOALS -> goalsTrackerId?.let { AppShellDestination.GoalsRoot(it) }
}

fun AppShellUiState.destinationFor(trackerType: DashboardTrackerType): AppShellDestination? = when (trackerType) {
    DashboardTrackerType.BUDGET -> budgetTrackerId?.let { AppShellDestination.BudgetRoot(it) }
    DashboardTrackerType.GOALS -> goalsTrackerId?.let { AppShellDestination.GoalsRoot(it) }
    DashboardTrackerType.TODOS -> todoTrackerId?.let { AppShellDestination.TodoRoot(it) }
}
