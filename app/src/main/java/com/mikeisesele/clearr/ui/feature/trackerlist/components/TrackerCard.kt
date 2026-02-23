package com.mikeisesele.clearr.ui.feature.trackerlist.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.theme.ClearrTheme

// ── Type color palette ────────────────────────────────────────────────────────

internal data class TypeStyle(
    val icon: String,
    val color: Color,
    val bgColor: Color,
    val label: String
)

internal val typeStyles = mapOf(
    TrackerType.DUES       to TypeStyle("₦",  Color(0xFF6C63FF), Color(0xFFEEF0FF), "Dues"),
    TrackerType.ATTENDANCE to TypeStyle("✓",  Color(0xFF00A67E), Color(0xFFE6F7F3), "Attendance"),
    TrackerType.TASKS      to TypeStyle("⬡",  Color(0xFFF59E0B), Color(0xFFFEF3C7), "Tasks"),
    TrackerType.EVENTS     to TypeStyle("◈",  Color(0xFFEF4444), Color(0xFFFEE2E2), "Events"),
    TrackerType.CUSTOM     to TypeStyle("☰",  Color(0xFF6C63FF), Color(0xFFEEF0FF), "Custom"),
)

internal val primaryColor = Color(0xFF6C63FF)

// ─────────────────────────────────────────────────────────────────────────────
// TrackerCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun TrackerCard(
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
// Extension: human-readable frequency labels
// ─────────────────────────────────────────────────────────────────────────────

internal fun Frequency.displayName(): String = when (this) {
    Frequency.MONTHLY   -> "Monthly"
    Frequency.WEEKLY    -> "Weekly"
    Frequency.QUARTERLY -> "Quarterly"
    Frequency.TERMLY    -> "Termly"
    Frequency.BIANNUAL  -> "Biannual"
    Frequency.ANNUAL    -> "Annual"
    Frequency.CUSTOM    -> "Custom"
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun TrackerCardPreview() {
    ClearrTheme {
        TrackerCard(
            summary = TrackerSummary(
                trackerId = 1L,
                name = "JSS Monthly Dues",
                type = TrackerType.DUES,
                frequency = Frequency.MONTHLY,
                currentPeriodLabel = "February 2026",
                totalMembers = 12,
                completedCount = 9,
                completionPercent = 75,
                isNew = true,
                createdAt = System.currentTimeMillis()
            ),
            onClick = {},
            onLongPress = {}
        )
    }
}
