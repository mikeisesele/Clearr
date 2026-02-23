package com.mikeisesele.clearr.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.ui.components.*
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import com.mikeisesele.clearr.ui.theme.WhatsAppGreen
import com.mikeisesele.clearr.ui.util.*
import com.mikeisesele.clearr.ui.viewmodel.HomeViewModel

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
    val C = LocalDuesColors.current
    val context = LocalContext.current
    val view = LocalView.current

    val currentYear = currentYear()
    val currentMonth = currentMonth()
    val dueAmount = state.yearConfig?.dueAmountPerMonth ?: 5000.0

    LaunchedEffect(trackerId) {
        viewModel.setCurrentTrackerId(trackerId.takeIf { it > 0 })
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
            viewModel.dismissConfetti()
        }
    }

    // Common data bag for all layout variants
    val layoutData = TrackerLayoutData(
        members = visibleMembers,
        selectedYear = state.selectedYear,
        currentYear = currentYear,
        currentMonth = currentMonth,
        dueAmount = dueAmount,
        isFullPaid = ::isFullPaid,
        isPartial = ::isPartial,
        paidForMonth = ::paidForMonth,
        onCellTap = { m, mi -> viewModel.togglePayment(m, state.selectedYear, mi, dueAmount) },
        onCellLongPress = { m, mi -> partialTarget = m to mi },
        onMemberTap = { memberDetail = it },
        onMemberLongPress = { contextTarget = it },
        C = C
    )

    Box(modifier = Modifier.fillMaxSize().background(C.bg)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ──────────────────────────────────────────────────────
            Surface(color = C.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back button when navigated from TrackerListScreen
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = C.text
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            state.trackerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = C.text
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Layout badge on the left to reduce right-side visual crowding
                            Surface(
                                color = C.accent.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.clickable { showLayoutSheet = true }
                            ) {
                                Text(
                                    layoutLabel(state.layoutStyle),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = C.accent,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                "${state.selectedYear}  ·  ${formatAmount(dueAmount)}/member",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = C.muted
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { shareScreenshot(context, view) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WhatsAppGreen),
                        border = BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("📤", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Text("Share", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ── Stats ─────────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        Triple("COLLECTED", formatAmount(totalCollected), C.green),
                        Triple("EXPECTED", formatAmount(totalExpected), C.text),
                        Triple("OUTSTANDING", formatAmount(outstanding), if (outstanding > 0) C.red else C.green)
                    ).forEach { (label, value, color) ->
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = C.card),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(label, style = MaterialTheme.typography.labelSmall, color = C.muted)
                                Spacer(Modifier.height(3.dp))
                                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = color)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { pct / 100f },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                    color = C.green,
                    trackColor = C.border
                )
                Text(
                    "$pct% collected",
                    style = MaterialTheme.typography.labelSmall,
                    color = C.muted,
                    modifier = Modifier.align(Alignment.End).padding(top = 3.dp)
                )
            }

            // ── Layout area (switches based on AppConfig.layoutStyle) ──────────
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
            containerColor = C.accent,
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
                            viewModel.undoLastRemoval(
                                pid,
                                snack.undoMemberId ?: 0,
                                snack.undoYear ?: state.selectedYear,
                                snack.undoMonthIndex ?: 0,
                                dueAmount
                            )
                        }
                    },
                    onDismiss = viewModel::dismissSnackbar
                )
            }
        }

        ConfettiOverlay(show = state.confettiMonth != null)
    }

    // ── Dialogs / sheets ─────────────────────────────────────────────────────
    if (showAddMember) {
        AddMemberDialog(
            onDismiss = { showAddMember = false },
            onAdd = { name, phone -> viewModel.addMember(name, phone) }
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
                viewModel.recordPartialPayment(member.id, state.selectedYear, mi, amount, note, dueAmount)
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
                viewModel.setMemberArchived(member.id, !member.isArchived)
                memberDetail = null
            },
            onDelete = {
                deleteTarget = member
                memberDetail = null
            },
            onBulkMarkPaid = {
                viewModel.markOutstandingMonthsPaid(
                    memberId = member.id,
                    year = state.selectedYear,
                    dueAmount = dueAmount,
                    trackerIdOverride = trackerId.takeIf { it > 0 }
                )
                memberDetail = null
            }
        )
    }

    editTarget?.let { member ->
        EditMemberDialog(
            initialName = member.name,
            initialPhone = member.phone,
            onDismiss = { editTarget = null },
            onSave = { name, phone -> viewModel.updateMember(member.copy(name = name, phone = phone)) }
        )
    }

    contextTarget?.let { member ->
        val C2 = LocalDuesColors.current
        AlertDialog(
            onDismissRequest = { contextTarget = null },
            containerColor = C2.surface,
            title = { Text(member.name, color = C2.text, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    TextButton(
                        onClick = { editTarget = member; contextTarget = null },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Edit", color = C2.accent) }
                    TextButton(
                        onClick = {
                            viewModel.setMemberArchived(member.id, !member.isArchived)
                            contextTarget = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (member.isArchived) "Restore" else "Archive",
                            color = if (member.isArchived) C2.green else C2.red
                        )
                    }
                    TextButton(
                        onClick = {
                            deleteTarget = member
                            contextTarget = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete", color = C2.red)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { contextTarget = null }) { Text("Cancel", color = C2.muted) }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    deleteTarget?.let { member ->
        val C2 = LocalDuesColors.current
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = C2.surface,
            title = { Text("Delete ${member.name}?", color = C2.text) },
            text = {
                Text(
                    "This removes the member and all their payment history. This cannot be undone.",
                    color = C2.muted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMember(member.id, trackerId.takeIf { it > 0 })
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = C2.red)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel", color = C2.muted)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showLayoutSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLayoutSheet = false },
            containerColor = C.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Choose Layout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = C.text
                )
                listOf(
                    LayoutStyle.GRID to "⊞ Grid",
                    LayoutStyle.KANBAN to "🗂 Kanban",
                    LayoutStyle.CARDS to "🃏 Cards",
                    LayoutStyle.RECEIPT to "🧾 Receipt"
                ).forEach { (style, label) ->
                    val selected = state.layoutStyle == style
                    Surface(
                        color = if (selected) C.accent.copy(alpha = 0.12f) else C.card,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setLayoutStyleForCurrentTracker(style)
                                showLayoutSheet = false
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = if (selected) C.accent else C.text)
                            if (selected) Text("✓", color = C.accent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun layoutLabel(style: LayoutStyle) = when (style) {
    LayoutStyle.GRID    -> "⊞ Grid"
    LayoutStyle.KANBAN  -> "🗂 Kanban"
    LayoutStyle.CARDS   -> "🃏 Cards"
    LayoutStyle.RECEIPT -> "🧾 Receipt"
}

// ── Grid layout (original table) ─────────────────────────────────────────────
@Composable
private fun TrackerGrid(d: TrackerLayoutData) {
    val C = d.C
    if (d.members.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No members yet.\nTap  +  to add one.",
                color = C.muted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        return
    }

    val memberColWidth = 140.dp
    val cellSize = 44.dp
    val cellPad = 4.dp

    val vertScroll = rememberScrollState()
    val horizScroll = rememberScrollState()

    Row(modifier = Modifier.fillMaxSize()) {

        // Sticky member-name column
        Column(
            modifier = Modifier
                .width(memberColWidth)
                .fillMaxHeight()
                .verticalScroll(vertScroll)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(C.surface)
            ) {
                Text(
                    "MEMBER",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = C.muted,
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp)
                )
            }
            d.members.forEachIndexed { idx, member ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cellSize + cellPad * 2)
                        .background(if (idx % 2 == 0) C.bg else C.surface.copy(alpha = 0.4f))
                        .border(BorderStroke(0.5.dp, C.border.copy(alpha = 0.25f)))
                        .pointerInput(member) {
                            detectTapGestures(
                                onTap = { d.onMemberTap(member) },
                                onLongPress = { d.onMemberLongPress(member) }
                            )
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            member.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (member.isArchived) C.muted else C.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (member.isArchived) {
                            Text("archived", style = MaterialTheme.typography.labelSmall, color = C.dim)
                        }
                    }
                }
            }
        }

        // Scrollable month columns
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .horizontalScroll(horizScroll)
                .verticalScroll(vertScroll)
        ) {
            Row(modifier = Modifier.background(C.surface)) {
                MONTHS.forEachIndexed { mi, month ->
                    val future = isFuture(d.selectedYear, mi)
                    val current = d.selectedYear == d.currentYear && mi == d.currentMonth
                    Column(
                        modifier = Modifier
                            .width(cellSize + cellPad * 2)
                            .height(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            month,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = when {
                                future -> C.dim
                                current -> C.accent
                                else -> C.muted
                            }
                        )
                        if (current) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(C.accent)
                            )
                        }
                    }
                }
            }

            d.members.forEachIndexed { idx, member ->
                Row(
                    modifier = Modifier
                        .background(if (idx % 2 == 0) C.bg else C.surface.copy(alpha = 0.4f))
                ) {
                    MONTHS.forEachIndexed { mi, _ ->
                        val future = isFuture(d.selectedYear, mi)
                        val full = !future && d.isFullPaid(member.id, mi)
                        val partial = !future && d.isPartial(member.id, mi)

                        val bgColor by animateColorAsState(
                            targetValue = when {
                                future -> C.surface
                                full -> C.green
                                partial -> C.amber
                                else -> C.card
                            },
                            animationSpec = tween(200),
                            label = "cell_bg"
                        )

                        Box(
                            modifier = Modifier
                                .padding(cellPad)
                                .size(cellSize)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .let {
                                    if (!future && !member.isArchived) {
                                        it.pointerInput(member.id, mi) {
                                            detectTapGestures(
                                                onTap = { d.onCellTap(member, mi) },
                                                onLongPress = { d.onCellLongPress(member, mi) }
                                            )
                                        }
                                    } else it
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                future -> Text("—", color = C.dim, fontSize = 12.sp)
                                full -> Text("✓", color = Color(0xFF0F172A), fontSize = 16.sp, fontWeight = FontWeight.Black)
                                partial -> Text("½", color = Color(0xFF0F172A), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
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
                val uri = saveBitmapToCache(
                    context,
                    bitmap,
                    "dues_tracker_${System.currentTimeMillis()}.png"
                )
                shareImageUri(context, uri, "Share Dues Summary")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
