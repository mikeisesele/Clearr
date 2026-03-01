package com.mikeisesele.clearr.ui.feature.todo

import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import com.mikeisesele.clearr.data.model.TodoStatus
import com.mikeisesele.clearr.core.time.MaxLocalDate
import com.mikeisesele.clearr.core.time.randomId
import com.mikeisesele.clearr.core.time.todayLocalDate
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import com.mikeisesele.clearr.domain.repository.TodoPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TodoStore(
    private val trackerId: Long,
    private val repository: ClearrRepository,
    private val todoPreferencesRepository: TodoPreferencesRepository,
    private val todoAiService: TodoAiService,
    private val scope: CoroutineScope,
    private val nowMillis: () -> Long
) {
    private val mutableState = MutableStateFlow(TodoUiState(trackerId = trackerId))
    val uiState: StateFlow<TodoUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<TodoEvent>(capacity = Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private val filterFlow = MutableStateFlow(TodoFilter.ALL)

    init {
        observeTracker()
        observeTodos()
        observeHintPreference()
    }

    fun onAction(action: TodoAction) {
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
        scope.launch {
            repository.getTrackerByIdFlow(trackerId).collect { tracker ->
                if (tracker == null) {
                    mutableState.update { it.copy(isLoading = false) }
                    return@collect
                }
                mutableState.update { it.copy(trackerName = tracker.name) }
            }
        }
    }

    private fun observeTodos() {
        scope.launch {
            filterFlow
                .flatMapLatest { filter ->
                    repository.getTodosForTracker(trackerId).map { todos ->
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
                .collect { (filter, todos, payload) ->
                    val (displayed, counts) = payload
                    val insight = todoAiService.todoInsight(todos)
                    mutableState.update {
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
        scope.launch {
            todoPreferencesRepository.swipeHintSeen.collect { seen ->
                mutableState.update { it.copy(showSwipeHint = !seen) }
            }
        }
    }

    private fun setFilter(filter: TodoFilter) {
        filterFlow.value = filter
        mutableState.update { it.copy(filter = filter) }
    }

    private fun addTodo(title: String, note: String?, priority: TodoPriority, dueDate: LocalDate?) {
        scope.launch {
            val ai = todoAiService.inferTodo(
                title = title,
                note = note,
                selectedPriority = priority,
                selectedDueDate = dueDate
            )
            if (ai.normalizedTitle.isBlank()) return@launch
            repository.insertTodo(
                TodoItem(
                    id = randomId(),
                    trackerId = trackerId,
                    title = ai.normalizedTitle,
                    note = ai.normalizedNote,
                    priority = ai.suggestedPriority,
                    dueDate = ai.suggestedDueDate,
                    status = TodoStatus.PENDING,
                    createdAt = nowMillis(),
                    completedAt = null
                )
            )
        }
    }

    private fun markDone(id: String) {
        scope.launch {
            repository.markTodoDone(id, nowMillis())
        }
    }

    private fun markAllDone() {
        scope.launch {
            uiState.value.todos
                .filter { todo -> todo.uiDerivedStatus() != TodoStatus.DONE }
                .forEach { todo ->
                    repository.markTodoDone(todo.id, nowMillis())
                }
        }
    }

    private fun delete(id: String) {
        scope.launch {
            repository.deleteTodo(id)
        }
    }

    private fun clearCompleted() {
        scope.launch {
            uiState.value.todos
                .filter { todo -> todo.uiDerivedStatus() == TodoStatus.DONE }
                .forEach { todo ->
                    repository.deleteTodo(todo.id)
                }
        }
    }

    private fun rename(id: String, title: String) {
        scope.launch {
            val normalizedTitle = todoAiService.normalizeTitle(title)
            if (normalizedTitle.isBlank()) return@launch
            val existing = repository.getTodoById(id) ?: return@launch
            repository.updateTodo(existing.copy(title = normalizedTitle))
        }
    }

    private fun markHintSeen() {
        scope.launch {
            todoPreferencesRepository.markSwipeHintSeen()
        }
    }
}

private fun List<TodoItem>.sortedForUi(now: LocalDate = todayLocalDate()): List<TodoItem> {
    val overdue = filter { it.uiDerivedStatus(now) == TodoStatus.OVERDUE }
        .sortedBy { it.dueDate ?: MaxLocalDate }

    val pending = filter { it.uiDerivedStatus(now) == TodoStatus.PENDING }
        .sortedWith(compareBy<TodoItem>({ it.priority.ordinal }, { it.dueDate ?: MaxLocalDate }))

    val done = filter { it.uiDerivedStatus(now) == TodoStatus.DONE }
        .sortedByDescending { it.completedAt ?: 0L }

    return overdue + pending + done
}

private fun TodoItem.uiDerivedStatus(today: LocalDate = todayLocalDate()): TodoStatus = when {
    status == TodoStatus.DONE -> TodoStatus.DONE
    dueDate?.let { it < today } == true -> TodoStatus.OVERDUE
    else -> TodoStatus.PENDING
}
