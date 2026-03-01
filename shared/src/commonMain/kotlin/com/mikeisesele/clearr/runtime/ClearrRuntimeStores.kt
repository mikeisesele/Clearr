package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.domain.trackers.ObserveTrackerSummariesUseCase
import com.mikeisesele.clearr.domain.trackers.TrackerBootstrapper
import com.mikeisesele.clearr.ui.feature.budget.BudgetStore
import com.mikeisesele.clearr.ui.feature.dashboard.DashboardStore
import com.mikeisesele.clearr.ui.feature.goals.GoalsStore
import com.mikeisesele.clearr.ui.feature.todo.TodoStore
import kotlinx.coroutines.CoroutineScope

fun ClearrRuntime.createDashboardStore(scope: CoroutineScope): DashboardStore = DashboardStore(
    trackerBootstrapper = TrackerBootstrapper(repository),
    observeTrackerSummaries = ObserveTrackerSummariesUseCase(repository),
    scope = scope
)

fun ClearrRuntime.createBudgetStore(
    trackerId: Long,
    scope: CoroutineScope
): BudgetStore = BudgetStore(
    trackerId = trackerId,
    repository = repository,
    budgetPreferencesRepository = budgetPreferencesRepository,
    budgetAiService = budgetAiService,
    scope = scope,
    nowMillis = nowMillis
)

fun ClearrRuntime.createTodoStore(
    trackerId: Long,
    scope: CoroutineScope
): TodoStore = TodoStore(
    trackerId = trackerId,
    repository = repository,
    todoPreferencesRepository = todoPreferencesRepository,
    todoAiService = todoAiService,
    scope = scope,
    nowMillis = nowMillis
)

fun ClearrRuntime.createGoalsStore(
    trackerId: Long,
    scope: CoroutineScope
): GoalsStore = GoalsStore(
    trackerId = trackerId,
    repository = repository,
    goalsAiService = goalsAiService,
    scope = scope,
    nowMillis = nowMillis
)
