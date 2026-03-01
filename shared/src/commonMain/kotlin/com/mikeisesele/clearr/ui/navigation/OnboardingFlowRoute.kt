package com.mikeisesele.clearr.ui.navigation

sealed class OnboardingFlowRoute(val route: String) {
    data object Splash : OnboardingFlowRoute("onboarding_splash")

    data object Slide : OnboardingFlowRoute("onboarding/{slideIndex}") {
        const val argument = "slideIndex"
        fun createRoute(slideIndex: Int): String = "onboarding/$slideIndex"
    }

    data object Completion : OnboardingFlowRoute("onboarding_complete")
}
