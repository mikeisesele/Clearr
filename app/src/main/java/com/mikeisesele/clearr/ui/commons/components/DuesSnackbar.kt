package com.mikeisesele.clearr.ui.commons.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import kotlinx.coroutines.delay

@Composable
fun DuesSnackbar(
    message: String?,
    onUndo: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val colors = LocalDuesColors.current

    LaunchedEffect(message) {
        if (message != null) {
            delay(5000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = message ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.text,
                    modifier = Modifier.weight(1f)
                )
                if (onUndo != null) {
                    TextButton(onClick = {
                        onUndo()
                        onDismiss()
                    }) {
                        Text("UNDO", color = colors.accent, style = MaterialTheme.typography.labelLarge)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("×", color = colors.muted, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DuesSnackbarPreview() {
    ClearrTheme {
        DuesSnackbar(
            message = "Payment recorded for Henry Nwazuru",
            onUndo = {},
            onDismiss = {}
        )
    }
}
