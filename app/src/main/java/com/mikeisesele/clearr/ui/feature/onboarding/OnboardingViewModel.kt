package com.mikeisesele.clearr.ui.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikeisesele.clearr.data.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    val totalSlides = 3

    private val _currentSlide = MutableStateFlow(0)
    val currentSlide: StateFlow<Int> = _currentSlide

    /**
     * Observed at the root NavHost to decide whether to show onboarding or go
     * straight to TrackerListScreen.
     * Starts as null (loading) — renders nothing until first emission.
     */
    val isOnboardingComplete: StateFlow<Boolean?> = onboardingRepository
        .isOnboardingComplete
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null   // null = still loading from DataStore
        )

    fun nextSlide() {
        if (_currentSlide.value < totalSlides - 1) _currentSlide.value++
    }

    fun prevSlide() {
        if (_currentSlide.value > 0) _currentSlide.value--
    }

    fun goToSlide(index: Int) {
        _currentSlide.value = index.coerceIn(0, totalSlides - 1)
    }

    /** Persist flag and mark onboarding complete. */
    fun completeOnboarding() {
        viewModelScope.launch { onboardingRepository.markComplete() }
    }
}
