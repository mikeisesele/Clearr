package com.mikeisesele.clearr.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.viewmodel.TrackerListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Type color palette (matches React spec exactly) ──────────────────────────
private data class TypeStyle(
    val icon: String,
    val color: Color,
    val bgColor: Color,
    val label: String
)

private val typeStyles = mapOf(
    TrackerType.DUES       to TypeStyle("₦",  Color(0xFF6C63FF), Color(0xFFEEF0FF), "Dues"),
    TrackerType.ATTENDANCE to TypeStyle("✓",  Color(0xFF00A67E), Color(0xFFE6F7F3), "Attendance"),
    TrackerType.TASKS      to TypeStyle("⬡",  Color(0xFFF59E0B), Color(0xFFFEF3C7), "Tasks"),
    TrackerType.EVENTS     to TypeStyle("◈",  Color(0xFFEF4444), Color(0xFFFEE2E2), "Events"),
    TrackerType.CUSTOM     to TypeStyle("☰",  Color(0xFF6C63FF), Color(0xFFEEF0FF), "Custom"),
)

private val primaryColor = Color(0xFF6C63FF)

// ─────────────────────────────────────────────────────────────────────────────
// Root screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TrackerListScreen(
    viewModel: TrackerListViewModel = hiltViewModel(),
    onTrackerClick: (trackerId: Long) -> Unit,
    onSettingsClick: () -> Unit,
    onCreateTracker: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var deleteTarget by remember { mutableStateOf<TrackerSummary?>(null) }
    var renameTarget by remember { mutableStateOf<TrackerSummary?>(null) }
    var renameValue by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7FB))
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    viewModel.refresh()
                    delay(350)
                    isRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
            Surface(
                color = Color.White,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "My Trackers",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1A1A2E),
                                fontSize = 22.sp
                            )
                        }
                        IconPill(icon = "⚙️", onClick = onSettingsClick)
                    }

                    Spacer(Modifier.height(14.dp))
                }
            }

            // ── Body ──────────────────────────────────────────────────────────
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
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp,
                            top = 16.dp, bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.summaries, key = { it.trackerId }) { summary ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                positionalThreshold = { it * 0.35f },
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        deleteTarget = summary
                                        true
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
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFFEF4444))
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                },
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true
                            ) {
                                TrackerCard(
                                    summary = summary,
                                    onClick = {
                                        if (summary.isNew) viewModel.clearNewFlag(summary.trackerId)
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

        // ── FAB (only in populated state) ─────────────────────────────────────
        if (!state.isLoading && state.summaries.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Label pill
                Surface(
                    color = Color(0xFF1A1A2E),
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        "New Tracker",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
                // FAB button
                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onCreateTracker),
                    color = primaryColor,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp,
                    tonalElevation = 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Light)
                    }
                }
            }
        }
        }
    }

    deleteTarget?.let { summary ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete ${summary.name}?") },
            text = {
                Text("This will remove the tracker and all members/records inside it.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTracker(summary.trackerId)
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete")
                }
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
                        viewModel.renameTracker(summary.trackerId, renameValue)
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


// ─────────────────────────────────────────────────────────────────────────────
// Tracker Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TrackerCard(
    summary: TrackerSummary,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val style = typeStyles[summary.type] ?: typeStyles[TrackerType.DUES]!!
    val allDone = summary.completedCount == summary.totalMembers && summary.totalMembers > 0
    val barColor = if (allDone) Color(0xFF00A67E) else style.color
    val pct = summary.completionPercent

    val animatedBarColor by animateColorAsState(
        targetValue = barColor,
        animationSpec = tween(400),
        label = "bar_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(summary.trackerId) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            // NEW badge border tint
            if (summary.isNew) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, style.color, RoundedCornerShape(16.dp))
                )
            }

            Column(modifier = Modifier.padding(18.dp, 18.dp, 16.dp, 18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Type icon square ──────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(style.bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            style.icon,
                            color = style.color,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // ── Name + meta + progress bar ────────────────────────────
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            summary.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1A1A2E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${summary.frequency.displayName()}  ·  ${summary.currentPeriodLabel}",
                            fontSize = 12.sp,
                            color = Color(0xFF888888)
                        )
                    }

                    // ── Progress ring ─────────────────────────────────────────
                    Box(
                        modifier = Modifier.size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { pct / 100f },
                            modifier = Modifier.size(44.dp),
                            color = if (allDone) Color(0xFF00A67E) else style.color,
                            trackColor = Color(0xFFF0F0F0),
                            strokeWidth = 4.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            "$pct%",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (allDone) Color(0xFF00A67E) else style.color
                        )
                    }
                }
            }

            // NEW badge
            if (summary.isNew) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(style.color)
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        "NEW",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty State
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyTrackerState(onCreate: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Illustration
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF4F3FF)),
                contentAlignment = Alignment.Center
            ) {
                Text("📋", fontSize = 36.sp)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "No trackers yet",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = Color(0xFF1A1A2E)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Create your first tracker to start managing dues, attendance, tasks, or events for your group.",
                fontSize = 14.sp,
                color = Color(0xFF888888),
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = onCreate,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
            ) {
                Text("+ Create Tracker", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Icon pill for header actions
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun IconPill(icon: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF4F3FF))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(icon, fontSize = 16.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Extension: human-readable frequency labels
// ─────────────────────────────────────────────────────────────────────────────

private fun Frequency.displayName(): String = when (this) {
    Frequency.MONTHLY   -> "Monthly"
    Frequency.WEEKLY    -> "Weekly"
    Frequency.QUARTERLY -> "Quarterly"
    Frequency.TERMLY    -> "Termly"
    Frequency.BIANNUAL  -> "Biannual"
    Frequency.ANNUAL    -> "Annual"
    Frequency.CUSTOM    -> "Custom"
}
