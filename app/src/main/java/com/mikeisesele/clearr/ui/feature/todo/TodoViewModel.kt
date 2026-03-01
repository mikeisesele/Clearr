package com.mikeisesele.clearr.ui.feature.todo

import androidx.lifecycle.SavedStateHandle
import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import com.mikeisesele.clearr.data.model.TodoStatus
import com.mikeisesele.clearr.data.repository.TodoPreferencesRepository
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: ClearrRepository,
    private val todoPreferencesRepository: TodoPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<TodoUiState, TodoAction, TodoEvent>(
    initialState = TodoUiState(trackerId = checkNotNull(savedStateHandle.get<Long>("trackerId")))
) {

    private val filterFlow = MutableStateFlow(TodoFilter.ALL)

    init {
        observeTracker()
        observeTodos()
        observeHintPreference()
    }

    override fun onAction(action: TodoAction) {
        when (action) {
            is TodoAction.SetFilter -> setFilter(action.filter)
            is TodoAction.AddTodo -> addTodo(action.title, action.note, action.priority, action.dueDate)
            is TodoAction.Rename -> rename(action.id, action.title)
            is TodoAction.MarkDone -> markDone(action.id)
            TodoAction.MarkAllDone -> markAllDone()
            is TodoAction.Delete -> delete(action.id)
            TodoAction.ClearCompleted -> clearCompleted()
            TodoAction.OnFirstSwipeAction -> markHintSeen()
        }
    }

    private fun observeTracker() {
        launch {
            repository.getTrackerByIdFlow(currentState.trackerId).collectLatest { tracker ->
                if (tracker == null) {
                    updateState { it.copy(isLoading = false) }
                    return@collectLatest
                }
                updateState {
                    it.copy(
                        trackerName = tracker.name
                    )
                }
            }
        }
    }

    private fun observeTodos() {
        launch {
            filterFlow
                .flatMapLatest { filter ->
                    repository.getTodosForTracker(currentState.trackerId).map { todos ->
                        val sortedTodos = todos.sortedForUi()
                        val displayed = when (filter) {
                            TodoFilter.ALL -> sortedTodos
                            TodoFilter.PENDING -> sortedTodos.filter { it.uiDerivedStatus() != TodoStatus.DONE }
                            TodoFilter.DONE -> sortedTodos.filter { it.uiDerivedStatus() == TodoStatus.DONE }
                        }
                        val counts = TodoCounts(
                            pending = sortedTodos.count { it.uiDerivedStatus() == TodoStatus.PENDING },
                            overdue = sortedTodos.count { it.uiDerivedStatus() == TodoStatus.OVERDUE },
                            done = sortedTodos.count { it.uiDerivedStatus() == TodoStatus.DONE }
                        )
                        Triple(filter, sortedTodos, displayed to counts)
                    }
                }
                .collectLatest { (filter, todos, payload) ->
                    val (displayed, counts) = payload
                    val insight = ClearrEdgeAi.todoInsightNanoAware(todos)
                    updateState {
                        it.copy(
                            filter = filter,
                            todos = todos,
                            displayedTodos = displayed,
                            counts = counts,
                            aiInsight = insight,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun observeHintPreference() {
        launch {
            todoPreferencesRepository.swipeHintSeen.collectLatest { seen ->
                updateState { it.copy(showSwipeHint = !seen) }
            }
        }
    }

    private fun setFilter(filter: TodoFilter) {
        filterFlow.value = filter
        updateState { it.copy(filter = filter) }
    }

    private fun addTodo(title: String, note: String?, priority: TodoPriority, dueDate: LocalDate?) {
        launch {
            val ai = ClearrEdgeAi.inferTodoNanoAware(
                title = title,
                note = note,
                selectedPriority = priority,
                selectedDueDate = dueDate
            )
            if (ai.normalizedTitle.isBlank()) return@launch
            repository.insertTodo(
                TodoItem(
                    id = UUID.randomUUID().toString(),
                    trackerId = currentState.trackerId,
                    title = ai.normalizedTitle,
                    note = ai.normalizedNote,
                    priority = ai.suggestedPriority,
                    dueDate = ai.suggestedDueDate,
                    status = TodoStatus.PENDING,
                    createdAt = System.currentTimeMillis(),
                    completedAt = null
                )
            )
        }
    }

    private fun markDone(id: String) {
        launch {
            repository.markTodoDone(id, System.currentTimeMillis())
        }
    }

    private fun markAllDone() {
        launch {
            currentState.todos
                .filter { todo -> todo.uiDerivedStatus() != TodoStatus.DONE }
                .forEach { todo ->
                    repository.markTodoDone(todo.id, System.currentTimeMillis())
                }
        }
    }

    private fun delete(id: String) {
        launch {
            repository.deleteTodo(id)
        }
    }

    private fun clearCompleted() {
        launch {
            currentState.todos
                .filter { todo -> todo.uiDerivedStatus() == TodoStatus.DONE }
                .forEach { todo ->
                    repository.deleteTodo(todo.id)
                }
        }
    }

    private fun rename(id: String, title: String) {
        launch {
            val normalizedTitle = ClearrEdgeAi.normalizeTitle(title)
            if (normalizedTitle.isBlank()) return@launch
            val existing = repository.getTodoById(id) ?: return@launch
            repository.updateTodo(existing.copy(title = normalizedTitle))
        }
    }

    private fun markHintSeen() {
        launch {
            todoPreferencesRepository.markSwipeHintSeen()
        }
    }
}

private fun List<TodoItem>.sortedForUi(now: LocalDate = LocalDate.now()): List<TodoItem> {
    val overdue = filter { it.uiDerivedStatus(now) == TodoStatus.OVERDUE }
        .sortedBy { it.dueDate ?: LocalDate.MAX }

    val pending = filter { it.uiDerivedStatus(now) == TodoStatus.PENDING }
        .sortedWith(
            compareBy<TodoItem>({ it.priority.ordinal }, { it.dueDate ?: LocalDate.MAX })
        )

    val done = filter { it.uiDerivedStatus(now) == TodoStatus.DONE }
        .sortedByDescending { it.completedAt ?: 0L }

    return overdue + pending + done
}

private fun TodoItem.uiDerivedStatus(today: LocalDate = LocalDate.now()): TodoStatus = when {
    status == TodoStatus.DONE -> TodoStatus.DONE
    dueDate?.let { it < today } == true -> TodoStatus.OVERDUE
    else -> TodoStatus.PENDING
}
