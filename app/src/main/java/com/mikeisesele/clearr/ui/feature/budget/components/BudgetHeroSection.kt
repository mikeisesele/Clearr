package com.mikeisesele.clearr.ui.feature.budget.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.data.model.BudgetPeriod
import com.mikeisesele.clearr.data.model.BudgetSummary
import com.mikeisesele.clearr.ui.feature.budget.previews.previewBudgetPeriods
import com.mikeisesele.clearr.ui.feature.budget.previews.previewBudgetSummary
import com.mikeisesele.clearr.ui.feature.budget.utils.formatKobo
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import kotlin.math.absoluteValue

@Composable
internal fun BudgetHeroSection(
    summary: BudgetSummary,
    periods: List<BudgetPeriod>,
    selectedPeriodId: Long?,
    onPeriodSelect: (Long) -> Unit,
    colors: DuesColors
) {
    val over = summary.totalRemainingKobo < 0
    val balanceColor = if (over) colors.red else colors.green
    val monthsListState = rememberLazyListState()

    LaunchedEffect(periods, selectedPeriodId) {
        val selectedIndex = periods.indexOfFirst { it.id == selectedPeriodId }
        if (selectedIndex >= 0) {
            monthsListState.animateScrollToItem(selectedIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bg)
            .padding(horizontal = ClearrDimens.dp20, vertical = ClearrDimens.dp20)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (over) "Over Budget" else "Remaining",
                fontSize = ClearrTextSizes.sp11,
                color = colors.muted,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(ClearrDimens.dp6))
            Text(
                text = formatKobo(summary.totalRemainingKobo.absoluteValue),
                fontSize = 52.sp,
                color = balanceColor,
                letterSpacing = (-1).sp
            )
            Spacer(Modifier.height(ClearrDimens.dp4))
            Text(
                text = "${formatKobo(summary.totalSpentKobo)} spent of ${formatKobo(summary.totalPlannedKobo)}",
                fontSize = ClearrTextSizes.sp13,
                color = colors.muted
            )
        }

        Spacer(Modifier.height(ClearrDimens.dp20))

        BudgetHealthMeter(summary = summary, colors = colors)

        Spacer(Modifier.height(ClearrDimens.dp16))

        LazyRow(
            state = monthsListState,
            horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp6)
        ) {
            items(periods, key = { it.id }) { period ->
                val selected = period.id == selectedPeriodId
                Surface(
                    color = if (selected) colors.accent else colors.surface,
                    shape = RoundedCornerShape(ClearrDimens.dp99),
                    modifier = Modifier.clickable { onPeriodSelect(period.id) }
                ) {
                    Text(
                        text = period.label,
                        modifier = Modifier.padding(horizontal = ClearrDimens.dp14, vertical = ClearrDimens.dp5),
                        fontSize = ClearrTextSizes.sp12,
                        color = if (selected) ClearrColors.Surface else colors.text
                    )
                }
            }
        }
    }
}

@Composable
internal fun BudgetHealthMeter(
    summary: BudgetSummary,
    colors: DuesColors
) {
    val pct = if (summary.totalPlannedKobo > 0L) {
        (summary.totalSpentKobo.toFloat() / summary.totalPlannedKobo).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animPct by animateFloatAsState(targetValue = pct, label = "health_meter")
    val over = summary.totalRemainingKobo < 0
    val healthLabel = when {
        over -> "Overspent"
        pct > 0.85f -> "Tight"
        else -> "Healthy"
    }
    val healthColor = when {
        over -> colors.red
        pct > 0.85f -> colors.amber
        else -> colors.green
    }

    Surface(color = colors.surface, shape = RoundedCornerShape(ClearrDimens.dp12)) {
        Column(modifier = Modifier.padding(ClearrDimens.dp14)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Budget health", fontSize = ClearrTextSizes.sp12, color = colors.muted)
                Text(healthLabel, fontSize = ClearrTextSizes.sp12, color = healthColor)
            }
            Spacer(Modifier.height(ClearrDimens.dp8))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ClearrDimens.dp6)
                    .background(colors.border, RoundedCornerShape(ClearrDimens.dp99))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animPct)
                        .height(ClearrDimens.dp6)
                        .background(healthColor, RoundedCornerShape(ClearrDimens.dp99))
                )
            }
            Spacer(Modifier.height(ClearrDimens.dp6))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${formatKobo(summary.totalSpentKobo)} spent", fontSize = ClearrTextSizes.sp11, color = colors.muted)
                Text("${formatKobo(summary.totalPlannedKobo)} planned", fontSize = ClearrTextSizes.sp11, color = colors.muted)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun BudgetHeroSectionPreview() {
    ClearrTheme {
        BudgetHeroSection(
            summary = previewBudgetSummary,
            periods = previewBudgetPeriods,
            selectedPeriodId = 4L,
            onPeriodSelect = {},
            colors = LocalDuesColors.current
        )
    }
}
