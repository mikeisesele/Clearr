package com.mikeisesele.clearr.ui.feature.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.ui.feature.todo.components.SwipeableTodoRow
import com.mikeisesele.clearr.ui.feature.todo.components.TodoActionsDropdown
import com.mikeisesele.clearr.ui.feature.todo.components.TodoDetailSheet
import com.mikeisesele.clearr.ui.feature.todo.components.TodoEmptyState
import com.mikeisesele.clearr.ui.feature.todo.components.TodoFab
import com.mikeisesele.clearr.ui.feature.todo.components.TodoFilterTabs
import com.mikeisesele.clearr.ui.feature.todo.components.TodoNavBar
import com.mikeisesele.clearr.ui.feature.todo.components.TodoSwipeHintStrip
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors

@Composable
fun TodoDetailScreen(
    state: TodoUiState,
    onAction: (TodoAction) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    onAddTodo: () -> Unit
) {
    val colors = LocalClearrUiColors.current
    var detailTodo by remember { mutableStateOf<TodoItem?>(null) }
    var renameTarget by remember { mutableStateOf<TodoItem?>(null) }
    var renameValue by remember { mutableStateOf("") }
    var showActionsMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box {
                TodoNavBar(
                    onBack = onNavigateBack,
                    actionIcon = "⋮",
                    onActionClick = { showActionsMenu = true }
                )
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                    TodoActionsDropdown(
                        expanded = showActionsMenu,
                        onDismiss = { showActionsMenu = false },
                        onMarkAllDone = { onAction(TodoAction.MarkAllDone) },
                        onClearCompleted = { onAction(TodoAction.ClearCompleted) }
                    )
                }
            }
            TodoFilterTabs(
                selected = state.filter,
                overdueCount = state.counts.overdue,
                doneCount = state.counts.done,
                onSelect = { onAction(TodoAction.SetFilter(it)) },
                colors = colors
            )

            if (!state.isLoading && state.displayedTodos.isEmpty()) {
                TodoEmptyState(filter = state.filter)
            } else {
                if (state.showSwipeHint) TodoSwipeHintStrip(colors = colors)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(state.displayedTodos, key = { _, todo -> todo.id }) { index, todo ->
                        SwipeableTodoRow(
                            todo = todo,
                            isLast = index == state.displayedTodos.lastIndex,
                            colors = colors,
                            hintDeleteAnimation = index == 0 && state.showSwipeHint,
                            onDone = {
                                onAction(TodoAction.MarkDone(it))
                                onAction(TodoAction.OnFirstSwipeAction)
                            },
                            onTap = { detailTodo = it },
                            onLongPress = {
                                renameTarget = it
                                renameValue = it.title
                            }
                        )
                    }
                }
            }
        }

        TodoFab(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = ClearrDimens.dp20, bottom = ClearrDimens.dp20),
            onClick = onAddTodo
        )
    }

    detailTodo?.let { todo ->
        TodoDetailSheet(
            todo = todo,
            colors = colors,
            onDismiss = { detailTodo = null },
            onMarkDone = {
                onAction(TodoAction.MarkDone(it))
                detailTodo = null
            },
            onDelete = {
                onAction(TodoAction.Delete(it))
                detailTodo = null
            }
        )
    }

    renameTarget?.let { todo ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            containerColor = colors.surface,
            title = { Text("Rename Todo", color = colors.text) },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxSize(),
                    label = { Text("Title") }
                )
            },
            confirmButton = {
                Button(
                    enabled = renameValue.isNotBlank(),
                    onClick = {
                        onAction(TodoAction.Rename(todo.id, renameValue))
                        renameTarget = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) {
                    Text("Cancel", color = colors.muted)
                }
            }
        )
    }
}
