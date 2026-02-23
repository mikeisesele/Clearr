package com.mikeisesele.clearr.ui.feature.analytics.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikeisesele.clearr.ui.commons.util.formatAmount
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
internal fun YearSummarySection(
    currentYear: Int,
    currentCollected: Double,
    currentExpected: Double,
    currentPct: Int,
    activeMemberCount: Int,
    C: DuesColors = LocalDuesColors.current
) {
    Text(
        "Year Summary",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = C.text
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = C.card),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$currentYear", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = C.text)
                Text(
                    "$currentPct% rate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (currentPct >= 80) C.green else if (currentPct >= 50) C.amber else C.red
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Collected: ${formatAmount(currentCollected)}", style = MaterialTheme.typography.bodySmall, color = C.green)
                Text("Expected: ${formatAmount(currentExpected)}", style = MaterialTheme.typography.bodySmall, color = C.muted)
            }
            Text("Active members: $activeMemberCount", style = MaterialTheme.typography.bodySmall, color = C.muted)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun YearSummarySectionPreview() {
    ClearrTheme {
        val C = LocalDuesColors.current
        YearSummarySection(
            currentYear = 2026,
            currentCollected = 45000.0,
            currentExpected = 60000.0,
            currentPct = 75,
            activeMemberCount = 10,
            C = C
        )
    }
}
