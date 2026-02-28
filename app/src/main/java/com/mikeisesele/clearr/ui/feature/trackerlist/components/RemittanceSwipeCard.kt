package com.mikeisesele.clearr.ui.feature.trackerlist.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.TrackerSummary
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDS
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RemittanceSwipeCard(
    summary: TrackerSummary,
    onDeleteRequest: () -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val spacing = ClearrDS.spacing
    val radii = ClearrDS.radii
    val hintOffset = remember { Animatable(0f) }
    var hintShown by rememberSaveable(summary.trackerId) { mutableStateOf(false) }
    val hintAlpha = (kotlin.math.abs(hintOffset.value) / 64f).coerceIn(0f, 1f)

    LaunchedEffect(summary.trackerId, summary.isNew) {
        if (summary.isNew && !hintShown) {
            hintShown = true
            delay(250)
            hintOffset.animateTo(targetValue = -64f, animationSpec = tween(durationMillis = 280))
            delay(140)
            hintOffset.animateTo(targetValue = 0f, animationSpec = tween(durationMillis = 260))
        }
    }

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.35f },
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequest()
            }
            false
        }
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(radii.lg))
                .background(ClearrColors.BrandDanger)
                .padding(horizontal = spacing.xl),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "Delete",
                color = ClearrColors.Surface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer { alpha = hintAlpha }
            )
        }

        SwipeToDismissBox(
            state = dismissState,
            modifier = Modifier.graphicsLayer { translationX = hintOffset.value },
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(radii.lg))
                        .background(ClearrColors.BrandDanger)
                        .padding(horizontal = spacing.xl),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text("Delete", color = ClearrColors.Surface, fontWeight = FontWeight.Bold)
                }
            },
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true
        ) {
            TrackerCard(summary = summary, onClick = onClick, onLongPress = onLongPress)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RemittanceSwipeCardPreview() {
    ClearrTheme {
        RemittanceSwipeCard(
            summary = TrackerSummary(
                trackerId = 1L,
                name = "Church Remittance",
                type = TrackerType.DUES,
                frequency = Frequency.MONTHLY,
                currentPeriodLabel = "February 2026",
                totalMembers = 12,
                completedCount = 7,
                completionPercent = 58,
                isNew = true,
                createdAt = System.currentTimeMillis()
            ),
            onDeleteRequest = {},
            onClick = {},
            onLongPress = {}
        )
    }
}
