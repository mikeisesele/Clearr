package com.mikeisesele.clearr.ui.feature.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
fun QuickSetupTypeScreen(
    viewModel: SetupViewModel = hiltViewModel(),
    onOpenDuesWizard: () -> Unit,
    onSetupComplete: () -> Unit
) {
    val colors = LocalDuesColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .padding(ClearrDimens.dp24),
        verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp16)
    ) {
        Text(
            "What do you want to track?",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.text
        )
        Text(
            "Pick what you need to organize.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted
        )
        QuickSetupTypeGrid(
            onTypeSelected = { type ->
                viewModel.onAction(SetupAction.SetTrackerType(type))
                if (type == TrackerType.DUES) {
                    viewModel.onAction(SetupAction.GoToStep(1))
                    onOpenDuesWizard()
                } else {
                    viewModel.onAction(SetupAction.FinishSetup(onSetupComplete))
                }
            }
        )
    }
}

@Composable
fun QuickSetupTypeGrid(onTypeSelected: (TrackerType) -> Unit) {
    val colors = LocalDuesColors.current
    val options = listOf(
        QuickSetupCardData("💰", "Remittance", "Financial dues and remittance tracking", TrackerType.DUES),
        QuickSetupCardData("🎯", "Goals", "Recurring goals and habit progress", TrackerType.GOALS),
        QuickSetupCardData("📝", "Todos", "Personal tasks and completion flow", TrackerType.TODO),
        QuickSetupCardData("💳", "Budget", "Planned vs actual spending", TrackerType.BUDGET)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(ClearrDimens.dp280),
        horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp12),
        verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp12),
        userScrollEnabled = false
    ) {
        items(options) { option ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ClearrDimens.dp130)
                    .clickable { onTypeSelected(option.type) },
                colors = CardDefaults.cardColors(containerColor = colors.card),
                shape = RoundedCornerShape(ClearrDimens.dp12),
                border = androidx.compose.foundation.BorderStroke(ClearrDimens.dp1, colors.border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(ClearrDimens.dp12),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(option.icon, fontSize = ClearrTextSizes.sp24)
                    Column(verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp4)) {
                        Text(option.title, color = colors.text, style = MaterialTheme.typography.titleSmall)
                        Text(
                            option.description,
                            color = colors.muted,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

private data class QuickSetupCardData(
    val icon: String,
    val title: String,
    val description: String,
    val type: TrackerType
)
