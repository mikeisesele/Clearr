package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.ui.navigation.components.AppBottomNavItem

sealed interface AppShellDestination {
    val route: String

    data object Dashboard : AppShellDestination {
        override val route: String = NavRoutes.Dashboard.route
    }

    data class BudgetRoot(val trackerId: Long) : AppShellDestination {
        override val route: String = NavRoutes.BudgetRoot.createRoute(trackerId)
    }

    data class TodoRoot(val trackerId: Long) : AppShellDestination {
        override val route: String = NavRoutes.TodoRoot.createRoute(trackerId)
    }

    data class GoalsRoot(val trackerId: Long) : AppShellDestination {
        override val route: String = NavRoutes.GoalsRoot.createRoute(trackerId)
    }

    data class TodoAdd(val trackerId: Long) : AppShellDestination {
        override val route: String = NavRoutes.TodoAdd.createRoute(trackerId)
    }

    data class GoalAdd(val trackerId: Long) : AppShellDestination {
        override val route: String = NavRoutes.GoalAdd.createRoute(trackerId)
    }

    data class BudgetAddCategory(val trackerId: Long) : AppShellDestination {
        override val route: String = NavRoutes.BudgetAddCategory.createRoute(trackerId)
    }
}

enum class AppShellDestinationKind(
    val routePrefix: String,
    val bottomNavItem: AppBottomNavItem? = null
) {
    DASHBOARD(NavRoutes.Dashboard.route, AppBottomNavItem.HOME),
    BUDGET_ROOT(NavRoutes.BudgetRoot.baseRoute, AppBottomNavItem.BUDGET),
    TODO_ROOT(NavRoutes.TodoRoot.baseRoute, AppBottomNavItem.TODOS),
    GOALS_ROOT(NavRoutes.GoalsRoot.baseRoute, AppBottomNavItem.GOALS),
    TODO_ADD("todo_add"),
    GOAL_ADD("goal_add"),
    BUDGET_ADD_CATEGORY("budget_add_category");
}

fun String?.toAppShellDestinationKind(): AppShellDestinationKind? = when {
    this == null -> null
    this == NavRoutes.Dashboard.route -> AppShellDestinationKind.DASHBOARD
    this.startsWith(NavRoutes.BudgetRoot.baseRoute) -> AppShellDestinationKind.BUDGET_ROOT
    this.startsWith(NavRoutes.TodoRoot.baseRoute) -> AppShellDestinationKind.TODO_ROOT
    this.startsWith(NavRoutes.GoalsRoot.baseRoute) -> AppShellDestinationKind.GOALS_ROOT
    this.startsWith("todo_add") -> AppShellDestinationKind.TODO_ADD
    this.startsWith("goal_add") -> AppShellDestinationKind.GOAL_ADD
    this.startsWith("budget_add_category") -> AppShellDestinationKind.BUDGET_ADD_CATEGORY
    else -> null
}
