package com.mikeisesele.clearr.ui.navigation

sealed class NavRoutes(val route: String) {
    object Setup : NavRoutes("setup")
    /** Tracker list — the new home screen */
    object TrackerList : NavRoutes("tracker_list")
    /** Detail / dues grid for a specific tracker */
    object TrackerDetail : NavRoutes("tracker_detail/{trackerId}") {
        fun createRoute(trackerId: Long) = "tracker_detail/$trackerId"
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
    object Settings : NavRoutes("settings")
    /** Kept for backward-compat: bottom nav still references Home route string */
    object Home : NavRoutes("tracker_list")
}
