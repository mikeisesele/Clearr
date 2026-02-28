package com.mikeisesele.clearr.ui.feature.trackerlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
import com.mikeisesele.clearr.ui.feature.trackerlist.components.DeleteTrackerDialog
import com.mikeisesele.clearr.ui.feature.trackerlist.components.RemittanceSwipeCard
import com.mikeisesele.clearr.ui.feature.trackerlist.components.RenameTrackerDialog
import com.mikeisesele.clearr.ui.feature.trackerlist.components.TrackerCard
import com.mikeisesele.clearr.ui.feature.trackerlist.components.primaryColor
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDS
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TrackerListScreen(
    viewModel: TrackerListViewModel = hiltViewModel(),
    onTrackerClick: (trackerId: Long) -> Unit,
    onCreateTracker: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val spacing = ClearrDS.spacing
    val radii = ClearrDS.radii
    val sizes = ClearrDS.sizes
    val colors = LocalDuesColors.current
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
            .background(colors.bg)
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
                    title = "Dashboard",
                    subtitle = null,
                    showLeading = false,
                    actionIcon = "⚙️",
                    onActionClick = onOpenSettings,
                    actionContainerColor = colors.card
                )

                // ── Body ──────────────────────────────────────────────────────
                when {
                    state.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = primaryColor)
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, end = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, top = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp100),
                            verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)
                        ) {
                            items(state.summaries, key = { it.trackerId }) { summary ->
                                if (summary.type == TrackerType.DUES || summary.type == TrackerType.EXPENSES) {
                                    RemittanceSwipeCard(
                                        summary = summary,
                                        onDeleteRequest = { deleteTarget = summary },
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
                                } else {
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
            if (!state.isLoading) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
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
                            "New Remittance",
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
        DeleteTrackerDialog(
            summary = summary,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.onAction(TrackerListAction.DeleteTracker(summary.trackerId))
                deleteTarget = null
            }
        )
    }

    renameTarget?.let { summary ->
        RenameTrackerDialog(
            value = renameValue,
            onValueChange = { renameValue = it },
            onDismiss = { renameTarget = null },
            onConfirm = {
                viewModel.onAction(TrackerListAction.RenameTracker(summary.trackerId, renameValue))
                renameTarget = null
            }
        )
    }
}
