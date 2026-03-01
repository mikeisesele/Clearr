package com.mikeisesele.clearr.ui.feature.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors

@Composable
internal fun PeriodContextBar(
    period: String,
    days: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalClearrUiColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ClearrDimens.dp20, vertical = ClearrDimens.dp12),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = period,
            style = MaterialTheme.typography.titleSmall,
            color = colors.text
        )
        Text(
            text = days,
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PeriodContextBarPreview() {
    ClearrTheme {
        PeriodContextBar(period = "Feb 2026", days = "3 days left")
    }
}
