package com.mikeisesele.clearr.ui.feature.analytics

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikeisesele.clearr.ui.commons.util.isFuture
import com.mikeisesele.clearr.ui.feature.analytics.components.CollectionGaugeCard
import com.mikeisesele.clearr.ui.feature.analytics.components.MonthlyBarChartCard
import com.mikeisesele.clearr.ui.feature.analytics.components.TopDefaultersCard
import com.mikeisesele.clearr.ui.feature.analytics.components.YearSummarySection
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalDuesColors.current
    val context = LocalContext.current

    val dueAmount = state.yearConfig?.dueAmountPerMonth ?: 5000.0

    val activeMembers = remember(state.members) { state.members.filter { !it.isArchived } }

    val paymentMap = remember(state.payments) {
        state.payments.filter { !it.isUndone }
            .groupBy { "${it.memberId}-${it.monthIndex}" }
            .mapValues { (_, r) -> r.sumOf { it.amountPaid } }
    }

    val prevPaymentMap = remember(state.prevYearPayments) {
        state.prevYearPayments.filter { !it.isUndone }
            .groupBy { "${it.memberId}-${it.monthIndex}" }
            .mapValues { (_, r) -> r.sumOf { it.amountPaid } }
    }

    fun paidForMonth(memberId: Long, monthIndex: Int) = paymentMap["$memberId-$monthIndex"] ?: 0.0

    val nonFutureMonths = (0..11).filter { !isFuture(state.selectedYear, it) }
    val totalCollected = activeMembers.sumOf { m -> nonFutureMonths.sumOf { mi -> paidForMonth(m.id, mi) } }
    val totalExpected = activeMembers.size * nonFutureMonths.size * dueAmount
    val outstanding = (totalExpected - totalCollected).coerceAtLeast(0.0)
    val pct = if (totalExpected > 0) (totalCollected / totalExpected * 100).toInt().coerceIn(0, 100) else 0

    val defaulters = activeMembers.map { m ->
        val unpaidMonths = nonFutureMonths.count { mi -> (paymentMap["${m.id}-$mi"] ?: 0.0) < dueAmount }
        m to unpaidMonths
    }.filter { it.second > 0 }.sortedByDescending { it.second }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Analytics — ${state.selectedYear}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = colors.text
        )

        CollectionGaugeCard(pct = pct, totalCollected = totalCollected, outstanding = outstanding, colors = colors)

        MonthlyBarChartCard(
            selectedYear = state.selectedYear,
            activeMembers = activeMembers,
            paymentMap = paymentMap,
            prevPaymentMap = prevPaymentMap,
            dueAmount = dueAmount,
            colors = colors
        )

        YearSummarySection(
            currentYear = state.selectedYear,
            currentCollected = totalCollected,
            currentExpected = totalExpected,
            currentPct = pct,
            activeMemberCount = activeMembers.size,
            colors = colors
        )

        TopDefaultersCard(
            defaulters = defaulters,
            dueAmount = dueAmount,
            year = state.selectedYear,
            context = context,
            colors = colors
        )

        Spacer(Modifier.height(80.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalyticsScreenPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Analytics — 2026", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = colors.text)
            CollectionGaugeCard(pct = 75, totalCollected = 45000.0, outstanding = 15000.0, colors = colors)
        }
    }
}
