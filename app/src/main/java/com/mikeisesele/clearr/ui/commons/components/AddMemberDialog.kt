package com.mikeisesele.clearr.ui.commons.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String?) -> Unit
) {
    val C = LocalDuesColors.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = C.surface,
        title = {
            Text("Add Member", color = C.text, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = outlinedTextFieldColors(C)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone (e.g. 08012345678)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = outlinedTextFieldColors(C)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name.trim(), phone.trim().ifBlank { null })
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = C.accent)
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = C.muted) }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun outlinedTextFieldColors(C: DuesColors) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = C.accent,
        unfocusedBorderColor = C.border,
        focusedLabelColor = C.accent,
        unfocusedLabelColor = C.muted,
        focusedTextColor = C.text,
        unfocusedTextColor = C.text,
        cursorColor = C.accent
    )

@Preview(showBackground = true)
@Composable
private fun AddMemberDialogPreview() {
    ClearrTheme {
        AddMemberDialog(onDismiss = {}, onAdd = { _, _ -> })
    }
}
