package com.mikeisesele.clearr.ui.screen

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import com.mikeisesele.clearr.ui.viewmodel.SetupViewModel

@Composable
fun SetupWizardScreen(
    viewModel: SetupViewModel = hiltViewModel(),
    onSetupComplete: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val C = LocalDuesColors.current
    val isDues = state.trackerType == TrackerType.DUES
    val totalSteps = if (isDues) 7 else 6
    val displayStep = if (!isDues && state.step >= 5) state.step - 1 else state.step
    val finalStep = 6

    LaunchedEffect(isDues, state.step) {
        if (!isDues && state.step == 4) viewModel.nextStep()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(C.bg)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Progress bar ──────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Setup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = C.text
                    )
                    Text(
                        "Step ${displayStep + 1} of $totalSteps",
                        style = MaterialTheme.typography.bodySmall,
                        color = C.muted
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (displayStep + 1) / totalSteps.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = C.accent,
                    trackColor = C.border
                )
                // Step dots
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(totalSteps) { i ->
                        Box(
                            modifier = Modifier
                                .size(if (i == displayStep) 10.dp else 7.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        i < displayStep -> C.accent
                                        i == displayStep -> C.accent
                                        else -> C.border
                                    }
                                )
                        )
                    }
                }
            }

            HorizontalDivider(color = C.border)

            // ── Step content ─────────────────────────────────────────────────
            Box(
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
                        0 -> WelcomeStep(C = C)
                        1 -> GroupInfoStep(
                            groupName = state.groupName,
                            trackerName = state.trackerName,
                            adminName = state.adminName,
                            adminPhone = state.adminPhone,
                            loadSampleMembers = state.loadSampleMembers,
                            onGroupName = viewModel::setGroupName,
                            onTrackerName = viewModel::setTrackerName,
                            onAdminName = viewModel::setAdminName,
                            onAdminPhone = viewModel::setAdminPhone,
                            onLoadSampleMembers = viewModel::setLoadSampleMembers,
                            C = C
                        )
                        2 -> TrackerTypeStep(
                            selected = state.trackerType,
                            onSelect = viewModel::setTrackerType,
                            C = C
                        )
                        3 -> FrequencyStep(
                            selected = state.frequency,
                            onSelect = viewModel::setFrequency,
                            C = C
                        )
                        4 -> AmountStep(
                            amount = state.defaultAmount,
                            frequency = state.frequency,
                            trackerType = state.trackerType,
                            onAmount = viewModel::setDefaultAmount,
                            C = C
                        )
                        5 -> LayoutStyleStep(
                            selected = state.layoutStyle,
                            onSelect = viewModel::setLayoutStyle,
                            C = C
                        )
                        6 -> ReviewStep(
                            groupName = state.groupName,
                            trackerName = state.trackerName,
                            trackerType = state.trackerType,
                            frequency = state.frequency,
                            layoutStyle = state.layoutStyle,
                            defaultAmount = state.defaultAmount,
                            loadSampleMembers = state.loadSampleMembers,
                            C = C
                        )
                        else -> WelcomeStep(C = C)
                    }
                }
            }

            // ── Navigation buttons ────────────────────────────────────────────
            HorizontalDivider(color = C.border)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.step > 0) {
                    OutlinedButton(
                        onClick = viewModel::prevStep,
                        border = androidx.compose.foundation.BorderStroke(1.dp, C.border)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = C.text)
                        Text("Back", color = C.text)
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                if (state.step < finalStep) {
                    Button(
                        onClick = viewModel::nextStep,
                        colors = ButtonDefaults.buttonColors(containerColor = C.accent)
                    ) {
                        Text("Next", color = Color.White)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                    }
                } else {
                    Button(
                        onClick = { viewModel.finishSetup(onSetupComplete) },
                        enabled = !state.isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = C.green)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Text("Finish Setup", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ── Step 0: Welcome ───────────────────────────────────────────────────────────
@Composable
private fun WelcomeStep(C: com.mikeisesele.clearr.ui.theme.DuesColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Quick setup",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = C.text,
            textAlign = TextAlign.Center
        )
        Text(
            "Let's get your group set up in just a few steps. You can always change these settings later.",
            style = MaterialTheme.typography.bodyMedium,
            color = C.muted,
            textAlign = TextAlign.Center
        )
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
    C: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StepHeader("Group Information", "Tell us about your group.", C)
        WizardTextField(
            value = groupName,
            onValueChange = onGroupName,
            label = "Group / Organisation Name",
            C = C
        )
        WizardTextField(
            value = trackerName,
            onValueChange = onTrackerName,
            label = "Tracker Name (e.g. Task Tracker, Event Tracker)",
            C = C
        )
        WizardTextField(
            value = adminName,
            onValueChange = onAdminName,
            label = "Admin Name (optional)",
            C = C
        )
        WizardTextField(
            value = adminPhone,
            onValueChange = onAdminPhone,
            label = "Admin Phone (optional)",
            keyboardType = KeyboardType.Phone,
            C = C
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = C.card),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Load sample members (dev)", color = C.text, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Seeds names like Michael, Simon, Henry into new dues tracker.",
                        style = MaterialTheme.typography.bodySmall,
                        color = C.muted
                    )
                }
                Switch(
                    checked = loadSampleMembers,
                    onCheckedChange = onLoadSampleMembers,
                    colors = SwitchDefaults.colors(checkedTrackColor = C.accent, checkedThumbColor = Color.White)
                )
            }
        }
    }
}

// ── Step 2: Tracker Type ──────────────────────────────────────────────────────
@Composable
private fun TrackerTypeStep(
    selected: TrackerType,
    onSelect: (TrackerType) -> Unit,
    C: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    val options = listOf(
        TrackerType.DUES to Pair("💰", "Financial Dues – Track monthly / periodic payments"),
        TrackerType.ATTENDANCE to Pair("✅", "Attendance – Track who showed up to meetings"),
        TrackerType.TASKS to Pair("📝", "Tasks – Track completion of assigned duties"),
        TrackerType.EVENTS to Pair("🎉", "Events – Track participation in events"),
        TrackerType.CUSTOM to Pair("✨", "Custom – Use your own labels")
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StepHeader("What are you tracking?", "Choose the type that best describes your group's needs.", C)
        options.forEach { (type, info) ->
            val (icon, desc) = info
            SelectionCard(
                icon = icon,
                title = type.name.lowercase().replaceFirstChar { it.uppercase() },
                description = desc,
                selected = selected == type,
                onClick = { onSelect(type) },
                C = C
            )
        }
    }
}

// ── Step 3: Frequency ─────────────────────────────────────────────────────────
@Composable
private fun FrequencyStep(
    selected: Frequency,
    onSelect: (Frequency) -> Unit,
    C: com.mikeisesele.clearr.ui.theme.DuesColors
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StepHeader("How often do you meet / collect?", "This determines how many periods appear in your tracker.", C)
        options.forEach { (freq, info) ->
            val (icon, desc) = info
            SelectionCard(
                icon = icon,
                title = freq.name.lowercase().replaceFirstChar { it.uppercase() },
                description = desc,
                selected = selected == freq,
                onClick = { onSelect(freq) },
                C = C
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
    C: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    val label = when (trackerType) {
        TrackerType.DUES -> "Amount per ${frequency.name.lowercase().replaceFirstChar { it.uppercase() }} (₦)"
        TrackerType.ATTENDANCE, TrackerType.TASKS, TrackerType.EVENTS -> "Not applicable for this tracker type"
        TrackerType.CUSTOM -> "Default amount (₦)"
    }
    val skipAmount = trackerType == TrackerType.ATTENDANCE || trackerType == TrackerType.TASKS
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StepHeader("Set the Amount", "How much is due per period per member?", C)
        if (skipAmount) {
            Card(
                colors = CardDefaults.cardColors(containerColor = C.card),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "No amount required for ${trackerType.name.lowercase()} tracking. You can skip this step.",
                    modifier = Modifier.padding(16.dp),
                    color = C.muted,
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
                    focusedBorderColor = C.accent,
                    unfocusedBorderColor = C.border,
                    focusedLabelColor = C.accent,
                    unfocusedLabelColor = C.muted,
                    focusedTextColor = C.text,
                    unfocusedTextColor = C.text,
                    cursorColor = C.accent
                )
            )
            Text(
                "This becomes the default. You can override it per year in Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = C.muted
            )
        }
    }
}

// ── Step 5: Layout Style ──────────────────────────────────────────────────────
@Composable
private fun LayoutStyleStep(
    selected: LayoutStyle,
    onSelect: (LayoutStyle) -> Unit,
    C: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    val options = listOf(
        LayoutStyle.GRID to Triple("⊞", "Grid", "Compact scrollable table – members × periods"),
        LayoutStyle.KANBAN to Triple("🗂️", "Kanban", "Period columns with member cards – great for small groups"),
        LayoutStyle.CARDS to Triple("🃏", "Cards", "One card per member showing all periods"),
        LayoutStyle.RECEIPT to Triple("🧾", "Receipt / Ledger", "Detailed financial ledger style")
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StepHeader("Choose a Layout Style", "Pick how you want to view your tracker. You can change this anytime.", C)
        options.forEach { (style, info) ->
            val (icon, title, desc) = info
            SelectionCard(
                icon = icon,
                title = title,
                description = desc,
                selected = selected == style,
                onClick = { onSelect(style) },
                C = C
            )
        }
    }
}

// ── Step 6: Reminders ─────────────────────────────────────────────────────────
@Composable
private fun ReviewStep(
    groupName: String,
    trackerName: String,
    trackerType: TrackerType,
    frequency: Frequency,
    layoutStyle: LayoutStyle,
    defaultAmount: String,
    loadSampleMembers: Boolean,
    C: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StepHeader("Review", "Confirm your setup before creating the tracker.", C)
        Card(
            colors = CardDefaults.cardColors(containerColor = C.card),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Group: ${groupName.ifBlank { "Unnamed Group" }}", color = C.text)
                Text("Tracker: ${trackerName.ifBlank { "Unnamed Tracker" }}", color = C.text)
                Text("Type: ${trackerType.name.lowercase().replaceFirstChar { it.uppercase() }}", color = C.text)
                Text("Frequency: ${frequency.name.lowercase().replaceFirstChar { it.uppercase() }}", color = C.text)
                if (trackerType == TrackerType.DUES) {
                    Text("Amount: ₦${defaultAmount.ifBlank { "5000" }}", color = C.text)
                    Text("Seed sample members: ${if (loadSampleMembers) "On" else "Off"}", color = C.text)
                }
                Text("Layout: ${layoutStyle.name.lowercase().replaceFirstChar { it.uppercase() }}", color = C.text)
            }
        }
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

@Composable
private fun StepHeader(
    title: String,
    subtitle: String,
    C: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = C.text
        )
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = C.muted)
    }
}

@Composable
private fun SelectionCard(
    icon: String,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    C: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    val borderColor = if (selected) C.accent else C.border
    val bgColor = if (selected) C.accent.copy(alpha = 0.08f) else C.card
    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 24.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = if (selected) C.accent else C.text)
                Text(description, style = MaterialTheme.typography.bodySmall, color = C.muted)
            }
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = C.accent, modifier = Modifier.size(20.dp))
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
    C: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
