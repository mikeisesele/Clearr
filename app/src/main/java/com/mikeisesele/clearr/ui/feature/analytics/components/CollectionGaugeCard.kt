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
    C: DuesColors = LocalDuesColors.current
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = C.card),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Donut gauge
            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                val sweepAngle = (pct / 100f) * 360f
                Canvas(modifier = Modifier.size(90.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    drawArc(
                        color = C.border,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = C.accent,
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
                    color = C.text
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Collection Rate", style = MaterialTheme.typography.labelMedium, color = C.muted)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(C.green))
                    Text("Collected: ${formatAmount(totalCollected)}", style = MaterialTheme.typography.bodyMedium, color = C.green)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(C.red))
                    Text("Outstanding: ${formatAmount(outstanding)}", style = MaterialTheme.typography.bodyMedium, color = C.red)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CollectionGaugeCardPreview() {
    ClearrTheme {
        val C = LocalDuesColors.current
        CollectionGaugeCard(pct = 75, totalCollected = 45000.0, outstanding = 15000.0, C = C)
    }
}
