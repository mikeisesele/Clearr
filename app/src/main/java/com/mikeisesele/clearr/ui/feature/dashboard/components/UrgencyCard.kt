package com.mikeisesele.clearr.ui.feature.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerType
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardUrgencyItem
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardUrgencySeverity
import com.mikeisesele.clearr.ui.feature.dashboard.utils.previewDashboardUi
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun UrgencyCard(
    item: DashboardUrgencyItem,
    onDismiss: () -> Unit,
    onQuickAction: (DashboardTrackerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClearrUiColors.current
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    var cardWidth by remember { mutableFloatStateOf(0f) }
    val thresholdFraction = 0.4f
    val isActionRevealed = offsetX.value > cardWidth * thresholdFraction
    val shape = RoundedCornerShape(ClearrDimens.dp20)
    val severityColor = item.severity.toColor(
        baseText = colors.text,
        warning = colors.amber,
        critical = colors.red,
        info = colors.green
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${item.message}. Swipe right to ${item.actionLabel}. Swipe left to dismiss."
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    when {
                        offsetX.value > 0f -> item.trackerType.surfaceTint()
                        offsetX.value < 0f -> colors.red.copy(alpha = 0.14f)
                        else -> colors.surface
                    }
                )
                .border(ClearrDimens.dp1, colors.border, shape)
        ) {
            if (offsetX.value > 0f) {
                Text(
                    text = item.actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = item.trackerType.accentColor,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = ClearrDimens.dp16)
                )
            }
            if (offsetX.value < 0f) {
                Text(
                    text = "Dismiss",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.red,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = ClearrDimens.dp16)
                )
            }
        }

        Surface(
            color = colors.surface,
            shape = shape,
            shadowElevation = ClearrDimens.dp4,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .onGloballyPositioned { cardWidth = it.size.width.toFloat() }
                .pointerInput(item.id) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount)
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offsetX.value >= cardWidth * thresholdFraction -> {
                                        offsetX.animateTo(
                                            targetValue = cardWidth * 0.44f,
                                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 320f)
                                        )
                                    }
                                    offsetX.value <= -(cardWidth * thresholdFraction) -> {
                                        offsetX.animateTo(-cardWidth * 1.1f, animationSpec = tween(180))
                                        onDismiss()
                                    }
                                    else -> offsetX.animateTo(0f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 320f))
                                }
                            }
                        }
                    )
                }
                .clickable(enabled = isActionRevealed) {
                    if (isActionRevealed) {
                        onQuickAction(item.trackerType)
                        scope.launch {
                            offsetX.animateTo(0f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f))
                        }
                    }
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp14)
            ) {
                Box(
                    modifier = Modifier
                        .size(ClearrDimens.dp10)
                        .clip(RoundedCornerShape(ClearrDimens.dp99))
                        .background(severityColor)
                )
                Spacer(Modifier.width(ClearrDimens.dp10))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.text
                    )
                    Spacer(Modifier.height(ClearrDimens.dp4))
                    Text(
                        text = item.trackerType.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.muted
                    )
                }
                AnimatedVisibility(
                    visible = isActionRevealed,
                    enter = slideInHorizontally { it / 3 } + fadeIn(),
                    exit = slideOutHorizontally { it / 3 } + fadeOut()
                ) {
                    Text(
                        text = item.actionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = item.trackerType.accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun DashboardUrgencySeverity.toColor(
    baseText: Color,
    warning: Color,
    critical: Color,
    info: Color
): Color = when (this) {
    DashboardUrgencySeverity.CRITICAL -> critical
    DashboardUrgencySeverity.WARNING -> warning
    DashboardUrgencySeverity.INFO -> info
}

private fun DashboardTrackerType.surfaceTint(): Color = accentColor.copy(alpha = 0.12f)

@Preview(showBackground = true)
@Composable
private fun UrgencyCardPreview() {
    ClearrTheme {
        Column(modifier = Modifier.padding(ClearrDimens.dp20)) {
            UrgencyCard(
                item = previewDashboardUi.urgencyItems.first(),
                onDismiss = {},
                onQuickAction = {}
            )
        }
    }
}
