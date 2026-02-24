package com.mikeisesele.clearr.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.mikeisesele.clearr.ui.feature.setup.QuickSetupTypeScreen
import com.mikeisesele.clearr.ui.feature.setup.SetupWizardScreen
import com.mikeisesele.clearr.ui.feature.todo.AddTodoScreen
import com.mikeisesele.clearr.ui.feature.todo.TodoDetailScreen
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
        // ── QUICK SETUP ENTRY (onboarding done, app not yet configured) ───────
        var showWizard by rememberSaveable { mutableStateOf(false) }
        if (showWizard) {
            SetupWizardScreen(onSetupComplete = {})
        } else {
            QuickSetupTypeScreen(
                onOpenDuesWizard = { showWizard = true },
                onSetupComplete = {}
            )
        }
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
                    navController.navigate(NavRoutes.QuickSetup.route) {
                        popUpTo("onboarding_complete") { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.QuickSetup.route) {
            QuickSetupTypeScreen(
                onOpenDuesWizard = {
                    navController.navigate("setup_wizard") { launchSingleTop = true }
                },
                onSetupComplete = {}
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

    val showBottomBar = currentRoute == NavRoutes.TrackerList.route ||
        currentRoute == NavRoutes.Settings.route

    val topLevelNonHomeRoutes = setOf(
        NavRoutes.Settings.route,
        NavRoutes.QuickSetup.route,
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

    Scaffold(
        containerColor = colors.bg,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = colors.surface,
                    tonalElevation = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp0,
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
                            icon = { Text(item.icon, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp17) },
                            label = {
                                Text(
                                    item.label,
                                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp10,
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
                    onCreateTracker = {
                        navController.navigate(NavRoutes.QuickSetup.route) { launchSingleTop = true }
                    }
                )
            }

            composable(NavRoutes.QuickSetup.route) {
                QuickSetupTypeScreen(
                    onOpenDuesWizard = {
                        navController.navigate(NavRoutes.Setup.route) { launchSingleTop = true }
                    },
                    onSetupComplete = {
                        navController.navigate(NavRoutes.TrackerList.route) {
                            popUpTo(NavRoutes.TrackerList.route) { inclusive = false }
                            launchSingleTop = true
                        }
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

            composable(NavRoutes.Settings.route)   { SettingsScreen(onThemeChange = onThemeChange) }
        }
    }
}
