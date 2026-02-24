package com.mikeisesele.clearr.ui.feature.setup

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.theme.ClearrColors
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        // ── Progress bar ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .statusBarsPadding()
                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Setup Wizard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.text
                )
                Text(
                    "Step ${displayStep + 1} of $totalSteps",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted
                )
            }
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10))
            LinearProgressIndicator(
                progress = { (displayStep + 1f) / totalSteps },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4)
                    .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)),
                color = colors.accent,
                trackColor = colors.border
            )
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10))
            Row(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)) {
                repeat(totalSteps) { i ->
                    Box(
                        modifier = Modifier
                            .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)
                            .clip(CircleShape)
                            .background(if (i <= displayStep) colors.accent else colors.border)
                    )
                }
            }
        }
        HorizontalDivider(color = colors.border)

        // ── Step content ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "wizard_step"
            ) { step ->
                when (step) {
                    1 -> GroupInfoStep(
                        groupName = state.groupName,
                        trackerName = state.trackerName,
                        adminName = state.adminName,
                        adminPhone = state.adminPhone,
                        loadSampleMembers = state.loadSampleMembers,
                            onGroupName = { viewModel.onAction(SetupAction.SetGroupName(it)) },
                            onTrackerName = { viewModel.onAction(SetupAction.SetTrackerName(it)) },
                            onAdminName = { viewModel.onAction(SetupAction.SetAdminName(it)) },
                            onAdminPhone = { viewModel.onAction(SetupAction.SetAdminPhone(it)) },
                            onLoadSampleMembers = { viewModel.onAction(SetupAction.SetLoadSampleMembers(it)) },
                        colors = colors
                    )
                    2 -> FrequencyStep(
                        selected = state.frequency,
                            onSelect = { viewModel.onAction(SetupAction.SetFrequency(it)) },
                        colors = colors
                    )
                    3 -> AmountStep(
                        amount = state.defaultAmount,
                        frequency = state.frequency,
                        trackerType = state.trackerType,
                            onAmount = { viewModel.onAction(SetupAction.SetDefaultAmount(it)) },
                        colors = colors
                    )
                    4 -> LayoutStyleStep(
                        selected = state.layoutStyle,
                            onSelect = { viewModel.onAction(SetupAction.SetLayoutStyle(it)) },
                        colors = colors
                    )
                    5 -> ReviewStep(
                        groupName = state.groupName,
                        trackerName = state.trackerName,
                        trackerType = state.trackerType,
                        frequency = state.frequency,
                        layoutStyle = state.layoutStyle,
                        defaultAmount = state.defaultAmount,
                        loadSampleMembers = state.loadSampleMembers,
                        colors = colors
                    )
                    else -> GroupInfoStep(
                        groupName = state.groupName,
                        trackerName = state.trackerName,
                        adminName = state.adminName,
                        adminPhone = state.adminPhone,
                        loadSampleMembers = state.loadSampleMembers,
                        onGroupName = { viewModel.onAction(SetupAction.SetGroupName(it)) },
                        onTrackerName = { viewModel.onAction(SetupAction.SetTrackerName(it)) },
                        onAdminName = { viewModel.onAction(SetupAction.SetAdminName(it)) },
                        onAdminPhone = { viewModel.onAction(SetupAction.SetAdminPhone(it)) },
                        onLoadSampleMembers = { viewModel.onAction(SetupAction.SetLoadSampleMembers(it)) },
                        colors = colors
                    )
                }
            }
        }

        // ── Navigation buttons ────────────────────────────────────────────────
        HorizontalDivider(color = colors.border)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { viewModel.onAction(SetupAction.PrevStep) },
                enabled = state.step > 1,
                border = androidx.compose.foundation.BorderStroke(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1, colors.border)
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = colors.text)
                Text("Back", color = colors.text)
            }

            if (state.step < finalStep) {
                Button(
                    onClick = { viewModel.onAction(SetupAction.NextStep) },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text("Next", color = ClearrColors.Surface)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ClearrColors.Surface)
                }
            } else {
                Button(
                    onClick = { viewModel.onAction(SetupAction.FinishSetup(onSetupComplete)) },
                    enabled = !state.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.green)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp18),
                            color = ClearrColors.Surface,
                            strokeWidth = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, tint = ClearrColors.Surface)
                        Spacer(Modifier.width(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4))
                        Text("Finish Setup", color = ClearrColors.Surface)
                    }
                }
            }
        }
    }
}

// ── Step 1: Group Info ────────────────────────────────────────────────────────
@Composable
private fun GroupInfoStep(
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
    colors: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24),
        verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
    ) {
        StepHeader("Group Information", "Tell us about your group.", colors)
        WizardTextField(value = groupName, onValueChange = onGroupName, label = "Group / Organisation Name", colors = colors)
        WizardTextField(value = trackerName, onValueChange = onTrackerName, label = "Tracker Name (e.g. Task Tracker, Event Tracker)", colors = colors)
        WizardTextField(value = adminName, onValueChange = onAdminName, label = "Admin Name (optional)", colors = colors)
        WizardTextField(value = adminPhone, onValueChange = onAdminPhone, label = "Admin Phone (optional)", keyboardType = KeyboardType.Phone, colors = colors)
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.card),
            shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Load sample members (dev)", color = colors.text, fontWeight = FontWeight.SemiBold)
                    Text("Seeds names like Michael, Simon, Henry into new dues tracker.", style = MaterialTheme.typography.bodySmall, color = colors.muted)
                }
                Switch(
                    checked = loadSampleMembers,
                    onCheckedChange = onLoadSampleMembers,
                    colors = SwitchDefaults.colors(checkedTrackColor = colors.accent, checkedThumbColor = ClearrColors.Surface)
                )
            }
        }
    }
}

// ── Step 2: Frequency ─────────────────────────────────────────────────────────
@Composable
private fun FrequencyStep(
    selected: Frequency,
    onSelect: (Frequency) -> Unit,
    colors: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    val options = listOf(
        Frequency.MONTHLY to Pair("📅", "Monthly – 12 periods per year (Jan – Dec)"),
        Frequency.WEEKLY to Pair("🗓️", "Weekly – 52 periods per year"),
        Frequency.QUARTERLY to Pair("📆", "Quarterly – 4 periods (Q1–Q4)"),
        Frequency.TERMLY to Pair("🏫", "Termly – 3 periods (Term 1–3)"),
        Frequency.BIANNUAL to Pair("🔄", "Bi-annual – 2 periods per year"),
        Frequency.ANNUAL to Pair("🎯", "Annual – 1 period per year"),
        Frequency.CUSTOM to Pair("🛠️", "Custom – Define your own period labels")
    )
    Column(modifier = Modifier.fillMaxWidth().padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24), verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10)) {
        StepHeader("How often do you meet / collect?", "This determines how many periods appear in your tracker.", colors)
        options.forEach { (freq, info) ->
            val (icon, desc) = info
            SelectionCard(
                icon = icon,
                title = freq.name.lowercase().replaceFirstChar { it.uppercase() },
                description = desc,
                selected = selected == freq,
                onClick = { onSelect(freq) },
                colors = colors
            )
        }
    }
}

// ── Step 4: Amount ────────────────────────────────────────────────────────────
@Composable
private fun AmountStep(
    amount: String,
    frequency: Frequency,
    trackerType: TrackerType,
    onAmount: (String) -> Unit,
    colors: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    val label = when (trackerType) {
        TrackerType.DUES -> "Amount per ${frequency.name.lowercase().replaceFirstChar { it.uppercase() }} (₦)"
        TrackerType.GOALS, TrackerType.TODO, TrackerType.BUDGET -> "Not applicable for this tracker type"
    }
    val skipAmount = trackerType != TrackerType.DUES
    Column(modifier = Modifier.fillMaxWidth().padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24), verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)) {
        StepHeader("Set the Amount", "How much is due per period per member?", colors)
        if (skipAmount) {
            Card(colors = CardDefaults.cardColors(containerColor = colors.card), shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "No amount required for ${trackerType.name.lowercase()} tracking. You can skip this step.",
                    modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
                    color = colors.muted,
                    style = MaterialTheme.typography.bodyMedium
                )
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

// ── Step 5: Layout Style ──────────────────────────────────────────────────────
@Composable
private fun LayoutStyleStep(
    selected: LayoutStyle,
    onSelect: (LayoutStyle) -> Unit,
    colors: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    val options = listOf(
        LayoutStyle.GRID to Triple("⊞", "Grid", "Compact scrollable table – members × periods"),
        LayoutStyle.KANBAN to Triple("🗂️", "Kanban", "Period columns with member cards – great for small groups"),
        LayoutStyle.CARDS to Triple("🃏", "Cards", "One card per member showing all periods"),
        LayoutStyle.RECEIPT to Triple("🧾", "Receipt / Ledger", "Detailed financial ledger style")
    )
    Column(modifier = Modifier.fillMaxWidth().padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24), verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12)) {
        StepHeader("Choose a Layout Style", "Pick how you want to view your tracker. You can change this anytime.", colors)
        options.forEach { (style, info) ->
            val (icon, title, desc) = info
            SelectionCard(icon = icon, title = title, description = desc, selected = selected == style, onClick = { onSelect(style) }, colors = colors)
        }
    }
}

// ── Step 6: Review ────────────────────────────────────────────────────────────
@Composable
private fun ReviewStep(
    groupName: String,
    trackerName: String,
    trackerType: TrackerType,
    frequency: Frequency,
    layoutStyle: LayoutStyle,
    defaultAmount: String,
    loadSampleMembers: Boolean,
    colors: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    Column(modifier = Modifier.fillMaxWidth().padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24), verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)) {
        StepHeader("Review", "Confirm your setup before creating the tracker.", colors)
        Card(colors = CardDefaults.cardColors(containerColor = colors.card), shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16), verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)) {
                Text("Group: ${groupName.ifBlank { "Unnamed Group" }}", color = colors.text)
                Text("Tracker: ${trackerName.ifBlank { "Unnamed Tracker" }}", color = colors.text)
                Text("Type: ${trackerType.name.lowercase().replaceFirstChar { it.uppercase() }}", color = colors.text)
                Text("Frequency: ${frequency.name.lowercase().replaceFirstChar { it.uppercase() }}", color = colors.text)
                if (trackerType == TrackerType.DUES) {
                    Text("Amount: ₦${defaultAmount.ifBlank { "5000" }}", color = colors.text)
                    Text("Seed sample members: ${if (loadSampleMembers) "On" else "Off"}", color = colors.text)
                }
                Text("Layout: ${layoutStyle.name.lowercase().replaceFirstChar { it.uppercase() }}", color = colors.text)
            }
        }
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

@Composable
private fun StepHeader(title: String, subtitle: String, colors: com.mikeisesele.clearr.ui.theme.DuesColors) {
    Column(verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = colors.text)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.muted)
    }
}

@Composable
private fun SelectionCard(
    icon: String,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    colors: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    val borderColor = if (selected) colors.accent else colors.border
    val bgColor = if (selected) colors.accent.copy(alpha = 0.08f) else colors.card
    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
        modifier = Modifier
            .fillMaxWidth()
            .border(width = if (selected) com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2 else com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1, color = borderColor, shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
            horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp24)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = if (selected) colors.accent else colors.text)
                Text(description, style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = colors.accent, modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20))
            }
        }
    }
}

@Composable
private fun WizardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    colors: com.mikeisesele.clearr.ui.theme.DuesColors
) {
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

@Preview(showBackground = true)
@Composable
private fun SetupWizardScreenPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
            Text(
                "Setup Wizard Preview",
                modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24),
                color = colors.text
            )
        }
    }
}
