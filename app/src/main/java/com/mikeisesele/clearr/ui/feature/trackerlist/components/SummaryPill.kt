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
    colors: DuesColors = LocalDuesColors.current
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8),
        horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)
    ) {
        PillItem(label = "Trackers", value = "$trackerCount", colors = colors, modifier = Modifier.weight(1f))
        PillItem(label = "Members", value = "$totalMembers", colors = colors, modifier = Modifier.weight(1f))
        PillItem(label = "Avg. Done", value = "$avgCompletion%", colors = colors, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PillItem(
    label: String,
    value: String,
    colors: DuesColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10))
            .background(colors.card)
            .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14, color = colors.accent)
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.muted)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryPillPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        SummaryPill(trackerCount = 3, totalMembers = 24, avgCompletion = 67, colors = colors)
    }
}
