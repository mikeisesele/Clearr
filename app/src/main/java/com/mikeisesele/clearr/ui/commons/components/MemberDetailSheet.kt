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
