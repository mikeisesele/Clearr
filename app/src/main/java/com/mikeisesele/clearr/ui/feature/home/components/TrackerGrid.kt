package com.mikeisesele.clearr.ui.feature.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.ui.commons.util.MONTHS
import com.mikeisesele.clearr.ui.commons.util.isFuture
import com.mikeisesele.clearr.ui.feature.home.TrackerLayoutData
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
internal fun TrackerGrid(d: TrackerLayoutData) {
    val colors = d.colors
    if (d.members.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No members yet.\nTap  +  to add one.",
                color = colors.muted,
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
                    .background(colors.surface)
            ) {
                Text(
                    "MEMBER",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.muted,
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp)
                )
            }
            d.members.forEachIndexed { idx, member ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cellSize + cellPad * 2)
                        .background(if (idx % 2 == 0) colors.bg else colors.surface.copy(alpha = 0.4f))
                        .border(BorderStroke(0.5.dp, colors.border.copy(alpha = 0.25f)))
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
                            color = if (member.isArchived) colors.muted else colors.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (member.isArchived) {
                            Text("archived", style = MaterialTheme.typography.labelSmall, color = colors.dim)
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
            Row(modifier = Modifier.background(colors.surface)) {
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
                                future -> colors.dim
                                current -> colors.accent
                                else -> colors.muted
                            }
                        )
                        if (current) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(colors.accent)
                            )
                        }
                    }
                }
            }

            d.members.forEachIndexed { idx, member ->
                Row(
                    modifier = Modifier
                        .background(if (idx % 2 == 0) colors.bg else colors.surface.copy(alpha = 0.4f))
                ) {
                    MONTHS.forEachIndexed { mi, _ ->
                        val future = isFuture(d.selectedYear, mi)
                        val full = !future && d.isFullPaid(member.id, mi)
                        val partial = !future && d.isPartial(member.id, mi)

                        val bgColor by animateColorAsState(
                            targetValue = when {
                                future -> colors.surface
                                full -> colors.green
                                partial -> colors.amber
                                else -> colors.card
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
                                future -> Text("—", color = colors.dim, fontSize = 12.sp)
                                full -> Text("✓", color = ClearrColors.BrandText, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                partial -> Text("½", color = ClearrColors.BrandText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 400)
@Composable
private fun TrackerGridPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        TrackerGrid(
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
                colors = colors
            )
        )
    }
}
