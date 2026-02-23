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
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDS
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.brandBackground
import com.mikeisesele.clearr.ui.theme.brandColor

// ── Type color palette ────────────────────────────────────────────────────────

internal data class TypeStyle(
    val icon: String,
    val color: Color,
    val bgColor: Color,
    val label: String
)

internal val typeStyles = mapOf(
    TrackerType.DUES to TypeStyle("₦", TrackerType.DUES.brandColor(), TrackerType.DUES.brandBackground(), "Dues"),
    TrackerType.GOALS to TypeStyle("🎯", TrackerType.GOALS.brandColor(), TrackerType.GOALS.brandBackground(), "Goals"),
    TrackerType.TODO to TypeStyle("☑", TrackerType.TODO.brandColor(), TrackerType.TODO.brandBackground(), "To-do"),
    TrackerType.BUDGET to TypeStyle("💳", TrackerType.BUDGET.brandColor(), TrackerType.BUDGET.brandBackground(), "Budget"),
    TrackerType.EXPENSES to TypeStyle("🧾", TrackerType.EXPENSES.brandColor(), TrackerType.EXPENSES.brandBackground(), "Expenses"),
)

internal val primaryColor = ClearrColors.BrandPrimary

// ─────────────────────────────────────────────────────────────────────────────
// TrackerCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun TrackerCard(
    summary: TrackerSummary,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val radii = ClearrDS.radii
    val spacing = ClearrDS.spacing
    val style = typeStyles[summary.type] ?: typeStyles[TrackerType.DUES]!!
    val allDone = summary.completedCount == summary.totalMembers && summary.totalMembers > 0
    val barColor = if (allDone) ClearrColors.BrandSecondary else style.color
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
        colors = CardDefaults.cardColors(containerColor = ClearrColors.Surface),
        shape = RoundedCornerShape(radii.lg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            // NEW badge border tint
            if (summary.isNew) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(radii.lg))
                        .border(2.dp, style.color, RoundedCornerShape(radii.lg))
                )
            }

            Column(modifier = Modifier.padding(start = spacing.xl - 2.dp, top = spacing.xl - 2.dp, end = spacing.lg, bottom = spacing.xl - 2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    // ── Type icon square ──────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(radii.md))
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
                            color = ClearrColors.BrandText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(spacing.xxs))
                        Text(
                            "${summary.frequency.displayName()}  ·  ${summary.currentPeriodLabel}",
                            fontSize = 12.sp,
                            color = ClearrColors.TextSecondary
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
                            color = if (allDone) ClearrColors.BrandSecondary else style.color,
                            trackColor = ClearrColors.Border,
                            strokeWidth = 4.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            "$pct%",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (allDone) ClearrColors.BrandSecondary else style.color
                        )
                    }
                }
            }

            // NEW badge
            if (summary.isNew) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(spacing.md - 2.dp)
                        .clip(RoundedCornerShape(radii.xl))
                        .background(style.color)
                        .padding(horizontal = spacing.sm - 2.dp, vertical = 1.dp)
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
