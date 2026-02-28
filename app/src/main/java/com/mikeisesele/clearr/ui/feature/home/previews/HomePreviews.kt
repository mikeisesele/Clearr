package com.mikeisesele.clearr.ui.feature.home.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mikeisesele.clearr.ui.feature.home.components.StatsRow
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Preview(showBackground = true)
@Composable
private fun HomeStatsPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {
            StatsRow(totalCollected = 45000.0, totalExpected = 60000.0, outstanding = 15000.0, pct = 75, colors = colors)
        }
    }
}
