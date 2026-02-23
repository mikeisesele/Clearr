package com.mikeisesele.clearr.ui.commons.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
fun EditMemberDialog(
    initialName: String,
    initialPhone: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String?) -> Unit
) {
    val colors = LocalDuesColors.current
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text("Edit Member", color = colors.text, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = outlinedTextFieldColors(colors)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = outlinedTextFieldColors(colors)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), phone.trim().ifBlank { null })
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.muted) }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun outlinedTextFieldColors(colors: DuesColors) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.accent,
        unfocusedBorderColor = colors.border,
        focusedLabelColor = colors.accent,
        unfocusedLabelColor = colors.muted,
        focusedTextColor = colors.text,
        unfocusedTextColor = colors.text,
        cursorColor = colors.accent
    )

@Preview(showBackground = true)
@Composable
private fun EditMemberDialogPreview() {
    ClearrTheme {
        EditMemberDialog(
            initialName = "Henry Nwazuru",
            initialPhone = "08012345678",
            onDismiss = {},
            onSave = { _, _ -> }
        )
    }
}
