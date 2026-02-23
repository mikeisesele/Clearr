package com.mikeisesele.clearr.ui.feature.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikeisesele.clearr.ui.commons.util.formatAmount
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
internal fun StatsRow(
    totalCollected: Double,
    totalExpected: Double,
    outstanding: Double,
    pct: Int,
    colors: DuesColors = LocalDuesColors.current
) {
    Column(modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10)) {
        Row(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10)) {
            listOf(
                Triple("COLLECTED", formatAmount(totalCollected), colors.green),
                Triple("EXPECTED", formatAmount(totalExpected), colors.text),
                Triple("OUTSTANDING", formatAmount(outstanding), if (outstanding > 0) colors.red else colors.green)
            ).forEach { (label, value, color) ->
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = colors.card),
                    shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)
                ) {
                    Column(modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10)) {
                        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.muted)
                        Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp3))
                        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = color)
                    }
                }
            }
        }
        Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
        LinearProgressIndicator(
            progress = { pct / 100f },
            modifier = Modifier.fillMaxWidth().height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp5).clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp3)),
            color = colors.green,
            trackColor = colors.border
        )
        Text(
            "$pct% collected",
            style = MaterialTheme.typography.labelSmall,
            color = colors.muted,
            modifier = Modifier.align(Alignment.End).padding(top = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp3)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsRowPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        StatsRow(
            totalCollected = 45000.0,
            totalExpected = 60000.0,
            outstanding = 15000.0,
            pct = 75,
            colors = colors
        )
    }
}
