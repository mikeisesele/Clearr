package com.mikeisesele.clearr.ui.feature.goals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
import com.mikeisesele.clearr.core.ai.GoalAiResult
import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.ui.feature.goals.components.GoalSectionTitle
import com.mikeisesele.clearr.ui.feature.goals.components.GoalSheetInput
import com.mikeisesele.clearr.ui.feature.goals.utils.goalPalette
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGoalScreen(
    trackerId: Long,
    onClose: () -> Unit,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalDuesColors.current
    if (state.trackerId != trackerId) return

    var title by rememberSaveable { mutableStateOf("") }
    var emoji by rememberSaveable { mutableStateOf("🎯") }
    var target by rememberSaveable { mutableStateOf("") }
    var frequency by rememberSaveable { mutableStateOf(GoalFrequency.DAILY) }
    var colorToken by rememberSaveable { mutableStateOf("Purple") }
    var showAllIcons by rememberSaveable { mutableStateOf(false) }
    val titleFocusRequester = remember { FocusRequester() }
    val canSubmit = title.trim().isNotEmpty()
    var aiDraft by remember { mutableStateOf<GoalAiResult?>(null) }
    var aiLoading by remember { mutableStateOf(false) }
    var frequencyTouched by rememberSaveable { mutableStateOf(false) }
    var emojiTouched by rememberSaveable { mutableStateOf(false) }
    var colorTouched by rememberSaveable { mutableStateOf(false) }
    var targetTouched by rememberSaveable { mutableStateOf(false) }

    val emojis = listOf("🎯", "🏃", "💰", "📚", "🥗", "🚿", "💪", "🧘", "✍️", "🎸", "🌅", "💊", "🧠", "🎨", "🧹", "🛌", "💼", "🧾", "🍎", "🏊", "🚶", "📖", "🧑‍💻", "🎵")
    val firstRowEmojis = emojis.take(6)
    val extraEmojis = emojis.drop(6)
    val colorTokens = listOf("Purple", "Emerald", "Blue", "Amber", "Coral")
    val palette = goalPalette(colorToken)

    LaunchedEffect(Unit) { titleFocusRequester.requestFocus() }
    LaunchedEffect(title, target) {
        if (title.trim().length < 3) {
            aiDraft = null
            aiLoading = false
            return@LaunchedEffect
        }
        aiLoading = true
        delay(350)
        val inferred = ClearrEdgeAi.inferGoalNanoAware(title = title, target = target, frequency = frequency, emoji = emoji, colorToken = colorToken)
        aiDraft = inferred
        if (!targetTouched && target.isBlank() && !inferred.suggestedTarget.isNullOrBlank()) target = inferred.suggestedTarget
        if (!frequencyTouched) frequency = inferred.suggestedFrequency
        if (!emojiTouched) emoji = inferred.suggestedEmoji
        if (!colorTouched) colorToken = inferred.suggestedColorToken
        aiLoading = false
    }

    Column(
        modifier = Modifier.fillMaxSize().background(colors.bg).statusBarsPadding().padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp8).navigationBarsPadding()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(ClearrDimens.dp34).clickable { onClose() }, contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.text)
            }
            Text("New Goal", fontSize = ClearrTextSizes.sp16, fontWeight = FontWeight.SemiBold, color = colors.text)
            Spacer(modifier = Modifier.size(ClearrDimens.dp34))
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(ClearrDimens.dp8))
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(ClearrDimens.dp12), color = palette.background.copy(alpha = 0.5f)) {
                Row(modifier = Modifier.padding(ClearrDimens.dp14), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp12)) {
                    Surface(modifier = Modifier.size(ClearrDimens.dp44), shape = RoundedCornerShape(ClearrDimens.dp12), color = palette.background) {
                        Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = ClearrTextSizes.sp22) }
                    }
                    Column {
                        Text(title.ifBlank { "Goal name" }, fontSize = ClearrTextSizes.sp16, fontWeight = FontWeight.Bold, color = colors.text)
                        Text("${target.ifBlank { "Set a target" }} · ${if (frequency == GoalFrequency.DAILY) "Daily" else "Weekly"}", fontSize = ClearrTextSizes.sp12, color = colors.text.copy(alpha = 0.78f))
                    }
                }
            }

            Spacer(Modifier.height(ClearrDimens.dp16))
            GoalSectionTitle("ICON")
            Column(modifier = Modifier.animateContentSize()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8), verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)) {
                    firstRowEmojis.forEach { value ->
                        Surface(
                            modifier = Modifier.size(ClearrDimens.dp38).clickable { emojiTouched = true; emoji = value },
                            shape = RoundedCornerShape(ClearrDimens.dp10),
                            color = if (emoji == value) palette.background else colors.card,
                            border = BorderStroke(ClearrDimens.dp2, if (emoji == value) palette.color else ClearrColors.Transparent)
                        ) {
                            Box(contentAlignment = Alignment.Center) { Text(value, fontSize = ClearrTextSizes.sp18) }
                        }
                    }
                }
                AnimatedVisibility(visible = showAllIcons) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8), verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)) {
                        extraEmojis.forEach { value ->
                            Surface(
                                modifier = Modifier.size(ClearrDimens.dp38).clickable { emojiTouched = true; emoji = value },
                                shape = RoundedCornerShape(ClearrDimens.dp10),
                                color = if (emoji == value) palette.background else colors.card,
                                border = BorderStroke(ClearrDimens.dp2, if (emoji == value) palette.color else ClearrColors.Transparent)
                            ) {
                                Box(contentAlignment = Alignment.Center) { Text(value, fontSize = ClearrTextSizes.sp18) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(ClearrDimens.dp6))
            TextButton(onClick = { showAllIcons = !showAllIcons }, modifier = Modifier.align(Alignment.Start)) {
                Text(if (showAllIcons) "Show fewer icons" else "Show more icons", color = palette.color, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(ClearrDimens.dp16))
            GoalSectionTitle("COLOR")
            Row(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp10)) {
                colorTokens.forEach { token ->
                    val tokenPalette = goalPalette(token)
                    Surface(
                        modifier = Modifier.size(ClearrDimens.dp28).clickable { colorTouched = true; colorToken = token },
                        shape = CircleShape,
                        color = tokenPalette.color,
                        border = BorderStroke(ClearrDimens.dp3, if (colorToken == token) ClearrColors.TextPrimary else ClearrColors.Transparent)
                    ) {}
                }
            }

            Spacer(Modifier.height(ClearrDimens.dp16))
            GoalSectionTitle("GOAL NAME")
            GoalSheetInput(
                value = title,
                onValueChange = { title = it },
                placeholder = "e.g. Exercise",
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth().focusRequester(titleFocusRequester)
            )

            Spacer(Modifier.height(ClearrDimens.dp12))
            GoalSectionTitle("TARGET (OPTIONAL)")
            GoalSheetInput(
                value = target,
                onValueChange = { targetTouched = true; target = it },
                placeholder = "e.g. 30 mins, ₦10,000, 20 pages",
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(ClearrDimens.dp16))
            GoalSectionTitle("FREQUENCY")
            Row(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8), modifier = Modifier.fillMaxWidth()) {
                listOf(GoalFrequency.DAILY, GoalFrequency.WEEKLY).forEach { value ->
                    val selected = value == frequency
                    Surface(
                        modifier = Modifier.weight(1f).height(ClearrDimens.dp44).clickable { frequencyTouched = true; frequency = value },
                        shape = RoundedCornerShape(ClearrDimens.dp10),
                        color = if (selected) palette.background else colors.card,
                        border = BorderStroke(ClearrDimens.dp2, if (selected) palette.color else ClearrColors.Transparent)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(if (value == GoalFrequency.DAILY) "Daily" else "Weekly", color = if (selected) palette.color else colors.muted, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = ClearrTextSizes.sp14)
                        }
                    }
                }
            }
            Spacer(Modifier.height(ClearrDimens.dp24))
            Button(
                onClick = {
                    viewModel.onAction(GoalsAction.AddGoal(title.trim(), emoji, colorToken, target.trim().ifBlank { null }, frequency))
                    onClose()
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ClearrDimens.dp14),
                colors = ButtonDefaults.buttonColors(containerColor = palette.color, disabledContainerColor = colors.border),
                contentPadding = PaddingValues(vertical = ClearrDimens.dp16)
            ) {
                Text("Add Goal", color = ClearrColors.Surface, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(ClearrDimens.dp12))
        }
    }
}
