package com.mikeisesele.clearr

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.mikeisesele.clearr.core.time.localDateAtEndOfDayEpochMillis
import com.mikeisesele.clearr.core.time.localDateAtStartOfDayEpochMillis
import com.mikeisesele.clearr.core.time.nowEpochMillis
import com.mikeisesele.clearr.core.time.plusDays
import com.mikeisesele.clearr.core.time.todayLocalDate
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetPeriod
import com.mikeisesele.clearr.data.model.BudgetStatus
import com.mikeisesele.clearr.data.model.BudgetSummary
import com.mikeisesele.clearr.data.model.CategorySummary
import com.mikeisesele.clearr.data.model.Goal
import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.data.model.GoalSummary
import com.mikeisesele.clearr.data.model.HistoryEntry
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import com.mikeisesele.clearr.data.model.TodoStatus
import com.mikeisesele.clearr.runtime.ClearrRuntime
import com.mikeisesele.clearr.runtime.createPlatformRuntime
import com.mikeisesele.clearr.runtime.createBudgetStore
import com.mikeisesele.clearr.runtime.createDashboardStore
import com.mikeisesele.clearr.runtime.createGoalsStore
import com.mikeisesele.clearr.runtime.createTodoStore
import com.mikeisesele.clearr.ui.feature.dashboard.DashboardAction
import com.mikeisesele.clearr.ui.feature.budget.AddBudgetCategoryScreen
import com.mikeisesele.clearr.ui.feature.budget.BudgetAction
import com.mikeisesele.clearr.ui.feature.budget.BudgetPlanDraft
import com.mikeisesele.clearr.ui.feature.budget.BudgetScreen
import com.mikeisesele.clearr.ui.feature.budget.BudgetUiState
import com.mikeisesele.clearr.ui.feature.dashboard.DashboardScreen
import com.mikeisesele.clearr.ui.feature.dashboard.DashboardEvent
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardClearanceScore
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerHealth
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerType
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardUiModel
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardUrgencyItem
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardUrgencySeverity
import com.mikeisesele.clearr.ui.feature.goals.AddGoalScreen
import com.mikeisesele.clearr.ui.feature.goals.GoalsAction
import com.mikeisesele.clearr.ui.feature.goals.GoalsAiResult
import com.mikeisesele.clearr.ui.feature.goals.GoalsScreen
import com.mikeisesele.clearr.ui.feature.goals.GoalsUiState
import com.mikeisesele.clearr.ui.feature.onboarding.CompletionScreen
import com.mikeisesele.clearr.ui.feature.onboarding.OnboardingScreen
import com.mikeisesele.clearr.ui.feature.onboarding.SplashScreen
import com.mikeisesele.clearr.ui.feature.todo.AddTodoScreen
import com.mikeisesele.clearr.ui.feature.todo.TodoAction
import com.mikeisesele.clearr.ui.feature.todo.TodoDetailScreen
import com.mikeisesele.clearr.ui.feature.todo.TodoUiState
import com.mikeisesele.clearr.ui.navigation.AppDestination
import com.mikeisesele.clearr.ui.navigation.AppShellDestination
import com.mikeisesele.clearr.ui.navigation.addFlowDestinationOrNull
import com.mikeisesele.clearr.ui.navigation.backDestinationOrNull
import com.mikeisesele.clearr.ui.navigation.components.AppBottomNav
import com.mikeisesele.clearr.ui.navigation.components.AppBottomNavItem
import com.mikeisesele.clearr.ui.navigation.isTopLevelDestination
import com.mikeisesele.clearr.ui.navigation.rememberAppShellNavigator
import com.mikeisesele.clearr.ui.navigation.rememberAppNavigator
import com.mikeisesele.clearr.ui.navigation.topLevelDestination
import com.mikeisesele.clearr.ui.theme.ClearrSharedTheme
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors

@Composable
fun ClearrApp(
    runtime: ClearrRuntime? = null
) {
    val appRuntime = remember(runtime) { runtime ?: createPlatformRuntime() }
    ClearrSharedTheme {
        val navigator = rememberAppNavigator()
        val navigationState by navigator.state.collectAsState()

        when (navigationState.current) {
            AppDestination.Splash -> SplashScreen(onGetStarted = navigator::openOnboarding)
            AppDestination.Onboarding -> OnboardingScreen(
                onComplete = navigator::completeOnboarding,
                onSkip = navigator::completeOnboarding
            )
            AppDestination.Completion -> CompletionScreen(onOpenApp = navigator::openDashboard)
            is AppDestination.MainShell -> MainShellPreview(
                initialDestination = (navigationState.current as AppDestination.MainShell).destination,
                runtime = appRuntime
            )
        }
    }
}

@Composable
private fun MainShellPreview(
    initialDestination: AppShellDestination,
    runtime: ClearrRuntime
) {
    val colors = LocalClearrUiColors.current
    val scope = rememberCoroutineScope()
    val shellNavigator = rememberAppShellNavigator(initialDestination)
    val shellState by shellNavigator.state.collectAsState()
    val destination = shellState.current
    val dashboardStore = remember(scope, runtime) { runtime.createDashboardStore(scope) }
    val dashboardState by dashboardStore.uiState.collectAsState()
    val activeBudgetTrackerId = when (destination) {
        is AppShellDestination.BudgetRoot -> destination.trackerId
        is AppShellDestination.BudgetAddCategory -> destination.trackerId
        else -> null
    }
    val activeTodoTrackerId = when (destination) {
        is AppShellDestination.TodoRoot -> destination.trackerId
        is AppShellDestination.TodoAdd -> destination.trackerId
        else -> null
    }
    val activeGoalsTrackerId = when (destination) {
        is AppShellDestination.GoalsRoot -> destination.trackerId
        is AppShellDestination.GoalAdd -> destination.trackerId
        else -> null
    }
    val budgetStore = activeBudgetTrackerId?.let { trackerId ->
        remember(trackerId, scope, runtime) { runtime.createBudgetStore(trackerId, scope) }
    }
    val budgetState by (budgetStore?.uiState?.collectAsState()
        ?: remember { androidx.compose.runtime.mutableStateOf<BudgetUiState?>(null) })
    val todoStore = activeTodoTrackerId?.let { trackerId ->
        remember(trackerId, scope, runtime) { runtime.createTodoStore(trackerId, scope) }
    }
    val todoState by (todoStore?.uiState?.collectAsState() ?: remember { androidx.compose.runtime.mutableStateOf<TodoUiState?>(null) })
    val goalsStore = activeGoalsTrackerId?.let { trackerId ->
        remember(trackerId, scope, runtime) { runtime.createGoalsStore(trackerId, scope) }
    }
    val goalsState by (goalsStore?.uiState?.collectAsState() ?: remember { androidx.compose.runtime.mutableStateOf<GoalsUiState?>(null) })

    LaunchedEffect(dashboardStore) {
        dashboardStore.events.collect { event ->
            when (event) {
                is DashboardEvent.OpenTracker -> {
                    shellNavigator.openTopLevel(
                        when (event.trackerType) {
                            DashboardTrackerType.BUDGET -> AppShellDestination.BudgetRoot(PREVIEW_BUDGET_TRACKER_ID)
                            DashboardTrackerType.GOALS -> AppShellDestination.GoalsRoot(PREVIEW_GOALS_TRACKER_ID)
                            DashboardTrackerType.TODOS -> AppShellDestination.TodoRoot(PREVIEW_TODO_TRACKER_ID)
                        }
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = colors.bg,
        bottomBar = {
            if (destination.isTopLevelDestination()) {
                AppBottomNav(
                    selectedItem = destination.bottomNavItemOrNull(),
                    onSelect = { item ->
                        shellNavigator.openTopLevel(item.toTopLevelDestination())
                    }
                )
            }
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            when (destination) {
                AppShellDestination.Dashboard -> DashboardScreen(
                    state = dashboardState.model,
                    isLoading = dashboardState.isLoading,
                    onDismissUrgency = { dashboardStore.onAction(DashboardAction.DismissUrgency(it)) },
                    onQuickAction = { dashboardStore.onAction(DashboardAction.QuickAction(it)) }
                )

                is AppShellDestination.BudgetRoot -> {
                    val state = budgetState
                    val store = budgetStore
                    if (state != null && store != null) {
                        BudgetScreen(
                            state = state,
                            colors = colors,
                            onAction = store::onAction,
                            onNavigateBack = {
                                destination.backDestinationOrNull()?.let(shellNavigator::openTopLevel)
                            },
                            onAddCategory = {
                                destination.addFlowDestinationOrNull()?.let(shellNavigator::push)
                            }
                        )
                    }
                }

                is AppShellDestination.GoalsRoot -> {
                    val state = goalsState
                    val store = goalsStore
                    if (state != null && store != null) {
                        GoalsScreen(
                            state = state,
                            colors = colors,
                            onAction = store::onAction,
                            onNavigateBack = {
                                destination.backDestinationOrNull()?.let(shellNavigator::openTopLevel)
                            },
                            onAddGoal = {
                                destination.addFlowDestinationOrNull()?.let(shellNavigator::push)
                            }
                        )
                    }
                }

                is AppShellDestination.TodoRoot -> {
                    val state = todoState
                    val store = todoStore
                    if (state != null && store != null) {
                        TodoDetailScreen(
                            state = state,
                            onAction = store::onAction,
                            onNavigateBack = {
                                destination.backDestinationOrNull()?.let(shellNavigator::openTopLevel)
                            },
                            onAddTodo = {
                                destination.addFlowDestinationOrNull()?.let(shellNavigator::push)
                            }
                        )
                    }
                }

                is AppShellDestination.TodoAdd -> AddTodoScreen(
                    onClose = { shellNavigator.pop() },
                    onAddTodo = { title, note, priority, dueDate ->
                        todoStore?.onAction(TodoAction.AddTodo(title, note, priority, dueDate))
                    }
                )

                is AppShellDestination.GoalAdd -> {
                    val state = goalsState
                    if (state != null) {
                        AddGoalScreen(
                            state = state,
                            colors = colors,
                            onClose = { shellNavigator.pop() },
                            onAddGoal = { title, emoji, colorToken, target, frequency ->
                                goalsStore?.onAction(GoalsAction.AddGoal(title, emoji, colorToken, target, frequency))
                            },
                            inferGoalDraft = { title, target, frequency, emoji, colorToken ->
                                runtime.goalsAiService.inferGoal(title, target, frequency, emoji, colorToken)
                            }
                        )
                    }
                }

                is AppShellDestination.BudgetAddCategory -> {
                    val state = budgetState
                    if (state != null) {
                        AddBudgetCategoryScreen(
                            state = state,
                            colors = colors,
                            onClose = { shellNavigator.pop() },
                            onAddCategory = { name, icon, colorToken, plannedAmountNaira ->
                                budgetStore?.onAction(BudgetAction.AddCategory(name, icon, colorToken, plannedAmountNaira))
                            }
                        )
                    }
                }
            }
        }
    }
}

private const val PREVIEW_BUDGET_TRACKER_ID = 1001L
private const val PREVIEW_GOALS_TRACKER_ID = 1002L
private const val PREVIEW_TODO_TRACKER_ID = 1003L

private fun AppShellDestination.bottomNavItemOrNull(): AppBottomNavItem? = when (topLevelDestination()) {
    AppShellDestination.Dashboard -> AppBottomNavItem.HOME
    is AppShellDestination.BudgetRoot -> AppBottomNavItem.BUDGET
    is AppShellDestination.TodoRoot -> AppBottomNavItem.TODOS
    is AppShellDestination.GoalsRoot -> AppBottomNavItem.GOALS
    else -> null
}

private fun AppBottomNavItem.toTopLevelDestination(): AppShellDestination = when (this) {
    AppBottomNavItem.HOME -> AppShellDestination.Dashboard
    AppBottomNavItem.BUDGET -> AppShellDestination.BudgetRoot(PREVIEW_BUDGET_TRACKER_ID)
    AppBottomNavItem.TODOS -> AppShellDestination.TodoRoot(PREVIEW_TODO_TRACKER_ID)
    AppBottomNavItem.GOALS -> AppShellDestination.GoalsRoot(PREVIEW_GOALS_TRACKER_ID)
}

private fun previewBudgetState(trackerId: Long): BudgetUiState {
    val today = todayLocalDate()
    val period = BudgetPeriod(
        id = 501,
        trackerId = trackerId,
        frequency = BudgetFrequency.MONTHLY,
        label = "Mar 2026",
        startDate = localDateAtStartOfDayEpochMillis(today),
        endDate = localDateAtEndOfDayEpochMillis(today.plusDays(30))
    )
    val housing = BudgetCategory(
        id = 1,
        trackerId = trackerId,
        frequency = BudgetFrequency.MONTHLY,
        name = "Housing",
        icon = "🏠",
        colorToken = "Violet",
        plannedAmountKobo = 4500000,
        sortOrder = 0
    )
    val food = BudgetCategory(
        id = 2,
        trackerId = trackerId,
        frequency = BudgetFrequency.MONTHLY,
        name = "Food",
        icon = "🍔",
        colorToken = "Orange",
        plannedAmountKobo = 1800000,
        sortOrder = 1
    )
    val transport = BudgetCategory(
        id = 3,
        trackerId = trackerId,
        frequency = BudgetFrequency.MONTHLY,
        name = "Transport",
        icon = "🚗",
        colorToken = "Blue",
        plannedAmountKobo = 900000,
        sortOrder = 2
    )
    val categories = listOf(
        CategorySummary(housing, 4500000, 3800000, 700000, 84f, BudgetStatus.NEAR_LIMIT),
        CategorySummary(food, 1800000, 1125000, 675000, 63f, BudgetStatus.ON_TRACK),
        CategorySummary(transport, 900000, 960000, -60000, 107f, BudgetStatus.OVER_BUDGET)
    )
    return BudgetUiState(
        trackerId = trackerId,
        trackerName = "Monthly Budget",
        frequency = BudgetFrequency.MONTHLY,
        periods = listOf(period),
        selectedPeriodId = period.id,
        categorySummaries = categories,
        budgetSummary = BudgetSummary(
            totalPlannedKobo = categories.sumOf { it.plannedAmountKobo },
            totalSpentKobo = categories.sumOf { it.spentAmountKobo },
            totalRemainingKobo = categories.sumOf { it.remainingAmountKobo },
            percentUsed = 82f,
            isOverBudget = true,
            overBudgetCategories = categories.filter { it.status == BudgetStatus.OVER_BUDGET }
        ),
        budgetSetupPeriodLabel = period.label,
        budgetSetupSourceLabel = "Copied from February",
        budgetSetupDrafts = categories.map {
            BudgetPlanDraft(
                categoryId = it.category.id,
                icon = it.category.icon,
                name = it.category.name,
                colorToken = it.category.colorToken,
                plannedAmountKobo = it.plannedAmountKobo
            )
        },
        showSwipeHint = false,
        isLoading = false
    )
}

private fun previewGoalsState(trackerId: Long): GoalsUiState {
    val now = nowEpochMillis()
    val goals = listOf(
        Goal("goal-1", trackerId, "Exercise", "💪", "Emerald", "30 mins", GoalFrequency.DAILY, now),
        Goal("goal-2", trackerId, "Read", "📚", "Blue", "20 pages", GoalFrequency.DAILY, now),
        Goal("goal-3", trackerId, "Save", "💰", "Amber", "₦10,000", GoalFrequency.WEEKLY, now)
    )
    val summaries = listOf(
        GoalSummary(
            goal = goals[0],
            isDoneThisPeriod = true,
            currentStreak = 4,
            bestStreak = 8,
            recentHistory = listOf(
                HistoryEntry("Mon", "Mon", true),
                HistoryEntry("Tue", "Tue", true),
                HistoryEntry("Wed", "Wed", false),
                HistoryEntry("Thu", "Thu", true)
            ),
            completionRate = 0.72f,
            completedAtForCurrentPeriod = now
        ),
        GoalSummary(
            goal = goals[1],
            isDoneThisPeriod = false,
            currentStreak = 1,
            bestStreak = 5,
            recentHistory = listOf(
                HistoryEntry("Mon", "Mon", false),
                HistoryEntry("Tue", "Tue", true),
                HistoryEntry("Wed", "Wed", false),
                HistoryEntry("Thu", "Thu", false)
            ),
            completionRate = 0.48f
        ),
        GoalSummary(
            goal = goals[2],
            isDoneThisPeriod = true,
            currentStreak = 2,
            bestStreak = 3,
            recentHistory = listOf(
                HistoryEntry("W09", "W9", true),
                HistoryEntry("W10", "W10", true),
                HistoryEntry("W11", "W11", false),
                HistoryEntry("W12", "W12", true)
            ),
            completionRate = 0.63f,
            completedAtForCurrentPeriod = now
        )
    )
    return GoalsUiState(
        trackerId = trackerId,
        trackerName = "Goals",
        summaries = summaries,
        doneCount = summaries.count { it.isDoneThisPeriod },
        totalCount = summaries.size,
        allDoneThisPeriod = false,
        aiInsight = "Momentum is strongest on exercise. Reading needs attention.",
        isLoading = false
    )
}

private fun previewGoalsAiResult(
    title: String,
    target: String?,
    frequency: GoalFrequency,
    emoji: String,
    colorToken: String
): GoalsAiResult = GoalsAiResult(
    normalizedTitle = title.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
    suggestedTarget = target ?: "30 mins",
    suggestedFrequency = frequency,
    suggestedEmoji = emoji,
    suggestedColorToken = colorToken
)
