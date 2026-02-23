package com.mikeisesele.clearr.ui.feature.onboarding

data class OnboardingState(
    val currentSlide: Int = 0,
    val totalSlides: Int = 3,
    val isComplete: Boolean? = null
)
