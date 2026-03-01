package com.mikeisesele.clearr.ui.feature.onboarding

import com.mikeisesele.clearr.domain.repository.OnboardingStatusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingStore(
    private val onboardingRepository: OnboardingStatusRepository,
    private val scope: CoroutineScope
) {
    private val mutableState = MutableStateFlow(OnboardingState())
    val uiState: StateFlow<OnboardingState> = mutableState.asStateFlow()

    private val eventChannel = Channel<OnboardingEvent>(capacity = Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        scope.launch {
            onboardingRepository.isOnboardingComplete.collect { complete ->
                mutableState.update { state -> state.copy(isComplete = complete) }
            }
        }
    }

    fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.NextSlide -> handleNextSlide()
            OnboardingAction.PrevSlide -> handlePrevSlide()
            is OnboardingAction.GoToSlide -> handleGoToSlide(action.index)
            OnboardingAction.CompleteOnboarding -> {
                scope.launch { onboardingRepository.markComplete() }
            }
        }
    }

    private fun handleNextSlide() {
        mutableState.update { state ->
            if (state.currentSlide >= state.totalSlides - 1) {
                state
            } else {
                state.copy(currentSlide = state.currentSlide + 1)
            }
        }
    }

    private fun handlePrevSlide() {
        mutableState.update { state ->
            if (state.currentSlide <= 0) {
                state
            } else {
                state.copy(currentSlide = state.currentSlide - 1)
            }
        }
    }

    private fun handleGoToSlide(index: Int) {
        mutableState.update { state ->
            state.copy(currentSlide = index.coerceIn(0, state.totalSlides - 1))
        }
    }
}
