package com.mikeisesele.clearr.ui.feature.budget.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mikeisesele.clearr.data.model.CategorySummary
import com.mikeisesele.clearr.ui.commons.components.PlatformBackHandler
import com.mikeisesele.clearr.ui.feature.budget.utils.formatKobo
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors
import com.mikeisesele.clearr.ui.theme.fromToken

@Composable
internal fun LogExpenseDialog(
    allCategories: List<CategorySummary>,
    preselectedCategory: CategorySummary?,
    onDismiss: () -> Unit,
    onSave: (category: CategorySummary, amountNaira: Double, note: String?) -> Unit
) {
    val colors = LocalClearrUiColors.current
    var amount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(preselectedCategory) }
    val amountNaira = amount.toDoubleOrNull() ?: 0.0
    val amountKobo = (amountNaira * 100).toLong()
    val canSave = amountNaira > 0.0 && selectedCategory != null
    val hasAmount = amountNaira > 0.0
    val amountFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { amountFocusRequester.requestFocus() }

    PlatformBackHandler(onBack = onDismiss)
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier.fillMaxSize().background(colors.text.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(ClearrDimens.dp20), color = colors.surface) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp12)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.size(ClearrDimens.dp34))
                        Text("Log Expense", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = ClearrTextSizes.sp16, color = colors.text)
                        Surface(modifier = Modifier.size(ClearrDimens.dp34).clickable(onClick = onDismiss), shape = RoundedCornerShape(ClearrDimens.dp10), color = colors.card) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.muted) }
                        }
                    }

                    Spacer(Modifier.height(ClearrDimens.dp10))
                    val token = selectedCategory?.let { ClearrColors.fromToken(it.category.colorToken) }
                    Surface(
                        color = colors.card,
                        shape = RoundedCornerShape(ClearrDimens.dp16),
                        modifier = Modifier.fillMaxWidth(),
                        border = token?.let { BorderStroke(width = 1.5.dp, color = it.color.copy(alpha = 0.25f)) }
                    ) {
                        Column(modifier = Modifier.padding(ClearrDimens.dp16)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp10)) {
                                Surface(
                                    shape = RoundedCornerShape(ClearrDimens.dp14),
                                    color = if (hasAmount && token != null) token.color.copy(alpha = 0.15f) else colors.surface,
                                    modifier = Modifier.size(ClearrDimens.dp44)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("₦", fontSize = ClearrTextSizes.sp20, color = if (hasAmount && token != null) token.color else colors.muted)
                                    }
                                }

                                BasicTextField(
                                    value = amount,
                                    onValueChange = { amount = it.filter(Char::isDigit) },
                                    modifier = Modifier.weight(1f).focusRequester(amountFocusRequester),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    cursorBrush = SolidColor(colors.text),
                                    textStyle = MaterialTheme.typography.headlineLarge.copy(color = colors.text, letterSpacing = (-1).sp),
                                    decorationBox = { inner ->
                                        Box(contentAlignment = Alignment.CenterStart) {
                                            if (amount.isBlank()) {
                                                Text("0", fontSize = 44.sp, color = colors.muted, fontWeight = FontWeight.Black)
                                            }
                                            inner()
                                        }
                                    }
                                )
                            }

                            selectedCategory?.let { category ->
                                if (hasAmount) {
                                    val projectedRemainingKobo = category.remainingAmountKobo - amountKobo
                                    val isOverBudget = projectedRemainingKobo < 0L
                                    Spacer(Modifier.height(ClearrDimens.dp12))
                                    Surface(
                                        color = if (isOverBudget) ClearrColors.BrandDanger.copy(alpha = 0.12f) else colors.green.copy(alpha = 0.10f),
                                        shape = RoundedCornerShape(ClearrDimens.dp10),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = ClearrDimens.dp12, vertical = ClearrDimens.dp8),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)
                                        ) {
                                            Text(if (isOverBudget) "⚠️" else "✅", fontSize = ClearrTextSizes.sp13)
                                            Text(
                                                text = if (isOverBudget) {
                                                    "Over budget in ${category.category.name}"
                                                } else {
                                                    "${formatKobo(projectedRemainingKobo.coerceAtLeast(0L))} remaining in ${category.category.name}"
                                                },
                                                fontSize = ClearrTextSizes.sp12,
                                                color = if (isOverBudget) ClearrColors.BrandDanger else colors.green
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(ClearrDimens.dp16))
                    Text("CATEGORY", fontSize = ClearrTextSizes.sp11, color = colors.muted)
                    Spacer(Modifier.height(ClearrDimens.dp10))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)) {
                        items(allCategories, key = { it.category.id }) { category ->
                            val categoryToken = ClearrColors.fromToken(category.category.colorToken)
                            val active = selectedCategory?.category?.id == category.category.id
                            Surface(
                                color = if (active) categoryToken.color else categoryToken.background,
                                shape = RoundedCornerShape(ClearrDimens.dp99),
                                modifier = Modifier.clickable { selectedCategory = category }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = ClearrDimens.dp12, vertical = ClearrDimens.dp7),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp6)
                                ) {
                                    Text(category.category.icon, fontSize = ClearrTextSizes.sp14)
                                    Text(category.category.name, fontSize = ClearrTextSizes.sp13, color = if (active) ClearrColors.Surface else categoryToken.color)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(ClearrDimens.dp16))
                    Text("NOTE (OPTIONAL)", fontSize = ClearrTextSizes.sp11, color = colors.muted)
                    Spacer(Modifier.height(ClearrDimens.dp8))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Grocery run, fuel...", color = colors.muted) },
                        singleLine = true,
                        shape = RoundedCornerShape(ClearrDimens.dp10)
                    )

                    Spacer(Modifier.height(ClearrDimens.dp16))
                    Button(
                        onClick = { selectedCategory?.let { onSave(it, amountNaira, note.ifBlank { null }) } },
                        enabled = canSave,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ClearrDimens.dp14),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.green, disabledContainerColor = colors.border),
                        contentPadding = PaddingValues(vertical = ClearrDimens.dp16)
                    ) {
                        Text(
                            text = if (canSave) "Log ${formatKobo((amountNaira * 100).toLong())} to ${selectedCategory!!.category.name}" else "Select a category to continue",
                            fontSize = ClearrTextSizes.sp15,
                            color = if (canSave) ClearrColors.Surface else colors.muted
                        )
                    }
                }
            }
        }
    }
}
