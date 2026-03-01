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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.mikeisesele.clearr.ui.feature.dashboard.utils.DashboardTrackerType
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

private data class EmptyStateCard(
    val trackerType: DashboardTrackerType,
    val description: String,
    val accentColor: Color,
    val backgroundColor: Color,
    val ctaLabel: String,
)

private val emptyStateCards = listOf(
    EmptyStateCard(
        trackerType = DashboardTrackerType.BUDGET,
        description = "Plan your monthly spend by category. Know exactly where your money goes.",
        accentColor = ClearrColors.Blue,
        backgroundColor = ClearrColors.BlueBg,
        ctaLabel = "Plan budget"
    ),
    EmptyStateCard(
        trackerType = DashboardTrackerType.GOALS,
        description = "Set recurring habits and targets. Track streaks and completion over time.",
        accentColor = ClearrColors.Emerald,
        backgroundColor = ClearrColors.EmeraldBg,
        ctaLabel = "Build goals"
    ),
    EmptyStateCard(
        trackerType = DashboardTrackerType.DUES,
        description = "Track remittance collections and see who has cleared at a glance.",
        accentColor = ClearrColors.Violet,
        backgroundColor = ClearrColors.VioletBg,
        ctaLabel = "Record payment"
    ),
    EmptyStateCard(
        trackerType = DashboardTrackerType.TODOS,
        description = "Capture personal tasks and deadlines. Clear obligations one by one.",
        accentColor = ClearrColors.Amber,
        backgroundColor = ClearrColors.AmberBg,
        ctaLabel = "Start todos"
    ),
)

@Composable
internal fun DashboardEmptyState(
    onNavigateToTab: (DashboardTrackerType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalDuesColors.current
    val pagerState = rememberPagerState(pageCount = { emptyStateCards.size })
    val currentPage = pagerState.currentPage.coerceIn(0, emptyStateCards.lastIndex)
    val activeCard = emptyStateCards[currentPage]
    val activeColor = activeCard.accentColor
    val containerTint by animateColorAsState(
        targetValue = lerp(colors.bg, activeCard.backgroundColor, 0.24f),
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "empty_state_container_tint",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(containerTint)
            .padding(horizontal = ClearrDimens.dp20),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(ClearrDimens.dp24))

        Text(
            text = "Nothing\nto clear\nyet.",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = colors.text,
            textAlign = TextAlign.Start,
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
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ClearrDimens.dp280),
        ) { page ->
            EmptyTrackerCard(
                card = emptyStateCards[page],
                onNavigateToTab = onNavigateToTab,
            )
        }

        Spacer(Modifier.height(ClearrDimens.dp20))

        PagerDots(
            count = emptyStateCards.size,
            current = currentPage,
            activeColor = activeColor,
        )

        Spacer(Modifier.height(ClearrDimens.dp32))
    }
}

@Composable
private fun EmptyTrackerCard(
    card: EmptyStateCard,
    onNavigateToTab: (DashboardTrackerType) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ClearrDimens.dp20))
            .background(card.backgroundColor)
            .padding(ClearrDimens.dp20),
        verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp12),
    ) {
        Text(
            text = card.trackerType.icon,
            style = MaterialTheme.typography.displaySmall,
        )

        Spacer(Modifier.height(ClearrDimens.dp12))

        Text(
            text = card.trackerType.label,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = card.accentColor,
        )

        Spacer(Modifier.height(ClearrDimens.dp8))

        Text(
            text = card.description,
            style = MaterialTheme.typography.bodyMedium,
            color = ClearrColors.TextPrimary.copy(alpha = 0.75f),
        )

        Spacer(Modifier.height(ClearrDimens.dp12))

        Button(
            onClick = { onNavigateToTab(card.trackerType) },
            shape = RoundedCornerShape(ClearrDimens.dp14),
            colors = ButtonDefaults.buttonColors(
                containerColor = card.accentColor,
                contentColor = ClearrColors.Surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(ClearrDimens.dp52),
        ) {
            Text(
                text = card.ctaLabel,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun PagerDots(
    count: Int,
    current: Int,
    activeColor: Color,
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp6),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(count) { index ->
                val isActive = index == current
                val color by animateColorAsState(
                    targetValue = if (isActive) activeColor else ClearrColors.Inactive,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                    label = "dot_color_$index",
                )
                val width by animateDpAsState(
                    targetValue = if (isActive) ClearrDimens.dp20 else ClearrDimens.dp6,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                    label = "dot_width_$index",
                )
                Box(
                    modifier = Modifier
                        .width(width)
                        .height(ClearrDimens.dp6)
                        .clip(CircleShape)
                        .background(color),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F7FB)
@Composable
private fun DashboardEmptyStatePreview() {
    ClearrTheme {
        DashboardEmptyState(
            onNavigateToTab = {},
            modifier = Modifier
                .background(ClearrColors.Background)
                .fillMaxWidth()
        )
    }
}
