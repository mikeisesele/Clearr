package com.mikeisesele.clearr.ui.feature.budget.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mikeisesele.clearr.ui.feature.budget.BudgetPlanDraft
import com.mikeisesele.clearr.ui.feature.budget.previews.previewBudgetPlanDrafts
import com.mikeisesele.clearr.ui.feature.budget.utils.formatKobo
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors
import com.mikeisesele.clearr.ui.theme.fromToken

@Composable
internal fun BudgetPlanSetupDialog(
    periodLabel: String?,
    sourceLabel: String?,
    drafts: List<BudgetPlanDraft>,
    onDismiss: () -> Unit,
    onAmountChange: (Long, Double) -> Unit,
    onConfirm: () -> Unit
) {
    val colors = LocalClearrUiColors.current
    val totalPlanned = drafts.sumOf { it.plannedAmountKobo }

    BackHandler(onBack = onDismiss)
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.text.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .width(520.dp),
                shape = RoundedCornerShape(ClearrDimens.dp20),
                color = colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp12)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.size(ClearrDimens.dp34))
                        Text(
                            text = periodLabel?.let { "Set $it budget" } ?: "Set month budget",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = ClearrTextSizes.sp16,
                            color = colors.text
                        )
                        Surface(
                            modifier = Modifier.size(ClearrDimens.dp34).clickable(onClick = onDismiss),
                            shape = RoundedCornerShape(ClearrDimens.dp10),
                            color = colors.card
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.muted)
                            }
                        }
                    }
                    Spacer(Modifier.height(ClearrDimens.dp10))
                    Text(
                        text = sourceLabel?.let { "Copied from $it. Adjust any amount for this period." }
                            ?: "Set how much you plan to spend across categories for this period.",
                        fontSize = ClearrTextSizes.sp12,
                        color = colors.muted
                    )
                    Spacer(Modifier.height(ClearrDimens.dp14))

                    drafts.forEach { draft ->
                        val token = ClearrColors.fromToken(draft.colorToken)
                        var amountInput by rememberSaveable(draft.categoryId, draft.plannedAmountKobo) {
                            mutableStateOf(if (draft.plannedAmountKobo == 0L) "" else (draft.plannedAmountKobo / 100).toString())
                        }

                        Surface(color = colors.card, shape = RoundedCornerShape(ClearrDimens.dp14), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(horizontal = ClearrDimens.dp12, vertical = ClearrDimens.dp10),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp12)
                            ) {
                                Surface(color = token.background, shape = RoundedCornerShape(ClearrDimens.dp10), modifier = Modifier.size(ClearrDimens.dp36)) {
                                    Box(contentAlignment = Alignment.Center) { Text(draft.icon, fontSize = ClearrTextSizes.sp18) }
                                }
                                Text(
                                    text = draft.name,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = ClearrTextSizes.sp12,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.text
                                )
                                Surface(
                                    color = colors.surface,
                                    shape = RoundedCornerShape(ClearrDimens.dp10),
                                    border = BorderStroke(1.dp, colors.border),
                                    modifier = Modifier.width(112.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = ClearrDimens.dp10, vertical = ClearrDimens.dp8),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp4)
                                    ) {
                                        Text("₦", color = colors.muted, fontSize = ClearrTextSizes.sp13, fontWeight = FontWeight.SemiBold)
                                        BasicTextField(
                                            value = amountInput,
                                            onValueChange = { next ->
                                                amountInput = next.filter { it.isDigit() }
                                                onAmountChange(draft.categoryId, amountInput.toDoubleOrNull() ?: 0.0)
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                color = colors.text,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = ClearrTextSizes.sp13
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            cursorBrush = SolidColor(colors.accent),
                                            decorationBox = { inner ->
                                                if (amountInput.isBlank()) {
                                                    Text("0", color = colors.muted.copy(alpha = 0.7f), fontSize = ClearrTextSizes.sp13)
                                                }
                                                inner()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(ClearrDimens.dp10))
                    }

                    Surface(color = colors.card, shape = RoundedCornerShape(ClearrDimens.dp14), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = ClearrDimens.dp14, vertical = ClearrDimens.dp12),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total planned", color = colors.muted, fontSize = ClearrTextSizes.sp12)
                            Text(formatKobo(totalPlanned), color = colors.text, fontSize = ClearrTextSizes.sp16, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(ClearrDimens.dp16))
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ClearrDimens.dp14),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                        contentPadding = PaddingValues(vertical = ClearrDimens.dp16)
                    ) {
                        Text("Save Month Budget", color = com.mikeisesele.clearr.ui.theme.ClearrColors.Surface, fontSize = ClearrTextSizes.sp15, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun BudgetPlanSetupDialogPreview() {
    ClearrTheme {
        BudgetPlanSetupDialog(
            periodLabel = "Feb 2026",
            sourceLabel = "Jan 2026",
            drafts = previewBudgetPlanDrafts,
            onDismiss = {},
            onAmountChange = { _, _ -> },
            onConfirm = {}
        )
    }
}
