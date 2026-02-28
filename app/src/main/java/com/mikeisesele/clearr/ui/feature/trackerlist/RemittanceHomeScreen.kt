package com.mikeisesele.clearr.ui.feature.trackerlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.commons.components.ClearrTopBar
import com.mikeisesele.clearr.ui.feature.trackerlist.components.DeleteTrackerDialog
import com.mikeisesele.clearr.ui.feature.trackerlist.components.RemittanceSwipeCard
import com.mikeisesele.clearr.ui.feature.trackerlist.components.RenameTrackerDialog
import com.mikeisesele.clearr.ui.feature.trackerlist.components.primaryColor
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDS
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
fun RemittanceHomeScreen(
    onTrackerClick: (Long) -> Unit,
    onCreateRemittance: () -> Unit,
    viewModel: TrackerListViewModel = hiltViewModel()
) {
    val colors = LocalDuesColors.current
    val spacing = ClearrDS.spacing
    val radii = ClearrDS.radii
    val sizes = ClearrDS.sizes
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val remittances = state.summaries.filter { summary ->
        summary.type == TrackerType.DUES || summary.type == TrackerType.EXPENSES
    }

    var deleteTarget by remember { mutableStateOf<TrackerSummary?>(null) }
    var renameTarget by remember { mutableStateOf<TrackerSummary?>(null) }
    var renameValue by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.onAction(TrackerListAction.Refresh)
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            ClearrTopBar(title = "Remittance", showLeading = false)

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = spacing.lg,
                        end = spacing.lg,
                        top = spacing.lg,
                        bottom = spacing.xxxl
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    items(remittances, key = { it.trackerId }) { summary ->
                        RemittanceSwipeCard(
                            summary = summary,
                            onDeleteRequest = { deleteTarget = summary },
                            onClick = {
                                if (summary.isNew) {
                                    viewModel.onAction(TrackerListAction.ClearNewFlag(summary.trackerId))
                                }
                                onTrackerClick(summary.trackerId)
                            },
                            onLongPress = {
                                renameTarget = summary
                                renameValue = summary.name
                            }
                        )
                    }
                }
            }
        }

        if (!state.isLoading) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = spacing.xl, bottom = spacing.xxl),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)
            ) {
                Surface(
                    modifier = Modifier.clickable { onCreateRemittance() },
                    color = ClearrColors.BrandText,
                    shape = RoundedCornerShape(radii.xl),
                    shadowElevation = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8
                ) {
                    Text(
                        "New Remittance",
                        color = ClearrColors.Surface,
                        fontSize = ClearrTextSizes.sp13,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md - com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(sizes.fab)
                        .clip(RoundedCornerShape(radii.lg))
                        .background(primaryColor)
                        .clickable { onCreateRemittance() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = ClearrColors.Surface, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp28, fontWeight = FontWeight.Light)
                }
            }
        }
    }

    deleteTarget?.let { summary ->
        DeleteTrackerDialog(
            summary = summary,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.onAction(TrackerListAction.DeleteTracker(summary.trackerId))
                deleteTarget = null
            }
        )
    }

    renameTarget?.let { summary ->
        RenameTrackerDialog(
            value = renameValue,
            onValueChange = { renameValue = it },
            onDismiss = { renameTarget = null },
            onConfirm = {
                viewModel.onAction(TrackerListAction.RenameTracker(summary.trackerId, renameValue))
                renameTarget = null
            }
        )
    }
}
