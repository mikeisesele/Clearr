package com.mikeisesele.clearr.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppShellNavigationState(
    val backstack: List<AppShellDestination> = listOf(AppShellDestination.Dashboard)
) {
    val current: AppShellDestination = backstack.lastOrNull() ?: AppShellDestination.Dashboard
    val canGoBack: Boolean = backstack.size > 1
}

class AppShellNavigator(
    initialDestination: AppShellDestination = AppShellDestination.Dashboard
) {
    private val mutableState = MutableStateFlow(
        AppShellNavigationState(backstack = listOf(initialDestination))
    )
    val state: StateFlow<AppShellNavigationState> = mutableState.asStateFlow()

    fun openTopLevel(destination: AppShellDestination) {
        val topLevelDestination = when (destination) {
            AppShellDestination.Dashboard -> destination
            is AppShellDestination.BudgetRoot -> destination
            is AppShellDestination.TodoRoot -> destination
            is AppShellDestination.GoalsRoot -> destination
            is AppShellDestination.TodoAdd -> AppShellDestination.TodoRoot(destination.trackerId)
            is AppShellDestination.GoalAdd -> AppShellDestination.GoalsRoot(destination.trackerId)
            is AppShellDestination.BudgetAddCategory -> AppShellDestination.BudgetRoot(destination.trackerId)
        }
        mutableState.update { AppShellNavigationState(backstack = listOf(topLevelDestination)) }
    }

    fun push(destination: AppShellDestination) {
        mutableState.update { nav ->
            AppShellNavigationState(backstack = nav.backstack + destination)
        }
    }

    fun replaceCurrent(destination: AppShellDestination) {
        mutableState.update { nav ->
            AppShellNavigationState(
                backstack = nav.backstack.dropLast(1) + destination
            )
        }
    }

    fun pop(): Boolean {
        val didPop = mutableState.value.canGoBack
        if (didPop) {
            mutableState.update { nav ->
                AppShellNavigationState(backstack = nav.backstack.dropLast(1))
            }
        }
        return didPop
    }
}

@Composable
fun rememberAppShellNavigator(
    initialDestination: AppShellDestination = AppShellDestination.Dashboard
): AppShellNavigator = remember(initialDestination) { AppShellNavigator(initialDestination) }
