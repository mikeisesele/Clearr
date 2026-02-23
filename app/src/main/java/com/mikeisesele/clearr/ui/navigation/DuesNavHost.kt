package com.mikeisesele.clearr.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mikeisesele.clearr.ui.commons.state.ThemeMode
import com.mikeisesele.clearr.ui.feature.analytics.AnalyticsScreen
import com.mikeisesele.clearr.ui.feature.budget.BudgetDetailScreen
import com.mikeisesele.clearr.ui.feature.home.HomeScreen
import com.mikeisesele.clearr.ui.feature.onboarding.CompletionScreen
import com.mikeisesele.clearr.ui.feature.onboarding.OnboardingAction
import com.mikeisesele.clearr.ui.feature.onboarding.OnboardingScreen
import com.mikeisesele.clearr.ui.feature.onboarding.OnboardingViewModel
import com.mikeisesele.clearr.ui.feature.onboarding.SplashScreen
import com.mikeisesele.clearr.ui.feature.settings.SettingsScreen
import com.mikeisesele.clearr.ui.feature.setup.SetupWizardScreen
import com.mikeisesele.clearr.ui.feature.trackerlist.TrackerListScreen
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

data class BottomNavItem(
    val route: String,
    val icon: String,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(NavRoutes.TrackerList.route, "📋", "Trackers"),
    BottomNavItem(NavRoutes.Analytics.route,   "📊", "Analytics"),
    BottomNavItem(NavRoutes.Settings.route,    "⚙️", "Settings"),
)

/**
 * Root composable — resolves the correct start destination:
 *
 *  1. Still loading DataStore or Room → blank screen (avoid flash)
 *  2. Onboarding NOT complete → splash / onboarding / completion flow
 *  3. Onboarding complete + setup NOT complete → wizard
 *  4. Both complete → main app with bottom nav
 */
@Composable
fun DuesNavHost(onThemeChange: (ThemeMode) -> Unit) {

    val onboardingVm: OnboardingViewModel = hiltViewModel()
    val appConfigVm: AppConfigViewModel   = hiltViewModel()

    // null = still loading DataStore
    val onboardingState by onboardingVm.uiState.collectAsStateWithLifecycle()
    val appConfigState by appConfigVm.uiState.collectAsStateWithLifecycle()
    val onboardingComplete = onboardingState.isComplete
    val appConfig = appConfigState.appConfig
    val appConfigLoading = appConfigState.isLoading

    val colors = LocalDuesColors.current

    // Show blank until both DataStore and Room have emitted at least once.
    if (onboardingComplete == null || appConfigLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (onboardingComplete == null) ClearrColors.Violet else colors.bg)
        )
        return
    }

    if (onboardingComplete == false) {
        // ── ONBOARDING FLOW ────────────────────────────────────────────────────
        OnboardingNavHost(onboardingVm = onboardingVm)
    } else if (appConfig?.setupComplete != true) {
        // ── SETUP WIZARD (onboarding done, app not yet configured) ─────────────
        SetupWizardScreen(onSetupComplete = {})
    } else {
        // ── MAIN APP ──────────────────────────────────────────────────────────
        MainNavHost(onThemeChange = onThemeChange)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Onboarding sub-nav  Splash → Slides → Completion → SetupWizard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OnboardingNavHost(onboardingVm: OnboardingViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onGetStarted = { navController.navigate("onboarding/0") }
            )
        }

        composable(
            route = "onboarding/{slideIndex}",
            arguments = listOf(navArgument("slideIndex") { type = NavType.IntType })
        ) { backStack ->
            val initialSlide = backStack.arguments?.getInt("slideIndex") ?: 0
            OnboardingScreen(
                initialSlide = initialSlide,
                onComplete = {
                    onboardingVm.onAction(OnboardingAction.CompleteOnboarding)
                    navController.navigate("onboarding_complete") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onSkip = {
                    onboardingVm.onAction(OnboardingAction.CompleteOnboarding)
                    navController.navigate("onboarding_complete") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding_complete") {
            CompletionScreen(
                onCreateTracker = {
                    navController.navigate("setup_wizard") {
                        popUpTo("onboarding_complete") { inclusive = true }
                    }
                }
            )
        }

        composable("setup_wizard") {
            SetupWizardScreen(onSetupComplete = {})
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Main app with bottom navigation
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MainNavHost(onThemeChange: (ThemeMode) -> Unit) {
    val navController = rememberNavController()
    val colors = LocalDuesColors.current
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val showBottomBar = currentRoute?.startsWith("tracker_detail") != true &&
        currentRoute != NavRoutes.Setup.route

    Scaffold(
        containerColor = colors.bg,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = colors.surface,
                    tonalElevation = 0.dp,
                    windowInsets = NavigationBarDefaults.windowInsets
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(item.icon, fontSize = 17.sp) },
                            label = {
                                Text(
                                    item.label,
                                    fontSize = 10.sp,
                                    color = if (isSelected) colors.accent else colors.muted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = colors.accent.copy(alpha = 0.15f),
                                selectedTextColor = colors.accent,
                                unselectedTextColor = colors.muted
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.TrackerList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.TrackerList.route) {
                TrackerListScreen(
                    onTrackerClick = { trackerId ->
                        navController.navigate(NavRoutes.TrackerDetail.createRoute(trackerId))
                    },
                    onSettingsClick = {
                        navController.navigate(NavRoutes.Settings.route) { launchSingleTop = true }
                    },
                    onCreateTracker = {
                        navController.navigate(NavRoutes.Setup.route) { launchSingleTop = true }
                    }
                )
            }

            composable(NavRoutes.Setup.route) {
                SetupWizardScreen(
                    onSetupComplete = {
                        navController.navigate(NavRoutes.TrackerList.route) {
                            popUpTo(NavRoutes.TrackerList.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = NavRoutes.TrackerDetail.route,
                arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                val detailVm: TrackerDetailHostViewModel = hiltViewModel()
                val detailState by detailVm.uiState.collectAsStateWithLifecycle()
                if (detailState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.bg),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.accent)
                    }
                } else if (detailState.trackerType == TrackerType.BUDGET) {
                    BudgetDetailScreen(
                        trackerId = trackerId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                } else {
                    HomeScreen(
                        trackerId = trackerId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(NavRoutes.Analytics.route)  { AnalyticsScreen() }
            composable(NavRoutes.Settings.route)   { SettingsScreen(onThemeChange = onThemeChange) }
        }
    }
}
