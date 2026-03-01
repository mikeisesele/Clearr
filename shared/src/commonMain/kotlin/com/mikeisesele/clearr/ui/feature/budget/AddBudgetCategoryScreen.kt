package com.mikeisesele.clearr.ui.feature.budget

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
import com.mikeisesele.clearr.ui.commons.components.PlatformBackHandler
import com.mikeisesele.clearr.ui.feature.budget.utils.BudgetCategoryPreset
import com.mikeisesele.clearr.ui.feature.budget.utils.budgetCategoryPresets
import com.mikeisesele.clearr.ui.feature.budget.utils.formatKobo
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.ClearrUiColors
import com.mikeisesele.clearr.ui.theme.fromToken

@Composable
fun AddBudgetCategoryScreen(
    state: BudgetUiState,
    colors: ClearrUiColors,
    onClose: () -> Unit,
    onAddCategory: (name: String, icon: String, colorToken: String, plannedAmountNaira: Double) -> Unit
) {
    if (state.trackerId < 0) return

    var selectedPreset by remember { mutableStateOf<BudgetCategoryPreset?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        ClearrTopBar(
            title = "Select Category",
            onLeadingClick = onClose
        )

        Spacer(Modifier.height(ClearrDimens.dp12))

        Surface(
            color = colors.surface,
            shape = RoundedCornerShape(ClearrDimens.dp14),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ClearrDimens.dp16)
        ) {
            Column {
                budgetCategoryPresets.forEachIndexed { index, preset ->
                    val token = ClearrColors.fromToken(preset.colorToken)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPreset = preset }
                            .padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp13),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp12)
                    ) {
                        Surface(
                            color = token.background,
                            shape = RoundedCornerShape(ClearrDimens.dp10),
                            modifier = Modifier.size(ClearrDimens.dp36)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(preset.icon, fontSize = ClearrTextSizes.sp18)
                            }
                        }
                        Text(
                            preset.name,
                            modifier = Modifier.weight(1f),
                            fontSize = ClearrTextSizes.sp15,
                            color = colors.text
                        )
                        Text("›", color = colors.muted)
                    }
                    if (index < budgetCategoryPresets.lastIndex) {
                        HorizontalDivider(
                            color = colors.border,
                            modifier = Modifier.padding(start = ClearrDimens.dp64)
                        )
                    }
                }
            }
        }
    }

    selectedPreset?.let { preset ->
        AddCategoryDetailDialog(
            preset = preset,
            colors = colors,
            onDismiss = { selectedPreset = null },
            onAdd = { name, amountNaira ->
                onAddCategory(name, preset.icon, preset.colorToken, amountNaira)
                selectedPreset = null
                onClose()
            }
        )
    }
}

@Composable
internal fun AddCategoryDetailDialog(
    preset: BudgetCategoryPreset,
    colors: ClearrUiColors,
    onDismiss: () -> Unit,
    onAdd: (name: String, plannedAmountNaira: Double) -> Unit
) {
    val token = ClearrColors.fromToken(preset.colorToken)
    var name by rememberSaveable(preset.name) {
        mutableStateOf(if (preset.name == "Custom") "" else preset.name)
    }
    var plannedAmount by rememberSaveable(preset.name) { mutableStateOf("") }
    val amountNaira = plannedAmount.toDoubleOrNull() ?: 0.0
    val canAdd = name.isNotBlank()

    PlatformBackHandler(onBack = onDismiss)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.text.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ClearrDimens.dp20),
                color = colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp12)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.size(ClearrDimens.dp34))
                        Text(
                            "Category Details",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = ClearrTextSizes.sp16,
                            color = colors.text
                        )
                        Surface(
                            modifier = Modifier
                                .size(ClearrDimens.dp34)
                                .clickable(onClick = onDismiss),
                            shape = RoundedCornerShape(ClearrDimens.dp10),
                            color = colors.card
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = colors.muted
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(ClearrDimens.dp10))

                    Surface(
                        color = colors.card,
                        shape = RoundedCornerShape(ClearrDimens.dp16),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = ClearrDimens.dp16)
                    ) {
                        Row(
                            modifier = Modifier.padding(ClearrDimens.dp16),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp12)
                        ) {
                            Surface(
                                color = token.background,
                                shape = RoundedCornerShape(ClearrDimens.dp12),
                                modifier = Modifier.size(ClearrDimens.dp44)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(preset.icon, fontSize = ClearrTextSizes.sp22)
                                }
                            }
                            Column {
                                Text(
                                    name.ifBlank { "Category name" },
                                    fontSize = ClearrTextSizes.sp16,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.text
                                )
                                Text(
                                    if (plannedAmount.isBlank()) {
                                        "Starts at ₦0"
                                    } else {
                                        "Budget: ${formatKobo((amountNaira * 100).toLong())}"
                                    },
                                    fontSize = ClearrTextSizes.sp12,
                                    color = colors.muted
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ClearrDimens.dp10)
                    )
                    Spacer(Modifier.height(ClearrDimens.dp10))
                    OutlinedTextField(
                        value = plannedAmount,
                        onValueChange = { plannedAmount = it.filter(Char::isDigit) },
                        label = { Text("Monthly Budget (₦)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ClearrDimens.dp10),
                        leadingIcon = {
                            Text("₦", fontWeight = FontWeight.Bold, color = colors.muted)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(ClearrDimens.dp16))
                    Button(
                        onClick = { onAdd(name.trim(), amountNaira) },
                        enabled = canAdd,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ClearrDimens.dp14),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            disabledContainerColor = colors.border
                        ),
                        contentPadding = PaddingValues(vertical = ClearrDimens.dp16)
                    ) {
                        Text("Add", color = ClearrColors.Surface, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
