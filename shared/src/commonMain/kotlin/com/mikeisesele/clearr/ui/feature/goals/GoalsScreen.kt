package com.mikeisesele.clearr.ui.feature.goals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.mikeisesele.clearr.data.model.GoalSummary
import com.mikeisesele.clearr.ui.feature.goals.components.AllClearedBanner
import com.mikeisesele.clearr.ui.feature.goals.components.GoalDetailSheet
import com.mikeisesele.clearr.ui.feature.goals.components.GoalsEmptyState
import com.mikeisesele.clearr.ui.feature.goals.components.GoalsNavBar
import com.mikeisesele.clearr.ui.feature.goals.components.GoalsSwipeHintStrip
import com.mikeisesele.clearr.ui.feature.goals.components.SwipeableGoalRow
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.ClearrUiColors

@Composable
fun GoalsScreen(
    state: GoalsUiState,
    colors: ClearrUiColors,
    onAction: (GoalsAction) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    onAddGoal: () -> Unit
) {
    var detailGoal by remember { mutableStateOf<GoalSummary?>(null) }
    var renameTarget by remember { mutableStateOf<GoalSummary?>(null) }
    var renameValue by remember { mutableStateOf("") }
    var playDeleteHint by rememberSaveable { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            GoalsNavBar(title = state.trackerName, onBack = onNavigateBack)

            AnimatedVisibility(visible = state.allDoneThisPeriod, enter = fadeIn() + expandVertically(), exit = fadeOut()) {
                AllClearedBanner(colors = colors)
            }

            if (!state.isLoading && state.summaries.isEmpty()) {
                GoalsEmptyState(modifier = Modifier.weight(1f))
            } else {
                GoalsSwipeHintStrip(colors = colors)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(state.summaries, key = { _, summary -> summary.goal.id }) { index, summary ->
                        SwipeableGoalRow(
                            summary = summary,
                            isLast = index == state.summaries.lastIndex,
                            colors = colors,
                            hintDeleteAnimation = index == 0 && playDeleteHint,
                            onHintAnimationPlayed = { playDeleteHint = false },
                            onDelete = { onAction(GoalsAction.Delete(it)) },
                            onTap = { detailGoal = it },
                            onLongPress = {
                                renameTarget = it
                                renameValue = it.goal.title
                            }
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = ClearrDimens.dp20, bottom = ClearrDimens.dp20).size(ClearrDimens.dp52).clickable { onAddGoal() },
            shape = RoundedCornerShape(ClearrDimens.dp16),
            color = ClearrColors.Violet,
            shadowElevation = ClearrDimens.dp8
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("+", color = ClearrColors.Surface, fontSize = ClearrTextSizes.sp24, fontWeight = FontWeight.Bold)
            }
        }
    }

    detailGoal?.let { summary ->
        GoalDetailSheet(
            summary = summary,
            colors = colors,
            onDismiss = { detailGoal = null },
            onMarkDone = {
                onAction(GoalsAction.MarkDone(it))
                detailGoal = null
            }
        )
    }

    renameTarget?.let { summary ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            containerColor = colors.surface,
            title = { Text("Rename Goal", color = colors.text) },
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
                Button(enabled = renameValue.isNotBlank(), onClick = {
                    onAction(GoalsAction.Rename(summary.goal.id, renameValue))
                    renameTarget = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel", color = colors.muted) }
            }
        )
    }
}
