package com.mikeisesele.clearr.ui.feature.todo.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun TodoActionsDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onMarkAllDone: () -> Unit,
    onClearCompleted: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("Mark all done") },
            onClick = {
                onDismiss()
                onMarkAllDone()
            }
        )
        DropdownMenuItem(
            text = { Text("Clear done") },
            onClick = {
                onDismiss()
                onClearCompleted()
            }
        )
    }
}
