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

    @Test
    fun `empty tracker flow produces non loading empty state`() = runTest {
        every { repository.getAllTrackers() } returns MutableStateFlow(emptyList())

        val viewModel = TrackerListViewModel(repository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.summaries.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `summary counts completed dues records only`() = runTest {
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

        val viewModel = TrackerListViewModel(repository)
        advanceUntilIdle()

        val summary = viewModel.uiState.value.summaries.single()
        assertEquals(1, summary.completedCount)
        assertEquals(3, summary.totalMembers)
        assertEquals(33, summary.completionPercent)
    }

    @Test
    fun `create tracker inserts tracker members period and current period`() = runTest {
        every { repository.getAllTrackers() } returns MutableStateFlow(emptyList())
        coEvery { repository.insertTracker(any()) } returns 45L
        coEvery { repository.insertTrackerMember(any()) } returns 1L
        coEvery { repository.insertPeriod(any()) } returns 400L
        coEvery { repository.setCurrentPeriod(45L, 400L) } just runs

        val trackerSlot = slot<Tracker>()
        val memberSlot = mutableListOf<TrackerMember>()
        coEvery { repository.insertTracker(capture(trackerSlot)) } returns 45L
        coEvery { repository.insertTrackerMember(capture(memberSlot)) } returns 1L

        val viewModel = TrackerListViewModel(repository)
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

        assertEquals("  New Tracker  ", trackerSlot.captured.name)
        assertEquals(2, memberSlot.size)
        assertEquals("Henry", memberSlot[0].name)
        assertEquals("Simon", memberSlot[1].name)
        coVerify(exactly = 1) { repository.insertPeriod(any()) }
        coVerify(exactly = 1) { repository.setCurrentPeriod(45L, 400L) }
    }

    @Test
    fun `rename tracker trims name and updates repository`() = runTest {
        val existing = Tracker(id = 8, name = "Old", type = TrackerType.TODO, frequency = Frequency.WEEKLY, layoutStyle = LayoutStyle.CARDS)
        every { repository.getAllTrackers() } returns MutableStateFlow(emptyList())
        coEvery { repository.getTrackerById(8) } returns existing
        coEvery { repository.updateTracker(any()) } just runs

        val viewModel = TrackerListViewModel(repository)
        viewModel.onAction(TrackerListAction.RenameTracker(8, "  New Name  "))
        advanceUntilIdle()

        coVerify {
            repository.updateTracker(match { it.id == 8L && it.name == "New Name" })
        }
    }

    @Test
    fun `delete and clear new flag delegate to repository`() = runTest {
        every { repository.getAllTrackers() } returns MutableStateFlow(emptyList())
        coEvery { repository.deleteTracker(2) } just runs
        coEvery { repository.clearTrackerNewFlag(2) } just runs

        val viewModel = TrackerListViewModel(repository)
        viewModel.onAction(TrackerListAction.DeleteTracker(2))
        viewModel.onAction(TrackerListAction.ClearNewFlag(2))
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.deleteTracker(2) }
        coVerify(exactly = 1) { repository.clearTrackerNewFlag(2) }
    }

    private fun assertTrue(value: Boolean) {
        org.junit.Assert.assertTrue(value)
    }
}
