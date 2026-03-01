package com.mikeisesele.clearr.ui.feature.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerType
import com.mikeisesele.clearr.ui.feature.dashboard.utils.backgroundColor
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors

private data class EmptyStateCard(
    val trackerType: DashboardTrackerType,
    val description: String,
    val ctaLabel: String,
)

private val emptyStateCards = listOf(
    EmptyStateCard(DashboardTrackerType.BUDGET, "Plan your monthly spend by category. Know exactly where your money goes.", "Plan budget"),
    EmptyStateCard(DashboardTrackerType.GOALS, "Set recurring habits and targets. Track streaks and completion over time.", "Build goals"),
    EmptyStateCard(DashboardTrackerType.TODOS, "Capture personal tasks and deadlines. Clear obligations one by one.", "Start todos")
)

@Composable
internal fun DashboardEmptyState(
    onNavigateToTab: (DashboardTrackerType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalClearrUiColors.current
    val pagerState = rememberPagerState(pageCount = { emptyStateCards.size })
    val currentPage = pagerState.currentPage.coerceIn(0, emptyStateCards.lastIndex)
    val activeCard = emptyStateCards[currentPage]
    val containerTint by animateColorAsState(
        targetValue = lerp(colors.bg, activeCard.trackerType.backgroundColor(), 0.32f),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "empty_state_container_tint",
    )

    Column(
        modifier = modifier.fillMaxWidth().background(containerTint).padding(horizontal = ClearrDimens.dp20),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(ClearrDimens.dp24))
        Text(
            text = "Nothing\nto clear\nyet.",
            style = MaterialTheme.typography.displaySmall,
            color = colors.text,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(ClearrDimens.dp8))
        Text(
            text = "Start with:",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(ClearrDimens.dp20))

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = ClearrDimens.dp0),
            pageSpacing = ClearrDimens.dp12,
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        ) { page ->
            EmptyTrackerCard(card = emptyStateCards[page], onNavigateToTab = onNavigateToTab)
        }

        Spacer(Modifier.height(ClearrDimens.dp20))
        PagerDots(count = emptyStateCards.size, current = currentPage, activeColor = activeCard.trackerType.accentColor)
        Spacer(Modifier.height(ClearrDimens.dp32))
    }
}

@Composable
private fun EmptyTrackerCard(card: EmptyStateCard, onNavigateToTab: (DashboardTrackerType) -> Unit) {
    val colors = LocalClearrUiColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ClearrDimens.dp320)
            .clip(RoundedCornerShape(ClearrDimens.dp20))
            .background(card.trackerType.backgroundColor())
            .padding(ClearrDimens.dp20),
        verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp12),
    ) {
        Text(text = card.trackerType.icon, style = MaterialTheme.typography.displaySmall)
        Text(
            text = card.trackerType.label,
            style = MaterialTheme.typography.titleLarge,
            color = card.trackerType.accentColor,
        )
        Text(
            text = card.description,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.text.copy(alpha = 0.75f),
        )
        Spacer(Modifier.weight(1f, fill = true))
        Button(
            onClick = { onNavigateToTab(card.trackerType) },
            shape = RoundedCornerShape(ClearrDimens.dp14),
            colors = ButtonDefaults.buttonColors(containerColor = card.trackerType.accentColor, contentColor = colors.surface),
            modifier = Modifier.fillMaxWidth().height(ClearrDimens.dp52),
        ) {
            Text(text = card.ctaLabel, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun PagerDots(count: Int, current: Int, activeColor: androidx.compose.ui.graphics.Color) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp6), verticalAlignment = Alignment.CenterVertically) {
            repeat(count) { index ->
                val isActive = index == current
                val color by animateColorAsState(
                    targetValue = if (isActive) activeColor else ClearrColors.Inactive,
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    label = "dot_color_$index",
                )
                val width by animateDpAsState(
                    targetValue = if (isActive) ClearrDimens.dp20 else ClearrDimens.dp6,
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    label = "dot_width_$index",
                )
                Box(modifier = Modifier.width(width).height(ClearrDimens.dp6).clip(CircleShape).background(color))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F7FB)
@Composable
private fun DashboardEmptyStatePreview() {
    ClearrTheme {
        DashboardEmptyState(onNavigateToTab = {}, modifier = Modifier.fillMaxWidth())
    }
}
