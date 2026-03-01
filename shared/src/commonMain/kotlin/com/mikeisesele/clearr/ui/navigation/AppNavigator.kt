package com.mikeisesele.clearr.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed interface AppDestination {
    data object Splash : AppDestination
    data object Onboarding : AppDestination
    data object Completion : AppDestination
    data class MainShell(val destination: AppShellDestination = AppShellDestination.Dashboard) : AppDestination
}

data class AppNavigationState(
    val backstack: List<AppDestination> = listOf(AppDestination.Splash)
) {
    val current: AppDestination = backstack.lastOrNull() ?: AppDestination.Splash
    val canGoBack: Boolean = backstack.size > 1
}

class AppNavigator(
    initialDestination: AppDestination = AppDestination.Splash
) {
    private val mutableState = MutableStateFlow(AppNavigationState(backstack = listOf(initialDestination)))
    val state: StateFlow<AppNavigationState> = mutableState.asStateFlow()

    fun replace(destination: AppDestination) {
        mutableState.update { AppNavigationState(backstack = listOf(destination)) }
    }

    fun push(destination: AppDestination) {
        mutableState.update { nav ->
            AppNavigationState(backstack = nav.backstack + destination)
        }
    }

    fun pop(): Boolean {
        val didPop = mutableState.value.canGoBack
        if (didPop) {
            mutableState.update { nav -> AppNavigationState(backstack = nav.backstack.dropLast(1)) }
        }
        return didPop
    }

    fun openOnboarding() = replace(AppDestination.Onboarding)

    fun completeOnboarding() = replace(AppDestination.Completion)

    fun openDashboard() = replace(AppDestination.MainShell())

    fun openMainShell(destination: AppShellDestination = AppShellDestination.Dashboard) {
        replace(AppDestination.MainShell(destination))
    }

    fun openShellDestination(destination: AppShellDestination) {
        mutableState.update { nav ->
            when (nav.current) {
                is AppDestination.MainShell -> AppNavigationState(backstack = nav.backstack.dropLast(1) + AppDestination.MainShell(destination))
                else -> AppNavigationState(backstack = nav.backstack + AppDestination.MainShell(destination))
            }
        }
    }
}

@Composable
fun rememberAppNavigator(
    initialDestination: AppDestination = AppDestination.Splash
): AppNavigator = remember(initialDestination) { AppNavigator(initialDestination) }
