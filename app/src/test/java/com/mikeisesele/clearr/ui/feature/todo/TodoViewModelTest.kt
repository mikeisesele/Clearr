package com.mikeisesele.clearr.ui.feature.todo

import androidx.lifecycle.SavedStateHandle
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import com.mikeisesele.clearr.data.model.TodoStatus
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.data.repository.TodoPreferencesRepository
import com.mikeisesele.clearr.domain.repository.DuesRepository
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TodoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<DuesRepository>()
    private val preferencesRepository = mockk<TodoPreferencesRepository>()
    private val trackerId = 5L

    @Test
    fun `loads todos with sorted display and counts`() = runTest {
        val today = LocalDate.now()
        val todosFlow = MutableStateFlow(
            listOf(
                TodoItem(
                    id = "done",
                    trackerId = trackerId,
                    title = "Done task",
                    priority = TodoPriority.LOW,
                    dueDate = today.minusDays(2),
                    status = TodoStatus.DONE,
                    createdAt = 1L,
                    completedAt = 50L
                ),
                TodoItem(
                    id = "overdue",
                    trackerId = trackerId,
                    title = "Overdue task",
                    priority = TodoPriority.MEDIUM,
                    dueDate = today.minusDays(1),
                    status = TodoStatus.PENDING,
                    createdAt = 1L
                ),
                TodoItem(
                    id = "pending",
                    trackerId = trackerId,
                    title = "Pending task",
                    priority = TodoPriority.HIGH,
                    dueDate = today.plusDays(1),
                    status = TodoStatus.PENDING,
                    createdAt = 1L
                )
            )
        )
        stubBaseFlows(todosFlow = todosFlow, hintSeen = false)

        val viewModel = TodoViewModel(
            repository = repository,
            todoPreferencesRepository = preferencesRepository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Todo Tracker", state.trackerName)
        assertEquals(1, state.counts.done)
        assertEquals(1, state.counts.overdue)
        assertEquals(1, state.counts.pending)
        assertTrue(state.showSwipeHint)
        assertEquals(listOf("overdue", "pending", "done"), state.displayedTodos.map { it.id })
    }

    @Test
    fun `set filter done shows only done todos`() = runTest {
        val todosFlow = MutableStateFlow(
            listOf(
                TodoItem(
                    id = "1",
                    trackerId = trackerId,
                    title = "Done",
                    priority = TodoPriority.MEDIUM,
                    status = TodoStatus.DONE,
                    createdAt = 1L,
                    completedAt = 1L
                ),
                TodoItem(
                    id = "2",
                    trackerId = trackerId,
                    title = "Pending",
                    priority = TodoPriority.MEDIUM,
                    status = TodoStatus.PENDING,
                    createdAt = 1L
                )
            )
        )
        stubBaseFlows(todosFlow = todosFlow, hintSeen = true)

        val viewModel = TodoViewModel(
            repository = repository,
            todoPreferencesRepository = preferencesRepository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(TodoAction.SetFilter(TodoFilter.DONE))
        advanceUntilIdle()

        assertEquals(TodoFilter.DONE, viewModel.uiState.value.filter)
        assertEquals(listOf("1"), viewModel.uiState.value.displayedTodos.map { it.id })
        assertFalse(viewModel.uiState.value.showSwipeHint)
    }

    @Test
    fun `add todo trims title and note then inserts`() = runTest {
        stubBaseFlows(todosFlow = MutableStateFlow(emptyList()), hintSeen = false)
        coEvery { repository.insertTodo(any()) } just runs

        val viewModel = TodoViewModel(
            repository = repository,
            todoPreferencesRepository = preferencesRepository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(
            TodoAction.AddTodo(
                title = "  Buy milk  ",
                note = "   ",
                priority = TodoPriority.HIGH,
                dueDate = LocalDate.now()
            )
        )
        advanceUntilIdle()

        coVerify {
            repository.insertTodo(
                match {
                    it.trackerId == trackerId &&
                        it.title == "Buy milk" &&
                        it.note == null &&
                        it.priority == TodoPriority.HIGH &&
                        it.status == TodoStatus.PENDING
                }
            )
        }
    }

    @Test
    fun `add todo auto capitalizes first word`() = runTest {
        stubBaseFlows(todosFlow = MutableStateFlow(emptyList()), hintSeen = false)
        coEvery { repository.insertTodo(any()) } just runs

        val viewModel = TodoViewModel(
            repository = repository,
            todoPreferencesRepository = preferencesRepository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(
            TodoAction.AddTodo(
                title = "buy milk and bread",
                note = null,
                priority = TodoPriority.MEDIUM,
                dueDate = null
            )
        )
        advanceUntilIdle()

        coVerify {
            repository.insertTodo(match { it.title == "Buy milk and bread" })
        }
    }

    @Test
    fun `mark all done updates only incomplete todos`() = runTest {
        val todosFlow = MutableStateFlow(
            listOf(
                TodoItem(
                    id = "done",
                    trackerId = trackerId,
                    title = "Done",
                    priority = TodoPriority.MEDIUM,
                    status = TodoStatus.DONE,
                    createdAt = 1L,
                    completedAt = 10L
                ),
                TodoItem(
                    id = "pending",
                    trackerId = trackerId,
                    title = "Pending",
                    priority = TodoPriority.HIGH,
                    status = TodoStatus.PENDING,
                    createdAt = 2L
                )
            )
        )
        stubBaseFlows(todosFlow = todosFlow, hintSeen = false)
        coEvery { repository.markTodoDone(any(), any()) } just runs

        val viewModel = TodoViewModel(
            repository = repository,
            todoPreferencesRepository = preferencesRepository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(TodoAction.MarkAllDone)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.markTodoDone("pending", any()) }
        coVerify(exactly = 0) { repository.markTodoDone("done", any()) }
    }

    @Test
    fun `clear completed deletes only done todos`() = runTest {
        val todosFlow = MutableStateFlow(
            listOf(
                TodoItem(
                    id = "done",
                    trackerId = trackerId,
                    title = "Done",
                    priority = TodoPriority.MEDIUM,
                    status = TodoStatus.DONE,
                    createdAt = 1L,
                    completedAt = 10L
                ),
                TodoItem(
                    id = "pending",
                    trackerId = trackerId,
                    title = "Pending",
                    priority = TodoPriority.HIGH,
                    status = TodoStatus.PENDING,
                    createdAt = 2L
                )
            )
        )
        stubBaseFlows(todosFlow = todosFlow, hintSeen = false)
        coEvery { repository.deleteTodo(any()) } just runs

        val viewModel = TodoViewModel(
            repository = repository,
            todoPreferencesRepository = preferencesRepository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(TodoAction.ClearCompleted)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.deleteTodo("done") }
        coVerify(exactly = 0) { repository.deleteTodo("pending") }
    }

    @Test
    fun `mark done delete and first swipe delegate to dependencies`() = runTest {
        stubBaseFlows(todosFlow = MutableStateFlow(emptyList()), hintSeen = false)
        coEvery { repository.markTodoDone("x", any()) } just runs
        coEvery { repository.deleteTodo("x") } just runs
        coEvery { preferencesRepository.markSwipeHintSeen() } just runs

        val viewModel = TodoViewModel(
            repository = repository,
            todoPreferencesRepository = preferencesRepository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(TodoAction.MarkDone("x"))
        viewModel.onAction(TodoAction.Delete("x"))
        viewModel.onAction(TodoAction.OnFirstSwipeAction)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.markTodoDone("x", any()) }
        coVerify(exactly = 1) { repository.deleteTodo("x") }
        coVerify(exactly = 1) { preferencesRepository.markSwipeHintSeen() }
    }

    @Test
    fun `rename trims title and updates existing todo`() = runTest {
        stubBaseFlows(todosFlow = MutableStateFlow(emptyList()), hintSeen = true)
        val existing = TodoItem(
            id = "t1",
            trackerId = trackerId,
            title = "Old",
            priority = TodoPriority.MEDIUM,
            status = TodoStatus.PENDING,
            createdAt = 1L
        )
        coEvery { repository.getTodoById("t1") } returns existing
        coEvery { repository.updateTodo(any()) } just runs

        val viewModel = TodoViewModel(
            repository = repository,
            todoPreferencesRepository = preferencesRepository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(TodoAction.Rename("t1", "  New title  "))
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.updateTodo(match { it.id == "t1" && it.title == "New title" })
        }
    }

    @Test
    fun `rename auto capitalizes first word`() = runTest {
        stubBaseFlows(todosFlow = MutableStateFlow(emptyList()), hintSeen = true)
        val existing = TodoItem(
            id = "t1",
            trackerId = trackerId,
            title = "Old",
            priority = TodoPriority.MEDIUM,
            status = TodoStatus.PENDING,
            createdAt = 1L
        )
        coEvery { repository.getTodoById("t1") } returns existing
        coEvery { repository.updateTodo(any()) } just runs

        val viewModel = TodoViewModel(
            repository = repository,
            todoPreferencesRepository = preferencesRepository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(TodoAction.Rename("t1", "new title from user"))
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.updateTodo(match { it.id == "t1" && it.title == "New title from user" })
        }
    }

    @Test
    fun `rename no-op for blank title or missing todo`() = runTest {
        stubBaseFlows(todosFlow = MutableStateFlow(emptyList()), hintSeen = true)
        coEvery { repository.getTodoById("missing") } returns null
        coEvery { repository.updateTodo(any()) } just runs

        val viewModel = TodoViewModel(
            repository = repository,
            todoPreferencesRepository = preferencesRepository,
            savedStateHandle = SavedStateHandle(mapOf("trackerId" to trackerId))
        )
        advanceUntilIdle()

        viewModel.onAction(TodoAction.Rename("missing", "Valid"))
        viewModel.onAction(TodoAction.Rename("t1", "   "))
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.updateTodo(any()) }
    }

    private fun stubBaseFlows(
        todosFlow: MutableStateFlow<List<TodoItem>>,
        hintSeen: Boolean
    ) {
        every { repository.getTrackerByIdFlow(trackerId) } returns flowOf(
            Tracker(
                id = trackerId,
                name = "Todo Tracker",
                type = TrackerType.TODO,
                frequency = Frequency.MONTHLY,
                layoutStyle = LayoutStyle.GRID,
                defaultAmount = 0.0,
                isNew = false,
                createdAt = 1L
            )
        )
        every { repository.getTodosForTracker(trackerId) } returns todosFlow
        every { preferencesRepository.swipeHintSeen } returns flowOf(hintSeen)
    }
}
