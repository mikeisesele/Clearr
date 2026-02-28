package com.mikeisesele.clearr.ui.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
internal fun MemberContextDialog(
    member: Member,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onArchiveToggle: () -> Unit,
    onDelete: () -> Unit,
    colors: DuesColors = LocalDuesColors.current
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text(member.name, color = colors.text, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column {
                TextButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                    Text("Edit", color = colors.accent)
                }
                TextButton(onClick = onArchiveToggle, modifier = Modifier.fillMaxWidth()) {
                    Text(if (member.isArchived) "Restore" else "Archive", color = if (member.isArchived) colors.green else colors.red)
                }
                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                    Text("Delete", color = colors.red)
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.muted) } },
        shape = RoundedCornerShape(ClearrDimens.dp16)
    )
}

@Composable
internal fun DeleteMemberDialog(
    member: Member,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    colors: DuesColors = LocalDuesColors.current
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text("Delete ${member.name}?", color = colors.text) },
        text = { Text("This removes the member and all their payment history. This cannot be undone.", color = colors.muted) },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = colors.red)) {
                Text("Delete")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.muted) } },
        shape = RoundedCornerShape(ClearrDimens.dp16)
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun LayoutPickerSheet(
    selectedLayout: LayoutStyle,
    onDismiss: () -> Unit,
    onSelect: (LayoutStyle) -> Unit,
    colors: DuesColors = LocalDuesColors.current
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp8),
            verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)
        ) {
            Text("Choose Layout", style = MaterialTheme.typography.titleMedium, color = colors.text)
            listOf(
                LayoutStyle.GRID to "⊞ Grid",
                LayoutStyle.KANBAN to "🗂 Kanban",
                LayoutStyle.CARDS to "🃏 Cards",
                LayoutStyle.RECEIPT to "🧾 Receipt"
            ).forEach { (style, label) ->
                val selected = selectedLayout == style
                Surface(
                    color = if (selected) colors.accent.copy(alpha = 0.12f) else colors.card,
                    shape = RoundedCornerShape(ClearrDimens.dp12),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(style) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ClearrDimens.dp14, vertical = ClearrDimens.dp12),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, color = if (selected) colors.accent else colors.text)
                        if (selected) Text("✓", color = colors.accent)
                    }
                }
            }
            Spacer(Modifier.padding(bottom = ClearrDimens.dp12))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LayoutPickerSheetPreview() {
    ClearrTheme {
        LayoutPickerSheet(selectedLayout = LayoutStyle.GRID, onDismiss = {}, onSelect = {})
    }
}
