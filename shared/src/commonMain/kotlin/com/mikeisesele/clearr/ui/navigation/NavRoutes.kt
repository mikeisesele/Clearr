package com.mikeisesele.clearr.ui.navigation

sealed class NavRoutes(val route: String) {
    data object Dashboard : NavRoutes("dashboard")
    data object BudgetRoot : NavRoutes("budget_root/{trackerId}") {
        fun createRoute(trackerId: Long) = "budget_root/$trackerId"
        const val baseRoute = "budget_root"
    }
    data object TodoRoot : NavRoutes("todo_root/{trackerId}") {
        fun createRoute(trackerId: Long) = "todo_root/$trackerId"
        const val baseRoute = "todo_root"
    }
    data object GoalsRoot : NavRoutes("goals_root/{trackerId}") {
        fun createRoute(trackerId: Long) = "goals_root/$trackerId"
        const val baseRoute = "goals_root"
    }
    data object TodoAdd : NavRoutes("todo_add/{trackerId}") {
        fun createRoute(trackerId: Long) = "todo_add/$trackerId"
    }
    data object GoalAdd : NavRoutes("goal_add/{trackerId}") {
        fun createRoute(trackerId: Long) = "goal_add/$trackerId"
    }
    data object BudgetAddCategory : NavRoutes("budget_add_category/{trackerId}") {
        fun createRoute(trackerId: Long) = "budget_add_category/$trackerId"
    }
    data object Home : NavRoutes("dashboard")
}
