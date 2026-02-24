package com.mikeisesele.clearr.ui.navigation

sealed class NavRoutes(val route: String) {
    object Setup : NavRoutes("setup")
    object QuickSetup : NavRoutes("quick_setup")
    /** Tracker list — the new home screen */
    object TrackerList : NavRoutes("tracker_list")
    /** Detail / dues grid for a specific tracker */
    object TrackerDetail : NavRoutes("tracker_detail/{trackerId}") {
        fun createRoute(trackerId: Long) = "tracker_detail/$trackerId"
    }
    object Analytics : NavRoutes("analytics")
    object Settings : NavRoutes("settings")
    /** Kept for backward-compat: bottom nav still references Home route string */
    object Home : NavRoutes("tracker_list")
}
