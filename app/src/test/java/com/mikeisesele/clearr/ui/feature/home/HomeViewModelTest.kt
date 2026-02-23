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
