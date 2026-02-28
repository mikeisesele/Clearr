package com.mikeisesele.clearr.ui.feature.dashboard.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
internal fun AllClearCard(
    hasTrackers: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalDuesColors.current
    Surface(
        color = if (hasTrackers) colors.green.copy(alpha = 0.12f) else colors.card,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(ClearrDimens.dp20),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(ClearrDimens.dp20)
        ) {
            if (hasTrackers) {
                Text(
                    text = "All clear this period 🎉",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.green,
                    textAlign = TextAlign.Center
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AmbientMorphBlob()
                    Spacer(Modifier.height(ClearrDimens.dp16))
                    Text(
                        text = "Set up your first tracker to see your score.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.text,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AllClearCardPreview() {
    ClearrTheme {
        AllClearCard(hasTrackers = true, modifier = Modifier.padding(ClearrDimens.dp20))
    }
}
