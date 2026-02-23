package com.mikeisesele.clearr.ui.feature.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikeisesele.clearr.ui.commons.util.formatAmount
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
internal fun CollectionGaugeCard(
    pct: Int,
    totalCollected: Double,
    outstanding: Double,
    colors: DuesColors = LocalDuesColors.current
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.card),
        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
        ) {
            // Donut gauge
            Box(
                modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp90),
                contentAlignment = Alignment.Center
            ) {
                val sweepAngle = (pct / 100f) * 360f
                Canvas(modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp90)) {
                    val strokeWidth = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14.toPx()
                    drawArc(
                        color = colors.border,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = colors.accent,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    "$pct%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.text
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6)) {
                Text("Collection Rate", style = MaterialTheme.typography.labelMedium, color = colors.muted)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6)) {
                    Box(modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10).clip(CircleShape).background(colors.green))
                    Text("Collected: ${formatAmount(totalCollected)}", style = MaterialTheme.typography.bodyMedium, color = colors.green)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6)) {
                    Box(modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10).clip(CircleShape).background(colors.red))
                    Text("Outstanding: ${formatAmount(outstanding)}", style = MaterialTheme.typography.bodyMedium, color = colors.red)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CollectionGaugeCardPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        CollectionGaugeCard(pct = 75, totalCollected = 45000.0, outstanding = 15000.0, colors = colors)
    }
}
