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
    onThemeChange: (ThemeMode) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalDuesColors.current

    val currentYearConfig = state.yearConfigs.find { it.year == state.selectedYear }
    val dueAmount = state.currentTrackerDueAmount ?: currentYearConfig?.dueAmountPerMonth ?: 5000.0
    val dueEditable = state.currentTrackerType == TrackerType.DUES

    var localDue by remember(state.selectedYear, dueAmount) { mutableStateOf(dueAmount.toInt().toString()) }
    var showResetDialog by remember { mutableStateOf(false) }
    var yearMenuExpanded by remember { mutableStateOf(false) }

    val cy = currentYear()
    val availableYears = remember(cy) { (cy until cy + 10).toList() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
        verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
    ) {
        ClearrTopBar(
            title = "Settings",
            leadingIcon = "⚙️",
            onLeadingClick = null,
            actionIcon = null,
            onActionClick = null
        )

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

        // ── Appearance ────────────────────────────────────────────────────────
        SectionCard(
            title = "Appearance",
            colors = colors,
            modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
        ) {
            Text("Theme", style = MaterialTheme.typography.bodyMedium, color = colors.muted)
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
            Row(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)) {
                ThemeMode.entries.forEach { mode ->
                    val selected = state.themeMode == mode
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onAction(SettingsAction.SetThemeMode(mode)); onThemeChange(mode) },
                        label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.accent,
                            selectedLabelColor = ClearrColors.Surface,
                            containerColor = colors.card,
                            labelColor = colors.muted
                        )
                    )
                }
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
