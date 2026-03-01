## `app/src/main/java/com/mikeisesele/clearr/ui/commons/components/AddMemberDialog.kt`

```kotlin
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
    val colors = LocalDuesColors.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text("Add Member", color = colors.text, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = outlinedTextFieldColors(colors)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone (e.g. 08012345678)") },
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
                        onAdd(name.trim(), phone.trim().ifBlank { null })
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.muted) }
        },
        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
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
private fun AddMemberDialogPreview() {
    ClearrTheme {
        AddMemberDialog(onDismiss = {}, onAdd = { _, _ -> })
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/commons/components/DuesSnackbar.kt`

```kotlin
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
                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8),
            shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            elevation = CardDefaults.cardElevation(defaultElevation = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)
        ) {
            Row(
                modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)
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
```

## `app/src/main/java/com/mikeisesele/clearr/ui/commons/components/EditMemberDialog.kt`

```kotlin
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
            Column(verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)) {
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
        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
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
```

## `app/src/main/java/com/mikeisesele/clearr/ui/commons/components/MemberDetailSheet.kt`

```kotlin
package com.mikeisesele.clearr.ui.commons.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.data.model.PaymentRecord
import com.mikeisesele.clearr.ui.commons.util.MONTHS
import com.mikeisesele.clearr.ui.commons.util.buildWhatsAppLink
import com.mikeisesele.clearr.ui.commons.util.currentMonth
import com.mikeisesele.clearr.ui.commons.util.formatAmount
import com.mikeisesele.clearr.ui.commons.util.formatTimestamp
import com.mikeisesele.clearr.ui.commons.util.isFuture
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.WhatsAppGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailSheet(
    member: Member,
    payments: List<PaymentRecord>,
    dueAmount: Double,
    selectedYear: Int,
    showBulkMarkPaid: Boolean = false,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onArchiveToggle: () -> Unit,
    onDelete: () -> Unit,
    onBulkMarkPaid: (() -> Unit)? = null
) {
    val colors = LocalDuesColors.current
    val context = LocalContext.current
    val currentMonth = currentMonth()

    val activePayments = payments.filter { !it.isUndone }
    val nonFutureMonths = (0..11).filter { !isFuture(selectedYear, it) }

    val totalPaidPerMonth = (0..11).associate { mi ->
        mi to activePayments.filter { it.monthIndex == mi }.sumOf { it.amountPaid }
    }

    val totalPaid = totalPaidPerMonth.values.sum()
    val outstanding = nonFutureMonths.sumOf { mi ->
        maxOf(0.0, dueAmount - (totalPaidPerMonth[mi] ?: 0.0))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.muted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20)
                .padding(bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp32)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        member.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    if (!member.phone.isNullOrBlank()) {
                        Text(member.phone, style = MaterialTheme.typography.bodyMedium, color = colors.muted)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)) {
                    TextButton(onClick = onEdit) {
                        Text("Edit", color = colors.accent)
                    }
                    if (showBulkMarkPaid && onBulkMarkPaid != null) {
                        TextButton(onClick = onBulkMarkPaid) {
                            Text("Mark Outstanding Paid", color = colors.green)
                        }
                    }
                    TextButton(onClick = onArchiveToggle) {
                        Text(
                            if (member.isArchived) "Restore" else "Archive",
                            color = if (member.isArchived) colors.green else colors.red
                        )
                    }
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = colors.red)
                    }
                }
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "OUTSTANDING $selectedYear",
                    value = formatAmount(outstanding),
                    valueColor = if (outstanding > 0) colors.red else colors.green,
                    colors = colors
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "PAID $selectedYear",
                    value = formatAmount(totalPaid),
                    valueColor = colors.green,
                    colors = colors
                )
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16))

            // WhatsApp button
            if (!member.phone.isNullOrBlank()) {
                Button(
                    onClick = {
                        val link = buildWhatsAppLink(
                            member.phone,
                            member.name,
                            MONTHS[currentMonth],
                            selectedYear,
                            formatAmount(dueAmount)
                        )
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                ) {
                    Text("WhatsApp ${member.name}", color = ClearrColors.Surface)
                }
                Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16))
            }

            // Payment history
            Text(
                "Payment History — $selectedYear",
                style = MaterialTheme.typography.titleMedium,
                color = colors.text,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))

            val historyEntries = activePayments
                .sortedWith(compareBy({ it.monthIndex }, { it.paidAt }))

            if (historyEntries.isEmpty()) {
                Text("No payments recorded for $selectedYear.", color = colors.muted, style = MaterialTheme.typography.bodyMedium)
            } else {
                historyEntries.forEach { payment ->
                    HorizontalDivider(color = colors.border)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "${MONTHS[payment.monthIndex]} $selectedYear",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.text,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                formatTimestamp(payment.paidAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.muted
                            )
                            if (!payment.note.isNullOrBlank()) {
                                Text(
                                    payment.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.muted
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                formatAmount(payment.amountPaid),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (payment.amountPaid >= payment.expectedAmount) colors.green else colors.amber
                            )
                            if (payment.amountPaid < payment.expectedAmount) {
                                Text("partial", style = MaterialTheme.typography.labelSmall, color = colors.amber)
                            }
                        }
                    }
                }
                HorizontalDivider(color = colors.border)
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16))
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    label: String,
    value: String,
    valueColor: Color,
    colors: DuesColors
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.card),
        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)
    ) {
        Column(modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.muted)
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatCardPreview() {
    ClearrTheme {
        val colors = com.mikeisesele.clearr.ui.theme.LocalDuesColors.current
        Row(modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16), horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "OUTSTANDING 2026",
                value = "₦15,000",
                valueColor = colors.red,
                colors = colors
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "PAID 2026",
                value = "₦45,000",
                valueColor = colors.green,
                colors = colors
            )
        }
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/commons/components/PartialPaymentDialog.kt`

```kotlin
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
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/home/HomeScreen.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.ui.commons.components.AddMemberDialog
import com.mikeisesele.clearr.ui.commons.components.ConfettiOverlay
import com.mikeisesele.clearr.ui.commons.components.DuesSnackbar
import com.mikeisesele.clearr.ui.commons.components.EditMemberDialog
import com.mikeisesele.clearr.ui.commons.components.MemberDetailSheet
import com.mikeisesele.clearr.ui.commons.components.PartialPaymentDialog
import com.mikeisesele.clearr.ui.commons.util.currentMonth
import com.mikeisesele.clearr.ui.commons.util.currentYear
import com.mikeisesele.clearr.ui.commons.util.isFuture
import com.mikeisesele.clearr.ui.feature.home.components.DeleteMemberDialog
import com.mikeisesele.clearr.ui.feature.home.components.LayoutPickerSheet
import com.mikeisesele.clearr.ui.feature.home.components.MemberContextDialog
import com.mikeisesele.clearr.ui.feature.home.components.StatsRow
import com.mikeisesele.clearr.ui.feature.home.components.TrackerGrid
import com.mikeisesele.clearr.ui.feature.home.utils.shareHomeScreenshot
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    /** The tracker being displayed (-1 = legacy / no tracker ID). */
    trackerId: Long = -1L,
    /** Called when user presses back. Null = no back button shown. */
    onBack: (() -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalDuesColors.current
    val context = LocalContext.current
    val view = LocalView.current

    val currentYear = currentYear()
    val currentMonth = currentMonth()
    val dueAmount = state.yearConfig?.dueAmountPerMonth ?: 5000.0

    LaunchedEffect(trackerId) {
        viewModel.onAction(HomeAction.SetCurrentTrackerId(trackerId.takeIf { it > 0 }))
    }

    val visibleMembers = remember(state.members, state.showArchived) {
        state.members.filter { state.showArchived || !it.isArchived }
    }
    val activeMembers = remember(state.members) { state.members.filter { !it.isArchived } }

    val paymentMap = remember(state.payments) {
        state.payments
            .filter { !it.isUndone }
            .groupBy { "${it.memberId}-${it.monthIndex}" }
            .mapValues { (_, r) -> r.sumOf { it.amountPaid } }
    }

    fun paidForMonth(memberId: Long, mi: Int) = paymentMap["$memberId-$mi"] ?: 0.0
    fun isFullPaid(memberId: Long, mi: Int) = paidForMonth(memberId, mi) >= dueAmount
    fun isPartial(memberId: Long, mi: Int): Boolean {
        val p = paidForMonth(memberId, mi); return p > 0 && p < dueAmount
    }

    val nonFutureMonths = (0..11).filter { !isFuture(state.selectedYear, it) }
    val totalCollected = activeMembers.sumOf { m -> nonFutureMonths.sumOf { mi -> paidForMonth(m.id, mi) } }
    val totalExpected = activeMembers.size * nonFutureMonths.size * dueAmount
    val outstanding = (totalExpected - totalCollected).coerceAtLeast(0.0)
    val pct = if (totalExpected > 0) (totalCollected / totalExpected * 100).toInt().coerceIn(0, 100) else 0

    var showAddMember by remember { mutableStateOf(false) }
    var partialTarget by remember { mutableStateOf<Pair<Member, Int>?>(null) }
    var memberDetail by remember { mutableStateOf<Member?>(null) }
    var editTarget by remember { mutableStateOf<Member?>(null) }
    var contextTarget by remember { mutableStateOf<Member?>(null) }
    var deleteTarget by remember { mutableStateOf<Member?>(null) }
    var showLayoutSheet by remember { mutableStateOf(false) }
    var blurMemberNames by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.confettiMonth) {
        if (state.confettiMonth != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.onAction(HomeAction.DismissConfetti)
        }
    }

    val layoutData = TrackerLayoutData(
        members = visibleMembers,
        selectedYear = state.selectedYear,
        currentYear = currentYear,
        currentMonth = currentMonth,
        dueAmount = dueAmount,
        isFullPaid = ::isFullPaid,
        isPartial = ::isPartial,
        paidForMonth = ::paidForMonth,
        onCellTap = { m, mi -> viewModel.onAction(HomeAction.TogglePayment(m, state.selectedYear, mi, dueAmount)) },
        onCellLongPress = { m, mi -> partialTarget = m to mi },
        onMemberTap = { memberDetail = it },
        onMemberLongPress = { contextTarget = it },
        blurMemberNames = blurMemberNames,
        colors = colors
    )

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ──────────────────────────────────────────────────────
            com.mikeisesele.clearr.ui.feature.home.components.HomeTopBar(
                trackerName = state.trackerName,
                layoutStyle = state.layoutStyle,
                selectedYear = state.selectedYear,
                dueAmount = dueAmount,
                showBlurToggle = state.trackerType == com.mikeisesele.clearr.data.model.TrackerType.DUES ||
                    state.trackerType == com.mikeisesele.clearr.data.model.TrackerType.EXPENSES,
                blurMemberNames = blurMemberNames,
                onBlurToggle = { blurMemberNames = !blurMemberNames },
                onBack = onBack,
                onLayoutClick = { showLayoutSheet = true },
                onShareClick = {
                    shareHomeScreenshot(
                        context,
                        view,
                        state.trackerType == com.mikeisesele.clearr.data.model.TrackerType.DUES ||
                            state.trackerType == com.mikeisesele.clearr.data.model.TrackerType.EXPENSES
                    )
                },
                colors = colors
            )

            // ── Stats ─────────────────────────────────────────────────────────
            state.aiRiskHint?.let { risk ->
                Text(
                    text = risk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6),
                    color = colors.muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            StatsRow(
                totalCollected = totalCollected,
                totalExpected = totalExpected,
                outstanding = outstanding,
                pct = pct,
                colors = colors
            )

            // ── Layout area ───────────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = state.layoutStyle,
                    transitionSpec = {
                        (fadeIn(tween(240)) + slideInVertically(tween(240)) { it / 12 })
                            .togetherWith(fadeOut(tween(180)) + slideOutVertically(tween(180)) { -it / 12 })
                    },
                    label = "layout_switch"
                ) { layout ->
                    when (layout) {
                        LayoutStyle.GRID -> TrackerGrid(layoutData)
                        LayoutStyle.KANBAN -> KanbanLayout(layoutData)
                        LayoutStyle.CARDS -> CardsLayout(layoutData)
                        LayoutStyle.RECEIPT -> ReceiptLayout(layoutData)
                    }
                }
            }
        }

        // ── Small FAB ────────────────────────────────────────────────────────
        SmallFloatingActionButton(
            onClick = { showAddMember = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20, bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20),
            containerColor = colors.accent,
            contentColor = ClearrColors.Surface,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4, com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add member", modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20))
        }

        // ── Snackbar ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp80)
        ) {
            state.snackbarMessage?.let { snack ->
                DuesSnackbar(
                    message = snack.message,
                    onUndo = snack.undoPaymentId?.let { pid ->
                        {
                            viewModel.onAction(HomeAction.UndoLastRemoval(
                                pid,
                                snack.undoMemberId ?: 0,
                                snack.undoYear ?: state.selectedYear,
                                snack.undoMonthIndex ?: 0,
                                dueAmount
                            ))
                        }
                    },
                    onDismiss = { viewModel.onAction(HomeAction.DismissSnackbar) }
                )
            }
        }

        ConfettiOverlay(show = state.confettiMonth != null)
    }

    // ── Dialogs / sheets ─────────────────────────────────────────────────────
    if (showAddMember) {
        AddMemberDialog(
            onDismiss = { showAddMember = false },
            onAdd = { name, phone -> viewModel.onAction(HomeAction.AddMember(name, phone)) }
        )
    }

    partialTarget?.let { (member, mi) ->
        PartialPaymentDialog(
            memberName = member.name,
            monthIndex = mi,
            year = state.selectedYear,
            alreadyPaid = paidForMonth(member.id, mi),
            dueAmount = dueAmount,
            onDismiss = { partialTarget = null },
            onRecord = { amount, note ->
                viewModel.onAction(HomeAction.RecordPartialPayment(member.id, state.selectedYear, mi, amount, note, dueAmount))
            }
        )
    }

    memberDetail?.let { member ->
        MemberDetailSheet(
            member = member,
            payments = state.payments.filter { it.memberId == member.id },
            dueAmount = dueAmount,
            selectedYear = state.selectedYear,
            showBulkMarkPaid = state.trackerType == com.mikeisesele.clearr.data.model.TrackerType.DUES ||
                state.trackerType == com.mikeisesele.clearr.data.model.TrackerType.EXPENSES,
            onDismiss = { memberDetail = null },
            onEdit = { editTarget = member; memberDetail = null },
            onArchiveToggle = {
                viewModel.onAction(HomeAction.SetMemberArchived(member.id, !member.isArchived))
                memberDetail = null
            },
            onDelete = {
                deleteTarget = member
                memberDetail = null
            },
            onBulkMarkPaid = {
                viewModel.onAction(HomeAction.MarkOutstandingMonthsPaid(
                    memberId = member.id,
                    year = state.selectedYear,
                    dueAmount = dueAmount,
                    trackerIdOverride = trackerId.takeIf { it > 0 }
                ))
                memberDetail = null
            }
        )
    }

    editTarget?.let { member ->
        EditMemberDialog(
            initialName = member.name,
            initialPhone = member.phone,
            onDismiss = { editTarget = null },
            onSave = { name, phone -> viewModel.onAction(HomeAction.UpdateMember(member.copy(name = name, phone = phone))) }
        )
    }

    contextTarget?.let { member ->
        MemberContextDialog(
            member = member,
            onDismiss = { contextTarget = null },
            onEdit = { editTarget = member; contextTarget = null },
            onArchiveToggle = {
                viewModel.onAction(HomeAction.SetMemberArchived(member.id, !member.isArchived))
                contextTarget = null
            },
            onDelete = { deleteTarget = member; contextTarget = null }
        )
    }

    deleteTarget?.let { member ->
        DeleteMemberDialog(
            member = member,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.onAction(HomeAction.DeleteMember(member.id, trackerId.takeIf { it > 0 }))
                deleteTarget = null
            }
        )
    }

    if (showLayoutSheet) {
        LayoutPickerSheet(
            selectedLayout = state.layoutStyle,
            onDismiss = { showLayoutSheet = false },
            onSelect = { style ->
                viewModel.onAction(HomeAction.SetLayoutStyleForCurrentTracker(style))
                showLayoutSheet = false
            }
        )
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/home/HomeState.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.home

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.data.model.PaymentRecord
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.data.model.YearConfig

data class HomeUiState(
    val selectedYear: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
    val members: List<Member> = emptyList(),
    val payments: List<PaymentRecord> = emptyList(),
    val yearConfig: YearConfig? = null,
    val showArchived: Boolean = false,
    val confettiMonth: Int? = null,
    val snackbarMessage: SnackbarData? = null,
    val layoutStyle: LayoutStyle = LayoutStyle.GRID,
    val trackerName: String = "Dues Tracker",
    val trackerType: TrackerType = TrackerType.DUES,
    val currentPeriodId: Long? = null,
    val aiRiskHint: String? = null
) : BaseState

data class SnackbarData(
    val message: String,
    val undoPaymentId: Long? = null,
    val undoMemberId: Long? = null,
    val undoYear: Int? = null,
    val undoMonthIndex: Int? = null
)

sealed interface HomeAction {
    data class SetShowArchived(val show: Boolean) : HomeAction
    data class TogglePayment(
        val member: Member,
        val year: Int,
        val monthIndex: Int,
        val dueAmount: Double
    ) : HomeAction
    data class RecordPartialPayment(
        val memberId: Long,
        val year: Int,
        val monthIndex: Int,
        val amount: Double,
        val note: String?,
        val dueAmount: Double
    ) : HomeAction
    data object DismissConfetti : HomeAction
    data class UndoLastRemoval(
        val paymentId: Long,
        val memberId: Long,
        val year: Int,
        val monthIndex: Int,
        val dueAmount: Double
    ) : HomeAction
    data object DismissSnackbar : HomeAction
    data class AddMember(val name: String, val phone: String?) : HomeAction
    data class UpdateMember(val member: Member) : HomeAction
    data class SetMemberArchived(val id: Long, val archived: Boolean) : HomeAction
    data class DeleteMember(val id: Long, val trackerIdOverride: Long? = null) : HomeAction
    data class SetCurrentTrackerId(val trackerId: Long?) : HomeAction
    data class SetLayoutStyleForCurrentTracker(val style: LayoutStyle) : HomeAction
    data class MarkOutstandingMonthsPaid(
        val memberId: Long,
        val year: Int,
        val dueAmount: Double,
        val trackerIdOverride: Long? = null
    ) : HomeAction
}

sealed interface HomeEvent : ViewEvent
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/home/HomeViewModel.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.home

import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.data.model.PaymentRecord
import com.mikeisesele.clearr.data.model.RecordStatus
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.data.model.TrackerPeriod
import com.mikeisesele.clearr.data.model.TrackerRecord
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.data.model.YearConfig
import com.mikeisesele.clearr.di.AppStateHolder
import com.mikeisesele.clearr.domain.repository.DuesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DuesRepository,
    private val appState: AppStateHolder
) : BaseViewModel<HomeUiState, HomeAction, HomeEvent>(
    initialState = HomeUiState()
) {

    private val showArchivedFlow = MutableStateFlow(false)
    private val confettiMonthFlow = MutableStateFlow<Int?>(null)
    private val snackbarFlow = MutableStateFlow<SnackbarData?>(null)

    private data class UiParams(
        val year: Int,
        val showArchived: Boolean,
        val confettiMonth: Int?,
        val snackbar: SnackbarData?,
        val appConfig: AppConfig?,
        val trackerId: Long?
    )

    init {
        launch {
            combine(
                appState.selectedYear,
                showArchivedFlow,
                confettiMonthFlow,
                snackbarFlow,
                appState.appConfig,
                appState.currentTrackerId
            ) { arr ->
                @Suppress("UNCHECKED_CAST")
                UiParams(
                    year = arr[0] as Int,
                    showArchived = arr[1] as Boolean,
                    confettiMonth = arr[2] as? Int,
                    snackbar = arr[3] as? SnackbarData,
                    appConfig = arr[4] as AppConfig?,
                    trackerId = arr[5] as Long?
                )
            }.flatMapLatest { p ->
                val trackerId = p.trackerId
                if (trackerId != null) {
                    val trackerFlow = repository.getTrackerByIdFlow(trackerId)
                    val membersFlow = repository.getAllMembersForTracker(trackerId)
                    val periodsFlow = repository.getPeriodsForTracker(trackerId)
                    val recordsFlow = repository.getRecordsForTracker(trackerId)

                    combine(trackerFlow, membersFlow, periodsFlow, recordsFlow) { tracker, members, periods, records ->
                        val mappedMembers = members.map { it.asUiMember() }
                        val periodById = periods.associateBy { it.id }
                        val mappedPayments = records.mapNotNull { rec ->
                            val period = periodById[rec.periodId] ?: return@mapNotNull null
                            val monthIndex = monthIndexFromLabel(period.label, p.year) ?: return@mapNotNull null
                            PaymentRecord(
                                memberId = rec.memberId,
                                year = p.year,
                                monthIndex = monthIndex,
                                amountPaid = rec.amountPaid,
                                expectedAmount = tracker?.defaultAmount ?: 5000.0,
                                paidAt = rec.updatedAt,
                                note = rec.note
                            )
                        }
                        val syntheticYearConfig = YearConfig(
                            year = p.year,
                            dueAmountPerMonth = tracker?.defaultAmount ?: 5000.0
                        )
                        val expectedMonths = 12
                        val riskHint = mappedMembers.asSequence()
                            .mapNotNull { member ->
                                val paidMonths = (0..11).count { mi ->
                                    mappedPayments.any { it.memberId == member.id && it.monthIndex == mi && it.amountPaid >= (tracker?.defaultAmount ?: 5000.0) }
                                }
                                ClearrEdgeAi.remittanceRiskLabel(member.name, paidMonths, expectedMonths)
                            }
                            .firstOrNull()
                        HomeUiState(
                            selectedYear = p.year,
                            members = mappedMembers,
                            payments = mappedPayments,
                            yearConfig = syntheticYearConfig,
                            showArchived = p.showArchived,
                            confettiMonth = p.confettiMonth,
                            snackbarMessage = p.snackbar,
                            layoutStyle = tracker?.layoutStyle ?: p.appConfig?.layoutStyle ?: LayoutStyle.GRID,
                            trackerName = tracker?.name ?: "Tracker",
                            trackerType = tracker?.type ?: TrackerType.DUES,
                            currentPeriodId = periods.firstOrNull { it.isCurrent }?.id,
                            aiRiskHint = if (tracker?.type == TrackerType.DUES || tracker?.type == TrackerType.EXPENSES) riskHint else null
                        )
                    }
                } else {
                    combine(
                        repository.getAllMembers(),
                        repository.getPaymentsForYear(p.year),
                        repository.getYearConfigFlow(p.year)
                    ) { members, payments, config ->
                        val expectedMonths = 12
                        val riskHint = members.asSequence()
                            .mapNotNull { member ->
                                val paidMonths = (0..11).count { mi ->
                                    payments.any { it.memberId == member.id && it.monthIndex == mi && it.amountPaid >= (config?.dueAmountPerMonth ?: 5000.0) }
                                }
                                ClearrEdgeAi.remittanceRiskLabel(member.name, paidMonths, expectedMonths)
                            }
                            .firstOrNull()
                        HomeUiState(
                            selectedYear = p.year,
                            members = members,
                            payments = payments,
                            yearConfig = config,
                            showArchived = p.showArchived,
                            confettiMonth = p.confettiMonth,
                            snackbarMessage = p.snackbar,
                            layoutStyle = p.appConfig?.layoutStyle ?: LayoutStyle.GRID,
                            trackerName = "Dues Tracker",
                            trackerType = TrackerType.DUES,
                            aiRiskHint = riskHint
                        )
                    }
                }
            }.collectLatest { newState -> updateState { newState } }
        }

        launch {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            repository.ensureYearConfig(currentYear)
        }
    }

    override fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.SetShowArchived -> setShowArchived(action.show)
            is HomeAction.TogglePayment -> togglePayment(action.member, action.year, action.monthIndex, action.dueAmount)
            is HomeAction.RecordPartialPayment -> recordPartialPayment(action.memberId, action.year, action.monthIndex, action.amount, action.note, action.dueAmount)
            HomeAction.DismissConfetti -> dismissConfetti()
            is HomeAction.UndoLastRemoval -> undoLastRemoval(action.paymentId, action.memberId, action.year, action.monthIndex, action.dueAmount)
            HomeAction.DismissSnackbar -> dismissSnackbar()
            is HomeAction.AddMember -> addMember(action.name, action.phone)
            is HomeAction.UpdateMember -> updateMember(action.member)
            is HomeAction.SetMemberArchived -> setMemberArchived(action.id, action.archived)
            is HomeAction.DeleteMember -> deleteMember(action.id, action.trackerIdOverride)
            is HomeAction.SetCurrentTrackerId -> setCurrentTrackerId(action.trackerId)
            is HomeAction.SetLayoutStyleForCurrentTracker -> setLayoutStyleForCurrentTracker(action.style)
            is HomeAction.MarkOutstandingMonthsPaid -> markOutstandingMonthsPaid(action.memberId, action.year, action.dueAmount, action.trackerIdOverride)
        }
    }

    private fun setShowArchived(show: Boolean) { showArchivedFlow.value = show }

    private fun togglePayment(member: Member, year: Int, monthIndex: Int, dueAmount: Double) {
        launch {
            val trackerId = appState.currentTrackerId.value
            if (trackerId == null) {
                val totalPaid = repository.getTotalPaidForMonth(member.id, year, monthIndex)
                if (totalPaid >= dueAmount) {
                    val latest = repository.getLatestPayment(member.id, year, monthIndex)
                    if (latest != null) {
                        repository.undoPayment(latest.id)
                        snackbarFlow.value = SnackbarData(
                            message = "Payment removed for ${member.name}",
                            undoPaymentId = latest.id,
                            undoMemberId = member.id,
                            undoYear = year,
                            undoMonthIndex = monthIndex
                        )
                    }
                } else {
                    repository.insertPayment(
                        PaymentRecord(
                            memberId = member.id,
                            year = year,
                            monthIndex = monthIndex,
                            amountPaid = dueAmount - totalPaid,
                            expectedAmount = dueAmount,
                            paidAt = System.currentTimeMillis()
                        )
                    )
                    checkAndTriggerConfetti(year, monthIndex, dueAmount)
                }
                return@launch
            }

            val tracker = repository.getTrackerById(trackerId) ?: return@launch
            val period = if (tracker.type == TrackerType.DUES || tracker.type == TrackerType.EXPENSES) {
                ensureMonthlyPeriod(trackerId, year, monthIndex)
            } else {
                repository.getCurrentPeriod(trackerId) ?: return@launch
            }
            val existing = repository.getRecord(trackerId, period.id, member.id)
            val now = System.currentTimeMillis()
            val updated = when (tracker.type) {
                TrackerType.DUES -> {
                    val currentlyCompleted = existing?.status == RecordStatus.PAID
                    val newStatus = if (currentlyCompleted) RecordStatus.UNPAID else RecordStatus.PAID
                    val newAmount = if (newStatus == RecordStatus.PAID) tracker.defaultAmount else 0.0
                    (existing ?: TrackerRecord(
                        trackerId = trackerId,
                        periodId = period.id,
                        memberId = member.id
                    )).copy(status = newStatus, amountPaid = newAmount, updatedAt = now)
                }
                TrackerType.EXPENSES -> {
                    val currentlyCompleted = existing?.status == RecordStatus.PAID
                    val newStatus = if (currentlyCompleted) RecordStatus.UNPAID else RecordStatus.PAID
                    val newAmount = if (newStatus == RecordStatus.PAID) tracker.defaultAmount else 0.0
                    (existing ?: TrackerRecord(
                        trackerId = trackerId,
                        periodId = period.id,
                        memberId = member.id
                    )).copy(status = newStatus, amountPaid = newAmount, updatedAt = now)
                }
                TrackerType.GOALS, TrackerType.TODO -> {
                    val next = if (existing?.status == RecordStatus.DONE) RecordStatus.PENDING else RecordStatus.DONE
                    (existing ?: TrackerRecord(
                        trackerId = trackerId,
                        periodId = period.id,
                        memberId = member.id
                    )).copy(status = next, amountPaid = 0.0, updatedAt = now)
                }
                TrackerType.BUDGET -> {
                    val next = if (existing?.status == RecordStatus.PAID) RecordStatus.UNPAID else RecordStatus.PAID
                    (existing ?: TrackerRecord(
                        trackerId = trackerId,
                        periodId = period.id,
                        memberId = member.id
                    )).copy(status = next, amountPaid = 0.0, updatedAt = now)
                }
            }
            if (existing == null) repository.insertRecord(updated) else repository.updateRecord(updated)
            checkAndTriggerTrackerConfetti(trackerId, period.id, tracker.type)
        }
    }

    private fun recordPartialPayment(memberId: Long, year: Int, monthIndex: Int, amount: Double, note: String?, dueAmount: Double) {
        launch {
            val trackerId = appState.currentTrackerId.value
            if (trackerId == null) {
                repository.insertPayment(
                    PaymentRecord(
                        memberId = memberId,
                        year = year,
                        monthIndex = monthIndex,
                        amountPaid = amount,
                        expectedAmount = dueAmount,
                        paidAt = System.currentTimeMillis(),
                        note = note
                    )
                )
                checkAndTriggerConfetti(year, monthIndex, dueAmount)
                return@launch
            }
            val tracker = repository.getTrackerById(trackerId) ?: return@launch
            val period = if (tracker.type == TrackerType.DUES || tracker.type == TrackerType.EXPENSES) {
                ensureMonthlyPeriod(trackerId, year, monthIndex)
            } else {
                repository.getCurrentPeriod(trackerId) ?: return@launch
            }
            val existing = repository.getRecord(trackerId, period.id, memberId)
            val status = if (amount >= tracker.defaultAmount) RecordStatus.PAID else RecordStatus.PARTIAL
            val updated = (existing ?: TrackerRecord(
                trackerId = trackerId,
                periodId = period.id,
                memberId = memberId
            )).copy(
                status = status,
                amountPaid = amount,
                note = note,
                updatedAt = System.currentTimeMillis()
            )
            if (existing == null) repository.insertRecord(updated) else repository.updateRecord(updated)
            checkAndTriggerTrackerConfetti(trackerId, period.id, tracker.type)
        }
    }

    private suspend fun checkAndTriggerConfetti(year: Int, monthIndex: Int, dueAmount: Double) {
        val allMembers = repository.getActiveMembers().first()
        val allPaid = allMembers.isNotEmpty() && allMembers.all { m ->
            repository.getTotalPaidForMonth(m.id, year, monthIndex) >= dueAmount
        }
        if (allPaid) confettiMonthFlow.value = monthIndex
    }

    private fun dismissConfetti() { confettiMonthFlow.value = null }

    private fun undoLastRemoval(paymentId: Long, memberId: Long, year: Int, monthIndex: Int, dueAmount: Double) {
        launch {
            repository.insertPayment(
                PaymentRecord(
                    memberId = memberId,
                    year = year,
                    monthIndex = monthIndex,
                    amountPaid = dueAmount,
                    expectedAmount = dueAmount,
                    paidAt = System.currentTimeMillis()
                )
            )
            snackbarFlow.value = null
        }
    }

    private fun dismissSnackbar() { snackbarFlow.value = null }

    private fun addMember(name: String, phone: String?) {
        launch {
            val trackerId = appState.currentTrackerId.value
            if (trackerId != null) {
                repository.insertTrackerMember(
                    TrackerMember(
                        trackerId = trackerId,
                        name = name.trim(),
                        phone = if (phone.isNullOrBlank()) null else phone.trim(),
                        createdAt = System.currentTimeMillis()
                    )
                )
            } else {
                repository.insertMember(
                    Member(
                        name = name.trim(),
                        phone = if (phone.isNullOrBlank()) null else phone.trim(),
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    private fun updateMember(member: Member) {
        launch {
            val trackerId = appState.currentTrackerId.value
            if (trackerId != null) {
                repository.updateTrackerMember(
                    TrackerMember(
                        id = member.id,
                        trackerId = trackerId,
                        name = member.name,
                        phone = member.phone,
                        isArchived = member.isArchived,
                        createdAt = member.createdAt
                    )
                )
            } else {
                repository.updateMember(member)
            }
        }
    }

    private fun setMemberArchived(id: Long, archived: Boolean) {
        launch {
            val trackerId = appState.currentTrackerId.value
            if (trackerId != null) {
                repository.setTrackerMemberArchived(id, archived)
            } else {
                repository.setMemberArchived(id, archived)
            }
        }
    }

    private fun deleteMember(id: Long, trackerIdOverride: Long? = null) {
        launch {
            val trackerId = trackerIdOverride ?: appState.currentTrackerId.value
            if (trackerId != null) {
                repository.deleteTrackerMember(trackerId, id)
            } else {
                repository.deleteMember(id)
            }
        }
    }

    private fun setCurrentTrackerId(trackerId: Long?) {
        appState.setCurrentTrackerId(trackerId)
    }

    private fun setLayoutStyleForCurrentTracker(style: LayoutStyle) {
        launch {
            val trackerId = appState.currentTrackerId.value ?: return@launch
            val tracker = repository.getTrackerById(trackerId) ?: return@launch
            repository.updateTracker(tracker.copy(layoutStyle = style))
        }
    }

    private suspend fun checkAndTriggerTrackerConfetti(trackerId: Long, periodId: Long, type: TrackerType) {
        val allMembers = repository.getActiveMembersForTracker(trackerId).first()
        val records = repository.getRecordsForPeriod(trackerId, periodId).first()
        val completed = when (type) {
            TrackerType.DUES -> setOf(RecordStatus.PAID)
            TrackerType.EXPENSES -> setOf(RecordStatus.PAID)
            TrackerType.GOALS -> setOf(RecordStatus.DONE)
            TrackerType.TODO -> setOf(RecordStatus.DONE)
            TrackerType.BUDGET -> setOf(RecordStatus.PAID)
        }
        val recordByMember = records.associateBy { it.memberId }
        val allDone = allMembers.isNotEmpty() && allMembers.all { m ->
            val status = recordByMember[m.id]?.status
            status != null && status in completed
        }
        if (allDone) confettiMonthFlow.value = Calendar.getInstance().get(Calendar.MONTH)
    }

    private fun TrackerMember.asUiMember(): Member = Member(
        id = id,
        name = name,
        phone = phone,
        isArchived = isArchived,
        createdAt = createdAt
    )

    private fun monthIndexFromLabel(label: String, year: Int): Int? {
        val fmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val date = runCatching { fmt.parse(label) }.getOrNull() ?: return null
        val cal = Calendar.getInstance().apply { time = date }
        if (cal.get(Calendar.YEAR) != year) return null
        return cal.get(Calendar.MONTH)
    }

    private suspend fun ensureMonthlyPeriod(trackerId: Long, year: Int, monthIndex: Int): TrackerPeriod {
        val label = monthlyLabel(year, monthIndex)
        val existing = repository.getPeriodByLabel(trackerId, label)
        if (existing != null) return existing

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = cal.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        val createdAt = System.currentTimeMillis()
        val periodId = repository.insertPeriod(
            TrackerPeriod(
                trackerId = trackerId,
                label = label,
                startDate = start,
                endDate = end,
                isCurrent = false,
                createdAt = createdAt
            )
        )
        return TrackerPeriod(
            id = periodId,
            trackerId = trackerId,
            label = label,
            startDate = start,
            endDate = end,
            isCurrent = false,
            createdAt = createdAt
        )
    }

    private fun monthlyLabel(year: Int, monthIndex: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    private fun markOutstandingMonthsPaid(
        memberId: Long,
        year: Int,
        dueAmount: Double,
        trackerIdOverride: Long? = null
    ) {
        launch {
            val trackerId = trackerIdOverride ?: appState.currentTrackerId.value
            val current = Calendar.getInstance()
            val endMonth = when {
                year < current.get(Calendar.YEAR) -> 11
                year == current.get(Calendar.YEAR) -> current.get(Calendar.MONTH)
                else -> -1
            }
            if (endMonth < 0) return@launch

            if (trackerId != null) {
                val tracker = repository.getTrackerById(trackerId) ?: return@launch
                if (tracker.type != TrackerType.DUES && tracker.type != TrackerType.EXPENSES) return@launch
                for (mi in 0..endMonth) {
                    val period = ensureMonthlyPeriod(trackerId, year, mi)
                    val existing = repository.getRecord(trackerId, period.id, memberId)
                    val paid = existing?.amountPaid ?: 0.0
                    if (paid < tracker.defaultAmount) {
                        val updated = (existing ?: TrackerRecord(
                            trackerId = trackerId,
                            periodId = period.id,
                            memberId = memberId
                        )).copy(
                            status = RecordStatus.PAID,
                            amountPaid = tracker.defaultAmount,
                            updatedAt = System.currentTimeMillis()
                        )
                        if (existing == null) repository.insertRecord(updated) else repository.updateRecord(updated)
                    }
                }
                val currentPeriod = repository.getCurrentPeriod(trackerId)
                if (currentPeriod != null) {
                    checkAndTriggerTrackerConfetti(trackerId, currentPeriod.id, TrackerType.DUES)
                }
                return@launch
            }

            for (mi in 0..endMonth) {
                val totalPaid = repository.getTotalPaidForMonth(memberId, year, mi)
                if (totalPaid < dueAmount) {
                    repository.insertPayment(
                        PaymentRecord(
                            memberId = memberId,
                            year = year,
                            monthIndex = mi,
                            amountPaid = dueAmount - totalPaid,
                            expectedAmount = dueAmount,
                            paidAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/settings/SettingsScreen.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
import com.mikeisesele.clearr.ui.commons.state.ThemeMode
import com.mikeisesele.clearr.ui.commons.util.currentYear
import com.mikeisesele.clearr.ui.commons.util.formatAmount
import com.mikeisesele.clearr.ui.feature.settings.components.SectionCard
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onThemeChange: (ThemeMode) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalDuesColors.current

    val currentYearConfig = state.yearConfigs.find { it.year == state.selectedYear }
    val dueAmount = state.currentTrackerDueAmount ?: currentYearConfig?.dueAmountPerMonth ?: 5000.0
    val dueEditable = state.currentTrackerType == TrackerType.DUES || state.currentTrackerType == TrackerType.EXPENSES

    var localDue by remember(state.selectedYear, dueAmount) { mutableStateOf(dueAmount.toInt().toString()) }
    var showResetDialog by remember { mutableStateOf(false) }
    var yearMenuExpanded by remember { mutableStateOf(false) }

    val cy = currentYear()
    val availableYears = remember(cy) { (cy until cy + 10).toList() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        ClearrTopBar(
            title = "Settings",
            showLeading = false,
            onLeadingClick = null,
            actionIcon = null,
            onActionClick = null,
            actionContainerColor = colors.card
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
            verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
        ) {
            // ── Active Year ───────────────────────────────────────────────────────
            SectionCard(
                title = "Active Year",
                colors = colors,
                modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
            ) {
                Text("Applies to the current tracker data.", style = MaterialTheme.typography.bodySmall, color = colors.muted)
                Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10))
                Box {
                    OutlinedButton(
                        onClick = { yearMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1, colors.accent),
                        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10)
                    ) {
                        Text(
                            "${state.selectedYear}${if (state.selectedYear == cy) "  (current)" else ""}",
                            color = colors.text,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = colors.accent)
                    }
                    DropdownMenu(
                        expanded = yearMenuExpanded,
                        onDismissRequest = { yearMenuExpanded = false },
                        modifier = Modifier.background(colors.card)
                    ) {
                        availableYears.forEach { y ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (y == cy) "$y  ●" else "$y",
                                        color = if (y == state.selectedYear) colors.accent else colors.text,
                                        fontWeight = if (y == state.selectedYear) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = { viewModel.onAction(SettingsAction.SelectYear(y)); yearMenuExpanded = false }
                            )
                        }
                        HorizontalDivider(color = colors.border)
                        DropdownMenuItem(
                            text = { Text("＋ Start ${state.selectedYear + 1}", color = colors.accent, fontWeight = FontWeight.SemiBold) },
                            onClick = { viewModel.onAction(SettingsAction.StartNewYear(state.selectedYear)); yearMenuExpanded = false }
                        )
                    }
                }
            }

            // ── Due Amount ────────────────────────────────────────────────────────
            SectionCard(
                title = "Due Amount",
                colors = colors,
                modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = localDue,
                        onValueChange = { localDue = it },
                        label = { Text("Amount (₦)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    Button(
                        onClick = {
                            val amt = localDue.toDoubleOrNull()
                            if (amt != null && amt > 0) viewModel.onAction(SettingsAction.UpdateDueAmount(state.selectedYear, amt))
                        },
                        enabled = dueEditable,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                    ) { Text("Save") }
                }
                Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4))
                Text("Current: ${formatAmount(dueAmount)} / member / period", style = MaterialTheme.typography.bodySmall, color = colors.muted)
                if (dueEditable) {
                    Text("Applies only to the current Dues tracker.", style = MaterialTheme.typography.bodySmall, color = colors.dim)
                } else {
                    Text("Open a Dues tracker to edit due amount.", style = MaterialTheme.typography.bodySmall, color = colors.dim)
                }
            }

            // ── App Info ──────────────────────────────────────────────────────────
            SectionCard(
                title = "App Info",
                colors = colors,
                modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Version", style = MaterialTheme.typography.bodyMedium, color = colors.text)
                    Text("1.0", style = MaterialTheme.typography.bodyMedium, color = colors.muted)
                }
                Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
                OutlinedButton(
                    onClick = { viewModel.onAction(SettingsAction.ResetSetup) },
                    border = BorderStroke(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1, colors.accent),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("⚙️  Re-run Setup Wizard", color = colors.accent) }
                Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
                OutlinedButton(
                    onClick = { showResetDialog = true },
                    border = BorderStroke(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1, colors.red),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Reset All Data", color = colors.red) }
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp80))
            Spacer(Modifier.navigationBarsPadding())
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = colors.surface,
            title = { Text("Reset All Data?", color = colors.text) },
            text = { Text("This will permanently delete all members, payments, and configuration. This action cannot be undone.", color = colors.muted) },
            confirmButton = {
                Button(onClick = { showResetDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = colors.red)) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Cancel", color = colors.muted) } },
            shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        Column(
            modifier = Modifier.fillMaxSize().background(colors.bg).padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
            verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
        ) {
            Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = colors.text)
            SectionCard(title = "Active Year", colors = colors) {
                Text("2026", color = colors.text)
            }
        }
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/settings/SettingsState.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.settings

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.data.model.YearConfig
import com.mikeisesele.clearr.ui.commons.state.ThemeMode

data class SettingsUiState(
    val selectedYear: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
    val allMembers: List<Member> = emptyList(),
    val yearConfigs: List<YearConfig> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currentTrackerType: TrackerType? = null,
    val currentTrackerDueAmount: Double? = null
) : BaseState

sealed interface SettingsAction {
    data class SelectYear(val year: Int) : SettingsAction
    data class SetThemeMode(val mode: ThemeMode) : SettingsAction
    data class UpdateDueAmount(val year: Int, val amount: Double) : SettingsAction
    data class SetMemberArchived(val id: Long, val archived: Boolean) : SettingsAction
    data class StartNewYear(val fromYear: Int) : SettingsAction
    data object ResetSetup : SettingsAction
}

sealed interface SettingsEvent : ViewEvent
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/settings/SettingsViewModel.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.settings

import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.di.AppStateHolder
import com.mikeisesele.clearr.domain.repository.DuesRepository
import com.mikeisesele.clearr.ui.commons.state.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: DuesRepository,
    private val appState: AppStateHolder
) : BaseViewModel<SettingsUiState, SettingsAction, SettingsEvent>(
    initialState = SettingsUiState()
) {
    private val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)

    init {
        launch {
            combine(
                appState.selectedYear,
                repository.getAllMembers(),
                repository.getAllYearConfigs(),
                themeModeFlow,
                appState.appConfig,
                appState.currentTrackerId
            ) { arr ->
                @Suppress("UNCHECKED_CAST")
                Sextuple(
                    selectedYear = arr[0] as Int,
                    allMembers = arr[1] as List<com.mikeisesele.clearr.data.model.Member>,
                    yearConfigs = arr[2] as List<com.mikeisesele.clearr.data.model.YearConfig>,
                    themeMode = arr[3] as ThemeMode,
                    appConfig = arr[4] as AppConfig?,
                    trackerId = arr[5] as Long?
                )
            }.flatMapLatest { p ->
                val trackerFlow: Flow<Tracker?> = p.trackerId?.let { repository.getTrackerByIdFlow(it) } ?: flowOf(null)
                trackerFlow.map { tracker ->
                    SettingsUiState(
                        selectedYear = p.selectedYear,
                        allMembers = p.allMembers,
                        yearConfigs = p.yearConfigs,
                        themeMode = p.themeMode,
                        currentTrackerType = tracker?.type,
                        currentTrackerDueAmount = tracker?.defaultAmount
                    )
                }
            }.collectLatest { newState ->
                updateState { newState }
            }
        }
    }

    override fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SelectYear -> handleSelectYear(action.year)
            is SettingsAction.SetThemeMode -> handleSetThemeMode(action.mode)
            is SettingsAction.UpdateDueAmount -> handleUpdateDueAmount(action.amount)
            is SettingsAction.SetMemberArchived -> handleSetMemberArchived(action.id, action.archived)
            is SettingsAction.StartNewYear -> handleStartNewYear(action.fromYear)
            SettingsAction.ResetSetup -> handleResetSetup()
        }
    }

    private fun handleSelectYear(year: Int) {
        appState.setYear(year)
        launch { repository.ensureYearConfig(year) }
    }

    private fun handleSetThemeMode(mode: ThemeMode) {
        themeModeFlow.value = mode
    }

    private fun handleUpdateDueAmount(amount: Double) {
        launch {
            val trackerId = appState.currentTrackerId.value
            if (trackerId != null) {
                val tracker = repository.getTrackerById(trackerId) ?: return@launch
                if (tracker.type == TrackerType.DUES || tracker.type == TrackerType.EXPENSES) {
                    repository.updateTracker(tracker.copy(defaultAmount = amount))
                }
            }
        }
    }

    private fun handleSetMemberArchived(id: Long, archived: Boolean) {
        launch { repository.setMemberArchived(id, archived) }
    }

    private fun handleStartNewYear(fromYear: Int) {
        launch {
            val currentConfig = repository.getYearConfig(fromYear)
            val nextYear = fromYear + 1
            repository.ensureYearConfig(nextYear, currentConfig?.dueAmountPerMonth ?: 5000.0)
            appState.setYear(nextYear)
        }
    }

    private fun handleResetSetup() {
        launch {
            val existing = repository.getAppConfig()
            val config = existing?.copy(setupComplete = false)
                ?: AppConfig(setupComplete = false)
            repository.upsertAppConfig(config)
            appState.setAppConfig(config)
        }
    }
    private data class Sextuple(
        val selectedYear: Int,
        val allMembers: List<com.mikeisesele.clearr.data.model.Member>,
        val yearConfigs: List<com.mikeisesele.clearr.data.model.YearConfig>,
        val themeMode: ThemeMode,
        val appConfig: AppConfig?,
        val trackerId: Long?
    )
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/settings/components/SectionCard.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
internal fun SectionCard(
    title: String,
    colors: DuesColors = LocalDuesColors.current,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.card),
        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = colors.text)
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionCardPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        SectionCard(title = "Active Year", colors = colors) {
            Text("2026", color = colors.text)
        }
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/setup/SetupState.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.setup

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.TrackerType

data class SetupWizardState(
    val step: Int = 1,                      // 1–5 setup steps
    val groupName: String = "JSS Durumi Brothers",
    val trackerName: String = "Remittance",
    val adminName: String = "",
    val adminPhone: String = "",
    val trackerType: TrackerType = TrackerType.DUES,
    val frequency: Frequency = Frequency.MONTHLY,
    val defaultAmount: String = "5000",
    val layoutStyle: LayoutStyle = LayoutStyle.GRID,
    val loadSampleMembers: Boolean = true,
    val isSaving: Boolean = false
) : BaseState

sealed interface SetupAction {
    data object NextStep : SetupAction
    data object PrevStep : SetupAction
    data class SetGroupName(val value: String) : SetupAction
    data class SetTrackerName(val value: String) : SetupAction
    data class SetAdminName(val value: String) : SetupAction
    data class SetAdminPhone(val value: String) : SetupAction
    data class SetTrackerType(val value: TrackerType) : SetupAction
    data class SetFrequency(val value: Frequency) : SetupAction
    data class SetDefaultAmount(val value: String) : SetupAction
    data class SetLayoutStyle(val value: LayoutStyle) : SetupAction
    data class SetLoadSampleMembers(val value: Boolean) : SetupAction
    data class GoToStep(val step: Int) : SetupAction
    data class FinishSetup(val onDone: () -> Unit) : SetupAction
    data class LoadExistingConfig(val config: AppConfig) : SetupAction
}

sealed interface SetupEvent : ViewEvent
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/setup/SetupViewModel.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.setup

import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.data.model.TrackerPeriod
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.di.AppStateHolder
import com.mikeisesele.clearr.domain.repository.DuesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val repository: DuesRepository,
    private val appState: AppStateHolder
) : BaseViewModel<SetupWizardState, SetupAction, SetupEvent>(
    initialState = SetupWizardState()
) {

    init {
        launch {
            val hasTrackers = repository.getAllTrackers().first().isNotEmpty()
            if (hasTrackers) {
                updateState { it.copy(step = 1) }
            }
        }
    }

    override fun onAction(action: SetupAction) {
        when (action) {
            SetupAction.NextStep -> handleNextStep()
            SetupAction.PrevStep -> handlePrevStep()
            is SetupAction.SetGroupName -> handleSetGroupName(action.value)
            is SetupAction.SetTrackerName -> handleSetTrackerName(action.value)
            is SetupAction.SetAdminName -> handleSetAdminName(action.value)
            is SetupAction.SetAdminPhone -> handleSetAdminPhone(action.value)
            is SetupAction.SetTrackerType -> handleSetTrackerType(action.value)
            is SetupAction.SetFrequency -> handleSetFrequency(action.value)
            is SetupAction.SetDefaultAmount -> handleSetDefaultAmount(action.value)
            is SetupAction.SetLayoutStyle -> handleSetLayoutStyle(action.value)
            is SetupAction.SetLoadSampleMembers -> handleSetLoadSampleMembers(action.value)
            is SetupAction.GoToStep -> handleGoToStep(action.step)
            is SetupAction.FinishSetup -> finishSetupInternal(action.onDone)
            is SetupAction.LoadExistingConfig -> handleLoadExistingConfig(action.config)
        }
    }

    private fun handleNextStep() = updateState { s -> s.copy(step = (s.step + 1).coerceAtMost(5)) }

    private fun handlePrevStep() = updateState { s -> s.copy(step = (s.step - 1).coerceAtLeast(1)) }

    private fun handleSetGroupName(value: String) = updateState { it.copy(groupName = value) }
    private fun handleSetTrackerName(value: String) {
        updateState { it.copy(trackerName = value) }
        launch {
            val ai = ClearrEdgeAi.parseSetupIntentNanoAware(value)
            updateState { state ->
                state.copy(
                    trackerType = ai.trackerType ?: state.trackerType,
                    frequency = ai.suggestedFrequency ?: state.frequency,
                    defaultAmount = ai.suggestedDefaultAmount?.toInt()?.toString() ?: state.defaultAmount
                )
            }
        }
    }
    private fun handleSetAdminName(value: String) = updateState { it.copy(adminName = value) }
    private fun handleSetAdminPhone(value: String) = updateState { it.copy(adminPhone = value) }

    private fun handleSetTrackerType(value: TrackerType) = updateState { s ->
        val oldDefault = defaultTrackerName(s.trackerType)
        val shouldAutoRename = s.trackerName.isBlank() || s.trackerName == oldDefault
        s.copy(
            trackerType = value,
            trackerName = if (shouldAutoRename) defaultTrackerName(value) else s.trackerName
        )
    }

    private fun handleSetFrequency(value: Frequency) = updateState { it.copy(frequency = value) }
    private fun handleSetDefaultAmount(value: String) = updateState { it.copy(defaultAmount = value) }
    private fun handleSetLayoutStyle(value: LayoutStyle) = updateState { it.copy(layoutStyle = value) }
    private fun handleSetLoadSampleMembers(value: Boolean) = updateState { it.copy(loadSampleMembers = value) }
    private fun handleGoToStep(step: Int) = updateState { it.copy(step = step.coerceIn(1, 5)) }

    private fun handleLoadExistingConfig(config: AppConfig) = updateState {
        it.copy(
            groupName = config.groupName,
            trackerName = defaultTrackerName(config.trackerType),
            adminName = config.adminName,
            adminPhone = config.adminPhone,
            trackerType = config.trackerType,
            frequency = config.frequency,
            defaultAmount = config.defaultAmount.toInt().toString(),
            layoutStyle = config.layoutStyle,
            loadSampleMembers = true
        )
    }

    private fun finishSetupInternal(onDone: () -> Unit) {
        val s = currentState
        val amount = s.defaultAmount.toDoubleOrNull()?.takeIf { it > 0 } ?: 5000.0
        val now = System.currentTimeMillis()
        val config = AppConfig(
            id = 1,
            groupName = s.groupName.trim().ifBlank { "JSS Durumi Brothers" },
            adminName = s.adminName.trim(),
            adminPhone = s.adminPhone.trim(),
            trackerType = s.trackerType,
            frequency = s.frequency,
            defaultAmount = amount,
            layoutStyle = s.layoutStyle,
            remindersEnabled = false,
            setupComplete = true
        )
        updateState { it.copy(isSaving = true) }
        launch {
            val trackerId = repository.insertTracker(
                Tracker(
                    name = s.trackerName.trim().ifBlank { defaultTrackerName(s.trackerType) },
                    type = s.trackerType,
                    frequency = s.frequency,
                    layoutStyle = s.layoutStyle,
                    defaultAmount = amount,
                    isNew = true,
                    createdAt = now
                )
            )
            val periodId = repository.insertPeriod(buildCurrentPeriod(trackerId, s.frequency, now))
            repository.setCurrentPeriod(trackerId, periodId)

            if ((s.trackerType == TrackerType.DUES || s.trackerType == TrackerType.EXPENSES) && s.loadSampleMembers) {
                SAMPLE_MEMBERS.forEach { name ->
                    repository.insertTrackerMember(
                        TrackerMember(
                            trackerId = trackerId,
                            name = name,
                            createdAt = now
                        )
                    )
                }
            }

            repeat(6) {
                if (repository.getTrackerById(trackerId) != null) return@repeat
                delay(50)
            }
            if (s.trackerType == TrackerType.BUDGET) {
                seedBudgetTracker(trackerId)
            }
            repository.upsertAppConfig(config)
            appState.setAppConfig(config)
            updateState { it.copy(isSaving = false) }
            onDone()
        }
    }

    private suspend fun seedBudgetTracker(trackerId: Long) {
        listOf(BudgetFrequency.MONTHLY, BudgetFrequency.WEEKLY).forEach { frequency ->
            repository.ensureBudgetPeriods(trackerId, frequency)
            if (repository.getBudgetMaxSortOrder(trackerId, frequency) >= 0) return@forEach
            defaultBudgetCategories.forEachIndexed { index, preset ->
                repository.addBudgetCategory(
                    BudgetCategory(
                        trackerId = trackerId,
                        frequency = frequency,
                        name = preset.name,
                        icon = preset.icon,
                        colorToken = preset.colorToken,
                        plannedAmountKobo = preset.plannedAmountKobo,
                        sortOrder = index
                    )
                )
            }
        }
    }
    private fun defaultTrackerName(type: TrackerType): String = when (type) {
        TrackerType.DUES -> "Remittance"
        TrackerType.EXPENSES -> "Remittance"
        TrackerType.GOALS -> "Goals Tracker"
        TrackerType.TODO -> "To-do Tracker"
        TrackerType.BUDGET -> "Budget Tracker"
    }

    private companion object {
        data class BudgetCategoryPreset(
            val name: String,
            val icon: String,
            val colorToken: String,
            val plannedAmountKobo: Long
        )

        val defaultBudgetCategories = listOf(
            BudgetCategoryPreset("Housing", "🏠", "Violet", 150_000_00),
            BudgetCategoryPreset("Food", "🍔", "Orange", 60_000_00),
            BudgetCategoryPreset("Transport", "🚗", "Blue", 30_000_00),
            BudgetCategoryPreset("Savings", "💰", "Teal", 50_000_00),
            BudgetCategoryPreset("Entertainment", "🎬", "Purple", 20_000_00),
            BudgetCategoryPreset("Utilities", "💡", "Violet", 15_000_00)
        )

        val SAMPLE_MEMBERS = listOf(
            "Henry Nwazuru",
            "Chidubem",
            "Simon Boniface",
            "Ikechukwu Udeh",
            "Oluwatobi Majekodunmi",
            "Dare Oladunjoye",
            "Michael Isesele",
            "Faruk Umar"
        )
    }

    private fun currentPeriodLabel(frequency: Frequency): String {
        val cal = Calendar.getInstance()
        return when (frequency) {
            Frequency.MONTHLY -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
            Frequency.WEEKLY -> "Week ${cal.get(Calendar.WEEK_OF_YEAR)}, ${cal.get(Calendar.YEAR)}"
            Frequency.QUARTERLY -> "Q${(cal.get(Calendar.MONTH) / 3) + 1} ${cal.get(Calendar.YEAR)}"
            Frequency.TERMLY -> "Term ${(cal.get(Calendar.MONTH) / 4) + 1} ${cal.get(Calendar.YEAR)}"
            Frequency.BIANNUAL -> "H${if (cal.get(Calendar.MONTH) < 6) 1 else 2} ${cal.get(Calendar.YEAR)}"
            Frequency.ANNUAL -> "${cal.get(Calendar.YEAR)}"
            Frequency.CUSTOM -> "Current Period"
        }
    }

    private fun buildCurrentPeriod(trackerId: Long, frequency: Frequency, now: Long): TrackerPeriod {
        val cal = Calendar.getInstance()
        val (start, end) = when (frequency) {
            Frequency.MONTHLY -> {
                val s = cal.apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val e = cal.apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis
                s to e
            }
            Frequency.WEEKLY -> {
                val s = cal.apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val e = cal.apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis
                s to e
            }
            Frequency.QUARTERLY -> {
                val quarter = cal.get(Calendar.MONTH) / 3
                val startMonth = quarter * 3
                val s = cal.apply {
                    set(Calendar.MONTH, startMonth)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val e = cal.apply {
                    set(Calendar.MONTH, startMonth + 2)
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis
                s to e
            }
            else -> {
                val yearStart = cal.apply {
                    set(Calendar.MONTH, 0)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val yearEnd = cal.apply {
                    set(Calendar.MONTH, 11)
                    set(Calendar.DAY_OF_MONTH, 31)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis
                yearStart to yearEnd
            }
        }

        return TrackerPeriod(
            trackerId = trackerId,
            label = currentPeriodLabel(frequency),
            startDate = start,
            endDate = end,
            isCurrent = true,
            createdAt = now
        )
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/setup/SetupWizardScreen.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.ui.feature.setup.components.AmountStep
import com.mikeisesele.clearr.ui.feature.setup.components.FrequencyStep
import com.mikeisesele.clearr.ui.feature.setup.components.GroupInfoStep
import com.mikeisesele.clearr.ui.feature.setup.components.LayoutStyleStep
import com.mikeisesele.clearr.ui.feature.setup.components.ReviewStep
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
fun SetupWizardScreen(
    viewModel: SetupViewModel = hiltViewModel(),
    onSetupComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalDuesColors.current
    val totalSteps = 5
    val displayStep = (state.step - 1).coerceIn(0, totalSteps - 1)
    val finalStep = 5

    Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        Column(modifier = Modifier.fillMaxWidth().background(colors.surface).statusBarsPadding().padding(horizontal = ClearrDimens.dp24, vertical = ClearrDimens.dp14)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Setup Wizard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = colors.text)
                Text("Step ${displayStep + 1} of $totalSteps", style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
            Spacer(Modifier.height(ClearrDimens.dp10))
            LinearProgressIndicator(progress = { (displayStep + 1f) / totalSteps }, modifier = Modifier.fillMaxWidth().height(ClearrDimens.dp4).clip(RoundedCornerShape(ClearrDimens.dp2)), color = colors.accent, trackColor = colors.border)
            Spacer(Modifier.height(ClearrDimens.dp10))
            Row(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)) {
                repeat(totalSteps) { i ->
                    Box(modifier = Modifier.size(ClearrDimens.dp8).clip(CircleShape).background(if (i <= displayStep) colors.accent else colors.border))
                }
            }
        }
        HorizontalDivider(color = colors.border)

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith (slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "wizard_step"
            ) { step ->
                when (step) {
                    1 -> GroupInfoStep(state.groupName, state.trackerName, state.adminName, state.adminPhone, state.loadSampleMembers, { viewModel.onAction(SetupAction.SetGroupName(it)) }, { viewModel.onAction(SetupAction.SetTrackerName(it)) }, { viewModel.onAction(SetupAction.SetAdminName(it)) }, { viewModel.onAction(SetupAction.SetAdminPhone(it)) }, { viewModel.onAction(SetupAction.SetLoadSampleMembers(it)) }, colors)
                    2 -> FrequencyStep(state.frequency, { viewModel.onAction(SetupAction.SetFrequency(it)) }, colors)
                    3 -> AmountStep(state.defaultAmount, state.frequency, state.trackerType, { viewModel.onAction(SetupAction.SetDefaultAmount(it)) }, colors)
                    4 -> LayoutStyleStep(state.layoutStyle, { viewModel.onAction(SetupAction.SetLayoutStyle(it)) }, colors)
                    5 -> ReviewStep(state.groupName, state.trackerName, state.trackerType, state.frequency, state.layoutStyle, state.defaultAmount, state.loadSampleMembers, colors)
                    else -> GroupInfoStep(state.groupName, state.trackerName, state.adminName, state.adminPhone, state.loadSampleMembers, { viewModel.onAction(SetupAction.SetGroupName(it)) }, { viewModel.onAction(SetupAction.SetTrackerName(it)) }, { viewModel.onAction(SetupAction.SetAdminName(it)) }, { viewModel.onAction(SetupAction.SetAdminPhone(it)) }, { viewModel.onAction(SetupAction.SetLoadSampleMembers(it)) }, colors)
                }
            }
        }

        HorizontalDivider(color = colors.border)
        Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = ClearrDimens.dp24, vertical = ClearrDimens.dp10), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { viewModel.onAction(SetupAction.PrevStep) }, enabled = state.step > 1, border = androidx.compose.foundation.BorderStroke(ClearrDimens.dp1, colors.border)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = colors.text)
                Text("Back", color = colors.text)
            }

            if (state.step < finalStep) {
                Button(onClick = { viewModel.onAction(SetupAction.NextStep) }, colors = ButtonDefaults.buttonColors(containerColor = colors.accent)) {
                    Text("Next", color = ClearrColors.Surface)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ClearrColors.Surface)
                }
            } else {
                Button(onClick = { viewModel.onAction(SetupAction.FinishSetup(onSetupComplete)) }, enabled = !state.isSaving, colors = ButtonDefaults.buttonColors(containerColor = colors.green)) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(ClearrDimens.dp18), color = ClearrColors.Surface, strokeWidth = ClearrDimens.dp2)
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, tint = ClearrColors.Surface)
                        Spacer(Modifier.size(ClearrDimens.dp4))
                        Text("Finish Setup", color = ClearrColors.Surface)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SetupWizardScreenPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
            Text("Setup Wizard Preview", modifier = Modifier.padding(ClearrDimens.dp24), color = colors.text)
        }
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/setup/components/SetupWizardSteps.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.setup.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
internal fun GroupInfoStep(
    groupName: String,
    trackerName: String,
    adminName: String,
    adminPhone: String,
    loadSampleMembers: Boolean,
    onGroupName: (String) -> Unit,
    onTrackerName: (String) -> Unit,
    onAdminName: (String) -> Unit,
    onAdminPhone: (String) -> Unit,
    onLoadSampleMembers: (Boolean) -> Unit,
    colors: DuesColors
) {
    Column(modifier = Modifier.fillMaxWidth().padding(ClearrDimens.dp24), verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp16)) {
        SetupStepHeader("Group Information", "Tell us about your group.", colors)
        WizardTextField(groupName, onGroupName, "Group / Organisation Name", colors = colors)
        WizardTextField(trackerName, onTrackerName, "Tracker Name (e.g. Task Tracker, Event Tracker)", colors = colors)
        WizardTextField(adminName, onAdminName, "Admin Name (optional)", colors = colors)
        WizardTextField(adminPhone, onAdminPhone, "Admin Phone (optional)", keyboardType = KeyboardType.Phone, colors = colors)
        Card(colors = CardDefaults.cardColors(containerColor = colors.card), shape = RoundedCornerShape(ClearrDimens.dp12), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = ClearrDimens.dp14, vertical = ClearrDimens.dp10), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Load sample members (dev)", color = colors.text, fontWeight = FontWeight.SemiBold)
                    Text("Seeds names like Michael, Simon, Henry into new dues tracker.", style = MaterialTheme.typography.bodySmall, color = colors.muted)
                }
                Switch(checked = loadSampleMembers, onCheckedChange = onLoadSampleMembers, colors = SwitchDefaults.colors(checkedTrackColor = colors.accent, checkedThumbColor = ClearrColors.Surface))
            }
        }
    }
}

@Composable
internal fun FrequencyStep(selected: Frequency, onSelect: (Frequency) -> Unit, colors: DuesColors) {
    val options = listOf(
        Frequency.MONTHLY to Pair("📅", "Monthly – 12 periods per year (Jan – Dec)"),
        Frequency.WEEKLY to Pair("🗓️", "Weekly – 52 periods per year"),
        Frequency.QUARTERLY to Pair("📆", "Quarterly – 4 periods (Q1–Q4)"),
        Frequency.TERMLY to Pair("🏫", "Termly – 3 periods (Term 1–3)"),
        Frequency.BIANNUAL to Pair("🔄", "Bi-annual – 2 periods per year"),
        Frequency.ANNUAL to Pair("🎯", "Annual – 1 period per year"),
        Frequency.CUSTOM to Pair("🛠️", "Custom – Define your own period labels")
    )
    Column(modifier = Modifier.fillMaxWidth().padding(ClearrDimens.dp24), verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp10)) {
        SetupStepHeader("How often do you meet / collect?", "This determines how many periods appear in your tracker.", colors)
        options.forEach { (freq, info) -> SelectionCard(info.first, freq.name.lowercase().replaceFirstChar { it.uppercase() }, info.second, selected == freq, { onSelect(freq) }, colors) }
    }
}

@Composable
internal fun AmountStep(amount: String, frequency: Frequency, trackerType: TrackerType, onAmount: (String) -> Unit, colors: DuesColors) {
    val label = when (trackerType) {
        TrackerType.DUES -> "Amount per ${frequency.name.lowercase().replaceFirstChar { it.uppercase() }} (₦)"
        TrackerType.EXPENSES -> "Amount per ${frequency.name.lowercase().replaceFirstChar { it.uppercase() }} (₦)"
        TrackerType.GOALS, TrackerType.TODO, TrackerType.BUDGET -> "Not applicable for this tracker type"
    }
    val skipAmount = trackerType != TrackerType.DUES && trackerType != TrackerType.EXPENSES
    Column(modifier = Modifier.fillMaxWidth().padding(ClearrDimens.dp24), verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp16)) {
        SetupStepHeader("Set the Amount", "How much is due per period per member?", colors)
        if (skipAmount) {
            Card(colors = CardDefaults.cardColors(containerColor = colors.card), shape = RoundedCornerShape(ClearrDimens.dp12), modifier = Modifier.fillMaxWidth()) {
                Text("No amount required for ${trackerType.name.lowercase()} tracking. You can skip this step.", modifier = Modifier.padding(ClearrDimens.dp16), color = colors.muted, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            OutlinedTextField(
                value = amount,
                onValueChange = onAmount,
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            Text("This becomes the default. You can override it per year in Settings.", style = MaterialTheme.typography.bodySmall, color = colors.muted)
        }
    }
}

@Composable
internal fun LayoutStyleStep(selected: LayoutStyle, onSelect: (LayoutStyle) -> Unit, colors: DuesColors) {
    val options = listOf(
        LayoutStyle.GRID to Triple("⊞", "Grid", "Compact scrollable table – members × periods"),
        LayoutStyle.KANBAN to Triple("🗂️", "Kanban", "Period columns with member cards – great for small groups"),
        LayoutStyle.CARDS to Triple("🃏", "Cards", "One card per member showing all periods"),
        LayoutStyle.RECEIPT to Triple("🧾", "Receipt / Ledger", "Detailed financial ledger style")
    )
    Column(modifier = Modifier.fillMaxWidth().padding(ClearrDimens.dp24), verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp12)) {
        SetupStepHeader("Choose a Layout Style", "Pick how you want to view your tracker. You can change this anytime.", colors)
        options.forEach { (style, info) -> SelectionCard(info.first, info.second, info.third, selected == style, { onSelect(style) }, colors) }
    }
}

@Composable
internal fun ReviewStep(
    groupName: String,
    trackerName: String,
    trackerType: TrackerType,
    frequency: Frequency,
    layoutStyle: LayoutStyle,
    defaultAmount: String,
    loadSampleMembers: Boolean,
    colors: DuesColors
) {
    Column(modifier = Modifier.fillMaxWidth().padding(ClearrDimens.dp24), verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp16)) {
        SetupStepHeader("Review", "Confirm your setup before creating the tracker.", colors)
        Card(colors = CardDefaults.cardColors(containerColor = colors.card), shape = RoundedCornerShape(ClearrDimens.dp16), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(ClearrDimens.dp16), verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)) {
                Text("Group: ${groupName.ifBlank { "Unnamed Group" }}", color = colors.text)
                Text("Tracker: ${trackerName.ifBlank { "Unnamed Tracker" }}", color = colors.text)
                Text("Type: ${trackerType.name.lowercase().replaceFirstChar { it.uppercase() }}", color = colors.text)
                Text("Frequency: ${frequency.name.lowercase().replaceFirstChar { it.uppercase() }}", color = colors.text)
                if (trackerType == TrackerType.DUES || trackerType == TrackerType.EXPENSES) {
                    Text("Amount: ₦${defaultAmount.ifBlank { "5000" }}", color = colors.text)
                    Text("Seed sample members: ${if (loadSampleMembers) "On" else "Off"}", color = colors.text)
                }
                Text("Layout: ${layoutStyle.name.lowercase().replaceFirstChar { it.uppercase() }}", color = colors.text)
            }
        }
    }
}

@Composable
internal fun SetupStepHeader(title: String, subtitle: String, colors: DuesColors) {
    Column(verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp4)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = colors.text)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.muted)
    }
}

@Composable
internal fun SelectionCard(icon: String, title: String, description: String, selected: Boolean, onClick: () -> Unit, colors: DuesColors) {
    val borderColor = if (selected) colors.accent else colors.border
    val bgColor = if (selected) colors.accent.copy(alpha = 0.08f) else colors.card
    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(ClearrDimens.dp12),
        modifier = Modifier.fillMaxWidth().border(width = if (selected) ClearrDimens.dp2 else ClearrDimens.dp1, color = borderColor, shape = RoundedCornerShape(ClearrDimens.dp12)).clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(ClearrDimens.dp12), horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp12), verticalAlignment = Alignment.CenterVertically) {
            Text(icon)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = if (selected) colors.accent else colors.text)
                Text(description, style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
            if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = colors.accent)
        }
    }
}

@Composable
internal fun WizardTextField(value: String, onValueChange: (String) -> Unit, label: String, keyboardType: KeyboardType = KeyboardType.Text, colors: DuesColors) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun FrequencyStepPreview() {
    ClearrTheme { FrequencyStep(selected = Frequency.MONTHLY, onSelect = {}, colors = LocalDuesColors.current) }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/RemittanceHomeScreen.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.trackerlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
import com.mikeisesele.clearr.ui.feature.trackerlist.components.DeleteTrackerDialog
import com.mikeisesele.clearr.ui.feature.trackerlist.components.RemittanceSwipeCard
import com.mikeisesele.clearr.ui.feature.trackerlist.components.RenameTrackerDialog
import com.mikeisesele.clearr.ui.feature.trackerlist.components.primaryColor
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDS
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
fun RemittanceHomeScreen(
    onTrackerClick: (Long) -> Unit,
    onCreateRemittance: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: TrackerListViewModel = hiltViewModel()
) {
    val colors = LocalDuesColors.current
    val spacing = ClearrDS.spacing
    val radii = ClearrDS.radii
    val sizes = ClearrDS.sizes
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val remittances = state.summaries.filter { summary ->
        summary.type == TrackerType.DUES || summary.type == TrackerType.EXPENSES
    }

    var deleteTarget by remember { mutableStateOf<TrackerSummary?>(null) }
    var renameTarget by remember { mutableStateOf<TrackerSummary?>(null) }
    var renameValue by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.onAction(TrackerListAction.Refresh)
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            ClearrTopBar(
                title = "Remittance",
                showLeading = false,
                actionIcon = "⚙",
                onActionClick = onOpenSettings
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = spacing.lg,
                        end = spacing.lg,
                        top = spacing.lg,
                        bottom = spacing.xxxl
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    items(remittances, key = { it.trackerId }) { summary ->
                        RemittanceSwipeCard(
                            summary = summary,
                            onDeleteRequest = { deleteTarget = summary },
                            onClick = {
                                if (summary.isNew) {
                                    viewModel.onAction(TrackerListAction.ClearNewFlag(summary.trackerId))
                                }
                                onTrackerClick(summary.trackerId)
                            },
                            onLongPress = {
                                renameTarget = summary
                                renameValue = summary.name
                            }
                        )
                    }
                }
            }
        }

        if (!state.isLoading) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = spacing.xl, bottom = spacing.xxl),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)
            ) {
                Surface(
                    modifier = Modifier.clickable { onCreateRemittance() },
                    color = ClearrColors.BrandText,
                    shape = RoundedCornerShape(radii.xl),
                    shadowElevation = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8
                ) {
                    Text(
                        "New Remittance",
                        color = ClearrColors.Surface,
                        fontSize = ClearrTextSizes.sp13,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(sizes.fab)
                        .clip(RoundedCornerShape(radii.lg))
                        .background(primaryColor)
                        .clickable { onCreateRemittance() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = ClearrColors.Surface, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp28, fontWeight = FontWeight.Light)
                }
            }
        }
    }

    deleteTarget?.let { summary ->
        DeleteTrackerDialog(
            summary = summary,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.onAction(TrackerListAction.DeleteTracker(summary.trackerId))
                deleteTarget = null
            }
        )
    }

    renameTarget?.let { summary ->
        RenameTrackerDialog(
            value = renameValue,
            onValueChange = { renameValue = it },
            onDismiss = { renameTarget = null },
            onConfirm = {
                viewModel.onAction(TrackerListAction.RenameTracker(summary.trackerId, renameValue))
                renameTarget = null
            }
        )
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/TrackerListScreen.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.trackerlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
import com.mikeisesele.clearr.ui.feature.trackerlist.components.DeleteTrackerDialog
import com.mikeisesele.clearr.ui.feature.trackerlist.components.RemittanceSwipeCard
import com.mikeisesele.clearr.ui.feature.trackerlist.components.RenameTrackerDialog
import com.mikeisesele.clearr.ui.feature.trackerlist.components.TrackerCard
import com.mikeisesele.clearr.ui.feature.trackerlist.components.primaryColor
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDS
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TrackerListScreen(
    viewModel: TrackerListViewModel = hiltViewModel(),
    onTrackerClick: (trackerId: Long) -> Unit,
    onCreateTracker: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val spacing = ClearrDS.spacing
    val radii = ClearrDS.radii
    val sizes = ClearrDS.sizes
    val colors = LocalDuesColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var deleteTarget by remember { mutableStateOf<TrackerSummary?>(null) }
    var renameTarget by remember { mutableStateOf<TrackerSummary?>(null) }
    var renameValue by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onAction(TrackerListAction.Refresh)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    viewModel.onAction(TrackerListAction.Refresh)
                    delay(350)
                    isRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Header ────────────────────────────────────────────────────
                ClearrTopBar(
                    title = "Dashboard",
                    subtitle = null,
                    showLeading = false,
                    actionIcon = "⚙️",
                    onActionClick = onOpenSettings,
                    actionContainerColor = colors.card
                )

                // ── Body ──────────────────────────────────────────────────────
                when {
                    state.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = primaryColor)
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, end = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, top = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16, bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp100),
                            verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)
                        ) {
                            items(state.summaries, key = { it.trackerId }) { summary ->
                                if (summary.type == TrackerType.DUES || summary.type == TrackerType.EXPENSES) {
                                    RemittanceSwipeCard(
                                        summary = summary,
                                        onDeleteRequest = { deleteTarget = summary },
                                        onClick = {
                                            if (summary.isNew) {
                                                viewModel.onAction(TrackerListAction.ClearNewFlag(summary.trackerId))
                                            }
                                            onTrackerClick(summary.trackerId)
                                        },
                                        onLongPress = {
                                            renameTarget = summary
                                            renameValue = summary.name
                                        }
                                    )
                                } else {
                                    TrackerCard(
                                        summary = summary,
                                        onClick = {
                                            if (summary.isNew) {
                                                viewModel.onAction(TrackerListAction.ClearNewFlag(summary.trackerId))
                                            }
                                            onTrackerClick(summary.trackerId)
                                        },
                                        onLongPress = {
                                            renameTarget = summary
                                            renameValue = summary.name
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── FAB ───────────────────────────────────────────────────────────
            if (!state.isLoading) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = spacing.xl, bottom = spacing.xxl),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)
                ) {
                    Surface(
                        color = ClearrColors.BrandText,
                        shape = RoundedCornerShape(radii.xl),
                        shadowElevation = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8
                    ) {
                        Text(
                            "New Remittance",
                            color = ClearrColors.Surface,
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(sizes.fab)
                            .clip(RoundedCornerShape(radii.lg))
                            .background(primaryColor)
                            .clickable { onCreateTracker() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = ClearrColors.Surface, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp28, fontWeight = FontWeight.Light)
                    }
                }
            }
        }
    }

    deleteTarget?.let { summary ->
        DeleteTrackerDialog(
            summary = summary,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.onAction(TrackerListAction.DeleteTracker(summary.trackerId))
                deleteTarget = null
            }
        )
    }

    renameTarget?.let { summary ->
        RenameTrackerDialog(
            value = renameValue,
            onValueChange = { renameValue = it },
            onDismiss = { renameTarget = null },
            onConfirm = {
                viewModel.onAction(TrackerListAction.RenameTracker(summary.trackerId, renameValue))
                renameTarget = null
            }
        )
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/TrackerListState.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.trackerlist

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType

data class TrackerListUiState(
    val summaries: List<TrackerSummary> = emptyList(),
    val isLoading: Boolean = true
) : BaseState

sealed interface TrackerListAction {
    data class CreateTracker(
        val name: String,
        val type: TrackerType,
        val frequency: Frequency,
        val defaultAmount: Double,
        val initialMembers: List<String>
    ) : TrackerListAction

    data class ClearNewFlag(val trackerId: Long) : TrackerListAction
    data class DeleteTracker(val trackerId: Long) : TrackerListAction
    data class RenameTracker(val trackerId: Long, val newName: String) : TrackerListAction
    data object Refresh : TrackerListAction
}

sealed interface TrackerListEvent : ViewEvent
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/TrackerListViewModel.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.trackerlist

import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.domain.repository.DuesRepository
import com.mikeisesele.clearr.domain.trackers.ObserveTrackerSummariesUseCase
import com.mikeisesele.clearr.domain.trackers.TrackerBootstrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TrackerListViewModel @Inject constructor(
    private val repository: DuesRepository,
    private val trackerBootstrapper: TrackerBootstrapper,
    private val observeTrackerSummaries: ObserveTrackerSummariesUseCase
) : BaseViewModel<TrackerListUiState, TrackerListAction, TrackerListEvent>(
    initialState = TrackerListUiState(isLoading = true)
) {

    private val refreshSignal = MutableStateFlow(0)

    init {
        launch {
            trackerBootstrapper.ensureStaticTrackers()
            observeTrackerSummaries().collectLatest { summaries ->
                updateState {
                    TrackerListUiState(
                        summaries = ClearrEdgeAi.prioritizeTrackers(summaries),
                        isLoading = false
                    )
                }
            }
        }
    }

    override fun onAction(action: TrackerListAction) {
        when (action) {
            is TrackerListAction.CreateTracker -> handleCreateTracker(
                name = action.name,
                type = action.type,
                frequency = action.frequency,
                defaultAmount = action.defaultAmount,
                initialMembers = action.initialMembers
            )
            is TrackerListAction.ClearNewFlag -> handleClearNewFlag(action.trackerId)
            is TrackerListAction.DeleteTracker -> handleDeleteTracker(action.trackerId)
            is TrackerListAction.RenameTracker -> handleRenameTracker(action.trackerId, action.newName)
            TrackerListAction.Refresh -> handleRefresh()
        }
    }

    private fun handleCreateTracker(
        name: String,
        type: com.mikeisesele.clearr.data.model.TrackerType,
        frequency: Frequency,
        defaultAmount: Double,
        initialMembers: List<String>
    ) {
        launch {
            val now = System.currentTimeMillis()
            val trackerId = repository.insertTracker(
                Tracker(
                    name = name,
                    type = type,
                    frequency = frequency,
                    layoutStyle = LayoutStyle.GRID,
                    defaultAmount = defaultAmount,
                    isNew = true,
                    createdAt = now
                )
            )
            initialMembers.forEach { memberName ->
                if (memberName.isNotBlank()) {
                    repository.insertTrackerMember(
                        TrackerMember(
                            trackerId = trackerId,
                            name = memberName.trim(),
                            createdAt = now
                        )
                    )
                }
            }
            val period = buildCurrentPeriod(trackerId, frequency, now)
            val periodId = repository.insertPeriod(period)
            repository.setCurrentPeriod(trackerId, periodId)
            if (type == com.mikeisesele.clearr.data.model.TrackerType.BUDGET) {
                listOf(BudgetFrequency.MONTHLY, BudgetFrequency.WEEKLY).forEach { budgetFrequency ->
                    repository.ensureBudgetPeriods(trackerId, budgetFrequency)
                }
            }
        }
    }

    private fun handleClearNewFlag(trackerId: Long) {
        launch { repository.clearTrackerNewFlag(trackerId) }
    }

    private fun handleDeleteTracker(trackerId: Long) {
        updateState { state ->
            state.copy(
                summaries = state.summaries.filterNot { it.trackerId == trackerId }
            )
        }
        launch {
            repository.deleteTracker(trackerId)
            refreshSignal.update { it + 1 }
        }
    }

    private fun handleRenameTracker(trackerId: Long, newName: String) {
        launch {
            val tracker = repository.getTrackerById(trackerId) ?: return@launch
            repository.updateTracker(tracker.copy(name = newName.trim()))
        }
    }

    private fun handleRefresh() {
        refreshSignal.update { it + 1 }
    }

    private fun buildCurrentPeriod(trackerId: Long, frequency: Frequency, now: Long): com.mikeisesele.clearr.data.model.TrackerPeriod {
        val cal = java.util.Calendar.getInstance()
        val (start, end) = when (frequency) {
            Frequency.MONTHLY -> {
                val s = cal.apply { set(java.util.Calendar.DAY_OF_MONTH, 1); set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis
                val e = cal.apply { set(java.util.Calendar.DAY_OF_MONTH, getActualMaximum(java.util.Calendar.DAY_OF_MONTH)); set(java.util.Calendar.HOUR_OF_DAY, 23); set(java.util.Calendar.MINUTE, 59); set(java.util.Calendar.SECOND, 59) }.timeInMillis
                s to e
            }
            Frequency.WEEKLY -> {
                val s = cal.apply { set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY); set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis
                val e = cal.apply { set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SUNDAY); set(java.util.Calendar.HOUR_OF_DAY, 23); set(java.util.Calendar.MINUTE, 59); set(java.util.Calendar.SECOND, 59) }.timeInMillis
                s to e
            }
            Frequency.QUARTERLY -> {
                val quarter = cal.get(java.util.Calendar.MONTH) / 3
                val startMonth = quarter * 3
                val s = cal.apply { set(java.util.Calendar.MONTH, startMonth); set(java.util.Calendar.DAY_OF_MONTH, 1); set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis
                val e = cal.apply { set(java.util.Calendar.MONTH, startMonth + 2); set(java.util.Calendar.DAY_OF_MONTH, getActualMaximum(java.util.Calendar.DAY_OF_MONTH)); set(java.util.Calendar.HOUR_OF_DAY, 23); set(java.util.Calendar.MINUTE, 59); set(java.util.Calendar.SECOND, 59) }.timeInMillis
                s to e
            }
            else -> {
                val yearStart = cal.apply { set(java.util.Calendar.MONTH, 0); set(java.util.Calendar.DAY_OF_MONTH, 1); set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis
                val yearEnd = cal.apply { set(java.util.Calendar.MONTH, 11); set(java.util.Calendar.DAY_OF_MONTH, 31); set(java.util.Calendar.HOUR_OF_DAY, 23); set(java.util.Calendar.MINUTE, 59); set(java.util.Calendar.SECOND, 59) }.timeInMillis
                yearStart to yearEnd
            }
        }
        return com.mikeisesele.clearr.data.model.TrackerPeriod(
            trackerId = trackerId,
            label = currentPeriodLabel(frequency),
            startDate = start,
            endDate = end,
            isCurrent = true,
            createdAt = now
        )
    }

    private fun currentPeriodLabel(frequency: Frequency): String {
        val cal = java.util.Calendar.getInstance()
        return when (frequency) {
            Frequency.MONTHLY -> java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(cal.time)
            Frequency.WEEKLY -> "Week ${cal.get(java.util.Calendar.WEEK_OF_YEAR)}, ${cal.get(java.util.Calendar.YEAR)}"
            Frequency.QUARTERLY -> "Q${(cal.get(java.util.Calendar.MONTH) / 3) + 1} ${cal.get(java.util.Calendar.YEAR)}"
            Frequency.TERMLY -> "Term ${(cal.get(java.util.Calendar.MONTH) / 4) + 1} ${cal.get(java.util.Calendar.YEAR)}"
            Frequency.BIANNUAL -> "H${if (cal.get(java.util.Calendar.MONTH) < 6) 1 else 2} ${cal.get(java.util.Calendar.YEAR)}"
            Frequency.ANNUAL -> "${cal.get(java.util.Calendar.YEAR)}"
            Frequency.CUSTOM -> "Current Period"
        }
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/components/CreateTrackerDialog.kt`

```kotlin
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
 * Stub dialog shown when the user taps "New Remittance" from within the list.
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
                "Create Remittance",
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
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/components/EmptyTrackerState.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.trackerlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDS
import com.mikeisesele.clearr.ui.theme.ClearrTheme

@Composable
internal fun EmptyTrackerState(onCreate: () -> Unit) {
    val spacing = ClearrDS.spacing
    val radii = ClearrDS.radii
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = spacing.xxxl)
        ) {
            // Illustration
            Box(
                modifier = Modifier
                    .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp80)
                    .clip(RoundedCornerShape(radii.xxl))
                    .background(ClearrColors.VioletBg),
                contentAlignment = Alignment.Center
            ) {
                Text("📋", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp36)
            }

            Spacer(Modifier.height(spacing.xl))

            Text(
                "No trackers yet",
                fontWeight = FontWeight.ExtraBold,
                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp20,
                color = ClearrColors.BrandText
            )

            Spacer(Modifier.height(spacing.sm))

            Text(
                "Create your first remittance to start tracking payments for your group.",
                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14,
                color = ClearrColors.TextSecondary,
                lineHeight = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp22,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(spacing.xxl + com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4))

            Button(
                onClick = onCreate,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(radii.md + com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2),
                contentPadding = PaddingValues(horizontal = spacing.xxl + com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4, vertical = spacing.md + com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)
            ) {
                Text("+ New Remittance", color = ClearrColors.Surface, fontWeight = FontWeight.Bold, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 500)
@Composable
private fun EmptyTrackerStatePreview() {
    ClearrTheme {
        EmptyTrackerState(onCreate = {})
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/components/RemittanceSwipeCard.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.trackerlist.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDS
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RemittanceSwipeCard(
    summary: TrackerSummary,
    onDeleteRequest: () -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val spacing = ClearrDS.spacing
    val radii = ClearrDS.radii
    val hintOffset = remember { Animatable(0f) }
    var hintShown by rememberSaveable(summary.trackerId) { mutableStateOf(false) }
    val hintAlpha = (kotlin.math.abs(hintOffset.value) / 64f).coerceIn(0f, 1f)

    LaunchedEffect(summary.trackerId, summary.isNew) {
        if (summary.isNew && !hintShown) {
            hintShown = true
            delay(250)
            hintOffset.animateTo(targetValue = -64f, animationSpec = tween(durationMillis = 280))
            delay(140)
            hintOffset.animateTo(targetValue = 0f, animationSpec = tween(durationMillis = 260))
        }
    }

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.35f },
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequest()
            }
            false
        }
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(radii.lg))
                .background(ClearrColors.BrandDanger)
                .padding(horizontal = spacing.xl),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "Delete",
                color = ClearrColors.Surface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer { alpha = hintAlpha }
            )
        }

        SwipeToDismissBox(
            state = dismissState,
            modifier = Modifier.graphicsLayer { translationX = hintOffset.value },
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(radii.lg))
                        .background(ClearrColors.BrandDanger)
                        .padding(horizontal = spacing.xl),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text("Delete", color = ClearrColors.Surface, fontWeight = FontWeight.Bold)
                }
            },
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true
        ) {
            TrackerCard(summary = summary, onClick = onClick, onLongPress = onLongPress)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RemittanceSwipeCardPreview() {
    ClearrTheme {
        RemittanceSwipeCard(
            summary = TrackerSummary(
                trackerId = 1L,
                name = "Church Remittance",
                type = TrackerType.DUES,
                frequency = Frequency.MONTHLY,
                currentPeriodLabel = "February 2026",
                totalMembers = 12,
                completedCount = 7,
                completionPercent = 58,
                isNew = true,
                createdAt = System.currentTimeMillis()
            ),
            onDeleteRequest = {},
            onClick = {},
            onLongPress = {}
        )
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/components/SummaryPill.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.trackerlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
internal fun SummaryPill(
    trackerCount: Int,
    totalMembers: Int,
    avgCompletion: Int,
    colors: DuesColors = LocalDuesColors.current
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8),
        horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)
    ) {
        PillItem(label = "Trackers", value = "$trackerCount", colors = colors, modifier = Modifier.weight(1f))
        PillItem(label = "Members", value = "$totalMembers", colors = colors, modifier = Modifier.weight(1f))
        PillItem(label = "Avg. Done", value = "$avgCompletion%", colors = colors, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PillItem(
    label: String,
    value: String,
    colors: DuesColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10))
            .background(colors.card)
            .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14, color = colors.accent)
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.muted)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryPillPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        SummaryPill(trackerCount = 3, totalMembers = 24, avgCompletion = 67, colors = colors)
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/components/TrackerCard.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.trackerlist.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.feature.trackerlist.extensions.displayName
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDS
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import com.mikeisesele.clearr.ui.theme.brandBackground
import com.mikeisesele.clearr.ui.theme.brandColor

// ── Type color palette ────────────────────────────────────────────────────────

internal data class TypeStyle(
    val icon: String,
    val color: Color,
    val bgColor: Color,
    val label: String
)

internal val typeStyles = mapOf(
    TrackerType.DUES to TypeStyle("₦", TrackerType.DUES.brandColor(), TrackerType.DUES.brandBackground(), "Remittance"),
    TrackerType.EXPENSES to TypeStyle("₦", TrackerType.DUES.brandColor(), TrackerType.DUES.brandBackground(), "Remittance"),
    TrackerType.GOALS to TypeStyle("🎯", TrackerType.GOALS.brandColor(), TrackerType.GOALS.brandBackground(), "Goals"),
    TrackerType.TODO to TypeStyle("☑", TrackerType.TODO.brandColor(), TrackerType.TODO.brandBackground(), "To-do"),
    TrackerType.BUDGET to TypeStyle("💳", TrackerType.BUDGET.brandColor(), TrackerType.BUDGET.brandBackground(), "Budget"),
)

internal val primaryColor = ClearrColors.BrandPrimary

// ─────────────────────────────────────────────────────────────────────────────
// TrackerCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun TrackerCard(
    summary: TrackerSummary,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val radii = ClearrDS.radii
    val spacing = ClearrDS.spacing
    val colors = LocalDuesColors.current
    val style = typeStyles[summary.type] ?: typeStyles[TrackerType.DUES]!!
    val allDone = summary.completedCount == summary.totalMembers && summary.totalMembers > 0
    val barColor = if (allDone) ClearrColors.BrandSecondary else style.color
    val pct = summary.completionPercent

    val animatedBarColor by animateColorAsState(
        targetValue = barColor,
        animationSpec = tween(400),
        label = "bar_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(summary.trackerId) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            },
        colors = CardDefaults.cardColors(containerColor = colors.card),
        shape = RoundedCornerShape(radii.lg),
        elevation = CardDefaults.cardElevation(defaultElevation = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)
    ) {
        Box {
            // NEW badge border tint
            if (summary.isNew) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(radii.lg))
                        .border(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2, style.color, RoundedCornerShape(radii.lg))
                )
            }

            Column(modifier = Modifier.padding(start = spacing.xl - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2, top = spacing.xl - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2, end = spacing.lg, bottom = spacing.xl - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    // ── Type icon square ──────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp40)
                            .clip(RoundedCornerShape(radii.md))
                            .background(style.bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            style.icon,
                            color = style.color,
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp18,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // ── Name + meta + progress bar ────────────────────────────
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            summary.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15,
                            color = colors.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(spacing.xxs))
                        Text(
                            "${summary.frequency.displayName()}  ·  ${summary.currentPeriodLabel}",
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12,
                            color = colors.muted
                        )
                    }

                    // ── Progress ring ─────────────────────────────────────────
                    Box(
                        modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp44),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { pct / 100f },
                            modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp44),
                            color = animatedBarColor,
                            trackColor = colors.border,
                            strokeWidth = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            "$pct%",
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp8,
                            fontWeight = FontWeight.Bold,
                            color = animatedBarColor
                        )
                    }
                }
            }

            // NEW badge
            if (summary.isNew) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(spacing.md - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)
                        .clip(RoundedCornerShape(radii.xl))
                        .background(style.color)
                        .padding(horizontal = spacing.sm - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1)
                ) {
                    Text(
                        "NEW",
                        color = ClearrColors.Surface,
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp8,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp0_5
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun TrackerCardPreview() {
    ClearrTheme {
        TrackerCard(
            summary = TrackerSummary(
                trackerId = 1L,
                name = "JSS Monthly Dues",
                type = TrackerType.DUES,
                frequency = Frequency.MONTHLY,
                currentPeriodLabel = "February 2026",
                totalMembers = 12,
                completedCount = 9,
                completionPercent = 75,
                isNew = true,
                createdAt = System.currentTimeMillis()
            ),
            onClick = {},
            onLongPress = {}
        )
    }
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/components/TrackerListDialogs.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.trackerlist.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.ui.theme.ClearrColors

@Composable
internal fun DeleteTrackerDialog(
    summary: TrackerSummary,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete ${summary.name}?") },
        text = { Text("This will remove the tracker and all members/records inside it.") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = ClearrColors.BrandDanger)) {
                Text("Delete")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
internal fun RenameTrackerDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Tracker Name") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tracker name") }
            )
        },
        confirmButton = {
            Button(enabled = value.isNotBlank(), onClick = onConfirm) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/feature/trackerlist/extensions/FrequencyExtensions.kt`

```kotlin
package com.mikeisesele.clearr.ui.feature.trackerlist.extensions

import com.mikeisesele.clearr.data.model.Frequency

internal fun Frequency.displayName(): String = when (this) {
    Frequency.MONTHLY -> "Monthly"
    Frequency.WEEKLY -> "Weekly"
    Frequency.QUARTERLY -> "Quarterly"
    Frequency.TERMLY -> "Termly"
    Frequency.BIANNUAL -> "Biannual"
    Frequency.ANNUAL -> "Annual"
    Frequency.CUSTOM -> "Custom"
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/navigation/DuesNavHost.kt`

```kotlin
package com.mikeisesele.clearr.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.commons.state.ThemeMode
import com.mikeisesele.clearr.ui.feature.budget.AddBudgetCategoryScreen
import com.mikeisesele.clearr.ui.feature.budget.BudgetDetailScreen
import com.mikeisesele.clearr.ui.feature.dashboard.DashboardScreen
import com.mikeisesele.clearr.ui.feature.goals.AddGoalScreen
import com.mikeisesele.clearr.ui.feature.goals.GoalsDetailScreen
import com.mikeisesele.clearr.ui.feature.home.HomeScreen
import com.mikeisesele.clearr.ui.feature.onboarding.CompletionScreen
import com.mikeisesele.clearr.ui.feature.onboarding.OnboardingAction
import com.mikeisesele.clearr.ui.feature.onboarding.OnboardingScreen
import com.mikeisesele.clearr.ui.feature.onboarding.OnboardingViewModel
import com.mikeisesele.clearr.ui.feature.onboarding.SplashScreen
import com.mikeisesele.clearr.ui.feature.settings.SettingsScreen
import com.mikeisesele.clearr.ui.feature.setup.SetupWizardScreen
import com.mikeisesele.clearr.ui.feature.todo.AddTodoScreen
import com.mikeisesele.clearr.ui.feature.todo.TodoDetailScreen
import com.mikeisesele.clearr.ui.feature.trackerlist.RemittanceHomeScreen
import com.mikeisesele.clearr.ui.navigation.components.AppBottomNav
import com.mikeisesele.clearr.ui.navigation.components.AppBottomNavItem
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
fun DuesNavHost(onThemeChange: (ThemeMode) -> Unit = {}) {
    val onboardingVm: OnboardingViewModel = hiltViewModel()
    val appConfigVm: AppConfigViewModel = hiltViewModel()

    val onboardingState by onboardingVm.uiState.collectAsStateWithLifecycle()
    val appConfigState by appConfigVm.uiState.collectAsStateWithLifecycle()
    val onboardingComplete = onboardingState.isComplete
    val appConfigLoading = appConfigState.isLoading

    val colors = LocalDuesColors.current

    if (onboardingComplete == null || appConfigLoading) {
        ApplySystemBars(darkIcons = false)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (onboardingComplete == null) ClearrColors.Violet else colors.bg)
        )
        return
    }

    if (onboardingComplete == false) {
        OnboardingNavHost(onboardingVm = onboardingVm)
    } else {
        MainNavHost(onThemeChange = onThemeChange)
    }
}

@Composable
private fun OnboardingNavHost(onboardingVm: OnboardingViewModel) {
    val navController = rememberNavController()
    val colors = LocalDuesColors.current
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val darkIcons = when (currentRoute) {
        "splash",
        "onboarding/{slideIndex}",
        "onboarding_complete" -> true
        "setup_wizard" -> !colors.isDark
        else -> !colors.isDark
    }
    ApplySystemBars(darkIcons = darkIcons)

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onGetStarted = { navController.navigate("onboarding/0") }
            )
        }

        composable(
            route = "onboarding/{slideIndex}",
            arguments = listOf(navArgument("slideIndex") { type = NavType.IntType })
        ) { backStack ->
            val initialSlide = backStack.arguments?.getInt("slideIndex") ?: 0
            OnboardingScreen(
                initialSlide = initialSlide,
                onComplete = {
                    onboardingVm.onAction(OnboardingAction.CompleteOnboarding)
                    navController.navigate("onboarding_complete") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onSkip = {
                    onboardingVm.onAction(OnboardingAction.CompleteOnboarding)
                    navController.navigate("onboarding_complete") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding_complete") {
            CompletionScreen(
                onCreateTracker = {
                    navController.navigate("setup_wizard") {
                        popUpTo("onboarding_complete") { inclusive = true }
                    }
                }
            )
        }

        composable("setup_wizard") {
            SetupWizardScreen(onSetupComplete = {})
        }
    }
}

@Composable
private fun MainNavHost(onThemeChange: (ThemeMode) -> Unit) {
    val navController = rememberNavController()
    val shellViewModel: AppShellViewModel = hiltViewModel()
    val shellState by shellViewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalDuesColors.current
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    ApplySystemBars(darkIcons = !colors.isDark)

    BackHandler(enabled = currentRoute.isTopLevelNonDashboardRoute()) {
        navController.navigate(NavRoutes.Dashboard.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = colors.bg,
        bottomBar = {
            if (currentRoute.isBottomNavRoute()) {
                AppBottomNav(
                    selectedItem = currentRoute?.toBottomNavItem(),
                    onSelect = { item ->
                        when (item) {
                            AppBottomNavItem.HOME -> navController.navigateTopLevel(NavRoutes.Dashboard.route)
                            AppBottomNavItem.BUDGET -> shellState.budgetTrackerId?.let { navController.navigateTopLevel(NavRoutes.BudgetRoot.createRoute(it)) }
                            AppBottomNavItem.TODOS -> shellState.todoTrackerId?.let { navController.navigateTopLevel(NavRoutes.TodoRoot.createRoute(it)) }
                            AppBottomNavItem.GOALS -> shellState.goalsTrackerId?.let { navController.navigateTopLevel(NavRoutes.GoalsRoot.createRoute(it)) }
                            AppBottomNavItem.REMITTANCE -> navController.navigateTopLevel(NavRoutes.RemittanceHome.route)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Surface(color = colors.bg) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.Dashboard.route,
                modifier = Modifier
                    .background(colors.bg)
                    .padding(innerPadding)
            ) {
                composable(NavRoutes.Dashboard.route) {
                    DashboardScreen(
                        onOpenBudget = { shellState.budgetTrackerId?.let { navController.navigate(NavRoutes.BudgetRoot.createRoute(it)) } },
                        onOpenTodos = { shellState.todoTrackerId?.let { navController.navigate(NavRoutes.TodoRoot.createRoute(it)) } },
                        onOpenGoals = { shellState.goalsTrackerId?.let { navController.navigate(NavRoutes.GoalsRoot.createRoute(it)) } },
                        onOpenRemittance = { navController.navigateTopLevel(NavRoutes.RemittanceHome.route) }
                    )
                }

                composable(NavRoutes.RemittanceHome.route) {
                    RemittanceHomeScreen(
                        onTrackerClick = { trackerId ->
                            navController.navigate(NavRoutes.TrackerDetail.createRoute(trackerId))
                        },
                        onCreateRemittance = { navController.navigate(NavRoutes.Setup.route) },
                        onOpenSettings = { navController.navigate(NavRoutes.Settings.route) }
                    )
                }

                composable(
                    route = NavRoutes.BudgetRoot.route,
                    arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                    BudgetDetailScreen(
                        trackerId = trackerId,
                        onAddCategory = { navController.navigate(NavRoutes.BudgetAddCategory.createRoute(trackerId)) }
                    )
                }

                composable(
                    route = NavRoutes.TodoRoot.route,
                    arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                    TodoDetailScreen(
                        trackerId = trackerId,
                        onAddTodo = { navController.navigate(NavRoutes.TodoAdd.createRoute(trackerId)) }
                    )
                }

                composable(
                    route = NavRoutes.GoalsRoot.route,
                    arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                    GoalsDetailScreen(
                        trackerId = trackerId,
                        onAddGoal = { navController.navigate(NavRoutes.GoalAdd.createRoute(trackerId)) }
                    )
                }

                composable(NavRoutes.Setup.route) {
                    SetupWizardScreen(
                        onSetupComplete = {
                            navController.navigateTopLevel(NavRoutes.RemittanceHome.route)
                        }
                    )
                }

                composable(
                    route = NavRoutes.TrackerDetail.route,
                    arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                    val detailVm: TrackerDetailHostViewModel = hiltViewModel()
                    val detailState by detailVm.uiState.collectAsStateWithLifecycle()
                    if (detailState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors.bg),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.accent)
                        }
                    } else if (detailState.trackerType == TrackerType.BUDGET) {
                        BudgetDetailScreen(
                            trackerId = trackerId,
                            onNavigateBack = { navController.popBackStack() },
                            onAddCategory = { navController.navigate(NavRoutes.BudgetAddCategory.createRoute(trackerId)) }
                        )
                    } else if (detailState.trackerType == TrackerType.TODO) {
                        TodoDetailScreen(
                            trackerId = trackerId,
                            onNavigateBack = { navController.popBackStack() },
                            onAddTodo = { navController.navigate(NavRoutes.TodoAdd.createRoute(trackerId)) }
                        )
                    } else if (detailState.trackerType == TrackerType.GOALS) {
                        GoalsDetailScreen(
                            trackerId = trackerId,
                            onNavigateBack = { navController.popBackStack() },
                            onAddGoal = { navController.navigate(NavRoutes.GoalAdd.createRoute(trackerId)) }
                        )
                    } else {
                        HomeScreen(
                            trackerId = trackerId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(
                    route = NavRoutes.TodoAdd.route,
                    arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                    AddTodoScreen(
                        trackerId = trackerId,
                        onClose = { navController.popBackStack() }
                    )
                }

                composable(
                    route = NavRoutes.GoalAdd.route,
                    arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                    AddGoalScreen(
                        trackerId = trackerId,
                        onClose = { navController.popBackStack() }
                    )
                }

                composable(
                    route = NavRoutes.BudgetAddCategory.route,
                    arguments = listOf(navArgument("trackerId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val trackerId = backStackEntry.arguments?.getLong("trackerId") ?: return@composable
                    AddBudgetCategoryScreen(
                        trackerId = trackerId,
                        onClose = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.Settings.route) {
                    SettingsScreen(onThemeChange = onThemeChange)
                }
            }
        }
    }
}

private fun String?.isTopLevelNonDashboardRoute(): Boolean = when {
    this == null -> false
    this == NavRoutes.RemittanceHome.route -> true
    this == NavRoutes.Settings.route -> true
    this.startsWith(NavRoutes.BudgetRoot.baseRoute) -> true
    this.startsWith(NavRoutes.TodoRoot.baseRoute) -> true
    this.startsWith(NavRoutes.GoalsRoot.baseRoute) -> true
    else -> false
}

private fun String.toBottomNavItem(): AppBottomNavItem? = when {
    this == NavRoutes.Dashboard.route -> AppBottomNavItem.HOME
    this.startsWith(NavRoutes.BudgetRoot.baseRoute) -> AppBottomNavItem.BUDGET
    this.startsWith(NavRoutes.TodoRoot.baseRoute) -> AppBottomNavItem.TODOS
    this.startsWith(NavRoutes.GoalsRoot.baseRoute) -> AppBottomNavItem.GOALS
    this == NavRoutes.RemittanceHome.route -> AppBottomNavItem.REMITTANCE
    else -> null
}

private fun androidx.navigation.NavController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}


private fun String?.isBottomNavRoute(): Boolean = when {
    this == null -> false
    this == NavRoutes.Dashboard.route -> true
    this == NavRoutes.RemittanceHome.route -> true
    this.startsWith(NavRoutes.BudgetRoot.baseRoute) -> true
    this.startsWith(NavRoutes.TodoRoot.baseRoute) -> true
    this.startsWith(NavRoutes.GoalsRoot.baseRoute) -> true
    else -> false
}
```

## `app/src/main/java/com/mikeisesele/clearr/ui/navigation/TrackerDetailHostState.kt`

```kotlin
package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.TrackerType

data class TrackerDetailHostState(
    val isLoading: Boolean = true,
    val trackerType: TrackerType = TrackerType.DUES
) : BaseState

sealed interface TrackerDetailHostAction

sealed interface TrackerDetailHostEvent : ViewEvent
```

## `app/src/main/java/com/mikeisesele/clearr/ui/navigation/TrackerDetailHostViewModel.kt`

```kotlin
package com.mikeisesele.clearr.ui.navigation

import androidx.lifecycle.SavedStateHandle
import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.domain.repository.DuesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class TrackerDetailHostViewModel @Inject constructor(
    private val repository: DuesRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<TrackerDetailHostState, TrackerDetailHostAction, TrackerDetailHostEvent>(
    initialState = TrackerDetailHostState()
) {

    private val trackerId = checkNotNull(savedStateHandle.get<Long>("trackerId"))

    init {
        launch {
            repository.getTrackerByIdFlow(trackerId).collectLatest { tracker ->
                if (tracker == null) {
                    updateState { it.copy(isLoading = false) }
                    return@collectLatest
                }
                updateState {
                    it.copy(
                        isLoading = false,
                        trackerType = tracker.type
                    )
                }
            }
        }
    }

    override fun onAction(action: TrackerDetailHostAction) = Unit
}
```

