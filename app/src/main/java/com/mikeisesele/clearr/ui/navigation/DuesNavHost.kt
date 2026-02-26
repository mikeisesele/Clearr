package com.mikeisesele.clearr.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import com.mikeisesele.clearr.ui.feature.budget.AddBudgetCategoryScreen
import com.mikeisesele.clearr.ui.feature.budget.BudgetDetailScreen
import com.mikeisesele.clearr.ui.feature.goals.AddGoalScreen
import com.mikeisesele.clearr.ui.feature.goals.GoalsDetailScreen
import com.mikeisesele.clearr.ui.feature.home.HomeScreen
import com.mikeisesele.clearr.ui.feature.onboarding.CompletionScreen
import com.mikeisesele.clearr.ui.feature.onboarding.OnboardingAction
import com.mikeisesele.clearr.ui.feature.onboarding.OnboardingScreen
import com.mikeisesele.clearr.ui.feature.onboarding.OnboardingViewModel
import com.mikeisesele.clearr.ui.feature.onboarding.SplashScreen
import com.mikeisesele.clearr.ui.feature.settings.SettingsScreen
import com.mikeisesele.clearr.ui.feature.setup.SetupWizardScreen
import com.mikeisesele.clearr.ui.feature.todo.AddTodoScreen
import com.mikeisesele.clearr.ui.feature.todo.TodoDetailScreen
import com.mikeisesele.clearr.ui.feature.trackerlist.TrackerListScreen
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

/**
 * Root composable — resolves the correct start destination:
 *
 *  1. Still loading DataStore or Room → blank screen (avoid flash)
 *  2. Onboarding NOT complete → splash / onboarding / completion flow
 *  3. Onboarding complete → main app
 */
@Composable
fun DuesNavHost(onThemeChange: (ThemeMode) -> Unit) {

    val onboardingVm: OnboardingViewModel = hiltViewModel()
    val appConfigVm: AppConfigViewModel   = hiltViewModel()

    // null = still loading DataStore
    val onboardingState by onboardingVm.uiState.collectAsStateWithLifecycle()
    val appConfigState by appConfigVm.uiState.collectAsStateWithLifecycle()
    val onboardingComplete = onboardingState.isComplete
    val appConfigLoading = appConfigState.isLoading

    val colors = LocalDuesColors.current

    // Show blank until both DataStore and Room have emitted at least once.
    if (onboardingComplete == null || appConfigLoading) {
        ApplySystemBars(darkIcons = false)
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
    val colors = LocalDuesColors.current
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val darkIcons = when (currentRoute) {
        "splash",
        "onboarding/{slideIndex}",
        "onboarding_complete" -> true
        "setup_wizard" -> !colors.isDark
        else -> !colors.isDark
    }
    ApplySystemBars(darkIcons = darkIcons)

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
// Main app navigation
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MainNavHost(onThemeChange: (ThemeMode) -> Unit) {
    val navController = rememberNavController()
    val colors = LocalDuesColors.current
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    ApplySystemBars(darkIcons = !colors.isDark)

    val topLevelNonHomeRoutes = setOf(
        NavRoutes.Settings.route,
        NavRoutes.Setup.route
    )

    BackHandler(enabled = currentRoute in topLevelNonHomeRoutes) {
        navController.navigate(NavRoutes.TrackerList.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Surface(color = colors.bg) {
        NavHost(
            navController = navController,
            startDestination = NavRoutes.TrackerList.route
        ) {
            composable(NavRoutes.TrackerList.route) {
                TrackerListScreen(
                    onTrackerClick = { trackerId ->
                        navController.navigate(NavRoutes.TrackerDetail.createRoute(trackerId))
                    },
                    onCreateTracker = {
                        navController.navigate(NavRoutes.Setup.route) { launchSingleTop = true }
                    },
                    onOpenSettings = {
                        navController.navigate(NavRoutes.Settings.route) { launchSingleTop = true }
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
                        onNavigateBack = { navController.popBackStack() },
                        onAddCategory = { navController.navigate(NavRoutes.BudgetAddCategory.createRoute(trackerId)) }
                    )
                } else if (detailState.trackerType == TrackerType.TODO) {
                    TodoDetailScreen(
                        trackerId = trackerId,
                        onNavigateBack = { navController.popBackStack() },
                        onAddTodo = { navController.navigate(NavRoutes.TodoAdd.createRoute(trackerId)) }
                    )
                } else if (detailState.trackerType == TrackerType.GOALS) {
                    GoalsDetailScreen(
                        trackerId = trackerId,
                        onNavigateBack = { navController.popBackStack() },
                        onAddGoal = { navController.navigate(NavRoutes.GoalAdd.createRoute(trackerId)) }
                    )
                } else {
                    HomeScreen(
                        trackerId = trackerId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(
                route = NavRoutes.TodoAdd.route,
                arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                AddTodoScreen(
                    trackerId = trackerId,
                    onClose = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.GoalAdd.route,
                arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                AddGoalScreen(
                    trackerId = trackerId,
                    onClose = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.BudgetAddCategory.route,
                arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                AddBudgetCategoryScreen(
                    trackerId = trackerId,
                    onClose = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.Settings.route)   { SettingsScreen(onThemeChange = onThemeChange) }
        }
    }
}
