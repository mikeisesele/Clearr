package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.ui.navigation.components.AppBottomNavItem

fun String?.isBottomNavRoute(): Boolean = toAppShellDestinationKind()?.bottomNavItem != null

fun String?.toBottomNavItem(): AppBottomNavItem? = toAppShellDestinationKind()?.bottomNavItem

fun String?.isTopLevelNonDashboardRoute(): Boolean {
    val kind = toAppShellDestinationKind()
    return kind == AppShellDestinationKind.BUDGET_ROOT ||
        kind == AppShellDestinationKind.TODO_ROOT ||
        kind == AppShellDestinationKind.GOALS_ROOT
}

fun AppShellUiState.destinationFor(item: AppBottomNavItem): AppShellDestination? = when (item) {
    AppBottomNavItem.HOME -> AppShellDestination.Dashboard
    AppBottomNavItem.BUDGET -> budgetTrackerId?.let { AppShellDestination.BudgetRoot(it) }
    AppBottomNavItem.TODOS -> todoTrackerId?.let { AppShellDestination.TodoRoot(it) }
    AppBottomNavItem.GOALS -> goalsTrackerId?.let { AppShellDestination.GoalsRoot(it) }
}
