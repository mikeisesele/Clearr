package com.mikeisesele.clearr.ui.commons.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikeisesele.clearr.ui.commons.util.MONTHS
import com.mikeisesele.clearr.ui.commons.util.formatAmount
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

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
    val colors = LocalDuesColors.current
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val remaining = (dueAmount - alreadyPaid).coerceAtLeast(0.0)
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val amount = amountText.toDoubleOrNull()
    val isValid = amount != null && amount > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text("Partial Payment", color = colors.text, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)) {
                Text(
                    "${memberName} · ${MONTHS[monthIndex]} $year",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted
                )
                if (alreadyPaid > 0) {
                    Text(
                        "Already paid: ${formatAmount(alreadyPaid)} · Remaining: ${formatAmount(remaining)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.amber
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
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border,
                        focusedLabelColor = colors.accent,
                        unfocusedLabelColor = colors.muted,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        cursorColor = colors.accent
                    )
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border,
                        focusedLabelColor = colors.accent,
                        unfocusedLabelColor = colors.muted,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        cursorColor = colors.accent
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
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) { Text("Record") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.muted) }
        },
        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
    )
}

@Preview(showBackground = true)
@Composable
private fun PartialPaymentDialogPreview() {
    ClearrTheme {
        PartialPaymentDialog(
            memberName = "Henry Nwazuru",
            monthIndex = 1,
            year = 2026,
            alreadyPaid = 2500.0,
            dueAmount = 5000.0,
            onDismiss = {},
            onRecord = { _, _ -> }
        )
    }
}
