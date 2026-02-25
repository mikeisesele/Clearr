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
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
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
    TrackerType.DUES to TypeStyle("₦", TrackerType.DUES.brandColor(), TrackerType.DUES.brandBackground(), "Remittance"),
    TrackerType.EXPENSES to TypeStyle("₦", TrackerType.DUES.brandColor(), TrackerType.DUES.brandBackground(), "Remittance"),
    TrackerType.GOALS to TypeStyle("🎯", TrackerType.GOALS.brandColor(), TrackerType.GOALS.brandBackground(), "Goals"),
    TrackerType.TODO to TypeStyle("☑", TrackerType.TODO.brandColor(), TrackerType.TODO.brandBackground(), "To-do"),
    TrackerType.BUDGET to TypeStyle("💳", TrackerType.BUDGET.brandColor(), TrackerType.BUDGET.brandBackground(), "Budget"),
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
    val colors = LocalDuesColors.current
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
        colors = CardDefaults.cardColors(containerColor = colors.card),
        shape = RoundedCornerShape(radii.lg),
        elevation = CardDefaults.cardElevation(defaultElevation = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)
    ) {
        Box {
            // NEW badge border tint
            if (summary.isNew) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(radii.lg))
                        .border(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2, style.color, RoundedCornerShape(radii.lg))
                )
            }

            Column(modifier = Modifier.padding(start = spacing.xl - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2, top = spacing.xl - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2, end = spacing.lg, bottom = spacing.xl - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    // ── Type icon square ──────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp40)
                            .clip(RoundedCornerShape(radii.md))
                            .background(style.bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            style.icon,
                            color = style.color,
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp18,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // ── Name + meta + progress bar ────────────────────────────
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            summary.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15,
                            color = colors.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(spacing.xxs))
                        Text(
                            "${summary.frequency.displayName()}  ·  ${summary.currentPeriodLabel}",
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12,
                            color = colors.muted
                        )
                    }

                    // ── Progress ring ─────────────────────────────────────────
                    Box(
                        modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp44),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { pct / 100f },
                            modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp44),
                            color = animatedBarColor,
                            trackColor = colors.border,
                            strokeWidth = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            "$pct%",
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp8,
                            fontWeight = FontWeight.Bold,
                            color = animatedBarColor
                        )
                    }
                }
            }

            // NEW badge
            if (summary.isNew) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(spacing.md - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)
                        .clip(RoundedCornerShape(radii.xl))
                        .background(style.color)
                        .padding(horizontal = spacing.sm - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1)
                ) {
                    Text(
                        "NEW",
                        color = ClearrColors.Surface,
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp8,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp0_5
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
