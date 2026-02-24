package com.mikeisesele.clearr.ui.feature.trackerlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
import com.mikeisesele.clearr.ui.feature.trackerlist.TrackerListAction
import com.mikeisesele.clearr.ui.feature.trackerlist.components.EmptyTrackerState
import com.mikeisesele.clearr.ui.feature.trackerlist.components.TrackerCard
import com.mikeisesele.clearr.ui.feature.trackerlist.components.primaryColor
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDS
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Root screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TrackerListScreen(
    viewModel: TrackerListViewModel = hiltViewModel(),
    onTrackerClick: (trackerId: Long) -> Unit,
    onCreateTracker: () -> Unit
) {
    val spacing = ClearrDS.spacing
    val radii = ClearrDS.radii
    val sizes = ClearrDS.sizes
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var deleteTarget by remember { mutableStateOf<TrackerSummary?>(null) }
    var renameTarget by remember { mutableStateOf<TrackerSummary?>(null) }
    var renameValue by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onAction(TrackerListAction.Refresh)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClearrColors.BrandBackground)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    viewModel.onAction(TrackerListAction.Refresh)
                    delay(350)
                    isRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Header ────────────────────────────────────────────────────
                ClearrTopBar(
                    title = "My Trackers",
                    subtitle = null,
                    leadingIcon = "📋",
                    onLeadingClick = null,
                    actionIcon = null,
                    onActionClick = null
                )

                // ── Body ──────────────────────────────────────────────────────
                when {
                    state.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = primaryColor)
                        }
                    }
                    state.summaries.isEmpty() -> {
                        EmptyTrackerState(onCreate = onCreateTracker)
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, end = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, top = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp100),
                            verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)
                        ) {
                            items(state.summaries, key = { it.trackerId }) { summary ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    positionalThreshold = { it * 0.35f },
                                    confirmValueChange = { value ->
                                        if (value == SwipeToDismissBoxValue.EndToStart) {
                                            deleteTarget = summary
                                            false
                                        } else {
                                            false
                                        }
                                    }
                                )
                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(radii.lg))
                                                .background(ClearrColors.BrandDanger)
                                                .padding(horizontal = spacing.xl),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Text("Delete", color = ClearrColors.Surface, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    enableDismissFromStartToEnd = false,
                                    enableDismissFromEndToStart = true
                                ) {
                                    TrackerCard(
                                        summary = summary,
                                        onClick = {
                                            if (summary.isNew) {
                                                viewModel.onAction(TrackerListAction.ClearNewFlag(summary.trackerId))
                                            }
                                            onTrackerClick(summary.trackerId)
                                        },
                                        onLongPress = {
                                            renameTarget = summary
                                            renameValue = summary.name
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── FAB ───────────────────────────────────────────────────────────
            if (!state.isLoading && state.summaries.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = spacing.xl, bottom = spacing.xxl),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)
                ) {
                    Surface(
                        color = ClearrColors.BrandText,
                        shape = RoundedCornerShape(radii.xl),
                        shadowElevation = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8
                    ) {
                        Text(
                            "New Tracker",
                            color = ClearrColors.Surface,
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(sizes.fab)
                            .clip(RoundedCornerShape(radii.lg))
                            .background(primaryColor)
                            .clickable { onCreateTracker() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = ClearrColors.Surface, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp28, fontWeight = FontWeight.Light)
                    }
                }
            }
        }
    }

    deleteTarget?.let { summary ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete ${summary.name}?") },
            text = { Text("This will remove the tracker and all members/records inside it.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onAction(TrackerListAction.DeleteTracker(summary.trackerId))
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClearrColors.BrandDanger)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }

    renameTarget?.let { summary ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Edit Tracker Name") },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tracker name") }
                )
            },
            confirmButton = {
                Button(
                    enabled = renameValue.isNotBlank(),
                    onClick = {
                        viewModel.onAction(TrackerListAction.RenameTracker(summary.trackerId, renameValue))
                        renameTarget = null
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackerListScreenEmptyPreview() {
    ClearrTheme {
        EmptyTrackerState(onCreate = {})
    }
}
