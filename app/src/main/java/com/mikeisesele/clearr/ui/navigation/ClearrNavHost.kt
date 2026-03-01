package com.mikeisesele.clearr.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mikeisesele.clearr.ui.commons.state.ThemeMode
import com.mikeisesele.clearr.ui.feature.budget.AddBudgetCategoryScreen
import com.mikeisesele.clearr.ui.feature.budget.BudgetDetailScreen
import com.mikeisesele.clearr.ui.feature.dashboard.DashboardRoute
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerType
import com.mikeisesele.clearr.ui.feature.goals.AddGoalScreen
import com.mikeisesele.clearr.ui.feature.goals.GoalsDetailScreen
import com.mikeisesele.clearr.ui.feature.onboarding.CompletionScreen
import com.mikeisesele.clearr.ui.feature.onboarding.OnboardingAction
import com.mikeisesele.clearr.ui.feature.onboarding.OnboardingScreen
import com.mikeisesele.clearr.ui.feature.onboarding.OnboardingViewModel
import com.mikeisesele.clearr.ui.feature.onboarding.SplashScreen
import com.mikeisesele.clearr.ui.feature.todo.AddTodoRoute
import com.mikeisesele.clearr.ui.feature.todo.TodoRoute
import com.mikeisesele.clearr.ui.navigation.components.AppBottomNav
import com.mikeisesele.clearr.ui.navigation.components.AppBottomNavItem
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors

@Composable
fun ClearrNavHost(onThemeChange: (ThemeMode) -> Unit = {}) {
    val onboardingVm: OnboardingViewModel = hiltViewModel()
    val appConfigVm: AppConfigViewModel = hiltViewModel()

    val onboardingState by onboardingVm.uiState.collectAsStateWithLifecycle()
    val appConfigState by appConfigVm.uiState.collectAsStateWithLifecycle()
    AppShellRoot(
        onboardingState = onboardingState,
        appConfigState = appConfigState,
        renderLoading = { darkIcons -> ApplySystemBars(darkIcons = darkIcons) },
        renderOnboarding = { darkIcons ->
            ApplySystemBars(darkIcons = darkIcons)
            OnboardingNavHost(onboardingVm = onboardingVm)
        },
        renderMainShell = { darkIcons ->
            ApplySystemBars(darkIcons = darkIcons)
            MainNavHost(onThemeChange = onThemeChange)
        }
    )
}

@Composable
private fun OnboardingNavHost(onboardingVm: OnboardingViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = OnboardingFlowRoute.Splash.route) {
        composable(OnboardingFlowRoute.Splash.route) {
            SplashScreen(onGetStarted = { navController.navigate(OnboardingFlowRoute.Slide.createRoute(0)) })
        }
        composable(
            route = OnboardingFlowRoute.Slide.route,
            arguments = listOf(navArgument(OnboardingFlowRoute.Slide.argument) { type = NavType.IntType })
        ) { backStack ->
            val initialSlide = backStack.arguments?.getInt(OnboardingFlowRoute.Slide.argument) ?: 0
            OnboardingScreen(
                initialSlide = initialSlide,
                onComplete = {
                    onboardingVm.onAction(OnboardingAction.CompleteOnboarding)
                    navController.navigate(OnboardingFlowRoute.Completion.route) {
                        popUpTo(OnboardingFlowRoute.Splash.route) { inclusive = true }
                    }
                },
                onSkip = {
                    onboardingVm.onAction(OnboardingAction.CompleteOnboarding)
                    navController.navigate(OnboardingFlowRoute.Completion.route) {
                        popUpTo(OnboardingFlowRoute.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(OnboardingFlowRoute.Completion.route) {
            CompletionScreen(onOpenApp = {})
        }
    }
}

@Composable
private fun MainNavHost(onThemeChange: (ThemeMode) -> Unit) {
    val navController = rememberNavController()
    val shellNavigator = rememberAppShellNavigator()
    val shellNavState by shellNavigator.state.collectAsState()
    val shellViewModel: AppShellViewModel = hiltViewModel()
    val shellState by shellViewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalClearrUiColors.current
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack.toAppShellDestination()
    val currentRoute = currentBackStack?.destination?.route

    BackHandler(enabled = currentRoute.isTopLevelNonDashboardRoute()) {
        shellNavigator.openTopLevel(AppShellDestination.Dashboard)
    }

    BackHandler(enabled = currentRoute.isAddFlowRoute()) {
        shellNavigator.pop()
    }

    LaunchedEffect(shellNavState.current, currentDestination) {
        val desired = shellNavState.current
        if (currentDestination == desired) return@LaunchedEffect

        val currentTopLevel = currentDestination?.topLevelDestination()
        when {
            currentDestination != null &&
                !currentDestination.isTopLevelDestination() &&
                desired == currentTopLevel -> {
                navController.popBackStack()
            }

            desired.isTopLevelDestination() -> navController.navigateTopLevel(desired)
            else -> navController.navigate(desired.route)
        }
    }

    androidx.compose.material3.Scaffold(
        containerColor = colors.bg,
        bottomBar = {
            if (currentRoute.isBottomNavRoute()) {
                AppBottomNav(
                    selectedItem = currentDestination?.toBottomNavItem() ?: currentRoute?.toBottomNavItem(),
                    onSelect = { item ->
                        shellState.destinationFor(item)?.let(shellNavigator::openTopLevel)
                    }
                )
            }
        }
    ) { innerPadding ->
        Surface(color = colors.bg) {
            NavHost(
                navController = navController,
                startDestination = AppShellDestinationKind.DASHBOARD.routePattern,
                modifier = Modifier.background(colors.bg).padding(innerPadding)
            ) {
                composable(AppShellDestinationKind.DASHBOARD.routePattern) {
                    DashboardRoute(
                        onOpenBudget = { shellState.destinationFor(DashboardTrackerType.BUDGET)?.let(shellNavigator::openTopLevel) },
                        onOpenTodos = { shellState.destinationFor(DashboardTrackerType.TODOS)?.let(shellNavigator::openTopLevel) },
                        onOpenGoals = { shellState.destinationFor(DashboardTrackerType.GOALS)?.let(shellNavigator::openTopLevel) }
                    )
                }
                composable(
                    route = AppShellDestinationKind.BUDGET_ROOT.routePattern,
                    arguments = listOf(navArgument(AppShellRouteArgs.TRACKER_ID) { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong(AppShellRouteArgs.TRACKER_ID) ?: return@composable
                    BudgetDetailScreen(
                        trackerId = trackerId,
                        onAddCategory = { shellNavigator.push(AppShellDestination.BudgetAddCategory(trackerId)) }
                    )
                }
                composable(
                    route = AppShellDestinationKind.TODO_ROOT.routePattern,
                    arguments = listOf(navArgument(AppShellRouteArgs.TRACKER_ID) { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong(AppShellRouteArgs.TRACKER_ID) ?: return@composable
                    TodoRoute(
                        trackerId = trackerId,
                        onAddTodo = { shellNavigator.push(AppShellDestination.TodoAdd(trackerId)) }
                    )
                }
                composable(
                    route = AppShellDestinationKind.GOALS_ROOT.routePattern,
                    arguments = listOf(navArgument(AppShellRouteArgs.TRACKER_ID) { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong(AppShellRouteArgs.TRACKER_ID) ?: return@composable
                    GoalsDetailScreen(
                        trackerId = trackerId,
                        onAddGoal = { shellNavigator.push(AppShellDestination.GoalAdd(trackerId)) }
                    )
                }
                composable(
                    route = AppShellDestinationKind.TODO_ADD.routePattern,
                    arguments = listOf(navArgument(AppShellRouteArgs.TRACKER_ID) { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong(AppShellRouteArgs.TRACKER_ID) ?: return@composable
                    AddTodoRoute(
                        trackerId = trackerId,
                        onClose = { shellNavigator.pop() }
                    )
                }
                composable(
                    route = AppShellDestinationKind.GOAL_ADD.routePattern,
                    arguments = listOf(navArgument(AppShellRouteArgs.TRACKER_ID) { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong(AppShellRouteArgs.TRACKER_ID) ?: return@composable
                    AddGoalScreen(
                        trackerId = trackerId,
                        onClose = { shellNavigator.pop() }
                    )
                }
                composable(
                    route = AppShellDestinationKind.BUDGET_ADD_CATEGORY.routePattern,
                    arguments = listOf(navArgument(AppShellRouteArgs.TRACKER_ID) { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong(AppShellRouteArgs.TRACKER_ID) ?: return@composable
                    AddBudgetCategoryScreen(
                        trackerId = trackerId,
                        onClose = { shellNavigator.pop() }
                    )
                }
            }
        }
    }
}

private fun NavHostController.navigateTopLevel(
    destination: AppShellDestination,
    builder: androidx.navigation.NavOptionsBuilder.() -> Unit = {}
) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
        builder()
    }
}

private fun NavBackStackEntry?.toAppShellDestination(): AppShellDestination? {
    val entry = this ?: return null
    val kind = entry.destination.route.toAppShellDestinationKind() ?: return null
    val trackerId = if (kind == AppShellDestinationKind.DASHBOARD) null
    else entry.arguments?.getLong(AppShellRouteArgs.TRACKER_ID)
    return kind.createDestination(trackerId)
}
