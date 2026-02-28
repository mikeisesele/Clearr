package com.mikeisesele.clearr.ui.feature.todo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
import com.mikeisesele.clearr.core.ai.TodoAiResult
import com.mikeisesele.clearr.data.model.TodoPriority
import com.mikeisesele.clearr.ui.feature.todo.components.CustomDatePickerDialog
import com.mikeisesele.clearr.ui.feature.todo.components.TodoSheetInput
import com.mikeisesele.clearr.ui.feature.todo.utils.dueDateFromOption
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
                normalizedNote = note.trim().ifBlank { null },
                suggestedPriority = priority,
                suggestedDueDate = dueDateFromOption(dueOption, customDate)
            )
        )
    }

    val options = listOf("Today", "Tomorrow", "This week", "Next week", "Custom", "No due date")
    val customLabel = customDate?.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))

    LaunchedEffect(Unit) { titleFocusRequester.requestFocus() }
    LaunchedEffect(title, note, priority, dueOption, customDate) {
        val selectedDueDate = dueDateFromOption(dueOption, customDate)
        if (title.isBlank()) {
            aiLoading = false
            aiDraft = TodoAiResult(ClearrEdgeAi.normalizeTitle(title), note.trim().ifBlank { null }, priority, selectedDueDate)
            return@LaunchedEffect
        }

        aiLoading = true
        delay(220)
        aiDraft = ClearrEdgeAi.inferTodoNanoAware(title = title, note = note, selectedPriority = priority, selectedDueDate = selectedDueDate)
        aiLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(colors.bg).statusBarsPadding().padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp8).navigationBarsPadding()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(ClearrDimens.dp34).clickable { onClose() }, contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.text)
            }
            Text("New Todo", fontSize = ClearrTextSizes.sp16, fontWeight = FontWeight.SemiBold, color = colors.text)
            Spacer(modifier = Modifier.size(ClearrDimens.dp34))
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(ClearrDimens.dp12))
            TodoSheetInput(
                value = title,
                onValueChange = { title = it },
                placeholder = "What needs to be done?",
                singleLine = true,
                modifier = Modifier.fillMaxWidth().focusRequester(titleFocusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.Sentences)
            )
            Spacer(Modifier.height(ClearrDimens.dp12))
            TodoSheetInput(
                value = note,
                onValueChange = { note = it },
                placeholder = "Add a note (optional)",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(Modifier.height(ClearrDimens.dp16))
            Text("PRIORITY", fontSize = ClearrTextSizes.sp12, color = colors.muted, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(ClearrDimens.dp8))
            Row(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8), modifier = Modifier.fillMaxWidth()) {
                listOf(TodoPriority.HIGH, TodoPriority.MEDIUM, TodoPriority.LOW).forEach { value ->
                    val selected = priority == value
                    val palette = when (value) {
                        TodoPriority.HIGH -> ClearrColors.CoralBg to ClearrColors.Coral
                        TodoPriority.MEDIUM -> ClearrColors.AmberBg to ClearrColors.Orange
                        TodoPriority.LOW -> ClearrColors.BlueBg to ClearrColors.Blue
                    }
                    Surface(modifier = Modifier.weight(1f).height(ClearrDimens.dp38).clickable { priority = value }, shape = RoundedCornerShape(ClearrDimens.dp10), color = if (selected) palette.first else colors.card) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(value.name.lowercase().replaceFirstChar { it.uppercase() }, color = if (selected) palette.second else colors.muted, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = ClearrTextSizes.sp13)
                        }
                    }
                }
            }

            Spacer(Modifier.height(ClearrDimens.dp16))
            Text("DUE DATE", fontSize = ClearrTextSizes.sp12, color = colors.muted, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(ClearrDimens.dp8))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp6), verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp6)) {
                options.forEach { option ->
                    val selected = dueOption == option || (option == "Custom" && dueOption == "Custom" && customDate != null)
                    Surface(modifier = Modifier.clickable { dueOption = option; if (option == "Custom") showCustomDatePicker = true }, color = if (selected) ClearrColors.Blue else colors.card, shape = RoundedCornerShape(ClearrDimens.dp20)) {
                        Text(
                            text = if (option == "Custom" && customLabel != null && dueOption == "Custom") "Custom: $customLabel" else option,
                            modifier = Modifier.padding(horizontal = ClearrDimens.dp12, vertical = ClearrDimens.dp7),
                            color = if (selected) ClearrColors.Surface else colors.muted,
                            fontSize = ClearrTextSizes.sp12,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(Modifier.height(ClearrDimens.dp24))
            Button(
                onClick = {
                    viewModel.onAction(TodoAction.AddTodo(title.trim(), note.trim().ifBlank { null }, priority, dueDateFromOption(dueOption, customDate)))
                    onClose()
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ClearrDimens.dp14),
                colors = ButtonDefaults.buttonColors(containerColor = ClearrColors.Blue, disabledContainerColor = colors.border),
                contentPadding = PaddingValues(vertical = ClearrDimens.dp16)
            ) {
                Text("Add Todo", color = ClearrColors.Surface, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(ClearrDimens.dp12))
        }
    }

    if (showCustomDatePicker) {
        CustomDatePickerDialog(
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
