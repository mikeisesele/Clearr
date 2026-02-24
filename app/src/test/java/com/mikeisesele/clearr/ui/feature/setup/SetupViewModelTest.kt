package com.mikeisesele.clearr.ui.feature.setup

import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.di.AppStateHolder
import com.mikeisesele.clearr.domain.repository.DuesRepository
import com.mikeisesele.clearr.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SetupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<DuesRepository>()
    private val appState = AppStateHolder()

    @Test
    fun `init starts at step 1 when trackers already exist`() = runTest {
        every { repository.getAllTrackers() } returns MutableStateFlow(listOf(Tracker(id = 1, name = "Existing")))

        val viewModel = SetupViewModel(repository, appState)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.step)
    }

    @Test
    fun `next and previous step move linearly through updated wizard`() = runTest {
        every { repository.getAllTrackers() } returns MutableStateFlow(emptyList())
        val viewModel = SetupViewModel(repository, appState)

        viewModel.onAction(SetupAction.GoToStep(2))
        viewModel.onAction(SetupAction.NextStep)
        assertEquals(3, viewModel.uiState.value.step)

        viewModel.onAction(SetupAction.PrevStep)
        assertEquals(2, viewModel.uiState.value.step)
    }

    @Test
    fun `tracker type change auto renames only when name is default or blank`() = runTest {
        every { repository.getAllTrackers() } returns MutableStateFlow(emptyList())
        val viewModel = SetupViewModel(repository, appState)

        viewModel.onAction(SetupAction.SetTrackerType(TrackerType.GOALS))
        assertEquals("Goals Tracker", viewModel.uiState.value.trackerName)

        viewModel.onAction(SetupAction.SetTrackerName("Custom Name"))
        viewModel.onAction(SetupAction.SetTrackerType(TrackerType.BUDGET))
        assertEquals("Custom Name", viewModel.uiState.value.trackerName)
    }

    @Test
    fun `load existing config maps expected fields`() = runTest {
        every { repository.getAllTrackers() } returns MutableStateFlow(emptyList())
        val viewModel = SetupViewModel(repository, appState)

        viewModel.onAction(
            SetupAction.LoadExistingConfig(
                AppConfig(
                    groupName = "Clearr",
                    adminName = "Mike",
                    adminPhone = "1234",
                    trackerType = TrackerType.TODO,
                    frequency = Frequency.WEEKLY,
                    defaultAmount = 3200.0,
                    layoutStyle = LayoutStyle.CARDS
                )
            )
        )

        val state = viewModel.uiState.value
        assertEquals("Clearr", state.groupName)
        assertEquals("To-do Tracker", state.trackerName)
        assertEquals("Mike", state.adminName)
        assertEquals("1234", state.adminPhone)
        assertEquals("3200", state.defaultAmount)
        assertTrue(state.loadSampleMembers)
    }

    @Test
    fun `finish setup persists tracker period app config and invokes callback`() = runTest {
        every { repository.getAllTrackers() } returns MutableStateFlow(emptyList())
        coEvery { repository.insertTracker(any()) } returns 101L
        coEvery { repository.insertPeriod(any()) } returns 202L
        coEvery { repository.setCurrentPeriod(101L, 202L) } just runs
        coEvery { repository.insertTrackerMember(any()) } returns 1L
        coEvery { repository.getTrackerById(101L) } returns Tracker(id = 101, name = "Dues Tracker")
        coEvery { repository.upsertAppConfig(any()) } just runs

        val configSlot = slot<AppConfig>()
        coEvery { repository.upsertAppConfig(capture(configSlot)) } just runs

        val viewModel = SetupViewModel(repository, appState)
        viewModel.onAction(SetupAction.SetTrackerType(TrackerType.DUES))
        viewModel.onAction(SetupAction.SetLoadSampleMembers(true))

        var doneCalled = false
        viewModel.onAction(SetupAction.FinishSetup { doneCalled = true })
        advanceUntilIdle()

        assertTrue(doneCalled)
        assertFalse(viewModel.uiState.value.isSaving)
        assertTrue(configSlot.captured.setupComplete)
        assertEquals(configSlot.captured, appState.appConfig.value)
        coVerify(exactly = 8) { repository.insertTrackerMember(any()) }
        coVerify(exactly = 1) { repository.insertPeriod(any()) }
        coVerify(exactly = 1) { repository.setCurrentPeriod(101L, 202L) }
    }
}
