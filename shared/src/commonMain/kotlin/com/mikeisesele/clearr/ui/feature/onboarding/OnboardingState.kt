package com.mikeisesele.clearr.ui.feature.onboarding

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent

data class OnboardingState(
    val currentSlide: Int = 0,
    val totalSlides: Int = 3,
    val isComplete: Boolean? = null
) : BaseState

sealed interface OnboardingAction {
    data object NextSlide : OnboardingAction
    data object PrevSlide : OnboardingAction
    data class GoToSlide(val index: Int) : OnboardingAction
    data object CompleteOnboarding : OnboardingAction
}

sealed interface OnboardingEvent : ViewEvent
