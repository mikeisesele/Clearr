## `app/src/test/java/com/mikeisesele/clearr/ui/feature/home/HomeViewModelTest.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.home

import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.data.model.PaymentRecord
import com.mikeisesele.clearr.data.model.RecordStatus
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.data.model.TrackerPeriod
import com.mikeisesele.clearr.data.model.TrackerRecord
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.data.model.YearConfig
import com.mikeisesele.clearr.di.AppStateHolder
import com.mikeisesele.clearr.domain.repository.DuesRepository
import com.mikeisesele.clearr.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<DuesRepository>()
    private val appState = AppStateHolder()

    @Test
    fun `add member inserts global member when no tracker selected`() = runTest {
        stubGlobalInit()
        coEvery { repository.insertMember(any()) } returns 1L

        val viewModel = HomeViewModel(repository, appState)
        viewModel.onAction(HomeAction.AddMember("  Henry  ", " 123 "))
        advanceUntilIdle()

        coVerify {
            repository.insertMember(match { it.name == "Henry" && it.phone == "123" })
        }
    }

    @Test
    fun `add member inserts tracker member when tracker selected`() = runTest {
        appState.setCurrentTrackerId(7)
        stubTrackerInit(trackerId = 7, trackerType = TrackerType.TODO)
        coEvery { repository.insertTrackerMember(any()) } returns 1L

        val viewModel = HomeViewModel(repository, appState)
        viewModel.onAction(HomeAction.AddMember(" Simon ", null))
        advanceUntilIdle()

        coVerify {
            repository.insertTrackerMember(match { it.trackerId == 7L && it.name == "Simon" })
        }
    }

    @Test
    fun `toggle payment inserts missing amount in global mode`() = runTest {
        stubGlobalInit()
        coEvery { repository.getTotalPaidForMonth(4, 2026, 1) } returns 1500.0
        coEvery { repository.insertPayment(any()) } returns 12L

        val viewModel = HomeViewModel(repository, appState)
        viewModel.onAction(
            HomeAction.TogglePayment(
                member = Member(id = 4, name = "Dare"),
                year = 2026,
                monthIndex = 1,
                dueAmount = 5000.0
            )
        )
        advanceUntilIdle()

        coVerify {
            repository.insertPayment(match {
                it.memberId == 4L && it.amountPaid == 3500.0 && it.expectedAmount == 5000.0
            })
        }
    }

    @Test
    fun `toggle payment removes latest payment and shows undo snackbar in global mode`() = runTest {
        stubGlobalInit()
        coEvery { repository.getTotalPaidForMonth(2, 2026, 0) } returns 5000.0
        coEvery { repository.getLatestPayment(2, 2026, 0) } returns PaymentRecord(
            id = 55,
            memberId = 2,
            year = 2026,
            monthIndex = 0,
            amountPaid = 5000.0,
            expectedAmount = 5000.0
        )
        coEvery { repository.undoPayment(55) } just runs

        val viewModel = HomeViewModel(repository, appState)
        viewModel.onAction(
            HomeAction.TogglePayment(
                member = Member(id = 2, name = "Mike"),
                year = 2026,
                monthIndex = 0,
                dueAmount = 5000.0
            )
        )
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.undoPayment(55) }
        assertNotNull(viewModel.uiState.value.snackbarMessage)
        assertEquals(55L, viewModel.uiState.value.snackbarMessage?.undoPaymentId)
    }

    @Test
    fun `mark outstanding months paid inserts payments up to current month in global mode`() = runTest {
        stubGlobalInit()
        coEvery { repository.getTotalPaidForMonth(3, any(), any()) } returns 0.0
        coEvery { repository.insertPayment(any()) } returns 1L

        val year = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

        val viewModel = HomeViewModel(repository, appState)
        viewModel.onAction(HomeAction.MarkOutstandingMonthsPaid(memberId = 3, year = year, dueAmount = 6000.0))
        advanceUntilIdle()

        coVerify(exactly = currentMonth + 1) { repository.insertPayment(match { it.memberId == 3L && it.expectedAmount == 6000.0 }) }
    }

    @Test
    fun `set layout style for current tracker updates only selected tracker`() = runTest {
        appState.setCurrentTrackerId(99)
        stubTrackerInit(trackerId = 99, trackerType = TrackerType.DUES)
        coEvery { repository.getTrackerById(99) } returns Tracker(id = 99, name = "Dues", type = TrackerType.DUES, layoutStyle = LayoutStyle.GRID)
        coEvery { repository.updateTracker(any()) } just runs

        val viewModel = HomeViewModel(repository, appState)
        viewModel.onAction(HomeAction.SetLayoutStyleForCurrentTracker(LayoutStyle.KANBAN))
        advanceUntilIdle()

        coVerify {
            repository.updateTracker(match { it.id == 99L && it.layoutStyle == LayoutStyle.KANBAN })
        }
    }

    @Test
    fun `dismiss snackbar clears message`() = runTest {
        stubGlobalInit()
        coEvery { repository.getTotalPaidForMonth(2, 2026, 0) } returns 5000.0
        coEvery { repository.getLatestPayment(2, 2026, 0) } returns PaymentRecord(
            id = 77,
            memberId = 2,
            year = 2026,
            monthIndex = 0,
            amountPaid = 5000.0,
            expectedAmount = 5000.0
        )
        coEvery { repository.undoPayment(77) } just runs

        val viewModel = HomeViewModel(repository, appState)
        viewModel.onAction(HomeAction.TogglePayment(Member(id = 2, name = "M"), 2026, 0, 5000.0))
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.snackbarMessage)

        viewModel.onAction(HomeAction.DismissSnackbar)
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.snackbarMessage)
    }

    private fun stubGlobalInit() {
        every { repository.getAllMembers() } returns flowOf(emptyList())
        every { repository.getPaymentsForYear(any()) } returns flowOf(emptyList())
        every { repository.getYearConfigFlow(any()) } returns flowOf(YearConfig(year = Calendar.getInstance().get(Calendar.YEAR), dueAmountPerMonth = 5000.0))
        coEvery { repository.ensureYearConfig(any(), any()) } just runs
        every { repository.getActiveMembers() } returns flowOf(emptyList())
    }

    private fun stubTrackerInit(trackerId: Long, trackerType: TrackerType) {
        every { repository.getTrackerByIdFlow(trackerId) } returns flowOf(
            Tracker(
                id = trackerId,
                name = "Tracker",
                type = trackerType,
                defaultAmount = 5000.0
            )
        )
        every { repository.getAllMembersForTracker(trackerId) } returns flowOf(listOf(TrackerMember(id = 1, trackerId = trackerId, name = "A")))
        every { repository.getPeriodsForTracker(trackerId) } returns flowOf(
            listOf(TrackerPeriod(id = 11, trackerId = trackerId, label = "February 2026", startDate = 0, endDate = 1, isCurrent = true))
        )
        every { repository.getRecordsForTracker(trackerId) } returns flowOf(emptyList())
        coEvery { repository.ensureYearConfig(any(), any()) } just runs
        every { repository.getActiveMembersForTracker(trackerId) } returns flowOf(emptyList())
        every { repository.getRecordsForPeriod(any(), any()) } returns flowOf(emptyList())
    }
}
```

## `app/src/test/java/com/mikeisesele/clearr/ui/feature/settings/SettingsViewModelTest.kt`

```kotlin
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
```

## `app/src/test/java/com/mikeisesele/clearr/ui/feature/setup/SetupViewModelTest.kt`

```kotlin
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
```

## `app/src/test/java/com/mikeisesele/clearr/ui/feature/trackerlist/TrackerListViewModelTest.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.trackerlist

import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.RecordStatus
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.data.model.TrackerPeriod
import com.mikeisesele.clearr.data.model.TrackerRecord
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.repository.DuesRepository
import com.mikeisesele.clearr.domain.trackers.ObserveTrackerSummariesUseCase
import com.mikeisesele.clearr.domain.trackers.TrackerBootstrapper
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
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<DuesRepository>()
    private val trackerBootstrapper = mockk<TrackerBootstrapper>()

    private fun stubStaticBootstrap() {
        coEvery { trackerBootstrapper.ensureStaticTrackers() } just runs
        coEvery { repository.insertTracker(any()) } returns 999L
        coEvery { repository.ensureBudgetPeriods(any(), any()) } just runs
    }

    @Test
    fun `empty tracker flow produces non loading empty state`() = runTest {
        stubStaticBootstrap()
        every { repository.getAllTrackers() } returns MutableStateFlow(emptyList())

        val viewModel = TrackerListViewModel(repository, trackerBootstrapper, ObserveTrackerSummariesUseCase(repository))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.summaries.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `summary counts completed dues records only`() = runTest {
        stubStaticBootstrap()
        val tracker = Tracker(id = 9, name = "Dues", type = TrackerType.DUES, frequency = Frequency.MONTHLY)
        every { repository.getAllTrackers() } returns MutableStateFlow(listOf(tracker))
        every { repository.getActiveMembersForTracker(9) } returns MutableStateFlow(
            listOf(
                TrackerMember(id = 1, trackerId = 9, name = "A"),
                TrackerMember(id = 2, trackerId = 9, name = "B"),
                TrackerMember(id = 3, trackerId = 9, name = "C")
            )
        )
        every { repository.getCurrentPeriodFlow(9) } returns MutableStateFlow(
            TrackerPeriod(id = 77, trackerId = 9, label = "February 2026", startDate = 0, endDate = 1, isCurrent = true)
        )
        every { repository.getRecordsForPeriod(9, 77) } returns MutableStateFlow(
            listOf(
                TrackerRecord(trackerId = 9, periodId = 77, memberId = 1, status = RecordStatus.PAID),
                TrackerRecord(trackerId = 9, periodId = 77, memberId = 2, status = RecordStatus.UNPAID)
            )
        )

        val viewModel = TrackerListViewModel(repository, trackerBootstrapper, ObserveTrackerSummariesUseCase(repository))
        advanceUntilIdle()

        val summary = viewModel.uiState.value.summaries.single()
        assertEquals(1, summary.completedCount)
        assertEquals(3, summary.totalMembers)
        assertEquals(33, summary.completionPercent)
    }

    @Test
    fun `create tracker inserts tracker members period and current period`() = runTest {
        coEvery { trackerBootstrapper.ensureStaticTrackers() } just runs
        coEvery { repository.ensureBudgetPeriods(any(), any()) } just runs
        every { repository.getAllTrackers() } returns MutableStateFlow(emptyList())
        coEvery { repository.insertTrackerMember(any()) } returns 1L
        coEvery { repository.insertPeriod(any()) } returns 400L
        coEvery { repository.setCurrentPeriod(45L, 400L) } just runs

        val insertedTrackers = mutableListOf<Tracker>()
        val memberSlot = mutableListOf<TrackerMember>()
        coEvery { repository.insertTracker(capture(insertedTrackers)) } answers {
            val tracker = firstArg<Tracker>()
            if (tracker.type == TrackerType.DUES && tracker.name.contains("New Tracker")) 45L else 900L
        }
        coEvery { repository.insertTrackerMember(capture(memberSlot)) } returns 1L

        val viewModel = TrackerListViewModel(repository, trackerBootstrapper, ObserveTrackerSummariesUseCase(repository))
        viewModel.onAction(
            TrackerListAction.CreateTracker(
                name = "  New Tracker  ",
                type = TrackerType.DUES,
                frequency = Frequency.MONTHLY,
                defaultAmount = 5500.0,
                initialMembers = listOf(" Henry ", "", " Simon ")
            )
        )
        advanceUntilIdle()

        val createdTracker = insertedTrackers.last { it.type == TrackerType.DUES && it.name.contains("New Tracker") }
        assertEquals("  New Tracker  ", createdTracker.name)
        assertEquals(2, memberSlot.size)
        assertEquals("Henry", memberSlot[0].name)
        assertEquals("Simon", memberSlot[1].name)
        coVerify(exactly = 1) { repository.insertPeriod(any()) }
        coVerify(exactly = 1) { repository.setCurrentPeriod(45L, 400L) }
    }

    @Test
    fun `rename tracker trims name and updates repository`() = runTest {
        stubStaticBootstrap()
        val existing = Tracker(id = 8, name = "Old", type = TrackerType.TODO, frequency = Frequency.WEEKLY, layoutStyle = LayoutStyle.CARDS)
        every { repository.getAllTrackers() } returns MutableStateFlow(emptyList())
        coEvery { repository.getTrackerById(8) } returns existing
        coEvery { repository.updateTracker(any()) } just runs

        val viewModel = TrackerListViewModel(repository, trackerBootstrapper, ObserveTrackerSummariesUseCase(repository))
        viewModel.onAction(TrackerListAction.RenameTracker(8, "  New Name  "))
        advanceUntilIdle()

        coVerify {
            repository.updateTracker(match { it.id == 8L && it.name == "New Name" })
        }
    }

    @Test
    fun `delete and clear new flag delegate to repository`() = runTest {
        stubStaticBootstrap()
        val trackersFlow = MutableStateFlow(
            listOf(
                Tracker(id = 2, name = "Remittance", type = TrackerType.DUES, frequency = Frequency.MONTHLY)
            )
        )
        every { repository.getAllTrackers() } returns trackersFlow
        every { repository.getActiveMembersForTracker(2) } returns MutableStateFlow(emptyList())
        every { repository.getCurrentPeriodFlow(2) } returns MutableStateFlow(null)
        coEvery {
            repository.deleteTracker(2)
        } answers {
            trackersFlow.value = emptyList()
        }
        coEvery { repository.clearTrackerNewFlag(2) } just runs

        val viewModel = TrackerListViewModel(repository, trackerBootstrapper, ObserveTrackerSummariesUseCase(repository))
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.summaries.size)

        viewModel.onAction(TrackerListAction.DeleteTracker(2))
        assertTrue(viewModel.uiState.value.summaries.isEmpty())

        viewModel.onAction(TrackerListAction.ClearNewFlag(2))
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.deleteTracker(2) }
        coVerify(exactly = 1) { repository.clearTrackerNewFlag(2) }
        assertTrue(viewModel.uiState.value.summaries.isEmpty())
    }

    private fun assertTrue(value: Boolean) {
        org.junit.Assert.assertTrue(value)
    }
}
```

## `app/src/test/java/com/mikeisesele/clearr/ui/navigation/TrackerDetailHostViewModelTest.kt`

```kotlin
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
```

