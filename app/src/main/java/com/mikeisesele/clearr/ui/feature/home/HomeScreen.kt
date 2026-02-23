package com.mikeisesele.clearr.ui.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.ui.commons.components.AddMemberDialog
import com.mikeisesele.clearr.ui.commons.components.ConfettiOverlay
import com.mikeisesele.clearr.ui.commons.components.DuesSnackbar
import com.mikeisesele.clearr.ui.commons.components.EditMemberDialog
import com.mikeisesele.clearr.ui.commons.components.MemberDetailSheet
import com.mikeisesele.clearr.ui.commons.components.PartialPaymentDialog
import com.mikeisesele.clearr.ui.commons.util.captureViewWithPixelCopy
import com.mikeisesele.clearr.ui.commons.util.currentMonth
import com.mikeisesele.clearr.ui.commons.util.currentYear
import com.mikeisesele.clearr.ui.commons.util.formatAmount
import com.mikeisesele.clearr.ui.commons.util.isFuture
import com.mikeisesele.clearr.ui.commons.util.saveBitmapToCache
import com.mikeisesele.clearr.ui.commons.util.shareImageUri
import com.mikeisesele.clearr.ui.feature.home.components.StatsRow
import com.mikeisesele.clearr.ui.feature.home.components.TrackerGrid
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    /** The tracker being displayed (-1 = legacy / no tracker ID). */
    trackerId: Long = -1L,
    /** Called when user presses back. Null = no back button shown. */
    onBack: (() -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalDuesColors.current
    val context = LocalContext.current
    val view = LocalView.current

    val currentYear = currentYear()
    val currentMonth = currentMonth()
    val dueAmount = state.yearConfig?.dueAmountPerMonth ?: 5000.0

    LaunchedEffect(trackerId) {
        viewModel.onAction(HomeAction.SetCurrentTrackerId(trackerId.takeIf { it > 0 }))
    }

    val visibleMembers = remember(state.members, state.showArchived) {
        state.members.filter { state.showArchived || !it.isArchived }
    }
    val activeMembers = remember(state.members) { state.members.filter { !it.isArchived } }

    val paymentMap = remember(state.payments) {
        state.payments
            .filter { !it.isUndone }
            .groupBy { "${it.memberId}-${it.monthIndex}" }
            .mapValues { (_, r) -> r.sumOf { it.amountPaid } }
    }

    fun paidForMonth(memberId: Long, mi: Int) = paymentMap["$memberId-$mi"] ?: 0.0
    fun isFullPaid(memberId: Long, mi: Int) = paidForMonth(memberId, mi) >= dueAmount
    fun isPartial(memberId: Long, mi: Int): Boolean {
        val p = paidForMonth(memberId, mi); return p > 0 && p < dueAmount
    }

    val nonFutureMonths = (0..11).filter { !isFuture(state.selectedYear, it) }
    val totalCollected = activeMembers.sumOf { m -> nonFutureMonths.sumOf { mi -> paidForMonth(m.id, mi) } }
    val totalExpected = activeMembers.size * nonFutureMonths.size * dueAmount
    val outstanding = (totalExpected - totalCollected).coerceAtLeast(0.0)
    val pct = if (totalExpected > 0) (totalCollected / totalExpected * 100).toInt().coerceIn(0, 100) else 0

    var showAddMember by remember { mutableStateOf(false) }
    var partialTarget by remember { mutableStateOf<Pair<Member, Int>?>(null) }
    var memberDetail by remember { mutableStateOf<Member?>(null) }
    var editTarget by remember { mutableStateOf<Member?>(null) }
    var contextTarget by remember { mutableStateOf<Member?>(null) }
    var deleteTarget by remember { mutableStateOf<Member?>(null) }
    var showLayoutSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.confettiMonth) {
        if (state.confettiMonth != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.onAction(HomeAction.DismissConfetti)
        }
    }

    val layoutData = TrackerLayoutData(
        members = visibleMembers,
        selectedYear = state.selectedYear,
        currentYear = currentYear,
        currentMonth = currentMonth,
        dueAmount = dueAmount,
        isFullPaid = ::isFullPaid,
        isPartial = ::isPartial,
        paidForMonth = ::paidForMonth,
        onCellTap = { m, mi -> viewModel.onAction(HomeAction.TogglePayment(m, state.selectedYear, mi, dueAmount)) },
        onCellLongPress = { m, mi -> partialTarget = m to mi },
        onMemberTap = { memberDetail = it },
        onMemberLongPress = { contextTarget = it },
        colors = colors
    )

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ──────────────────────────────────────────────────────
            com.mikeisesele.clearr.ui.feature.home.components.HomeTopBar(
                trackerName = state.trackerName,
                layoutStyle = state.layoutStyle,
                selectedYear = state.selectedYear,
                dueAmount = dueAmount,
                onBack = onBack,
                onLayoutClick = { showLayoutSheet = true },
                onShareClick = { shareScreenshot(context, view) },
                colors = colors
            )

            // ── Stats ─────────────────────────────────────────────────────────
            StatsRow(
                totalCollected = totalCollected,
                totalExpected = totalExpected,
                outstanding = outstanding,
                pct = pct,
                colors = colors
            )

            // ── Layout area ───────────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = state.layoutStyle,
                    transitionSpec = {
                        (fadeIn(tween(240)) + slideInVertically(tween(240)) { it / 12 })
                            .togetherWith(fadeOut(tween(180)) + slideOutVertically(tween(180)) { -it / 12 })
                    },
                    label = "layout_switch"
                ) { layout ->
                    when (layout) {
                        LayoutStyle.GRID -> TrackerGrid(layoutData)
                        LayoutStyle.KANBAN -> KanbanLayout(layoutData)
                        LayoutStyle.CARDS -> CardsLayout(layoutData)
                        LayoutStyle.RECEIPT -> ReceiptLayout(layoutData)
                    }
                }
            }
        }

        // ── Small FAB ────────────────────────────────────────────────────────
        SmallFloatingActionButton(
            onClick = { showAddMember = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
            containerColor = colors.accent,
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(4.dp, 6.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add member", modifier = Modifier.size(20.dp))
        }

        // ── Snackbar ──────────────────────────────────────────────────────────
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)) {
            state.snackbarMessage?.let { snack ->
                DuesSnackbar(
                    message = snack.message,
                    onUndo = snack.undoPaymentId?.let { pid ->
                        {
                            viewModel.onAction(HomeAction.UndoLastRemoval(
                                pid,
                                snack.undoMemberId ?: 0,
                                snack.undoYear ?: state.selectedYear,
                                snack.undoMonthIndex ?: 0,
                                dueAmount
                            ))
                        }
                    },
                    onDismiss = { viewModel.onAction(HomeAction.DismissSnackbar) }
                )
            }
        }

        ConfettiOverlay(show = state.confettiMonth != null)
    }

    // ── Dialogs / sheets ─────────────────────────────────────────────────────
    if (showAddMember) {
        AddMemberDialog(
            onDismiss = { showAddMember = false },
            onAdd = { name, phone -> viewModel.onAction(HomeAction.AddMember(name, phone)) }
        )
    }

    partialTarget?.let { (member, mi) ->
        PartialPaymentDialog(
            memberName = member.name,
            monthIndex = mi,
            year = state.selectedYear,
            alreadyPaid = paidForMonth(member.id, mi),
            dueAmount = dueAmount,
            onDismiss = { partialTarget = null },
            onRecord = { amount, note ->
                viewModel.onAction(HomeAction.RecordPartialPayment(member.id, state.selectedYear, mi, amount, note, dueAmount))
            }
        )
    }

    memberDetail?.let { member ->
        MemberDetailSheet(
            member = member,
            payments = state.payments.filter { it.memberId == member.id },
            dueAmount = dueAmount,
            selectedYear = state.selectedYear,
            showBulkMarkPaid = state.trackerType == com.mikeisesele.clearr.data.model.TrackerType.DUES,
            onDismiss = { memberDetail = null },
            onEdit = { editTarget = member; memberDetail = null },
            onArchiveToggle = {
                viewModel.onAction(HomeAction.SetMemberArchived(member.id, !member.isArchived))
                memberDetail = null
            },
            onDelete = {
                deleteTarget = member
                memberDetail = null
            },
            onBulkMarkPaid = {
                viewModel.onAction(HomeAction.MarkOutstandingMonthsPaid(
                    memberId = member.id,
                    year = state.selectedYear,
                    dueAmount = dueAmount,
                    trackerIdOverride = trackerId.takeIf { it > 0 }
                ))
                memberDetail = null
            }
        )
    }

    editTarget?.let { member ->
        EditMemberDialog(
            initialName = member.name,
            initialPhone = member.phone,
            onDismiss = { editTarget = null },
            onSave = { name, phone -> viewModel.onAction(HomeAction.UpdateMember(member.copy(name = name, phone = phone))) }
        )
    }

    contextTarget?.let { member ->
        val previewColors = LocalDuesColors.current
        AlertDialog(
            onDismissRequest = { contextTarget = null },
            containerColor = previewColors.surface,
            title = { Text(member.name, color = previewColors.text, style = MaterialTheme.typography.titleMedium) },
            text = {
                Column {
                    TextButton(onClick = { editTarget = member; contextTarget = null }, modifier = Modifier.fillMaxWidth()) {
                        Text("Edit", color = previewColors.accent)
                    }
                    TextButton(
                        onClick = { viewModel.onAction(HomeAction.SetMemberArchived(member.id, !member.isArchived)); contextTarget = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (member.isArchived) "Restore" else "Archive", color = if (member.isArchived) previewColors.green else previewColors.red)
                    }
                    TextButton(onClick = { deleteTarget = member; contextTarget = null }, modifier = Modifier.fillMaxWidth()) {
                        Text("Delete", color = previewColors.red)
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { contextTarget = null }) { Text("Cancel", color = previewColors.muted) } },
            shape = RoundedCornerShape(16.dp)
        )
    }

    deleteTarget?.let { member ->
        val previewColors = LocalDuesColors.current
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = previewColors.surface,
            title = { Text("Delete ${member.name}?", color = previewColors.text) },
            text = { Text("This removes the member and all their payment history. This cannot be undone.", color = previewColors.muted) },
            confirmButton = {
                Button(
                    onClick = { viewModel.onAction(HomeAction.DeleteMember(member.id, trackerId.takeIf { it > 0 })); deleteTarget = null },
                    colors = ButtonDefaults.buttonColors(containerColor = previewColors.red)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel", color = previewColors.muted) } },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showLayoutSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLayoutSheet = false },
            containerColor = colors.surface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Choose Layout", style = MaterialTheme.typography.titleMedium, color = colors.text)
                listOf(
                    LayoutStyle.GRID to "⊞ Grid",
                    LayoutStyle.KANBAN to "🗂 Kanban",
                    LayoutStyle.CARDS to "🃏 Cards",
                    LayoutStyle.RECEIPT to "🧾 Receipt"
                ).forEach { (style, label) ->
                    val selected = state.layoutStyle == style
                    Surface(
                        color = if (selected) colors.accent.copy(alpha = 0.12f) else colors.card,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.onAction(HomeAction.SetLayoutStyleForCurrentTracker(style))
                            showLayoutSheet = false
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = if (selected) colors.accent else colors.text)
                            if (selected) Text("✓", color = colors.accent)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

private fun shareScreenshot(
    context: android.content.Context,
    view: android.view.View
) {
    val window = (context as? android.app.Activity)?.window ?: return
    captureViewWithPixelCopy(view, window) { bitmap ->
        if (bitmap != null) {
            try {
                val uri = saveBitmapToCache(context, bitmap, "dues_tracker_${System.currentTimeMillis()}.png")
                shareImageUri(context, uri, "Share Dues Summary")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {
            StatsRow(totalCollected = 45000.0, totalExpected = 60000.0, outstanding = 15000.0, pct = 75, colors = colors)
        }
    }
}
