package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.ui.navigation.components.AppBottomNavItem

object AppShellRouteArgs {
    const val TRACKER_ID = "trackerId"
}

sealed interface AppShellDestination {
    val route: String

    data object Dashboard : AppShellDestination {
        override val route: String = AppShellDestinationKind.DASHBOARD.createRoute()
    }

    data class BudgetRoot(val trackerId: Long) : AppShellDestination {
        override val route: String = AppShellDestinationKind.BUDGET_ROOT.createRoute(trackerId)
    }

    data class TodoRoot(val trackerId: Long) : AppShellDestination {
        override val route: String = AppShellDestinationKind.TODO_ROOT.createRoute(trackerId)
    }

    data class GoalsRoot(val trackerId: Long) : AppShellDestination {
        override val route: String = AppShellDestinationKind.GOALS_ROOT.createRoute(trackerId)
    }

    data class TodoAdd(val trackerId: Long) : AppShellDestination {
        override val route: String = AppShellDestinationKind.TODO_ADD.createRoute(trackerId)
    }

    data class GoalAdd(val trackerId: Long) : AppShellDestination {
        override val route: String = AppShellDestinationKind.GOAL_ADD.createRoute(trackerId)
    }

    data class BudgetAddCategory(val trackerId: Long) : AppShellDestination {
        override val route: String = AppShellDestinationKind.BUDGET_ADD_CATEGORY.createRoute(trackerId)
    }
}

enum class AppShellDestinationKind(
    val routePattern: String,
    val baseRoute: String,
    val bottomNavItem: AppBottomNavItem? = null
) {
    DASHBOARD("dashboard", "dashboard", AppBottomNavItem.HOME),
    BUDGET_ROOT("budget_root/{${AppShellRouteArgs.TRACKER_ID}}", "budget_root", AppBottomNavItem.BUDGET),
    TODO_ROOT("todo_root/{${AppShellRouteArgs.TRACKER_ID}}", "todo_root", AppBottomNavItem.TODOS),
    GOALS_ROOT("goals_root/{${AppShellRouteArgs.TRACKER_ID}}", "goals_root", AppBottomNavItem.GOALS),
    TODO_ADD("todo_add/{${AppShellRouteArgs.TRACKER_ID}}", "todo_add"),
    GOAL_ADD("goal_add/{${AppShellRouteArgs.TRACKER_ID}}", "goal_add"),
    BUDGET_ADD_CATEGORY("budget_add_category/{${AppShellRouteArgs.TRACKER_ID}}", "budget_add_category");

    fun createRoute(trackerId: Long? = null): String = when (this) {
        DASHBOARD -> baseRoute
        else -> "$baseRoute/${requireNotNull(trackerId)}"
    }
}

fun String?.toAppShellDestinationKind(): AppShellDestinationKind? = when {
    this == null -> null
    this == AppShellDestinationKind.DASHBOARD.baseRoute -> AppShellDestinationKind.DASHBOARD
    this.startsWith(AppShellDestinationKind.BUDGET_ROOT.baseRoute) -> AppShellDestinationKind.BUDGET_ROOT
    this.startsWith(AppShellDestinationKind.TODO_ROOT.baseRoute) -> AppShellDestinationKind.TODO_ROOT
    this.startsWith(AppShellDestinationKind.GOALS_ROOT.baseRoute) -> AppShellDestinationKind.GOALS_ROOT
    this.startsWith(AppShellDestinationKind.TODO_ADD.baseRoute) -> AppShellDestinationKind.TODO_ADD
    this.startsWith(AppShellDestinationKind.GOAL_ADD.baseRoute) -> AppShellDestinationKind.GOAL_ADD
    this.startsWith(AppShellDestinationKind.BUDGET_ADD_CATEGORY.baseRoute) -> AppShellDestinationKind.BUDGET_ADD_CATEGORY
    else -> null
}
