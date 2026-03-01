package com.mikeisesele.clearr.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikeisesele.clearr.ui.feature.onboarding.OnboardingState
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors

@Composable
fun AppShellRoot(
    onboardingState: OnboardingState,
    appConfigState: AppConfigUiState,
    renderLoading: @Composable (darkIcons: Boolean) -> Unit,
    renderOnboarding: @Composable (darkIcons: Boolean) -> Unit,
    renderMainShell: @Composable (darkIcons: Boolean) -> Unit
) {
    val onboardingComplete = onboardingState.isComplete
    val appConfigLoading = appConfigState.isLoading
    val colors = LocalClearrUiColors.current

    when {
        onboardingComplete == null || appConfigLoading -> {
            renderLoading(false)
            Box(modifier = Modifier.fillMaxSize().background(ClearrColors.Violet))
        }
        onboardingComplete == false -> renderOnboarding(!colors.isDark)
        else -> renderMainShell(!colors.isDark)
    }
}
