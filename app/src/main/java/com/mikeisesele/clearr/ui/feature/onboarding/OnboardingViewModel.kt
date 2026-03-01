package com.mikeisesele.clearr.ui.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikeisesele.clearr.data.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val store = OnboardingStore(
        onboardingRepository = onboardingRepository,
        scope = viewModelScope
    )

    val uiState: StateFlow<OnboardingState> = store.uiState
    val events = store.events

    fun onAction(action: OnboardingAction) {
        store.onAction(action)
    }
}
