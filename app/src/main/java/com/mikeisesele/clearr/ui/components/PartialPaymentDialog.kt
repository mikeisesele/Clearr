package com.mikeisesele.clearr.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import com.mikeisesele.clearr.ui.util.MONTHS
import com.mikeisesele.clearr.ui.util.formatAmount

@Composable
fun PartialPaymentDialog(
    memberName: String,
    monthIndex: Int,
    year: Int,
    alreadyPaid: Double,
    dueAmount: Double,
    onDismiss: () -> Unit,
    onRecord: (amount: Double, note: String?) -> Unit
) {
    val C = LocalDuesColors.current
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val remaining = (dueAmount - alreadyPaid).coerceAtLeast(0.0)
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val amount = amountText.toDoubleOrNull()
    val isValid = amount != null && amount > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = C.surface,
        title = {
            Text("Partial Payment", color = C.text, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "${memberName} · ${MONTHS[monthIndex]} $year",
                    style = MaterialTheme.typography.bodyMedium,
                    color = C.muted
                )
                if (alreadyPaid > 0) {
                    Text(
                        "Already paid: ${formatAmount(alreadyPaid)} · Remaining: ${formatAmount(remaining)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = C.amber
                    )
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₦)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = C.accent,
                        unfocusedBorderColor = C.border,
                        focusedLabelColor = C.accent,
                        unfocusedLabelColor = C.muted,
                        focusedTextColor = C.text,
                        unfocusedTextColor = C.text,
                        cursorColor = C.accent
                    )
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = C.accent,
                        unfocusedBorderColor = C.border,
                        focusedLabelColor = C.accent,
                        unfocusedLabelColor = C.muted,
                        focusedTextColor = C.text,
                        unfocusedTextColor = C.text,
                        cursorColor = C.accent
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onRecord(amount!!, note.trim().ifBlank { null })
                        onDismiss()
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = C.accent)
            ) { Text("Record") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = C.muted) }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
