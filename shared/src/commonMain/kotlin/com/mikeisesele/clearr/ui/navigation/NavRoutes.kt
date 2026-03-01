package com.mikeisesele.clearr.ui.navigation

sealed class NavRoutes(val route: String) {
    companion object {
        const val TRACKER_ID_ARG = "trackerId"
    }

    data object Dashboard : NavRoutes("dashboard")
    data object BudgetRoot : NavRoutes("budget_root/{$TRACKER_ID_ARG}") {
        const val baseRoute = "budget_root"
        fun createRoute(trackerId: Long) = "budget_root/$trackerId"
    }
    data object TodoRoot : NavRoutes("todo_root/{$TRACKER_ID_ARG}") {
        const val baseRoute = "todo_root"
        fun createRoute(trackerId: Long) = "todo_root/$trackerId"
    }
    data object GoalsRoot : NavRoutes("goals_root/{$TRACKER_ID_ARG}") {
        const val baseRoute = "goals_root"
        fun createRoute(trackerId: Long) = "goals_root/$trackerId"
    }
    data object TodoAdd : NavRoutes("todo_add/{$TRACKER_ID_ARG}") {
        const val baseRoute = "todo_add"
        fun createRoute(trackerId: Long) = "todo_add/$trackerId"
    }
    data object GoalAdd : NavRoutes("goal_add/{$TRACKER_ID_ARG}") {
        const val baseRoute = "goal_add"
        fun createRoute(trackerId: Long) = "goal_add/$trackerId"
    }
    data object BudgetAddCategory : NavRoutes("budget_add_category/{$TRACKER_ID_ARG}") {
        const val baseRoute = "budget_add_category"
        fun createRoute(trackerId: Long) = "budget_add_category/$trackerId"
    }
    data object Home : NavRoutes("dashboard")
}
