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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.commons.state.ThemeMode
import com.mikeisesele.clearr.ui.commons.util.currentYear
import com.mikeisesele.clearr.ui.commons.util.formatAmount
import com.mikeisesele.clearr.ui.feature.settings.components.SectionCard
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = colors.text)

        // ── Active Year ───────────────────────────────────────────────────────
        SectionCard(title = "Active Year", colors = colors) {
            Text("Applies to the tracker, reminders, and analytics.", style = MaterialTheme.typography.bodySmall, color = colors.muted)
            Spacer(Modifier.height(10.dp))
            Box {
                OutlinedButton(
                    onClick = { yearMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, colors.accent),
                    shape = RoundedCornerShape(10.dp)
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
        SectionCard(title = "Due Amount", colors = colors) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
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
            Spacer(Modifier.height(4.dp))
            Text("Current: ${formatAmount(dueAmount)} / member / period", style = MaterialTheme.typography.bodySmall, color = colors.muted)
            if (dueEditable) {
                Text("Applies only to the current Dues tracker.", style = MaterialTheme.typography.bodySmall, color = colors.dim)
            } else {
                Text("Open a Dues tracker to edit due amount.", style = MaterialTheme.typography.bodySmall, color = colors.dim)
            }
        }

        // ── Layout Style ──────────────────────────────────────────────────────
        SectionCard(title = "Tracker Layout", colors = colors) {
            Text(
                "Choose how the current tracker screen displays data. Changes apply instantly to that tracker only.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted
            )
            Spacer(Modifier.height(12.dp))

            val layoutOptions = listOf(
                LayoutStyle.GRID    to Triple("⊞", "Grid",    "Compact scrollable table — members × months"),
                LayoutStyle.KANBAN  to Triple("🗂️", "Kanban", "Month columns with member status chips"),
                LayoutStyle.CARDS   to Triple("🃏", "Cards",  "One card per member with month breakdown"),
                LayoutStyle.RECEIPT to Triple("🧾", "Receipt","Detailed financial ledger per member")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                layoutOptions.forEach { (style, info) ->
                    val (icon, title, desc) = info
                    val selected = state.layoutStyle == style
                    val borderColor = if (selected) colors.accent else colors.border
                    val bgColor = if (selected) colors.accent.copy(alpha = 0.08f) else Color.Transparent

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .border(width = if (selected) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(10.dp))
                            .clickable { viewModel.onAction(SettingsAction.SetLayoutStyle(style)) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(icon, fontSize = 22.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = if (selected) colors.accent else colors.text)
                            Text(desc, style = MaterialTheme.typography.labelSmall, color = colors.muted)
                        }
                        if (selected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // ── Appearance ────────────────────────────────────────────────────────
        SectionCard(title = "Appearance", colors = colors) {
            Text("Theme", style = MaterialTheme.typography.bodyMedium, color = colors.muted)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    val selected = state.themeMode == mode
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onAction(SettingsAction.SetThemeMode(mode)); onThemeChange(mode) },
                        label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.accent,
                            selectedLabelColor = Color.White,
                            containerColor = colors.card,
                            labelColor = colors.muted
                        )
                    )
                }
            }
        }

        // ── App Info ──────────────────────────────────────────────────────────
        SectionCard(title = "App Info", colors = colors) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Version", style = MaterialTheme.typography.bodyMedium, color = colors.text)
                Text("1.0", style = MaterialTheme.typography.bodyMedium, color = colors.muted)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { viewModel.onAction(SettingsAction.ResetSetup) },
                border = BorderStroke(1.dp, colors.accent),
                modifier = Modifier.fillMaxWidth()
            ) { Text("⚙️  Re-run Setup Wizard", color = colors.accent) }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showResetDialog = true },
                border = BorderStroke(1.dp, colors.red),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Reset All Data", color = colors.red) }
        }

        Spacer(Modifier.height(80.dp))
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
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        Column(
            modifier = Modifier.fillMaxSize().background(colors.bg).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = colors.text)
            SectionCard(title = "Active Year", colors = colors) {
                Text("2026", color = colors.text)
            }
        }
    }
}
