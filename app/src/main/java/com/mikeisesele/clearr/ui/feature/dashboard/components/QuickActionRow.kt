package com.mikeisesele.clearr.ui.feature.dashboard.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import kotlinx.coroutines.launch

@Composable
internal fun QuickActionRow(
    onLogSpend: () -> Unit,
    onMarkGoal: () -> Unit,
    onRecordDue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ClearrDimens.dp20, vertical = ClearrDimens.dp12),
        horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)
    ) {
        QuickActionButton(label = "Log spend", onClick = onLogSpend, modifier = Modifier.weight(1f))
        QuickActionButton(label = "Mark done", onClick = onMarkGoal, modifier = Modifier.weight(1f))
        QuickActionButton(label = "Record", onClick = onRecordDue, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDuesColors.current
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .scale(scale.value)
            .background(colors.card, RoundedCornerShape(ClearrDimens.dp16))
            .clickable {
                scope.launch {
                    scale.animateTo(0.94f, animationSpec = spring(stiffness = 600f))
                    scale.animateTo(1f, animationSpec = spring(stiffness = 420f))
                }
                onClick()
            }
            .padding(horizontal = ClearrDimens.dp14, vertical = ClearrDimens.dp12)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = colors.accent
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun QuickActionRowPreview() {
    ClearrTheme {
        QuickActionRow(onLogSpend = {}, onMarkGoal = {}, onRecordDue = {})
    }
}
