package com.mikeisesele.clearr.ui.feature.trackerlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
internal fun SummaryPill(
    trackerCount: Int,
    totalMembers: Int,
    avgCompletion: Int,
    C: DuesColors = LocalDuesColors.current
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PillItem(label = "Trackers", value = "$trackerCount", C = C, modifier = Modifier.weight(1f))
        PillItem(label = "Members", value = "$totalMembers", C = C, modifier = Modifier.weight(1f))
        PillItem(label = "Avg. Done", value = "$avgCompletion%", C = C, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PillItem(
    label: String,
    value: String,
    C: DuesColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(C.card)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = C.accent)
            Text(label, style = MaterialTheme.typography.labelSmall, color = C.muted)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryPillPreview() {
    ClearrTheme {
        val C = LocalDuesColors.current
        SummaryPill(trackerCount = 3, totalMembers = 24, avgCompletion = 67, C = C)
    }
}
