package com.mikeisesele.clearr.ui.feature.todo.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoPriority
import com.mikeisesele.clearr.data.model.TodoStatus
import com.mikeisesele.clearr.data.model.derivedStatus
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
import com.mikeisesele.clearr.ui.feature.todo.TodoFilter
import com.mikeisesele.clearr.ui.feature.todo.previews.previewTodoItem
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import com.mikeisesele.clearr.ui.feature.todo.utils.dueLabel
import com.mikeisesele.clearr.ui.feature.todo.utils.dueLabelColor
import com.mikeisesele.clearr.ui.feature.todo.utils.priorityDotColor
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
internal fun TodoNavBar(
    onBack: (() -> Unit)? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    ClearrTopBar(
        title = "Todos",
        showLeading = onBack != null,
        leadingIcon = "←",
        onLeadingClick = onBack,
        actionText = actionText,
        onActionClick = onActionClick,
        leadingContainerColor = Color.Transparent
    )
}

@Composable
internal fun TodoFilterTabs(
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

    Row(modifier = Modifier.fillMaxWidth().background(colors.surface)) {
        tabs.forEach { (filter, label) ->
            val selectedTab = selected == filter
            val textColor by animateColorAsState(targetValue = if (selectedTab) ClearrColors.Blue else colors.muted, label = "todo_tab_text")
            val lineColor by animateColorAsState(targetValue = if (selectedTab) ClearrColors.Blue else Color.Transparent, label = "todo_tab_line")
            Column(
                modifier = Modifier.weight(1f).height(ClearrDimens.dp42).clickable { onSelect(filter) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, modifier = Modifier.padding(top = ClearrDimens.dp10), color = textColor, fontSize = ClearrTextSizes.sp13, fontWeight = if (selectedTab) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box(modifier = Modifier.fillMaxWidth().height(ClearrDimens.dp2).background(lineColor))
            }
        }
    }
}

@Composable
internal fun TodoSwipeHintStrip(colors: DuesColors) {
    Box(modifier = Modifier.fillMaxWidth().height(ClearrDimens.dp28).background(colors.bg), contentAlignment = Alignment.Center) {
        Text("Swipe left or right to mark done", fontSize = ClearrTextSizes.sp11, color = colors.muted)
    }
}

@Composable
internal fun SwipeableTodoRow(
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
    val maxSwipePx = with(density) { ClearrDimens.dp120.toPx() }
    val thresholdPx = with(density) { ClearrDimens.dp90.toPx() }
    val tapThresholdPx = with(density) { ClearrDimens.dp5.toPx() }

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
            hintOffset.animateTo(-64f, animationSpec = androidx.compose.animation.core.tween(durationMillis = 280))
            hintOffset.animateTo(0f, animationSpec = androidx.compose.animation.core.tween(durationMillis = 260))
        }
    }

    Box(modifier = Modifier.fillMaxWidth().background(bgColor)) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = ClearrDimens.dp20, vertical = ClearrDimens.dp12)) {
            if (totalOffset > 12f) {
                Text("✓ Done", color = ClearrColors.Surface, fontSize = ClearrTextSizes.sp18, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterStart))
            }
            if (totalOffset < -12f) {
                Text("✓ Done", color = ClearrColors.Surface, fontSize = ClearrTextSizes.sp18, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterEnd))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(colors.surface).alpha(if (isDone) 0.55f else 1f)
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
                            scope.launch { offsetX.snapTo((offsetX.value + dragAmount).coerceIn(-maxSwipePx, maxSwipePx)) }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (abs(offsetX.value) >= thresholdPx) onDone(todo.id)
                                offsetX.animateTo(0f, spring())
                            }
                        },
                        onDragCancel = { scope.launch { offsetX.animateTo(0f, spring()) } }
                    )
                }
                .padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp13),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp12)
        ) {
            Box(modifier = Modifier.padding(top = if (isDone) 0.dp else ClearrDimens.dp5).size(ClearrDimens.dp10).background(priorityDotColor(todo, derived), CircleShape))
            Column(modifier = Modifier.weight(1f), verticalArrangement = if (isDone) Arrangement.Center else Arrangement.Top) {
                Text(todo.title, fontSize = ClearrTextSizes.sp15, fontWeight = FontWeight.Medium, color = if (isDone) colors.muted else colors.text, textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None, maxLines = if (isDone) 1 else 2, overflow = TextOverflow.Ellipsis)
                if (!todo.note.isNullOrBlank()) {
                    Spacer(Modifier.height(ClearrDimens.dp3))
                    Text(todo.note, fontSize = ClearrTextSizes.sp12, color = colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(if (isDone) 0.dp else ClearrDimens.dp3))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)) {
                    Text(if (isDone) "Done" else dueLabel(todo.dueDate), fontSize = ClearrTextSizes.sp11, fontWeight = FontWeight.SemiBold, color = dueLabelColor(todo, derived, colors.muted))
                    if (todo.priority == TodoPriority.HIGH && !isDone) StatusPill("High", ClearrColors.CoralBg, ClearrColors.Coral)
                    if (derived == TodoStatus.OVERDUE) StatusPill("Overdue", ClearrColors.CoralBg, ClearrColors.Coral)
                }
            }
            if (isDone) {
                Surface(modifier = Modifier.size(ClearrDimens.dp22), shape = CircleShape, color = ClearrColors.EmeraldBg) {
                    Box(contentAlignment = Alignment.Center) { Text("✓", color = ClearrColors.Emerald, fontSize = ClearrTextSizes.sp12) }
                }
            }
        }

        if (!isLast) HorizontalDivider(color = colors.border, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
internal fun StatusPill(label: String, bg: Color, fg: Color) {
    Surface(shape = RoundedCornerShape(ClearrDimens.dp20), color = bg) {
        Text(label, modifier = Modifier.padding(horizontal = ClearrDimens.dp6, vertical = ClearrDimens.dp1), fontSize = ClearrTextSizes.sp10, fontWeight = FontWeight.Bold, color = fg)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TodoDetailSheet(
    todo: TodoItem,
    colors: DuesColors,
    onDismiss: () -> Unit,
    onMarkDone: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val derived = todo.derivedStatus()
    val isDone = derived == TodoStatus.DONE

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = ClearrDimens.dp20, vertical = ClearrDimens.dp8).navigationBarsPadding()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss) { Text("Close", color = colors.muted) }
                Text("Detail", fontSize = ClearrTextSizes.sp16, fontWeight = FontWeight.SemiBold, color = colors.text)
                TextButton(onClick = { onDelete(todo.id) }) { Text("Delete", color = ClearrColors.Coral, fontWeight = FontWeight.SemiBold) }
            }

            Text(todo.title, fontSize = ClearrTextSizes.sp18, fontWeight = FontWeight.Bold, color = if (isDone) colors.muted else colors.text, textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None)
            if (!todo.note.isNullOrBlank()) {
                Spacer(Modifier.height(ClearrDimens.dp12))
                Surface(color = colors.card, shape = RoundedCornerShape(ClearrDimens.dp10), modifier = Modifier.fillMaxWidth()) {
                    Text(todo.note, modifier = Modifier.padding(horizontal = ClearrDimens.dp14, vertical = ClearrDimens.dp12), fontSize = ClearrTextSizes.sp14, color = colors.muted)
                }
            }
            Spacer(Modifier.height(ClearrDimens.dp16))
            Row(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)) {
                val priorityPalette = when (todo.priority) {
                    TodoPriority.HIGH -> ClearrColors.CoralBg to ClearrColors.Coral
                    TodoPriority.MEDIUM -> ClearrColors.AmberBg to ClearrColors.Orange
                    TodoPriority.LOW -> ClearrColors.BlueBg to ClearrColors.Blue
                }
                StatusPill("${todo.priority.name.lowercase().replaceFirstChar { it.uppercase() }} Priority", priorityPalette.first, priorityPalette.second)
                val statusPalette = when (derived) {
                    TodoStatus.DONE -> ClearrColors.EmeraldBg to ClearrColors.Emerald
                    TodoStatus.OVERDUE -> ClearrColors.CoralBg to ClearrColors.Coral
                    TodoStatus.PENDING -> colors.card to colors.muted
                }
                StatusPill(if (derived == TodoStatus.DONE) "Done" else dueLabel(todo.dueDate), statusPalette.first, statusPalette.second)
            }
            if (!isDone) {
                Spacer(Modifier.height(ClearrDimens.dp24))
                Button(onClick = { onMarkDone(todo.id) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ClearrColors.Emerald), shape = RoundedCornerShape(ClearrDimens.dp14)) {
                    Text("Mark as Done ✓", color = ClearrColors.Surface, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(ClearrDimens.dp12))
        }
    }
}

@Composable
internal fun TodoEmptyState(filter: TodoFilter) {
    val colors = LocalDuesColors.current
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("✓", fontSize = ClearrTextSizes.sp40)
        Spacer(Modifier.height(ClearrDimens.dp12))
        Text(if (filter == TodoFilter.DONE) "Nothing done yet" else "All clear!", fontSize = ClearrTextSizes.sp16, fontWeight = FontWeight.Bold, color = colors.text)
        Spacer(Modifier.height(ClearrDimens.dp4))
        Text(if (filter == TodoFilter.DONE) "Complete a task to see it here" else "No pending tasks", fontSize = ClearrTextSizes.sp13, color = colors.muted)
    }
}

@Composable
internal fun TodoFab(modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier.size(ClearrDimens.dp52), shape = RoundedCornerShape(ClearrDimens.dp16), color = ClearrColors.Blue, shadowElevation = ClearrDimens.dp10) {
        Box(modifier = Modifier.fillMaxSize().clickable(onClick = onClick), contentAlignment = Alignment.Center) {
            Text("+", color = ClearrColors.Surface, fontSize = ClearrTextSizes.sp24)
        }
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun TodoRowPreview() {
    ClearrTheme {
        SwipeableTodoRow(
            todo = previewTodoItem,
            isLast = true,
            colors = LocalDuesColors.current,
            hintDeleteAnimation = false,
            onDone = {},
            onTap = {},
            onLongPress = {}
        )
    }
}
