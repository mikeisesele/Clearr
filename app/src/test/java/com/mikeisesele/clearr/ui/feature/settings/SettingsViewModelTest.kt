package com.mikeisesele.clearr.ui.feature.settings

import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.data.model.YearConfig
import com.mikeisesele.clearr.di.AppStateHolder
import com.mikeisesele.clearr.domain.repository.DuesRepository
import com.mikeisesele.clearr.testutil.MainDispatcherRule
import com.mikeisesele.clearr.ui.commons.state.ThemeMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<DuesRepository>()
    private val appState = AppStateHolder()

    @Test
    fun `state reflects selected tracker due amount`() = runTest {
        val tracker = Tracker(id = 99, name = "Dues", type = TrackerType.DUES, defaultAmount = 7000.0)
        appState.setCurrentTrackerId(99)

        every { repository.getAllMembers() } returns flowOf(emptyList())
        every { repository.getAllYearConfigs() } returns flowOf(emptyList())
        every { repository.getTrackerByIdFlow(99) } returns flowOf(tracker)

        val viewModel = SettingsViewModel(repository, appState)
        advanceUntilIdle()

        assertEquals(TrackerType.DUES, viewModel.uiState.value.currentTrackerType)
        assertEquals(7000.0, viewModel.uiState.value.currentTrackerDueAmount)
    }

    @Test
    fun `update due amount applies only for dues tracker`() = runTest {
        every { repository.getAllMembers() } returns flowOf(emptyList())
        every { repository.getAllYearConfigs() } returns flowOf(emptyList())

        appState.setCurrentTrackerId(10)
        every { repository.getTrackerByIdFlow(10) } returns MutableStateFlow(null)
        coEvery { repository.getTrackerById(10) } returns Tracker(id = 10, name = "Dues", type = TrackerType.DUES, defaultAmount = 5000.0)
        coEvery { repository.updateTracker(any()) } just runs

        val viewModel = SettingsViewModel(repository, appState)
        viewModel.onAction(SettingsAction.UpdateDueAmount(year = 2026, amount = 9500.0))
        advanceUntilIdle()

        coVerify { repository.updateTracker(match { it.id == 10L && it.defaultAmount == 9500.0 }) }

        coEvery { repository.getTrackerById(10) } returns Tracker(id = 10, name = "Todo", type = TrackerType.TODO, defaultAmount = 0.0)
        viewModel.onAction(SettingsAction.UpdateDueAmount(year = 2026, amount = 1111.0))
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.updateTracker(any()) }
    }

    @Test
    fun `reset setup marks setup as incomplete`() = runTest {
        every { repository.getAllMembers() } returns flowOf(emptyList())
        every { repository.getAllYearConfigs() } returns flowOf(listOf(YearConfig(year = 2026, dueAmountPerMonth = 5000.0)))
        coEvery { repository.getAppConfig() } returns AppConfig(setupComplete = true)
        coEvery { repository.upsertAppConfig(any()) } just runs

        val viewModel = SettingsViewModel(repository, appState)
        viewModel.onAction(SettingsAction.ResetSetup)
        advanceUntilIdle()

        coVerify {
            repository.upsertAppConfig(match { it.setupComplete.not() })
        }
        assertEquals(false, appState.appConfig.value?.setupComplete)
    }

    @Test
    fun `start new year ensures next year and updates app state year`() = runTest {
        every { repository.getAllMembers() } returns flowOf(emptyList())
        every { repository.getAllYearConfigs() } returns flowOf(emptyList())
        coEvery { repository.getYearConfig(2026) } returns YearConfig(year = 2026, dueAmountPerMonth = 6400.0)
        coEvery { repository.ensureYearConfig(2027, 6400.0) } just runs

        SettingsViewModel(repository, appState).onAction(SettingsAction.StartNewYear(2026))
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.ensureYearConfig(2027, 6400.0) }
        assertEquals(2027, appState.selectedYear.value)
    }

    @Test
    fun `set theme mode updates ui state`() = runTest {
        every { repository.getAllMembers() } returns flowOf(emptyList())
        every { repository.getAllYearConfigs() } returns flowOf(emptyList())
        val viewModel = SettingsViewModel(repository, appState)

        viewModel.onAction(SettingsAction.SetThemeMode(ThemeMode.LIGHT))
        advanceUntilIdle()

        assertEquals(ThemeMode.LIGHT, viewModel.uiState.value.themeMode)
    }
}
