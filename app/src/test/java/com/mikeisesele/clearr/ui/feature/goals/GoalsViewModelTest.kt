package com.mikeisesele.clearr.ui.feature.goals

import androidx.lifecycle.SavedStateHandle
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.Goal
import com.mikeisesele.clearr.data.model.GoalCompletion
import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.data.model.GoalPeriodKey
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.repository.GoalsRepository
import com.mikeisesele.clearr.testutil.MainDispatcherRule
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<GoalsRepository>()
    private val trackerId = 1L

    @Test
    fun `summaries and counts reflect goals completion state`() = runTest {
        val goalsFlow = MutableStateFlow(
            listOf(
                Goal(
                    id = "g_pending",
                    trackerId = trackerId,
                    title = "Exercise",
                    emoji = "🏃",
                    colorToken = "Purple",
                    target = "30 mins",
                    frequency = GoalFrequency.DAILY,
                    createdAt = 1L
                ),
                Goal(
                    id = "g_done",
                    trackerId = trackerId,
                    title = "Read",
                    emoji = "📚",
                    colorToken = "Blue",
                    target = "20 pages",
                    frequency = GoalFrequency.DAILY,
                    createdAt = 1L
                )
            )
        )
        val completionsFlow = MutableStateFlow(
            listOf(
                GoalCompletion(
                    id = "c1",
                    goalId = "g_done",
                    periodKey = GoalPeriodKey.currentKey(GoalFrequency.DAILY),
                    completedAt = 100L
                )
            )
        )
        stubBaseFlows(goalsFlow, completionsFlow)

        val viewModel = GoalsViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Goals Tracker", state.trackerName)
        assertEquals(2, state.totalCount)
        assertEquals(1, state.doneCount)
        assertFalse(state.allDoneThisPeriod)
        assertEquals("g_pending", state.summaries.first().goal.id)
        assertEquals("g_done", state.summaries.last().goal.id)
    }

    @Test
    fun `all done flag is true when all goals completed in current period`() = runTest {
        val goalsFlow = MutableStateFlow(
            listOf(
                Goal(
                    id = "g1",
                    trackerId = trackerId,
                    title = "Exercise",
                    emoji = "🏃",
                    colorToken = "Purple",
                    target = null,
                    frequency = GoalFrequency.DAILY,
                    createdAt = 1L
                )
            )
        )
        val completionsFlow = MutableStateFlow(
            listOf(
                GoalCompletion(
                    id = "c1",
                    goalId = "g1",
                    periodKey = GoalPeriodKey.currentKey(GoalFrequency.DAILY),
                    completedAt = 100L
                )
            )
        )
        stubBaseFlows(goalsFlow, completionsFlow)

        val viewModel = GoalsViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.allDoneThisPeriod)
    }

    @Test
    fun `mark done inserts completion for current goal period key`() = runTest {
        val goal = Goal(
            id = "g1",
            trackerId = trackerId,
            title = "Exercise",
            emoji = "🏃",
            colorToken = "Purple",
            target = null,
            frequency = GoalFrequency.WEEKLY,
            createdAt = 1L
        )
        stubBaseFlows(
            goalsFlow = MutableStateFlow(listOf(goal)),
            completionsFlow = MutableStateFlow(emptyList())
        )
        coEvery { repository.addGoalCompletion(any()) } just runs

        val viewModel = GoalsViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(GoalsAction.MarkDone("g1"))
        advanceUntilIdle()

        coVerify {
            repository.addGoalCompletion(
                match {
                    it.goalId == "g1" &&
                        it.periodKey == GoalPeriodKey.currentKey(GoalFrequency.WEEKLY)
                }
            )
        }
    }

    @Test
    fun `add goal trims title and maps blank target to null`() = runTest {
        stubBaseFlows(
            goalsFlow = MutableStateFlow(emptyList()),
            completionsFlow = MutableStateFlow(emptyList())
        )
        coEvery { repository.insertGoal(any()) } just runs

        val viewModel = GoalsViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(
            GoalsAction.AddGoal(
                title = "  New Goal  ",
                emoji = "🎯",
                colorToken = "Purple",
                target = "   ",
                frequency = GoalFrequency.DAILY
            )
        )
        advanceUntilIdle()

        coVerify {
            repository.insertGoal(
                match {
                    it.trackerId == trackerId &&
                        it.title == "New Goal" &&
                        it.emoji == "🎯" &&
                        it.colorToken == "Purple" &&
                        it.frequency == GoalFrequency.DAILY &&
                        it.target == null
                }
            )
        }
    }

    @Test
    fun `add goal auto capitalizes first word`() = runTest {
        stubBaseFlows(
            goalsFlow = MutableStateFlow(emptyList()),
            completionsFlow = MutableStateFlow(emptyList())
        )
        coEvery { repository.insertGoal(any()) } just runs

        val viewModel = GoalsViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(
            GoalsAction.AddGoal(
                title = "exercise every day",
                emoji = "🏃",
                colorToken = "Purple",
                target = null,
                frequency = GoalFrequency.DAILY
            )
        )
        advanceUntilIdle()

        coVerify {
            repository.insertGoal(match { it.title == "Exercise every day" })
        }
    }

    @Test
    fun `rename updates goal title when goal exists`() = runTest {
        val goalsFlow = MutableStateFlow(
            listOf(
                Goal(
                    id = "g1",
                    trackerId = trackerId,
                    title = "Old Goal",
                    emoji = "🎯",
                    colorToken = "Purple",
                    target = null,
                    frequency = GoalFrequency.DAILY,
                    createdAt = 1L
                )
            )
        )
        stubBaseFlows(goalsFlow, MutableStateFlow(emptyList()))
        coEvery { repository.insertGoal(any()) } just runs

        val viewModel = GoalsViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(GoalsAction.Rename(goalId = "g1", title = "  New Goal  "))
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.insertGoal(match { it.id == "g1" && it.title == "New Goal" })
        }
    }

    @Test
    fun `rename auto capitalizes first word`() = runTest {
        val goalsFlow = MutableStateFlow(
            listOf(
                Goal(
                    id = "g1",
                    trackerId = trackerId,
                    title = "Old Goal",
                    emoji = "🎯",
                    colorToken = "Purple",
                    target = null,
                    frequency = GoalFrequency.DAILY,
                    createdAt = 1L
                )
            )
        )
        stubBaseFlows(goalsFlow, MutableStateFlow(emptyList()))
        coEvery { repository.insertGoal(any()) } just runs

        val viewModel = GoalsViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(GoalsAction.Rename(goalId = "g1", title = "new goal title"))
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.insertGoal(match { it.id == "g1" && it.title == "New goal title" })
        }
    }

    @Test
    fun `rename no-op when goal missing or title blank`() = runTest {
        stubBaseFlows(
            goalsFlow = MutableStateFlow(emptyList()),
            completionsFlow = MutableStateFlow(emptyList())
        )
        coEvery { repository.insertGoal(any()) } just runs

        val viewModel = GoalsViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(GoalsAction.Rename(goalId = "missing", title = "Valid"))
        viewModel.onAction(GoalsAction.Rename(goalId = "missing", title = "  "))
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.insertGoal(any()) }
    }

    @Test
    fun `delete delegates to repository`() = runTest {
        stubBaseFlows(
            goalsFlow = MutableStateFlow(emptyList()),
            completionsFlow = MutableStateFlow(emptyList())
        )
        coEvery { repository.deleteGoal("g1") } just runs

        val viewModel = GoalsViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(GoalsAction.Delete("g1"))
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.deleteGoal("g1") }
    }

    private fun stubBaseFlows(
        goalsFlow: MutableStateFlow<List<Goal>>,
        completionsFlow: MutableStateFlow<List<GoalCompletion>>
    ) {
        every { repository.getTrackerByIdFlow(trackerId) } returns flowOf(
            Tracker(
                id = trackerId,
                name = "Goals Tracker",
                type = TrackerType.GOALS,
                frequency = Frequency.MONTHLY,
                layoutStyle = LayoutStyle.GRID,
                defaultAmount = 0.0,
                isNew = false,
                createdAt = 1L
            )
        )
        every { repository.getGoalsForTracker(trackerId) } returns goalsFlow
        every { repository.getGoalCompletionsForTracker(trackerId) } returns completionsFlow
    }
}
