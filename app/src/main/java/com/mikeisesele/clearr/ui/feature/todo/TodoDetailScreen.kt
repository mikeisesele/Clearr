package com.mikeisesele.clearr.ui.feature.todo

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
import com.mikeisesele.clearr.core.ai.TodoAiResult
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import com.mikeisesele.clearr.data.model.TodoStatus
import com.mikeisesele.clearr.data.model.derivedStatus
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TodoDetailScreen(
    trackerId: Long,
    onNavigateBack: () -> Unit,
    onAddTodo: () -> Unit,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalDuesColors.current
    var detailTodo by remember { mutableStateOf<TodoItem?>(null) }
    var renameTarget by remember { mutableStateOf<TodoItem?>(null) }
    var renameValue by remember { mutableStateOf("") }

    if (state.trackerId != trackerId) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TodoNavBar(onBack = onNavigateBack)

            TodoFilterTabs(
                selected = state.filter,
                overdueCount = state.counts.overdue,
                doneCount = state.counts.done,
                onSelect = { viewModel.onAction(TodoAction.SetFilter(it)) },
                colors = colors
            )
            state.aiInsight?.let { insight ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bg)
                        .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6)
                ) {
                    Text(
                        text = insight,
                        color = colors.muted,
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12
                    )
                }
            }

            if (!state.isLoading && state.displayedTodos.isEmpty()) {
                TodoEmptyState(filter = state.filter)
            } else {
                if (state.showSwipeHint) SwipeHintStrip(colors = colors)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(state.displayedTodos, key = { _, todo -> todo.id }) { index, todo ->
                        SwipeableTodoRow(
                            todo = todo,
                            isLast = index == state.displayedTodos.lastIndex,
                            colors = colors,
                            hintDeleteAnimation = index == 0 && state.showSwipeHint,
                            onDone = {
                                viewModel.onAction(TodoAction.MarkDone(it))
                                viewModel.onAction(TodoAction.OnFirstSwipeAction)
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
                .navigationBarsPadding()
                .padding(end = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20, bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24),
            onClick = onAddTodo
        )
    }

    detailTodo?.let { todo ->
        TodoDetailSheet(
            todo = todo,
            colors = colors,
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

    renameTarget?.let { todo ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { renameTarget = null },
            containerColor = colors.surface,
            title = { Text("Rename Todo", color = colors.text) },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Title") }
                )
            },
            confirmButton = {
                Button(
                    enabled = renameValue.isNotBlank(),
                    onClick = {
                        viewModel.onAction(TodoAction.Rename(todo.id, renameValue))
                        renameTarget = null
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel", color = colors.muted) }
            }
        )
    }
}

@Composable
private fun TodoNavBar(
    onBack: () -> Unit
) {
    ClearrTopBar(
        title = "Todos",
        leadingIcon = "←",
        onLeadingClick = onBack,
        actionIcon = null,
        onActionClick = null,
        leadingContainerColor = Color.Transparent
    )
}

@Composable
private fun TodoFilterTabs(
    selected: TodoFilter,
    overdueCount: Int,
    doneCount: Int,
    onSelect: (TodoFilter) -> Unit,
    colors: DuesColors
) {
    val tabs = listOf(
        TodoFilter.ALL to "All",
        TodoFilter.PENDING to if (overdueCount > 0) "Pending ($overdueCount late)" else "Pending",
        TodoFilter.DONE to "Done ($doneCount)"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
    ) {
        tabs.forEach { (filter, label) ->
            val selectedTab = selected == filter
            val textColor by animateColorAsState(
                targetValue = if (selectedTab) ClearrColors.Blue else colors.muted,
                label = "todo_tab_text"
            )
            val lineColor by animateColorAsState(
                targetValue = if (selectedTab) ClearrColors.Blue else Color.Transparent,
                label = "todo_tab_line"
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp42)
                    .clickable { onSelect(filter) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(top = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10),
                    color = textColor,
                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13,
                    fontWeight = if (selectedTab) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(modifier = Modifier.fillMaxWidth().height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2).background(lineColor))
            }
        }
    }
}

@Composable
private fun SwipeHintStrip(colors: DuesColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp28)
            .background(colors.bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Swipe left or right to mark done",
            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp11,
            color = colors.muted
        )
    }
}

@Composable
private fun SwipeableTodoRow(
    todo: TodoItem,
    isLast: Boolean,
    colors: DuesColors,
    hintDeleteAnimation: Boolean,
    onDone: (String) -> Unit,
    onTap: (TodoItem) -> Unit,
    onLongPress: (TodoItem) -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val maxSwipePx = with(density) { com.mikeisesele.clearr.ui.theme.ClearrDimens.dp120.toPx() }
    val thresholdPx = with(density) { com.mikeisesele.clearr.ui.theme.ClearrDimens.dp90.toPx() }
    val tapThresholdPx = with(density) { com.mikeisesele.clearr.ui.theme.ClearrDimens.dp5.toPx() }

    val offsetX = remember(todo.id) { Animatable(0f) }
    val hintOffset = remember(todo.id) { Animatable(0f) }
    var hintShown by rememberSaveable(todo.id) { mutableStateOf(false) }
    var dragMagnitudePx by remember { mutableStateOf(0f) }

    val derived = todo.derivedStatus()
    val isDone = derived == TodoStatus.DONE

    val totalOffset = offsetX.value + hintOffset.value
    val bgColor = if (kotlin.math.abs(totalOffset) > 12f) ClearrColors.Emerald else colors.border

    LaunchedEffect(hintDeleteAnimation) {
        if (hintDeleteAnimation && !hintShown) {
            hintShown = true
            hintOffset.animateTo(
                targetValue = -64f,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 280)
            )
            hintOffset.animateTo(
                targetValue = 0f,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 260)
            )
        }
    }

    Box(modifier = Modifier.fillMaxWidth().background(bgColor)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)
        ) {
            if (totalOffset > 12f) {
                Text(
                    text = "✓ Done",
                    color = ClearrColors.Surface,
                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp18,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
            if (totalOffset < -12f) {
                Text(
                    text = "✓ Done",
                    color = ClearrColors.Surface,
                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp18,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .alpha(if (isDone) 0.55f else 1f)
                .offset { IntOffset((offsetX.value + hintOffset.value).roundToInt(), 0) }
                .pointerInput(todo.id, isDone) {
                    detectTapGestures(
                        onTap = {
                            if (dragMagnitudePx < tapThresholdPx) onTap(todo)
                            dragMagnitudePx = 0f
                        },
                        onLongPress = {
                            if (dragMagnitudePx < tapThresholdPx) onLongPress(todo)
                            dragMagnitudePx = 0f
                        }
                    )
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
                                if (abs(offsetX.value) >= thresholdPx) {
                                    onDone(todo.id)
                                }
                                offsetX.animateTo(0f, spring())
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetX.animateTo(0f, spring()) }
                        }
                    )
                }
                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp13),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = if (isDone) 0.dp else com.mikeisesele.clearr.ui.theme.ClearrDimens.dp5)
                    .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10)
                    .background(priorityDotColor(todo, derived), CircleShape)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = if (isDone) Arrangement.Center else Arrangement.Top
            ) {
                Text(
                    text = todo.title,
                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15,
                    fontWeight = FontWeight.Medium,
                    color = if (isDone) colors.muted else colors.text,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = if (isDone) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!todo.note.isNullOrBlank()) {
                    Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp3))
                    Text(
                        text = todo.note,
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12,
                        color = colors.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(if (isDone) 0.dp else com.mikeisesele.clearr.ui.theme.ClearrDimens.dp3))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)) {
                    Text(
                        text = if (isDone) "Done" else dueLabel(todo.dueDate),
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp11,
                        fontWeight = FontWeight.SemiBold,
                        color = dueLabelColor(todo, derived, colors.muted)
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
                Surface(modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp22), shape = CircleShape, color = ClearrColors.EmeraldBg) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✓", color = ClearrColors.Emerald, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12)
                    }
                }
            }
        }

        if (!isLast) HorizontalDivider(color = colors.border, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun StatusPill(label: String, bg: Color, fg: Color) {
    Surface(shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20), color = bg) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1),
            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp10,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTodoScreen(
    trackerId: Long,
    onClose: () -> Unit,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalDuesColors.current
    if (state.trackerId != trackerId) return

    var title by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf(TodoPriority.MEDIUM) }
    var dueOption by rememberSaveable { mutableStateOf("Today") }
    var customDate by remember { mutableStateOf<LocalDate?>(null) }
    var showCustomDatePicker by remember { mutableStateOf(false) }
    val titleFocusRequester = remember { FocusRequester() }
    val canSubmit = title.trim().isNotEmpty()
    var aiLoading by remember { mutableStateOf(false) }
    var aiDraft by remember {
        mutableStateOf(
            TodoAiResult(
                normalizedTitle = ClearrEdgeAi.normalizeTitle(title),
                normalizedNote = note?.trim()?.ifBlank { null },
                suggestedPriority = priority,
                suggestedDueDate = dueDateFromOption(dueOption, customDate)
            )
        )
    }

    val options = listOf("Today", "Tomorrow", "This week", "Next week", "Custom", "No due date")
    val customLabel = customDate?.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))

    LaunchedEffect(Unit) {
        titleFocusRequester.requestFocus()
    }

    LaunchedEffect(title, note, priority, dueOption, customDate) {
        val selectedDueDate = dueDateFromOption(dueOption, customDate)
        if (title.isBlank()) {
            aiLoading = false
            aiDraft = TodoAiResult(
                normalizedTitle = ClearrEdgeAi.normalizeTitle(title),
                normalizedNote = note?.trim()?.ifBlank { null },
                suggestedPriority = priority,
                suggestedDueDate = selectedDueDate
            )
            return@LaunchedEffect
        }

        aiLoading = true
        delay(220)
        aiDraft = ClearrEdgeAi.inferTodoNanoAware(
            title = title,
            note = note,
            selectedPriority = priority,
            selectedDueDate = selectedDueDate
        )
        aiLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)
            .navigationBarsPadding()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp34)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.text
                )
            }
            Text("New Todo", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp16, fontWeight = FontWeight.SemiBold, color = colors.text)
            Spacer(modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp34))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
            StyledSheetInput(
                value = title,
                onValueChange = { title = it },
                placeholder = "What needs to be done?",
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
            StyledSheetInput(
                value = note,
                onValueChange = { note = it },
                placeholder = "Add a note (optional)",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16))
            if (title.isNotBlank()) {
                Text(
                    text = if (aiLoading) {
                        "AI: Thinking..."
                    } else {
                        "AI: ${aiDraft.suggestedPriority.name.lowercase().replaceFirstChar { it.uppercase() }} priority${
                            aiDraft.suggestedDueDate?.let {
                                " · due ${it.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))}"
                            } ?: ""
                        }"
                    },
                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12,
                    color = colors.muted
                )
                Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
            }
            Text("PRIORITY", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12, color = colors.muted, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
            Row(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8), modifier = Modifier.fillMaxWidth()) {
                listOf(TodoPriority.HIGH, TodoPriority.MEDIUM, TodoPriority.LOW).forEach { value ->
                    val selected = priority == value
                    val palette = when (value) {
                        TodoPriority.HIGH -> ClearrColors.CoralBg to ClearrColors.Coral
                        TodoPriority.MEDIUM -> ClearrColors.AmberBg to ClearrColors.Orange
                        TodoPriority.LOW -> ClearrColors.BlueBg to ClearrColors.Blue
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp38)
                            .clickable { priority = value },
                        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10),
                        color = if (selected) palette.first else colors.card
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = value.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = if (selected) palette.second else colors.muted,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16))
            Text("DUE DATE", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12, color = colors.muted, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6),
                verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6)
            ) {
                options.forEach { option ->
                    val selected = dueOption == option || (option == "Custom" && dueOption == "Custom" && customDate != null)
                    Surface(
                        modifier = Modifier.clickable {
                            dueOption = option
                            if (option == "Custom") {
                                showCustomDatePicker = true
                            }
                        },
                        color = if (selected) ClearrColors.Blue else colors.card,
                        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20)
                    ) {
                        Text(
                            text = if (option == "Custom" && customLabel != null && dueOption == "Custom") "Custom: $customLabel" else option,
                            modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp7),
                            color = if (selected) ClearrColors.Surface else colors.muted,
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24))
            Button(
                onClick = {
                    viewModel.onAction(
                        TodoAction.AddTodo(
                            title = title.trim(),
                            note = note.trim().ifBlank { null },
                            priority = priority,
                            dueDate = dueDateFromOption(dueOption, customDate)
                        )
                    )
                    onClose()
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClearrColors.Blue,
                    disabledContainerColor = colors.border
                ),
                contentPadding = PaddingValues(vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
            ) {
                Text("Add Todo", color = ClearrColors.Surface, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
        }
    }

    if (showCustomDatePicker) {
        CustomDatePickerSheet(
            initialDate = customDate ?: LocalDate.now().plusDays(1),
            onDismiss = { showCustomDatePicker = false },
            onDateSelected = { date ->
                customDate = date
                dueOption = "Custom"
                showCustomDatePicker = false
            }
        )
    }
}

@Composable
private fun StyledSheetInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val colors = LocalDuesColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
        color = colors.card,
        border = BorderStroke(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1, colors.border)
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14,
                vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp13
            )
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                keyboardOptions = keyboardOptions,
                cursorBrush = SolidColor(colors.muted),
                textStyle = TextStyle(
                    color = colors.text,
                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isBlank()) {
                        Text(placeholder, color = colors.muted, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15)
                    }
                    inner()
                }
            )
        }
    }
}

@Composable
private fun CustomDatePickerSheet(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val colors = LocalDuesColors.current
    val minSelectableDate = LocalDate.now().plusDays(1)
    val initial = if (initialDate.isBefore(minSelectableDate)) minSelectableDate else initialDate
    var displayedMonth by remember { mutableStateOf(YearMonth.from(initial)) }
    var selectedDate by remember { mutableStateOf(initial) }
    val firstDayOfMonth = displayedMonth.atDay(1)
    val leadingSpaces = firstDayOfMonth.dayOfWeek.value - 1
    val monthDays = displayedMonth.lengthOfMonth()
    val cells = buildList<LocalDate?> {
        repeat(leadingSpaces) { add(null) }
        (1..monthDays).forEach { day -> add(displayedMonth.atDay(day)) }
        while (size % 7 != 0) add(null)
    }.chunked(7)

    BackHandler(onBack = onDismiss)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20),
                color = colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) { Text("‹", color = ClearrColors.Blue, fontWeight = FontWeight.Bold) }
                        Text(
                            displayedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp16,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.text
                        )
                        TextButton(onClick = { displayedMonth = displayedMonth.plusMonths(1) }) { Text("›", color = ClearrColors.Blue, fontWeight = FontWeight.Bold) }
                    }

                    Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { label ->
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(label, color = colors.muted, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp11, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6))

                    cells.forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            week.forEach { date ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (date != null) {
                                        val selectable = !date.isBefore(minSelectableDate)
                                        val isSelected = date == selectedDate
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable(enabled = selectable) { selectedDate = date },
                                            shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10),
                                            color = when {
                                                isSelected -> ClearrColors.Blue
                                                selectable -> colors.card
                                                else -> colors.bg
                                            }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    date.dayOfMonth.toString(),
                                                    color = when {
                                                        isSelected -> ClearrColors.Surface
                                                        selectable -> colors.text
                                                        else -> colors.muted.copy(alpha = 0.6f)
                                                    },
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
                    Button(
                        onClick = { onDateSelected(selectedDate) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14),
                        colors = ButtonDefaults.buttonColors(containerColor = ClearrColors.Blue)
                    ) {
                        Text("Use Date", color = ClearrColors.Surface, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoDetailSheet(
    todo: TodoItem,
    colors: DuesColors,
    onDismiss: () -> Unit,
    onMarkDone: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val derived = todo.derivedStatus()
    val isDone = derived == TodoStatus.DONE

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)
                .navigationBarsPadding()
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss) { Text("Close", color = colors.muted) }
                Text("Detail", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp16, fontWeight = FontWeight.SemiBold, color = colors.text)
                TextButton(onClick = { onDelete(todo.id) }) { Text("Delete", color = ClearrColors.Coral, fontWeight = FontWeight.SemiBold) }
            }

            Text(
                text = todo.title,
                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp18,
                fontWeight = FontWeight.Bold,
                color = if (isDone) colors.muted else colors.text,
                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
            )

            if (!todo.note.isNullOrBlank()) {
                Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
                Surface(color = colors.card, shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = todo.note,
                        modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14,
                        color = colors.muted
                    )
                }
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16))
            Row(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)) {
                val priorityPalette = when (todo.priority) {
                    TodoPriority.HIGH -> ClearrColors.CoralBg to ClearrColors.Coral
                    TodoPriority.MEDIUM -> ClearrColors.AmberBg to ClearrColors.Orange
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
                    TodoStatus.PENDING -> colors.card to colors.muted
                }
                StatusPill(
                    label = if (derived == TodoStatus.DONE) "Done" else dueLabel(todo.dueDate),
                    bg = statusPalette.first,
                    fg = statusPalette.second
                )
            }

            if (!isDone) {
                Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24))
                Button(
                    onClick = { onMarkDone(todo.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ClearrColors.Emerald),
                    shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14)
                ) {
                    Text("Mark as Done ✓", color = ClearrColors.Surface, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
        }
    }
}

@Composable
private fun TodoEmptyState(filter: TodoFilter) {
    val colors = LocalDuesColors.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✓", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp40)
        Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
        Text(
            text = if (filter == TodoFilter.DONE) "Nothing done yet" else "All clear!",
            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp16,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )
        Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4))
        Text(
            text = if (filter == TodoFilter.DONE) "Complete a task to see it here" else "No pending tasks",
            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13,
            color = colors.muted
        )
    }
}

@Composable
private fun TodoFab(modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp52),
        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
        color = ClearrColors.Blue,
        shadowElevation = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = ClearrColors.Surface, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp24)
        }
    }
}

private fun priorityDotColor(todo: TodoItem, derived: TodoStatus): Color {
    if (derived == TodoStatus.DONE) return ClearrColors.Emerald
    return when (todo.priority) {
        TodoPriority.HIGH -> ClearrColors.Coral
        TodoPriority.MEDIUM -> ClearrColors.Orange
        TodoPriority.LOW -> ClearrColors.Blue
    }
}

private fun dueLabelColor(todo: TodoItem, derived: TodoStatus, mutedColor: Color): Color = when {
    derived == TodoStatus.DONE -> mutedColor
    derived == TodoStatus.OVERDUE -> ClearrColors.Coral
    todo.dueDate == LocalDate.now() -> ClearrColors.Orange
    else -> mutedColor
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

private fun dueDateFromOption(option: String, customDate: LocalDate? = null): LocalDate? {
    val today = LocalDate.now()
    return when (option) {
        "Today" -> today
        "Tomorrow" -> today.plusDays(1)
        "This week" -> {
            val saturday = today.with(DayOfWeek.SATURDAY)
            if (saturday.isBefore(today)) saturday.plusWeeks(1) else saturday
        }
        "Next week" -> today.plusWeeks(1).with(DayOfWeek.MONDAY)
        "Custom" -> customDate ?: today.plusDays(1)
        "No due date" -> null
        else -> today
    }
}
