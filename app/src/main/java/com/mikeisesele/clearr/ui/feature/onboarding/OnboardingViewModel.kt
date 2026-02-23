package com.mikeisesele.clearr.ui.feature.onboarding

import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.data.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) : BaseViewModel<OnboardingState, OnboardingAction, OnboardingEvent>(
    initialState = OnboardingState()
) {

    init {
        observeOnboardingCompletion()
    }

    override fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.NextSlide -> handleNextSlide()
            OnboardingAction.PrevSlide -> handlePrevSlide()
            is OnboardingAction.GoToSlide -> handleGoToSlide(action.index)
            OnboardingAction.CompleteOnboarding -> handleCompleteOnboarding()
        }
    }

    private fun observeOnboardingCompletion() {
        launch {
            onboardingRepository.isOnboardingComplete.collectLatest { complete ->
                updateState { it.copy(isComplete = complete) }
            }
        }
    }

    private fun handleNextSlide() {
        if (currentState.currentSlide < currentState.totalSlides - 1) {
            updateState { it.copy(currentSlide = it.currentSlide + 1) }
        }
    }

    private fun handlePrevSlide() {
        if (currentState.currentSlide > 0) {
            updateState { it.copy(currentSlide = it.currentSlide - 1) }
        }
    }

    private fun handleGoToSlide(index: Int) {
        updateState { it.copy(currentSlide = index.coerceIn(0, it.totalSlides - 1)) }
    }

    private fun handleCompleteOnboarding() {
        launch { onboardingRepository.markComplete() }
    }
}
