package com.mikeisesele.clearr.ui.feature.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerType
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardUiModel
import com.mikeisesele.clearr.ui.feature.dashboard.utils.previewDashboardUi
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
internal fun UrgencyHeader(
    modifier: Modifier = Modifier
) {
    val colors = LocalDuesColors.current
    Text(
        text = "Needs attention",
        style = MaterialTheme.typography.labelSmall,
        color = colors.muted,
        modifier = modifier
    )
}

@Composable
internal fun UrgencyStrip(
    state: DashboardUiModel,
    onDismissUrgency: (String) -> Unit,
    onQuickAction: (DashboardTrackerType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp10)
    ) {
        state.urgencyItems.forEach { item ->
            UrgencyCard(
                item = item,
                onDismiss = { onDismissUrgency(item.id) },
                onQuickAction = onQuickAction
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 420)
@Composable
private fun UrgencyStripPreview() {
    ClearrTheme {
        UrgencyStrip(
            state = previewDashboardUi,
            onDismissUrgency = {},
            onQuickAction = {},
            modifier = Modifier.padding(ClearrDimens.dp20)
        )
    }
}
