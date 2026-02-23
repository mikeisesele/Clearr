package com.mikeisesele.clearr.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.util.MONTHS
import com.mikeisesele.clearr.ui.util.formatAmount
import com.mikeisesele.clearr.ui.util.isFuture

// ─────────────────────────────────────────────────────────────────────────────
// Common data bag passed into every layout
// ─────────────────────────────────────────────────────────────────────────────
data class TrackerLayoutData(
    val members: List<Member>,
    val selectedYear: Int,
    val currentYear: Int,
    val currentMonth: Int,
    val dueAmount: Double,
    val isFullPaid: (Long, Int) -> Boolean,
    val isPartial: (Long, Int) -> Boolean,
    val paidForMonth: (Long, Int) -> Double,
    val onCellTap: (Member, Int) -> Unit,
    val onCellLongPress: (Member, Int) -> Unit,
    val onMemberTap: (Member) -> Unit,
    val onMemberLongPress: (Member) -> Unit,
    val C: DuesColors
)

// ─────────────────────────────────────────────────────────────────────────────
// KANBAN  –  one column per month, member chips scroll down inside each column
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun KanbanLayout(d: TrackerLayoutData) {
    val visibleMonths = (0..11).filter { !isFuture(d.selectedYear, it) || it == d.currentMonth }

    if (d.members.isEmpty()) { EmptyState(d.C); return }

    LazyRow(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(MONTHS.size) { mi ->
            val future = isFuture(d.selectedYear, mi)
            val isCurrent = d.selectedYear == d.currentYear && mi == d.currentMonth
            val paidCount = d.members.count { d.isFullPaid(it.id, mi) }
            val total = d.members.size

            Card(
                modifier = Modifier
                    .width(160.dp)
                    .wrapContentHeight()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) d.C.accent.copy(alpha = 0.08f) else d.C.card
                ),
                shape = RoundedCornerShape(14.dp),
                border = if (isCurrent) BorderStroke(1.5.dp, d.C.accent) else null
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Column header
                    Text(
                        MONTHS[mi],
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCurrent) d.C.accent else if (future) d.C.dim else d.C.text
                    )
                    Text(
                        if (future) "upcoming" else "$paidCount / $total paid",
                        style = MaterialTheme.typography.labelSmall,
                        color = d.C.muted
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = d.C.border
                    )
                    // Member chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        d.members.forEach { member ->
                            val full = d.isFullPaid(member.id, mi)
                            val partial = d.isPartial(member.id, mi)

                            val chipBg by animateColorAsState(
                                targetValue = when {
                                    future -> d.C.surface
                                    full -> d.C.green
                                    partial -> d.C.amber
                                    else -> d.C.surface
                                },
                                animationSpec = tween(200),
                                label = "kanban_chip"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(chipBg)
                                    .border(
                                        width = 0.5.dp,
                                        color = d.C.border,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .let {
                                        if (!future && !member.isArchived) {
                                            it.pointerInput(member.id, mi) {
                                                detectTapGestures(
                                                    onTap = { d.onCellTap(member, mi) },
                                                    onLongPress = { d.onCellLongPress(member, mi) }
                                                )
                                            }
                                        } else it
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        when {
                                            future -> "—"
                                            full -> "✓"
                                            partial -> "½"
                                            else -> "○"
                                        },
                                        fontSize = 12.sp,
                                        color = if (full || partial) Color(0xFF0F172A) else d.C.dim
                                    )
                                    Text(
                                        member.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (full || partial) Color(0xFF0F172A)
                                               else if (future) d.C.dim else d.C.text,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CARDS  –  one big card per member, month chips across
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun CardsLayout(d: TrackerLayoutData) {
    if (d.members.isEmpty()) { EmptyState(d.C); return }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(d.members) { member ->
            val paidMonths = (0..11).count { d.isFullPaid(member.id, it) && !isFuture(d.selectedYear, it) }
            val totalDue = (0..11).count { !isFuture(d.selectedYear, it) }
            val totalPaidAmt = (0..11).sumOf { d.paidForMonth(member.id, it) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(member) {
                        detectTapGestures(
                            onTap = { d.onMemberTap(member) },
                            onLongPress = { d.onMemberLongPress(member) }
                        )
                    },
                colors = CardDefaults.cardColors(containerColor = d.C.card),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Member header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                member.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (member.isArchived) d.C.muted else d.C.text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!member.phone.isNullOrBlank()) {
                                Text(
                                    member.phone,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = d.C.muted
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "$paidMonths/$totalDue months",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (paidMonths == totalDue) d.C.green else d.C.muted
                            )
                            Text(
                                formatAmount(totalPaidAmt),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = d.C.green
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Month chips row (horizontally scrollable)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MONTHS.forEachIndexed { mi, month ->
                            val future = isFuture(d.selectedYear, mi)
                            val full = !future && d.isFullPaid(member.id, mi)
                            val partial = !future && d.isPartial(member.id, mi)
                            val isCur = d.selectedYear == d.currentYear && mi == d.currentMonth

                            val chipBg by animateColorAsState(
                                targetValue = when {
                                    future -> d.C.surface
                                    full -> d.C.green
                                    partial -> d.C.amber
                                    else -> d.C.surface
                                },
                                animationSpec = tween(200), label = "card_chip"
                            )
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(chipBg)
                                    .border(
                                        width = if (isCur) 1.5.dp else 0.5.dp,
                                        color = if (isCur) d.C.accent else d.C.border,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .let {
                                        if (!future && !member.isArchived) {
                                            it.pointerInput(member.id, mi) {
                                                detectTapGestures(
                                                    onTap = { d.onCellTap(member, mi) },
                                                    onLongPress = { d.onCellLongPress(member, mi) }
                                                )
                                            }
                                        } else it
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    month,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = if (isCur) d.C.accent else if (future) d.C.dim else d.C.muted
                                )
                                Text(
                                    when {
                                        future -> "—"
                                        full -> "✓"
                                        partial -> "½"
                                        else -> "·"
                                    },
                                    fontSize = 13.sp,
                                    color = if (full || partial) Color(0xFF0F172A) else d.C.dim
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RECEIPT / LEDGER  –  running ledger, one row per member × month
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ReceiptLayout(d: TrackerLayoutData) {
    if (d.members.isEmpty()) { EmptyState(d.C); return }

    val pastMonths = (0..11).filter { !isFuture(d.selectedYear, it) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Global totals header
        item {
            val totalExpected = d.members.filter { !it.isArchived }.size * pastMonths.size * d.dueAmount
            val totalCollected = d.members.filter { !it.isArchived }
                .sumOf { m -> pastMonths.sumOf { mi -> d.paidForMonth(m.id, mi) } }
            Card(
                colors = CardDefaults.cardColors(containerColor = d.C.card),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LedgerStat("EXPECTED", formatAmount(totalExpected), d.C.muted)
                    LedgerStat("COLLECTED", formatAmount(totalCollected), d.C.green)
                    LedgerStat("BALANCE", formatAmount(totalExpected - totalCollected),
                        if (totalCollected < totalExpected) d.C.red else d.C.green)
                }
            }
        }

        // Per-member ledger
        items(d.members.filter { !it.isArchived }) { member ->
            Card(
                colors = CardDefaults.cardColors(containerColor = d.C.card),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(member) {
                        detectTapGestures(
                            onTap = { d.onMemberTap(member) },
                            onLongPress = { d.onMemberLongPress(member) }
                        )
                    }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        member.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = d.C.text
                    )
                    Spacer(Modifier.height(8.dp))
                    // Row header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Month",
                            modifier = Modifier.weight(1.5f),
                            style = MaterialTheme.typography.labelSmall,
                            color = d.C.muted
                        )
                        Text(
                            "Due",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = d.C.muted,
                            textAlign = TextAlign.End
                        )
                        Text(
                            "Paid",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = d.C.muted,
                            textAlign = TextAlign.End
                        )
                        Text(
                            "Status",
                            modifier = Modifier.weight(0.8f),
                            style = MaterialTheme.typography.labelSmall,
                            color = d.C.muted,
                            textAlign = TextAlign.End
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = d.C.border
                    )
                    pastMonths.forEach { mi ->
                        val paid = d.paidForMonth(member.id, mi)
                        val full = d.isFullPaid(member.id, mi)
                        val partial = d.isPartial(member.id, mi)
                        val rowBg = when {
                            full -> d.C.green.copy(alpha = 0.08f)
                            partial -> d.C.amber.copy(alpha = 0.08f)
                            else -> Color.Transparent
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(rowBg)
                                .pointerInput(member.id, mi) {
                                    detectTapGestures(
                                        onTap = { d.onCellTap(member, mi) },
                                        onLongPress = { d.onCellLongPress(member, mi) }
                                    )
                                }
                                .padding(vertical = 5.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                MONTHS[mi],
                                modifier = Modifier.weight(1.5f),
                                style = MaterialTheme.typography.bodySmall,
                                color = d.C.text
                            )
                            Text(
                                formatAmount(d.dueAmount),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                color = d.C.muted,
                                textAlign = TextAlign.End
                            )
                            Text(
                                if (paid > 0) formatAmount(paid) else "—",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (paid > 0) d.C.green else d.C.dim,
                                textAlign = TextAlign.End
                            )
                            Text(
                                when {
                                    full -> "✓ PAID"
                                    partial -> "½ PART"
                                    else -> "UNPAID"
                                },
                                modifier = Modifier.weight(0.8f),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    full -> d.C.green
                                    partial -> d.C.amber
                                    else -> d.C.red
                                },
                                textAlign = TextAlign.End
                            )
                        }
                    }
                    // Member total row
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = d.C.border
                    )
                    val memberTotal = pastMonths.sumOf { d.paidForMonth(member.id, it) }
                    val memberDue = pastMonths.size * d.dueAmount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = d.C.text
                        )
                        Text(
                            "${formatAmount(memberTotal)} / ${formatAmount(memberDue)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (memberTotal >= memberDue) d.C.green else d.C.red
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(C: DuesColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "No members yet.\nTap  +  to add one.",
            color = C.muted,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LedgerStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
    }
}
