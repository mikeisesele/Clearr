package com.mikeisesele.clearr.ui.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
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
import com.mikeisesele.clearr.ui.commons.util.currentMonth
import com.mikeisesele.clearr.ui.commons.util.currentYear
import com.mikeisesele.clearr.ui.commons.util.isFuture
import com.mikeisesele.clearr.ui.feature.home.components.DeleteMemberDialog
import com.mikeisesele.clearr.ui.feature.home.components.LayoutPickerSheet
import com.mikeisesele.clearr.ui.feature.home.components.MemberContextDialog
import com.mikeisesele.clearr.ui.feature.home.components.StatsRow
import com.mikeisesele.clearr.ui.feature.home.components.TrackerGrid
import com.mikeisesele.clearr.ui.feature.home.utils.shareHomeScreenshot
import com.mikeisesele.clearr.ui.theme.ClearrColors
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
    var blurMemberNames by rememberSaveable { mutableStateOf(false) }

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
        blurMemberNames = blurMemberNames,
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
                showBlurToggle = state.trackerType == com.mikeisesele.clearr.data.model.TrackerType.DUES ||
                    state.trackerType == com.mikeisesele.clearr.data.model.TrackerType.EXPENSES,
                blurMemberNames = blurMemberNames,
                onBlurToggle = { blurMemberNames = !blurMemberNames },
                onBack = onBack,
                onLayoutClick = { showLayoutSheet = true },
                onShareClick = {
                    shareHomeScreenshot(
                        context,
                        view,
                        state.trackerType == com.mikeisesele.clearr.data.model.TrackerType.DUES ||
                            state.trackerType == com.mikeisesele.clearr.data.model.TrackerType.EXPENSES
                    )
                },
                colors = colors
            )

            // ── Stats ─────────────────────────────────────────────────────────
            state.aiRiskHint?.let { risk ->
                Text(
                    text = risk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6),
                    color = colors.muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
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
                .navigationBarsPadding()
                .padding(end = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20, bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20),
            containerColor = colors.accent,
            contentColor = ClearrColors.Surface,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4, com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add member", modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20))
        }

        // ── Snackbar ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp80)
        ) {
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
            showBulkMarkPaid = state.trackerType == com.mikeisesele.clearr.data.model.TrackerType.DUES ||
                state.trackerType == com.mikeisesele.clearr.data.model.TrackerType.EXPENSES,
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
        MemberContextDialog(
            member = member,
            onDismiss = { contextTarget = null },
            onEdit = { editTarget = member; contextTarget = null },
            onArchiveToggle = {
                viewModel.onAction(HomeAction.SetMemberArchived(member.id, !member.isArchived))
                contextTarget = null
            },
            onDelete = { deleteTarget = member; contextTarget = null }
        )
    }

    deleteTarget?.let { member ->
        DeleteMemberDialog(
            member = member,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.onAction(HomeAction.DeleteMember(member.id, trackerId.takeIf { it > 0 }))
                deleteTarget = null
            }
        )
    }

    if (showLayoutSheet) {
        LayoutPickerSheet(
            selectedLayout = state.layoutStyle,
            onDismiss = { showLayoutSheet = false },
            onSelect = { style ->
                viewModel.onAction(HomeAction.SetLayoutStyleForCurrentTracker(style))
                showLayoutSheet = false
            }
        )
    }
}
