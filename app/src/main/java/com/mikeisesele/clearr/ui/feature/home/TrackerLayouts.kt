package com.mikeisesele.clearr.ui.feature.home

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.ui.commons.util.MONTHS
import com.mikeisesele.clearr.ui.commons.util.formatAmount
import com.mikeisesele.clearr.ui.commons.util.isFuture
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

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
    val blurMemberNames: Boolean,
    val colors: DuesColors
)

// ─────────────────────────────────────────────────────────────────────────────
// KANBAN  –  one column per month, member chips scroll down inside each column
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun KanbanLayout(d: TrackerLayoutData) {
    val visibleMonths = (0..11).filter { !isFuture(d.selectedYear, it) || it == d.currentMonth }

    if (d.members.isEmpty()) { EmptyState(d.colors); return }

    LazyRow(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8),
        horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10)
    ) {
        items(MONTHS.size) { mi ->
            val future = isFuture(d.selectedYear, mi)
            val isCurrent = d.selectedYear == d.currentYear && mi == d.currentMonth
            val paidCount = d.members.count { d.isFullPaid(it.id, mi) }
            val total = d.members.size

            Card(
                modifier = Modifier
                    .width(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp160)
                    .wrapContentHeight()
                    .padding(bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) d.colors.accent.copy(alpha = 0.08f) else d.colors.card
                ),
                shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14),
                border = if (isCurrent) BorderStroke(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1_5, d.colors.accent) else null
            ) {
                Column(modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10)) {
                    Text(
                        MONTHS[mi],
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCurrent) d.colors.accent else if (future) d.colors.dim else d.colors.text
                    )
                    Text(
                        if (future) "upcoming" else "$paidCount / $total paid",
                        style = MaterialTheme.typography.labelSmall,
                        color = d.colors.muted
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8), color = d.colors.border)
                    Column(verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6)) {
                        d.members.forEach { member ->
                            val full = d.isFullPaid(member.id, mi)
                            val partial = d.isPartial(member.id, mi)

                            val chipBg by animateColorAsState(
                                targetValue = when {
                                    future -> d.colors.surface
                                    full -> d.colors.green
                                    partial -> d.colors.amber
                                    else -> d.colors.surface
                                },
                                animationSpec = tween(200),
                                label = "kanban_chip"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
                                    .background(chipBg)
                                    .border(width = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp0_5, color = d.colors.border, shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
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
                                    .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6)
                                ) {
                                    Text(
                                        when {
                                            future -> "—"
                                            full -> "✓"
                                            partial -> "½"
                                            else -> "○"
                                        },
                                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12,
                                        color = if (full || partial) ClearrColors.BrandText else d.colors.dim
                                    )
                                    Text(
                                        privacyName(member.name, d.blurMemberNames),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (full || partial) ClearrColors.BrandText else if (future) d.colors.dim else d.colors.text,
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
    if (d.members.isEmpty()) { EmptyState(d.colors); return }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8),
        verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10)
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
                colors = CardDefaults.cardColors(containerColor = d.colors.card),
                shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
            ) {
                Column(modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                privacyName(member.name, d.blurMemberNames),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (member.isArchived) d.colors.muted else d.colors.text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!member.phone.isNullOrBlank()) {
                                Text(member.phone, style = MaterialTheme.typography.labelSmall, color = d.colors.muted)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "$paidMonths/$totalDue months",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (paidMonths == totalDue) d.colors.green else d.colors.muted
                            )
                            Text(
                                formatAmount(totalPaidAmt),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = d.colors.green
                            )
                        }
                    }

                    Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6)
                    ) {
                        MONTHS.forEachIndexed { mi, month ->
                            val future = isFuture(d.selectedYear, mi)
                            val full = !future && d.isFullPaid(member.id, mi)
                            val partial = !future && d.isPartial(member.id, mi)
                            val isCur = d.selectedYear == d.currentYear && mi == d.currentMonth

                            val chipBg by animateColorAsState(
                                targetValue = when {
                                    future -> d.colors.surface
                                    full -> d.colors.green
                                    partial -> d.colors.amber
                                    else -> d.colors.surface
                                },
                                animationSpec = tween(200), label = "card_chip"
                            )
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
                                    .background(chipBg)
                                    .border(
                                        width = if (isCur) com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1_5 else com.mikeisesele.clearr.ui.theme.ClearrDimens.dp0_5,
                                        color = if (isCur) d.colors.accent else d.colors.border,
                                        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)
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
                                    .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    month,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp9,
                                    color = if (isCur) d.colors.accent else if (future) d.colors.dim else d.colors.muted
                                )
                                Text(
                                    when {
                                        future -> "—"
                                        full -> "✓"
                                        partial -> "½"
                                        else -> "·"
                                    },
                                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13,
                                    color = if (full || partial) ClearrColors.BrandText else d.colors.dim
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
    if (d.members.isEmpty()) { EmptyState(d.colors); return }

    val pastMonths = (0..11).filter { !isFuture(d.selectedYear, it) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8),
        verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10)
    ) {
        item {
            val totalExpected = d.members.filter { !it.isArchived }.size * pastMonths.size * d.dueAmount
            val totalCollected = d.members.filter { !it.isArchived }
                .sumOf { m -> pastMonths.sumOf { mi -> d.paidForMonth(m.id, mi) } }
            Card(
                colors = CardDefaults.cardColors(containerColor = d.colors.card),
                shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LedgerStat("EXPECTED", formatAmount(totalExpected), d.colors.muted)
                    LedgerStat("COLLECTED", formatAmount(totalCollected), d.colors.green)
                    LedgerStat("BALANCE", formatAmount(totalExpected - totalCollected),
                        if (totalCollected < totalExpected) d.colors.red else d.colors.green)
                }
            }
        }

        items(d.members.filter { !it.isArchived }) { member ->
            Card(
                colors = CardDefaults.cardColors(containerColor = d.colors.card),
                shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14),
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(member) {
                        detectTapGestures(
                            onTap = { d.onMemberTap(member) },
                            onLongPress = { d.onMemberLongPress(member) }
                        )
                    }
            ) {
                Column(modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14)) {
                    Text(
                        privacyName(member.name, d.blurMemberNames),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = d.colors.text
                    )
                    Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Month", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelSmall, color = d.colors.muted)
                        Text("Due", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = d.colors.muted, textAlign = TextAlign.End)
                        Text("Paid", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = d.colors.muted, textAlign = TextAlign.End)
                        Text("Status", modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.labelSmall, color = d.colors.muted, textAlign = TextAlign.End)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4), color = d.colors.border)
                    pastMonths.forEach { mi ->
                        val paid = d.paidForMonth(member.id, mi)
                        val full = d.isFullPaid(member.id, mi)
                        val partial = d.isPartial(member.id, mi)
                        val rowBg = when {
                            full -> d.colors.green.copy(alpha = 0.08f)
                            partial -> d.colors.amber.copy(alpha = 0.08f)
                            else -> ClearrColors.Transparent
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6))
                                .background(rowBg)
                                .pointerInput(member.id, mi) {
                                    detectTapGestures(
                                        onTap = { d.onCellTap(member, mi) },
                                        onLongPress = { d.onCellLongPress(member, mi) }
                                    )
                                }
                                .padding(vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp5, horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(MONTHS[mi], modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodySmall, color = d.colors.text)
                            Text(formatAmount(d.dueAmount), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = d.colors.muted, textAlign = TextAlign.End)
                            Text(if (paid > 0) formatAmount(paid) else "—", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = if (paid > 0) d.colors.green else d.colors.dim, textAlign = TextAlign.End)
                            Text(
                                when { full -> "✓ PAID"; partial -> "½ PART"; else -> "UNPAID" },
                                modifier = Modifier.weight(0.8f),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp9,
                                fontWeight = FontWeight.Bold,
                                color = when { full -> d.colors.green; partial -> d.colors.amber; else -> d.colors.red },
                                textAlign = TextAlign.End
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4), color = d.colors.border)
                    val memberTotal = pastMonths.sumOf { d.paidForMonth(member.id, it) }
                    val memberDue = pastMonths.size * d.dueAmount
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = d.colors.text)
                        Text(
                            "${formatAmount(memberTotal)} / ${formatAmount(memberDue)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (memberTotal >= memberDue) d.colors.green else d.colors.red
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
private fun EmptyState(colors: DuesColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "No members yet.\nTap  +  to add one.",
            color = colors.muted,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

private fun privacyName(name: String, blurred: Boolean): String {
    if (!blurred) return name
    val length = name.count { !it.isWhitespace() }.coerceIn(4, 12)
    return "•".repeat(length)
}

@Composable
private fun LedgerStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = ClearrColors.TextSecondary)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun KanbanLayoutPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        KanbanLayout(
            d = TrackerLayoutData(
                members = emptyList(),
                selectedYear = 2026,
                currentYear = 2026,
                currentMonth = 1,
                dueAmount = 5000.0,
                isFullPaid = { _, _ -> false },
                isPartial = { _, _ -> false },
                paidForMonth = { _, _ -> 0.0 },
                onCellTap = { _, _ -> },
                onCellLongPress = { _, _ -> },
                onMemberTap = {},
                onMemberLongPress = {},
                blurMemberNames = false,
                colors = colors
            )
        )
    }
}
