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

val AppShellDestination.kind: AppShellDestinationKind
    get() = when (this) {
        AppShellDestination.Dashboard -> AppShellDestinationKind.DASHBOARD
        is AppShellDestination.BudgetRoot -> AppShellDestinationKind.BUDGET_ROOT
        is AppShellDestination.TodoRoot -> AppShellDestinationKind.TODO_ROOT
        is AppShellDestination.GoalsRoot -> AppShellDestinationKind.GOALS_ROOT
        is AppShellDestination.TodoAdd -> AppShellDestinationKind.TODO_ADD
        is AppShellDestination.GoalAdd -> AppShellDestinationKind.GOAL_ADD
        is AppShellDestination.BudgetAddCategory -> AppShellDestinationKind.BUDGET_ADD_CATEGORY
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

    fun createDestination(trackerId: Long? = null): AppShellDestination = when (this) {
        DASHBOARD -> AppShellDestination.Dashboard
        BUDGET_ROOT -> AppShellDestination.BudgetRoot(requireNotNull(trackerId))
        TODO_ROOT -> AppShellDestination.TodoRoot(requireNotNull(trackerId))
        GOALS_ROOT -> AppShellDestination.GoalsRoot(requireNotNull(trackerId))
        TODO_ADD -> AppShellDestination.TodoAdd(requireNotNull(trackerId))
        GOAL_ADD -> AppShellDestination.GoalAdd(requireNotNull(trackerId))
        BUDGET_ADD_CATEGORY -> AppShellDestination.BudgetAddCategory(requireNotNull(trackerId))
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

fun AppShellDestination.topLevelDestination(): AppShellDestination = when (this) {
    AppShellDestination.Dashboard -> AppShellDestination.Dashboard
    is AppShellDestination.BudgetRoot -> this
    is AppShellDestination.TodoRoot -> this
    is AppShellDestination.GoalsRoot -> this
    is AppShellDestination.TodoAdd -> AppShellDestination.TodoRoot(trackerId)
    is AppShellDestination.GoalAdd -> AppShellDestination.GoalsRoot(trackerId)
    is AppShellDestination.BudgetAddCategory -> AppShellDestination.BudgetRoot(trackerId)
}

fun AppShellDestination.isTopLevelDestination(): Boolean = this == topLevelDestination()

fun AppShellDestination.isAddFlowDestination(): Boolean = when (this) {
    is AppShellDestination.TodoAdd,
    is AppShellDestination.GoalAdd,
    is AppShellDestination.BudgetAddCategory -> true
    else -> false
}

fun AppShellDestination.backDestinationOrNull(): AppShellDestination? = when (this) {
    AppShellDestination.Dashboard -> null
    is AppShellDestination.BudgetRoot,
    is AppShellDestination.TodoRoot,
    is AppShellDestination.GoalsRoot -> AppShellDestination.Dashboard
    is AppShellDestination.TodoAdd,
    is AppShellDestination.GoalAdd,
    is AppShellDestination.BudgetAddCategory -> topLevelDestination()
}

fun AppShellDestination.addFlowDestinationOrNull(): AppShellDestination? = when (this) {
    is AppShellDestination.BudgetRoot -> AppShellDestination.BudgetAddCategory(trackerId)
    is AppShellDestination.TodoRoot -> AppShellDestination.TodoAdd(trackerId)
    is AppShellDestination.GoalsRoot -> AppShellDestination.GoalAdd(trackerId)
    AppShellDestination.Dashboard,
    is AppShellDestination.TodoAdd,
    is AppShellDestination.GoalAdd,
    is AppShellDestination.BudgetAddCategory -> null
}
