package com.mikeisesele.clearr.ui.feature.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.ui.feature.setup.components.AmountStep
import com.mikeisesele.clearr.ui.feature.setup.components.FrequencyStep
import com.mikeisesele.clearr.ui.feature.setup.components.GroupInfoStep
import com.mikeisesele.clearr.ui.feature.setup.components.LayoutStyleStep
import com.mikeisesele.clearr.ui.feature.setup.components.ReviewStep
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
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

    Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        Column(modifier = Modifier.fillMaxWidth().background(colors.surface).statusBarsPadding().padding(horizontal = ClearrDimens.dp24, vertical = ClearrDimens.dp14)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Setup Wizard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = colors.text)
                Text("Step ${displayStep + 1} of $totalSteps", style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
            Spacer(Modifier.height(ClearrDimens.dp10))
            LinearProgressIndicator(progress = { (displayStep + 1f) / totalSteps }, modifier = Modifier.fillMaxWidth().height(ClearrDimens.dp4).clip(RoundedCornerShape(ClearrDimens.dp2)), color = colors.accent, trackColor = colors.border)
            Spacer(Modifier.height(ClearrDimens.dp10))
            Row(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)) {
                repeat(totalSteps) { i ->
                    Box(modifier = Modifier.size(ClearrDimens.dp8).clip(CircleShape).background(if (i <= displayStep) colors.accent else colors.border))
                }
            }
        }
        HorizontalDivider(color = colors.border)

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith (slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "wizard_step"
            ) { step ->
                when (step) {
                    1 -> GroupInfoStep(state.groupName, state.trackerName, state.adminName, state.adminPhone, state.loadSampleMembers, { viewModel.onAction(SetupAction.SetGroupName(it)) }, { viewModel.onAction(SetupAction.SetTrackerName(it)) }, { viewModel.onAction(SetupAction.SetAdminName(it)) }, { viewModel.onAction(SetupAction.SetAdminPhone(it)) }, { viewModel.onAction(SetupAction.SetLoadSampleMembers(it)) }, colors)
                    2 -> FrequencyStep(state.frequency, { viewModel.onAction(SetupAction.SetFrequency(it)) }, colors)
                    3 -> AmountStep(state.defaultAmount, state.frequency, state.trackerType, { viewModel.onAction(SetupAction.SetDefaultAmount(it)) }, colors)
                    4 -> LayoutStyleStep(state.layoutStyle, { viewModel.onAction(SetupAction.SetLayoutStyle(it)) }, colors)
                    5 -> ReviewStep(state.groupName, state.trackerName, state.trackerType, state.frequency, state.layoutStyle, state.defaultAmount, state.loadSampleMembers, colors)
                    else -> GroupInfoStep(state.groupName, state.trackerName, state.adminName, state.adminPhone, state.loadSampleMembers, { viewModel.onAction(SetupAction.SetGroupName(it)) }, { viewModel.onAction(SetupAction.SetTrackerName(it)) }, { viewModel.onAction(SetupAction.SetAdminName(it)) }, { viewModel.onAction(SetupAction.SetAdminPhone(it)) }, { viewModel.onAction(SetupAction.SetLoadSampleMembers(it)) }, colors)
                }
            }
        }

        HorizontalDivider(color = colors.border)
        Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = ClearrDimens.dp24, vertical = ClearrDimens.dp10), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { viewModel.onAction(SetupAction.PrevStep) }, enabled = state.step > 1, border = androidx.compose.foundation.BorderStroke(ClearrDimens.dp1, colors.border)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = colors.text)
                Text("Back", color = colors.text)
            }

            if (state.step < finalStep) {
                Button(onClick = { viewModel.onAction(SetupAction.NextStep) }, colors = ButtonDefaults.buttonColors(containerColor = colors.accent)) {
                    Text("Next", color = ClearrColors.Surface)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ClearrColors.Surface)
                }
            } else {
                Button(onClick = { viewModel.onAction(SetupAction.FinishSetup(onSetupComplete)) }, enabled = !state.isSaving, colors = ButtonDefaults.buttonColors(containerColor = colors.green)) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(ClearrDimens.dp18), color = ClearrColors.Surface, strokeWidth = ClearrDimens.dp2)
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, tint = ClearrColors.Surface)
                        Spacer(Modifier.size(ClearrDimens.dp4))
                        Text("Finish Setup", color = ClearrColors.Surface)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SetupWizardScreenPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
            Text("Setup Wizard Preview", modifier = Modifier.padding(ClearrDimens.dp24), color = colors.text)
        }
    }
}
