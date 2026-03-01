package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.ui.navigation.components.AppBottomNavItem

fun String?.isBottomNavRoute(): Boolean = when {
    this == null -> false
    this == NavRoutes.Dashboard.route -> true
    this.startsWith(NavRoutes.BudgetRoot.baseRoute) -> true
    this.startsWith(NavRoutes.TodoRoot.baseRoute) -> true
    this.startsWith(NavRoutes.GoalsRoot.baseRoute) -> true
    else -> false
}

fun String?.toBottomNavItem(): AppBottomNavItem? = when {
    this == NavRoutes.Dashboard.route -> AppBottomNavItem.HOME
    this?.startsWith(NavRoutes.BudgetRoot.baseRoute) == true -> AppBottomNavItem.BUDGET
    this?.startsWith(NavRoutes.TodoRoot.baseRoute) == true -> AppBottomNavItem.TODOS
    this?.startsWith(NavRoutes.GoalsRoot.baseRoute) == true -> AppBottomNavItem.GOALS
    else -> null
}

fun String?.isTopLevelNonDashboardRoute(): Boolean = when {
    this == null -> false
    this.startsWith(NavRoutes.BudgetRoot.baseRoute) -> true
    this.startsWith(NavRoutes.TodoRoot.baseRoute) -> true
    this.startsWith(NavRoutes.GoalsRoot.baseRoute) -> true
    else -> false
}

fun AppShellUiState.routeFor(item: AppBottomNavItem): String? = when (item) {
    AppBottomNavItem.HOME -> NavRoutes.Dashboard.route
    AppBottomNavItem.BUDGET -> budgetTrackerId?.let { NavRoutes.BudgetRoot.createRoute(it) }
    AppBottomNavItem.TODOS -> todoTrackerId?.let { NavRoutes.TodoRoot.createRoute(it) }
    AppBottomNavItem.GOALS -> goalsTrackerId?.let { NavRoutes.GoalsRoot.createRoute(it) }
}
