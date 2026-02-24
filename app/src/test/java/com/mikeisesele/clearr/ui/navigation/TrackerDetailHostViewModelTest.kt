package com.mikeisesele.clearr.ui.navigation

import androidx.lifecycle.SavedStateHandle
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.repository.DuesRepository
import com.mikeisesele.clearr.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerDetailHostViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<DuesRepository>()
    private val trackerId = 55L

    @Test
    fun `loads tracker type and clears loading`() = runTest {
        every { repository.getTrackerByIdFlow(trackerId) } returns MutableStateFlow(
            Tracker(
                id = trackerId,
                name = "Budget",
                type = TrackerType.BUDGET,
                frequency = Frequency.MONTHLY,
                layoutStyle = LayoutStyle.GRID,
                defaultAmount = 0.0,
                isNew = false,
                createdAt = 1L
            )
        )

        val viewModel = TrackerDetailHostViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(TrackerType.BUDGET, viewModel.uiState.value.trackerType)
    }

    @Test
    fun `null tracker sets loading false and keeps default type`() = runTest {
        every { repository.getTrackerByIdFlow(trackerId) } returns MutableStateFlow(null)

        val viewModel = TrackerDetailHostViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(TrackerType.DUES, viewModel.uiState.value.trackerType)
    }
}
