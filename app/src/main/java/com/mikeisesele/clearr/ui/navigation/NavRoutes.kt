package com.mikeisesele.clearr.ui.navigation

sealed class NavRoutes(val route: String) {
    object Dashboard : NavRoutes("dashboard")
    object BudgetRoot : NavRoutes("budget_root/{trackerId}") {
        fun createRoute(trackerId: Long) = "budget_root/$trackerId"
        const val baseRoute = "budget_root"
    }
    object TodoRoot : NavRoutes("todo_root/{trackerId}") {
        fun createRoute(trackerId: Long) = "todo_root/$trackerId"
        const val baseRoute = "todo_root"
    }
    object GoalsRoot : NavRoutes("goals_root/{trackerId}") {
        fun createRoute(trackerId: Long) = "goals_root/$trackerId"
        const val baseRoute = "goals_root"
    }
    object TodoAdd : NavRoutes("todo_add/{trackerId}") {
        fun createRoute(trackerId: Long) = "todo_add/$trackerId"
    }
    object GoalAdd : NavRoutes("goal_add/{trackerId}") {
        fun createRoute(trackerId: Long) = "goal_add/$trackerId"
    }
    object BudgetAddCategory : NavRoutes("budget_add_category/{trackerId}") {
        fun createRoute(trackerId: Long) = "budget_add_category/$trackerId"
    }
    object Home : NavRoutes("dashboard")
}
