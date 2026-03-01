package com.mikeisesele.clearr.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
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
    val shellViewModel: AppShellViewModel = hiltViewModel()
    val shellState by shellViewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalClearrUiColors.current
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    BackHandler(enabled = currentRoute.isTopLevelNonDashboardRoute()) {
        navController.navigate(NavRoutes.Dashboard.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    androidx.compose.material3.Scaffold(
        containerColor = colors.bg,
        bottomBar = {
            if (currentRoute.isBottomNavRoute()) {
                AppBottomNav(
                    selectedItem = currentRoute?.toBottomNavItem(),
                    onSelect = { item ->
                        shellState.routeFor(item)?.let(navController::navigateTopLevel)
                    }
                )
            }
        }
    ) { innerPadding ->
        Surface(color = colors.bg) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.Dashboard.route,
                modifier = Modifier.background(colors.bg).padding(innerPadding)
            ) {
                composable(NavRoutes.Dashboard.route) {
                    DashboardRoute(
                        onOpenBudget = { shellState.budgetTrackerId?.let { navController.navigateTopLevel(NavRoutes.BudgetRoot.createRoute(it)) } },
                        onOpenTodos = { shellState.todoTrackerId?.let { navController.navigateTopLevel(NavRoutes.TodoRoot.createRoute(it)) } },
                        onOpenGoals = { shellState.goalsTrackerId?.let { navController.navigateTopLevel(NavRoutes.GoalsRoot.createRoute(it)) } }
                    )
                }
                composable(
                    route = NavRoutes.BudgetRoot.route,
                    arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                    BudgetDetailScreen(trackerId = trackerId, onAddCategory = { navController.navigate(NavRoutes.BudgetAddCategory.createRoute(trackerId)) })
                }
                composable(
                    route = NavRoutes.TodoRoot.route,
                    arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                    TodoRoute(trackerId = trackerId, onAddTodo = { navController.navigate(NavRoutes.TodoAdd.createRoute(trackerId)) })
                }
                composable(
                    route = NavRoutes.GoalsRoot.route,
                    arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                    GoalsDetailScreen(trackerId = trackerId, onAddGoal = { navController.navigate(NavRoutes.GoalAdd.createRoute(trackerId)) })
                }
                composable(
                    route = NavRoutes.TodoAdd.route,
                    arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                    AddTodoRoute(trackerId = trackerId, onClose = { navController.popBackStack() })
                }
                composable(
                    route = NavRoutes.GoalAdd.route,
                    arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                    AddGoalScreen(trackerId = trackerId, onClose = { navController.popBackStack() })
                }
                composable(
                    route = NavRoutes.BudgetAddCategory.route,
                    arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                    AddBudgetCategoryScreen(trackerId = trackerId, onClose = { navController.popBackStack() })
                }
            }
        }
    }
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
