package com.mikeisesele.clearr.ui.feature.trackerlist.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

/**
 * Stub dialog shown when the user taps "New Tracker" from within the list.
 * The actual tracker-creation flow is delegated to SetupWizardScreen via
 * [onNavigateToSetup]. This component exists as a future-ready anchor for
 * an inline creation sheet.
 */
@Composable
internal fun CreateTrackerDialog(
    onDismiss: () -> Unit,
    onNavigateToSetup: () -> Unit
) {
    val colors = LocalDuesColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
        title = {
            Text(
                "Create Tracker",
                color = colors.text,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)) {
                Text(
                    "Use the Setup Wizard to create a new tracker with a name, type, frequency, and members.",
                    color = colors.muted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onNavigateToSetup(); onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) {
                Text("Open Wizard")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.muted)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun CreateTrackerDialogPreview() {
    ClearrTheme {
        CreateTrackerDialog(onDismiss = {}, onNavigateToSetup = {})
    }
}
