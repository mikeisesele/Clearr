package com.mikeisesele.clearr.ui.feature.todo

import androidx.lifecycle.SavedStateHandle
import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import com.mikeisesele.clearr.data.model.TodoStatus
import com.mikeisesele.clearr.data.model.derivedStatus
import com.mikeisesele.clearr.data.repository.TodoPreferencesRepository
import com.mikeisesele.clearr.domain.repository.DuesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: DuesRepository,
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
            is TodoAction.Delete -> delete(action.id)
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
                            TodoFilter.PENDING -> sortedTodos.filter { it.derivedStatus() != TodoStatus.DONE }
                            TodoFilter.DONE -> sortedTodos.filter { it.derivedStatus() == TodoStatus.DONE }
                        }
                        val counts = TodoCounts(
                            pending = sortedTodos.count { it.derivedStatus() == TodoStatus.PENDING },
                            overdue = sortedTodos.count { it.derivedStatus() == TodoStatus.OVERDUE },
                            done = sortedTodos.count { it.derivedStatus() == TodoStatus.DONE }
                        )
                        Triple(filter, sortedTodos, displayed to counts)
                    }
                }
                .collectLatest { (filter, todos, payload) ->
                    val (displayed, counts) = payload
                    updateState {
                        it.copy(
                            filter = filter,
                            todos = todos,
                            displayedTodos = displayed,
                            counts = counts,
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
            val normalizedTitle = title.normalizeFirstWordCapitalized()
            if (normalizedTitle.isBlank()) return@launch
            repository.insertTodo(
                TodoItem(
                    id = UUID.randomUUID().toString(),
                    trackerId = currentState.trackerId,
                    title = normalizedTitle,
                    note = note?.trim()?.ifBlank { null },
                    priority = priority,
                    dueDate = dueDate,
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

    private fun delete(id: String) {
        launch {
            repository.deleteTodo(id)
        }
    }

    private fun rename(id: String, title: String) {
        launch {
            val normalizedTitle = title.normalizeFirstWordCapitalized()
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

private fun String.normalizeFirstWordCapitalized(): String {
    val trimmed = trim()
    if (trimmed.isBlank()) return trimmed
    return buildString(trimmed.length) {
        var capitalizedFirstWord = false
        for (index in trimmed.indices) {
            val ch = trimmed[index]
            if (!capitalizedFirstWord && ch.isLetter()) {
                append(ch.titlecaseChar())
                capitalizedFirstWord = true
            } else {
                append(ch)
            }
        }
    }
}

private fun List<TodoItem>.sortedForUi(now: LocalDate = LocalDate.now()): List<TodoItem> {
    val overdue = filter { it.derivedStatus(now) == TodoStatus.OVERDUE }
        .sortedBy { it.dueDate ?: LocalDate.MAX }

    val pending = filter { it.derivedStatus(now) == TodoStatus.PENDING }
        .sortedWith(
            compareBy<TodoItem>({ it.priority.ordinal }, { it.dueDate ?: LocalDate.MAX })
        )

    val done = filter { it.derivedStatus(now) == TodoStatus.DONE }
        .sortedByDescending { it.completedAt ?: 0L }

    return overdue + pending + done
}
