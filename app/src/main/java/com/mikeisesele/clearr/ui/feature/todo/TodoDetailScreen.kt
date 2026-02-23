package com.mikeisesele.clearr.ui.feature.todo

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import com.mikeisesele.clearr.data.model.TodoStatus
import com.mikeisesele.clearr.data.model.derivedStatus
import com.mikeisesele.clearr.ui.theme.ClearrColors
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TodoDetailScreen(
    trackerId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    var detailTodo by remember { mutableStateOf<TodoItem?>(null) }

    if (state.trackerId != trackerId) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClearrColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TodoNavBar(
                pendingCount = state.counts.pending + state.counts.overdue,
                doneCount = state.counts.done,
                onBack = onNavigateBack,
                onAdd = { showAddSheet = true }
            )

            TodoFilterTabs(
                selected = state.filter,
                overdueCount = state.counts.overdue,
                doneCount = state.counts.done,
                onSelect = { viewModel.onAction(TodoAction.SetFilter(it)) }
            )

            if (state.showSwipeHint) SwipeHintStrip()

            if (!state.isLoading && state.displayedTodos.isEmpty()) {
                TodoEmptyState(filter = state.filter)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(state.displayedTodos, key = { _, todo -> todo.id }) { index, todo ->
                        SwipeableTodoRow(
                            todo = todo,
                            isLast = index == state.displayedTodos.lastIndex,
                            onDone = {
                                viewModel.onAction(TodoAction.MarkDone(it))
                                viewModel.onAction(TodoAction.OnFirstSwipeAction)
                            },
                            onDelete = {
                                viewModel.onAction(TodoAction.Delete(it))
                                viewModel.onAction(TodoAction.OnFirstSwipeAction)
                            },
                            onTap = { detailTodo = it }
                        )
                    }
                }
            }
        }

        TodoFab(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp),
            onClick = { showAddSheet = true }
        )
    }

    if (showAddSheet) {
        AddTodoSheet(
            onDismiss = { showAddSheet = false },
            onConfirm = { title, note, priority, dueDate ->
                viewModel.onAction(TodoAction.AddTodo(title, note, priority, dueDate))
                showAddSheet = false
            }
        )
    }

    detailTodo?.let { todo ->
        TodoDetailSheet(
            todo = todo,
            onDismiss = { detailTodo = null },
            onMarkDone = {
                viewModel.onAction(TodoAction.MarkDone(it))
                detailTodo = null
            },
            onDelete = {
                viewModel.onAction(TodoAction.Delete(it))
                detailTodo = null
            }
        )
    }
}

@Composable
private fun TodoNavBar(
    pendingCount: Int,
    doneCount: Int,
    onBack: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ClearrColors.Surface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(34.dp)
                .clickable(onClick = onBack),
            shape = RoundedCornerShape(10.dp),
            color = ClearrColors.NavBg
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("←", fontSize = 15.sp, color = ClearrColors.TextPrimary)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("My Todos", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = ClearrColors.TextPrimary)
            Text("$pendingCount pending · $doneCount done", fontSize = 12.sp, color = ClearrColors.TextMuted)
        }

        Surface(
            modifier = Modifier
                .size(34.dp)
                .clickable(onClick = onAdd),
            shape = RoundedCornerShape(10.dp),
            color = ClearrColors.Blue
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("+", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
    HorizontalDivider(color = ClearrColors.Border)
}

@Composable
private fun TodoFilterTabs(
    selected: TodoFilter,
    overdueCount: Int,
    doneCount: Int,
    onSelect: (TodoFilter) -> Unit
) {
    val tabs = listOf(
        TodoFilter.ALL to "All",
        TodoFilter.PENDING to if (overdueCount > 0) "Pending ($overdueCount late)" else "Pending",
        TodoFilter.DONE to "Done ($doneCount)"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ClearrColors.Surface)
    ) {
        tabs.forEach { (filter, label) ->
            val selectedTab = selected == filter
            val textColor by animateColorAsState(
                targetValue = if (selectedTab) ClearrColors.Blue else ClearrColors.TextMuted,
                label = "todo_tab_text"
            )
            val lineColor by animateColorAsState(
                targetValue = if (selectedTab) ClearrColors.Blue else Color.Transparent,
                label = "todo_tab_line"
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clickable { onSelect(filter) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(top = 10.dp),
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = if (selectedTab) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(lineColor))
            }
        }
    }
}

@Composable
private fun SwipeHintStrip() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(ClearrColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "← Swipe right to mark done  ·  Swipe left to delete →",
            fontSize = 11.sp,
            color = ClearrColors.TextMuted
        )
    }
}

@Composable
private fun SwipeableTodoRow(
    todo: TodoItem,
    isLast: Boolean,
    onDone: (String) -> Unit,
    onDelete: (String) -> Unit,
    onTap: (TodoItem) -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val maxSwipePx = with(density) { 120.dp.toPx() }
    val thresholdPx = with(density) { 90.dp.toPx() }
    val tapThresholdPx = with(density) { 5.dp.toPx() }

    val offsetX = remember(todo.id) { Animatable(0f) }
    var rowWidthPx by remember { mutableStateOf(0f) }
    var dragMagnitudePx by remember { mutableStateOf(0f) }

    val derived = todo.derivedStatus()
    val isDone = derived == TodoStatus.DONE

    val bgColor = when {
        offsetX.value > 20f -> ClearrColors.Emerald
        offsetX.value < -20f -> ClearrColors.Coral
        else -> ClearrColors.Border
    }

    Box(modifier = Modifier.fillMaxWidth().background(bgColor)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (offsetX.value > 20f) "✓" else "", color = Color.White, fontSize = 20.sp)
            Text(if (offsetX.value < -20f) "🗑" else "", color = Color.White, fontSize = 18.sp)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ClearrColors.Surface)
                .alpha(if (isDone) 0.55f else 1f)
                .onSizeChanged { rowWidthPx = it.width.toFloat() }
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(todo.id, isDone) {
                    detectTapGestures(onTap = {
                        if (dragMagnitudePx < tapThresholdPx) onTap(todo)
                        dragMagnitudePx = 0f
                    })
                }
                .pointerInput(todo.id, isDone) {
                    if (isDone) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { dragMagnitudePx = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragMagnitudePx += abs(dragAmount)
                            scope.launch {
                                val next = (offsetX.value + dragAmount).coerceIn(-maxSwipePx, maxSwipePx)
                                offsetX.snapTo(next)
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offsetX.value >= thresholdPx -> {
                                        offsetX.animateTo(rowWidthPx.coerceAtLeast(maxSwipePx), spring())
                                        onDone(todo.id)
                                    }
                                    offsetX.value <= -thresholdPx -> {
                                        offsetX.animateTo(-rowWidthPx.coerceAtLeast(maxSwipePx), spring())
                                        onDelete(todo.id)
                                    }
                                    else -> offsetX.animateTo(0f, spring())
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetX.animateTo(0f, spring()) }
                        }
                    )
                }
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(10.dp)
                    .background(priorityDotColor(todo, derived), CircleShape)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDone) ClearrColors.TextMuted else ClearrColors.TextPrimary,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!todo.note.isNullOrBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = todo.note,
                        fontSize = 12.sp,
                        color = ClearrColors.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isDone) "Done" else dueLabel(todo.dueDate),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = dueLabelColor(todo, derived)
                    )

                    if (todo.priority == TodoPriority.HIGH && !isDone) {
                        StatusPill("High", ClearrColors.CoralBg, ClearrColors.Coral)
                    }
                    if (derived == TodoStatus.OVERDUE) {
                        StatusPill("Overdue", ClearrColors.CoralBg, ClearrColors.Coral)
                    }
                }
            }

            if (isDone) {
                Surface(modifier = Modifier.size(22.dp), shape = CircleShape, color = ClearrColors.EmeraldBg) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✓", color = ClearrColors.Emerald, fontSize = 12.sp)
                    }
                }
            }
        }

        if (!isLast) HorizontalDivider(color = ClearrColors.Border, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun StatusPill(label: String, bg: Color, fg: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AddTodoSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, TodoPriority, LocalDate?) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf(TodoPriority.MEDIUM) }
    var dueOption by rememberSaveable { mutableStateOf("Today") }

    val options = listOf("Today", "Tomorrow", "This week", "Next week", "No due date")

    BackHandler(onBack = onDismiss)
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ClearrColors.Surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss) { Text("Cancel", color = ClearrColors.TextSecondary) }
                Text("New Todo", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ClearrColors.TextPrimary)
                TextButton(
                    enabled = title.trim().isNotEmpty(),
                    onClick = {
                        onConfirm(
                            title.trim(),
                            note.trim().ifBlank { null },
                            priority,
                            dueDateFromOption(dueOption)
                        )
                    }
                ) {
                    Text("Add", color = if (title.trim().isNotEmpty()) ClearrColors.Blue else ClearrColors.TextMuted, fontWeight = FontWeight.SemiBold)
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What needs to be done?") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add a note (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(Modifier.height(16.dp))
            Text("PRIORITY", fontSize = 12.sp, color = ClearrColors.TextMuted, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(TodoPriority.HIGH, TodoPriority.MEDIUM, TodoPriority.LOW).forEach { value ->
                    val selected = priority == value
                    val palette = when (value) {
                        TodoPriority.HIGH -> ClearrColors.CoralBg to ClearrColors.Coral
                        TodoPriority.MEDIUM -> ClearrColors.AmberBg to Color(0xFFFF9500)
                        TodoPriority.LOW -> ClearrColors.BlueBg to ClearrColors.Blue
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clickable { priority = value },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) palette.first else ClearrColors.Background
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = value.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = if (selected) palette.second else ClearrColors.TextMuted,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("DUE DATE", fontSize = 12.sp, color = ClearrColors.TextMuted, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                options.forEach { option ->
                    val selected = dueOption == option
                    Surface(
                        modifier = Modifier.clickable { dueOption = option },
                        color = if (selected) ClearrColors.Blue else ClearrColors.Background,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            color = if (selected) Color.White else ClearrColors.TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoDetailSheet(
    todo: TodoItem,
    onDismiss: () -> Unit,
    onMarkDone: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val derived = todo.derivedStatus()
    val isDone = derived == TodoStatus.DONE

    BackHandler(onBack = onDismiss)
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ClearrColors.Surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss) { Text("Close", color = ClearrColors.TextSecondary) }
                Text("Detail", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ClearrColors.TextPrimary)
                TextButton(onClick = { onDelete(todo.id) }) { Text("Delete", color = ClearrColors.Coral, fontWeight = FontWeight.SemiBold) }
            }

            Text(
                text = todo.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDone) ClearrColors.TextMuted else ClearrColors.TextPrimary,
                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
            )

            if (!todo.note.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Surface(color = ClearrColors.Background, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = todo.note,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        fontSize = 14.sp,
                        color = ClearrColors.TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val priorityPalette = when (todo.priority) {
                    TodoPriority.HIGH -> ClearrColors.CoralBg to ClearrColors.Coral
                    TodoPriority.MEDIUM -> ClearrColors.AmberBg to Color(0xFFFF9500)
                    TodoPriority.LOW -> ClearrColors.BlueBg to ClearrColors.Blue
                }
                StatusPill(
                    label = "${todo.priority.name.lowercase().replaceFirstChar { it.uppercase() }} Priority",
                    bg = priorityPalette.first,
                    fg = priorityPalette.second
                )

                val statusPalette = when (derived) {
                    TodoStatus.DONE -> ClearrColors.EmeraldBg to ClearrColors.Emerald
                    TodoStatus.OVERDUE -> ClearrColors.CoralBg to ClearrColors.Coral
                    TodoStatus.PENDING -> ClearrColors.Background to ClearrColors.TextSecondary
                }
                StatusPill(
                    label = if (derived == TodoStatus.DONE) "Done" else dueLabel(todo.dueDate),
                    bg = statusPalette.first,
                    fg = statusPalette.second
                )
            }

            if (!isDone) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { onMarkDone(todo.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ClearrColors.Emerald),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Mark as Done ✓", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TodoEmptyState(filter: TodoFilter) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✓", fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (filter == TodoFilter.DONE) "Nothing done yet" else "All clear!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = ClearrColors.TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (filter == TodoFilter.DONE) "Complete a task to see it here" else "No pending tasks",
            fontSize = 13.sp,
            color = ClearrColors.TextMuted
        )
    }
}

@Composable
private fun TodoFab(modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.size(52.dp),
        shape = RoundedCornerShape(16.dp),
        color = ClearrColors.Blue,
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = Color.White, fontSize = 24.sp)
        }
    }
}

private fun priorityDotColor(todo: TodoItem, derived: TodoStatus): Color {
    if (derived == TodoStatus.DONE) return ClearrColors.Emerald
    return when (todo.priority) {
        TodoPriority.HIGH -> ClearrColors.Coral
        TodoPriority.MEDIUM -> Color(0xFFFF9500)
        TodoPriority.LOW -> ClearrColors.Blue
    }
}

private fun dueLabelColor(todo: TodoItem, derived: TodoStatus): Color = when {
    derived == TodoStatus.DONE -> ClearrColors.TextMuted
    derived == TodoStatus.OVERDUE -> ClearrColors.Coral
    todo.dueDate == LocalDate.now() -> Color(0xFFFF9500)
    else -> ClearrColors.TextMuted
}

private fun dueLabel(dueDate: LocalDate?): String {
    if (dueDate == null) return "No due date"
    val today = LocalDate.now()
    return when (dueDate) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        else -> dueDate.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))
    }
}

private fun dueDateFromOption(option: String): LocalDate? {
    val today = LocalDate.now()
    return when (option) {
        "Today" -> today
        "Tomorrow" -> today.plusDays(1)
        "This week" -> {
            val saturday = today.with(DayOfWeek.SATURDAY)
            if (saturday.isBefore(today)) saturday.plusWeeks(1) else saturday
        }
        "Next week" -> today.plusWeeks(1).with(DayOfWeek.MONDAY)
        "No due date" -> null
        else -> today
    }
}
