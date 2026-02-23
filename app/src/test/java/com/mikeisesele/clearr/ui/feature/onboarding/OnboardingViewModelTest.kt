package com.mikeisesele.clearr.ui.feature.onboarding

import com.mikeisesele.clearr.data.repository.OnboardingRepository
import com.mikeisesele.clearr.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val onboardingRepository = mockk<OnboardingRepository>()

    @Test
    fun `init observes onboarding completion flow`() = runTest {
        val completionFlow = MutableStateFlow(false)
        every { onboardingRepository.isOnboardingComplete } returns completionFlow
        coEvery { onboardingRepository.markComplete() } just runs

        val viewModel = OnboardingViewModel(onboardingRepository)

        completionFlow.value = true
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isComplete == true)
    }

    @Test
    fun `next and prev slide respect bounds`() = runTest {
        every { onboardingRepository.isOnboardingComplete } returns MutableStateFlow(false)
        coEvery { onboardingRepository.markComplete() } just runs
        val viewModel = OnboardingViewModel(onboardingRepository)

        repeat(10) { viewModel.onAction(OnboardingAction.NextSlide) }
        assertEquals(2, viewModel.uiState.value.currentSlide)

        repeat(10) { viewModel.onAction(OnboardingAction.PrevSlide) }
        assertEquals(0, viewModel.uiState.value.currentSlide)
    }

    @Test
    fun `go to slide clamps to valid range`() = runTest {
        every { onboardingRepository.isOnboardingComplete } returns MutableStateFlow(false)
        coEvery { onboardingRepository.markComplete() } just runs
        val viewModel = OnboardingViewModel(onboardingRepository)

        viewModel.onAction(OnboardingAction.GoToSlide(99))
        assertEquals(2, viewModel.uiState.value.currentSlide)

        viewModel.onAction(OnboardingAction.GoToSlide(-4))
        assertEquals(0, viewModel.uiState.value.currentSlide)
    }

    @Test
    fun `complete onboarding delegates to repository`() = runTest {
        every { onboardingRepository.isOnboardingComplete } returns MutableStateFlow(false)
        coEvery { onboardingRepository.markComplete() } just runs
        val viewModel = OnboardingViewModel(onboardingRepository)

        viewModel.onAction(OnboardingAction.CompleteOnboarding)
        advanceUntilIdle()

        coVerify(exactly = 1) { onboardingRepository.markComplete() }
    }
}
