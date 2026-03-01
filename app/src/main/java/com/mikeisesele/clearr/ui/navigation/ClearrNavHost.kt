package com.mikeisesele.clearr.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.mikeisesele.clearr.ui.feature.todo.AddTodoScreen
import com.mikeisesele.clearr.ui.feature.todo.TodoRoute
import com.mikeisesele.clearr.ui.navigation.components.AppBottomNav
import com.mikeisesele.clearr.ui.navigation.components.AppBottomNavItem
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors

@Composable
fun ClearrNavHost(onThemeChange: (ThemeMode) -> Unit = {}) {
    val onboardingVm: OnboardingViewModel = hiltViewModel()
    val appConfigVm: AppConfigViewModel = hiltViewModel()

    val onboardingState by onboardingVm.uiState.collectAsStateWithLifecycle()
    val appConfigState by appConfigVm.uiState.collectAsStateWithLifecycle()
    val onboardingComplete = onboardingState.isComplete
    val appConfigLoading = appConfigState.isLoading
    val colors = LocalClearrUiColors.current

    if (onboardingComplete == null || appConfigLoading) {
        ApplySystemBars(darkIcons = false)
        Box(modifier = Modifier.fillMaxSize().background(ClearrColors.Violet))
        return
    }

    if (onboardingComplete == false) {
        OnboardingNavHost(onboardingVm = onboardingVm)
    } else {
        MainNavHost(onThemeChange = onThemeChange)
    }
}

@Composable
private fun OnboardingNavHost(onboardingVm: OnboardingViewModel) {
    val navController = rememberNavController()
    val colors = LocalClearrUiColors.current
    ApplySystemBars(darkIcons = !colors.isDark)

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onGetStarted = { navController.navigate("onboarding/0") })
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
    ApplySystemBars(darkIcons = !colors.isDark)

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
                        when (item) {
                            AppBottomNavItem.HOME -> navController.navigateTopLevel(NavRoutes.Dashboard.route)
                            AppBottomNavItem.BUDGET -> shellState.budgetTrackerId?.let { navController.navigateTopLevel(NavRoutes.BudgetRoot.createRoute(it)) }
                            AppBottomNavItem.TODOS -> shellState.todoTrackerId?.let { navController.navigateTopLevel(NavRoutes.TodoRoot.createRoute(it)) }
                            AppBottomNavItem.GOALS -> shellState.goalsTrackerId?.let { navController.navigateTopLevel(NavRoutes.GoalsRoot.createRoute(it)) }
                        }
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
                    AddTodoScreen(trackerId = trackerId, onClose = { navController.popBackStack() })
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

private fun String?.isBottomNavRoute(): Boolean = when {
    this == null -> false
    this == NavRoutes.Dashboard.route -> true
    this.startsWith(NavRoutes.BudgetRoot.baseRoute) -> true
    this.startsWith(NavRoutes.TodoRoot.baseRoute) -> true
    this.startsWith(NavRoutes.GoalsRoot.baseRoute) -> true
    else -> false
}

private fun String?.toBottomNavItem(): AppBottomNavItem? = when {
    this == NavRoutes.Dashboard.route -> AppBottomNavItem.HOME
    this?.startsWith(NavRoutes.BudgetRoot.baseRoute) == true -> AppBottomNavItem.BUDGET
    this?.startsWith(NavRoutes.TodoRoot.baseRoute) == true -> AppBottomNavItem.TODOS
    this?.startsWith(NavRoutes.GoalsRoot.baseRoute) == true -> AppBottomNavItem.GOALS
    else -> null
}

private fun String?.isTopLevelNonDashboardRoute(): Boolean = when {
    this == null -> false
    this.startsWith(NavRoutes.BudgetRoot.baseRoute) -> true
    this.startsWith(NavRoutes.TodoRoot.baseRoute) -> true
    this.startsWith(NavRoutes.GoalsRoot.baseRoute) -> true
    else -> false
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
