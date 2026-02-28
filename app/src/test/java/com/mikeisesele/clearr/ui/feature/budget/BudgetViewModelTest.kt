package com.mikeisesele.clearr.ui.feature.budget

import androidx.lifecycle.SavedStateHandle
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetEntry
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetPeriod
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.repository.DuesRepository
import com.mikeisesele.clearr.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.slot
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<DuesRepository>()
    private val trackerId = 77L

    @Test
    fun `loads budget summaries and totals`() = runTest {
        val periodsFlow = MutableStateFlow(
            listOf(
                BudgetPeriod(
                    id = 10L,
                    trackerId = trackerId,
                    frequency = BudgetFrequency.MONTHLY,
                    label = "Feb 2026",
                    startDate = 0L,
                    endDate = 1L
                )
            )
        )
        val categoriesFlow = MutableStateFlow(
            listOf(
                BudgetCategory(
                    id = 1L,
                    trackerId = trackerId,
                    frequency = BudgetFrequency.MONTHLY,
                    name = "Food",
                    icon = "🍔",
                    colorToken = "Orange",
                    plannedAmountKobo = 50_000L,
                    sortOrder = 0
                ),
                BudgetCategory(
                    id = 2L,
                    trackerId = trackerId,
                    frequency = BudgetFrequency.MONTHLY,
                    name = "Transport",
                    icon = "🚗",
                    colorToken = "Blue",
                    plannedAmountKobo = 30_000L,
                    sortOrder = 1
                )
            )
        )
        val entriesFlow = MutableStateFlow(
            listOf(
                BudgetEntry(
                    id = 1L,
                    trackerId = trackerId,
                    categoryId = 1L,
                    periodId = 10L,
                    amountKobo = 50_000L
                ),
                BudgetEntry(
                    id = 2L,
                    trackerId = trackerId,
                    categoryId = 2L,
                    periodId = 10L,
                    amountKobo = 10_000L
                )
            )
        )
        stubBaseFlows(periodsFlow, categoriesFlow, entriesFlow)

        val viewModel = BudgetViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Budget Tracker", state.trackerName)
        assertEquals(10L, state.selectedPeriodId)
        assertEquals(2, state.categorySummaries.size)
        assertEquals(80_000L, state.budgetSummary.totalPlannedKobo)
        assertEquals(60_000L, state.budgetSummary.totalSpentKobo)
        assertEquals(20_000L, state.budgetSummary.totalRemainingKobo)
    }

    @Test
    fun `log expense converts naira to kobo and trims note`() = runTest {
        val periodsFlow = MutableStateFlow(
            listOf(
                BudgetPeriod(
                    id = 44L,
                    trackerId = trackerId,
                    frequency = BudgetFrequency.MONTHLY,
                    label = "Feb 2026",
                    startDate = 0L,
                    endDate = 1L
                )
            )
        )
        stubBaseFlows(
            periodsFlow = periodsFlow,
            categoriesFlow = MutableStateFlow(emptyList()),
            entriesFlow = MutableStateFlow(emptyList())
        )
        coEvery { repository.addBudgetEntry(any()) } returns 1L
        coEvery { repository.addBudgetCategory(any()) } returns 1L

        val viewModel = BudgetViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()
        viewModel.onAction(BudgetAction.SelectPeriod(44L))

        val entrySlot = slot<BudgetEntry>()
        viewModel.onAction(
            BudgetAction.LogExpense(
                categoryId = 3L,
                amountNaira = 1234.5,
                note = "  fuel  "
            )
        )
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.addBudgetEntry(capture(entrySlot)) }
        assertEquals(trackerId, entrySlot.captured.trackerId)
        assertEquals(44L, entrySlot.captured.periodId)
        assertEquals(123_450L, entrySlot.captured.amountKobo)
        assertEquals("fuel", entrySlot.captured.note)
    }

    @Test
    fun `add category uses next sort order and trimmed name`() = runTest {
        stubBaseFlows(
            periodsFlow = MutableStateFlow(emptyList()),
            categoriesFlow = MutableStateFlow(emptyList()),
            entriesFlow = MutableStateFlow(emptyList())
        )
        coEvery { repository.getBudgetMaxSortOrder(trackerId, BudgetFrequency.MONTHLY) } returns 4
        coEvery { repository.addBudgetCategory(any()) } returns 1L

        val viewModel = BudgetViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(
            BudgetAction.AddCategory(
                name = "  Utilities ",
                icon = "💡",
                colorToken = "Violet",
                plannedAmountNaira = 1000.0
            )
        )
        advanceUntilIdle()

        coVerify {
            repository.addBudgetCategory(
                match {
                    it.trackerId == trackerId &&
                        it.name == "Utilities" &&
                        it.icon == "💡" &&
                        it.colorToken == "Violet" &&
                        it.plannedAmountKobo == 100_000L &&
                        it.sortOrder == 5
                }
            )
        }
    }

    @Test
    fun `set frequency updates state and reloads for new frequency`() = runTest {
        val monthlyPeriods = MutableStateFlow(
            listOf(
                BudgetPeriod(
                    id = 1L,
                    trackerId = trackerId,
                    frequency = BudgetFrequency.MONTHLY,
                    label = "Feb 2026",
                    startDate = 0L,
                    endDate = 1L
                )
            )
        )
        val weeklyPeriods = MutableStateFlow(
            listOf(
                BudgetPeriod(
                    id = 2L,
                    trackerId = trackerId,
                    frequency = BudgetFrequency.WEEKLY,
                    label = "Week 8",
                    startDate = 0L,
                    endDate = 1L
                )
            )
        )

        every { repository.getTrackerByIdFlow(trackerId) } returns flowOf(
            Tracker(
                id = trackerId,
                name = "Budget Tracker",
                type = TrackerType.BUDGET,
                frequency = Frequency.MONTHLY,
                layoutStyle = LayoutStyle.GRID,
                defaultAmount = 0.0,
                isNew = false,
                createdAt = 1L
            )
        )
        coEvery { repository.ensureBudgetPeriods(trackerId, any()) } just runs
        coEvery { repository.getBudgetMaxSortOrder(trackerId, any()) } returns 0
        coEvery { repository.addBudgetCategory(any()) } returns 1L
        every { repository.getBudgetPeriods(trackerId, BudgetFrequency.MONTHLY) } returns monthlyPeriods
        every { repository.getBudgetPeriods(trackerId, BudgetFrequency.WEEKLY) } returns weeklyPeriods
        every { repository.getBudgetCategories(trackerId, any()) } returns MutableStateFlow(emptyList())
        every { repository.getBudgetEntriesForTracker(trackerId) } returns MutableStateFlow(emptyList())

        val viewModel = BudgetViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()
        assertEquals(BudgetFrequency.MONTHLY, viewModel.uiState.value.frequency)
        assertEquals(1L, viewModel.uiState.value.selectedPeriodId)

        viewModel.onAction(BudgetAction.SetFrequency(BudgetFrequency.WEEKLY))
        advanceUntilIdle()

        coVerify { repository.ensureBudgetPeriods(trackerId, BudgetFrequency.WEEKLY) }
        assertEquals(weeklyPeriods.value, viewModel.uiState.value.periods)
        assertEquals(weeklyPeriods.value.lastOrNull()?.id, viewModel.uiState.value.selectedPeriodId)
        assertTrue(viewModel.uiState.value.periods.any { it.frequency == BudgetFrequency.WEEKLY })
    }

    private fun stubBaseFlows(
        periodsFlow: MutableStateFlow<List<BudgetPeriod>>,
        categoriesFlow: MutableStateFlow<List<BudgetCategory>>,
        entriesFlow: MutableStateFlow<List<BudgetEntry>>
    ) {
        every { repository.getTrackerByIdFlow(trackerId) } returns flowOf(
            Tracker(
                id = trackerId,
                name = "Budget Tracker",
                type = TrackerType.BUDGET,
                frequency = Frequency.MONTHLY,
                layoutStyle = LayoutStyle.GRID,
                defaultAmount = 0.0,
                isNew = false,
                createdAt = 1L
            )
        )
        coEvery { repository.ensureBudgetPeriods(trackerId, any()) } just runs
        coEvery { repository.getBudgetMaxSortOrder(trackerId, any()) } returns 0
        every { repository.getBudgetPeriods(trackerId, BudgetFrequency.MONTHLY) } returns periodsFlow
        every { repository.getBudgetPeriods(trackerId, BudgetFrequency.WEEKLY) } returns MutableStateFlow(emptyList())
        every { repository.getBudgetCategories(trackerId, any()) } returns categoriesFlow
        every { repository.getBudgetEntriesForTracker(trackerId) } returns entriesFlow
    }
}
