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
