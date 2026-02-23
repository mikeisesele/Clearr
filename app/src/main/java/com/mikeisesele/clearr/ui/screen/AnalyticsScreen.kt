package com.mikeisesele.clearr.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import com.mikeisesele.clearr.ui.theme.WhatsAppGreen
import com.mikeisesele.clearr.ui.util.*
import com.mikeisesele.clearr.ui.viewmodel.AnalyticsViewModel

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val C = LocalDuesColors.current
    val context = LocalContext.current

    val dueAmount = state.yearConfig?.dueAmountPerMonth ?: 5000.0
    val prevDueAmount = state.prevYearConfig?.dueAmountPerMonth ?: 5000.0

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

    // Defaulters
    val defaulters = activeMembers.map { m ->
        val unpaidMonths = nonFutureMonths.count { mi -> (paymentMap["${m.id}-$mi"] ?: 0.0) < dueAmount }
        m to unpaidMonths
    }.filter { it.second > 0 }.sortedByDescending { it.second }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(C.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Analytics — ${state.selectedYear}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = C.text
        )

        // Collection gauge
        CollectionGaugeCard(
            pct = pct,
            totalCollected = totalCollected,
            outstanding = outstanding,
            C = C
        )

        // Monthly bar chart
        MonthlyBarChartCard(
            selectedYear = state.selectedYear,
            activeMembers = activeMembers,
            paymentMap = paymentMap,
            prevPaymentMap = prevPaymentMap,
            dueAmount = dueAmount,
            C = C
        )

        // Year summary cards
        YearSummarySection(
            currentYear = state.selectedYear,
            currentCollected = totalCollected,
            currentExpected = totalExpected,
            currentPct = pct,
            activeMemberCount = activeMembers.size,
            C = C
        )

        // Top defaulters
        TopDefaultersCard(
            defaulters = defaulters,
            dueAmount = dueAmount,
            year = state.selectedYear,
            context = context,
            C = C
        )

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun CollectionGaugeCard(
    pct: Int,
    totalCollected: Double,
    outstanding: Double,
    C: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = C.card),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Donut gauge
            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                val sweepAngle = (pct / 100f) * 360f
                Canvas(modifier = Modifier.size(90.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    drawArc(
                        color = C.border,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = C.accent,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    "$pct%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = C.text
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Collection Rate", style = MaterialTheme.typography.labelMedium, color = C.muted)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(C.green))
                    Text("Collected: ${formatAmount(totalCollected)}", style = MaterialTheme.typography.bodyMedium, color = C.green)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(C.red))
                    Text("Outstanding: ${formatAmount(outstanding)}", style = MaterialTheme.typography.bodyMedium, color = C.red)
                }
            }
        }
    }
}

@Composable
private fun MonthlyBarChartCard(
    selectedYear: Int,
    activeMembers: List<Member>,
    paymentMap: Map<String, Double>,
    prevPaymentMap: Map<String, Double>,
    dueAmount: Double,
    C: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    var showPrevYear by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = C.card),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Monthly Collection Rate",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = C.text
                )
                TextButton(onClick = { showPrevYear = !showPrevYear }) {
                    Text(
                        if (showPrevYear) "Hide ${selectedYear - 1}" else "vs ${selectedYear - 1}",
                        color = C.accent,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            val maxBarHeightDp = 80

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((maxBarHeightDp + 24).dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                MONTHS.forEachIndexed { mi, month ->
                    val isFutureMonth = isFuture(selectedYear, mi)
                    val rate = if (isFutureMonth || activeMembers.isEmpty()) 0f else {
                        activeMembers.count { m -> (paymentMap["${m.id}-$mi"] ?: 0.0) >= dueAmount }
                            .toFloat() / activeMembers.size
                    }
                    val prevRate = if (activeMembers.isEmpty()) 0f else {
                        activeMembers.count { m -> (prevPaymentMap["${m.id}-$mi"] ?: 0.0) >= dueAmount }
                            .toFloat() / activeMembers.size
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (showPrevYear) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.4f)
                                    .height((prevRate * maxBarHeightDp).dp.coerceAtLeast(3.dp))
                                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                    .background(C.muted.copy(alpha = 0.5f))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (showPrevYear) 0.6f else 0.8f)
                                .height((rate * maxBarHeightDp).dp.coerceAtLeast(3.dp))
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(
                                    when {
                                        isFutureMonth -> C.border
                                        rate >= 1f -> C.green
                                        rate > 0 -> C.accent
                                        else -> C.dim
                                    }
                                )
                        )
                        Text(
                            month,
                            fontSize = 8.sp,
                            color = C.muted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            if (showPrevYear) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(C.accent))
                        Text("$selectedYear", fontSize = 11.sp, color = C.muted)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(C.muted.copy(alpha = 0.5f)))
                        Text("${selectedYear - 1}", fontSize = 11.sp, color = C.muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun YearSummarySection(
    currentYear: Int,
    currentCollected: Double,
    currentExpected: Double,
    currentPct: Int,
    activeMemberCount: Int,
    C: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    Text(
        "Year Summary",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = C.text
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = C.card),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$currentYear", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = C.text)
                Text("$currentPct% rate", style = MaterialTheme.typography.bodyMedium, color = if (currentPct >= 80) C.green else if (currentPct >= 50) C.amber else C.red)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Collected: ${formatAmount(currentCollected)}", style = MaterialTheme.typography.bodySmall, color = C.green)
                Text("Expected: ${formatAmount(currentExpected)}", style = MaterialTheme.typography.bodySmall, color = C.muted)
            }
            Text("Active members: $activeMemberCount", style = MaterialTheme.typography.bodySmall, color = C.muted)
        }
    }
}

@Composable
private fun TopDefaultersCard(
    defaulters: List<Pair<Member, Int>>,
    dueAmount: Double,
    year: Int,
    context: android.content.Context,
    C: com.mikeisesele.clearr.ui.theme.DuesColors
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = C.card),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🏆 Top Defaulters",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = C.text
            )
            Spacer(Modifier.height(12.dp))

            if (defaulters.isEmpty()) {
                Text(
                    "🎉 No defaulters! Everyone is up to date.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = C.green
                )
            } else {
                defaulters.take(5).forEachIndexed { i, (member, unpaidCount) ->
                    if (i > 0) HorizontalDivider(color = C.border)
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when (i) {
                                        0 -> C.red
                                        1 -> C.amber
                                        else -> C.dim
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${i + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = C.text)
                            Text(
                                "$unpaidCount month${if (unpaidCount > 1) "s" else ""} unpaid · ${formatAmount(unpaidCount * dueAmount)} owed",
                                style = MaterialTheme.typography.bodySmall,
                                color = C.muted
                            )
                        }
                        if (!member.phone.isNullOrBlank()) {
                            val currentMonth = currentMonth()
                            Button(
                                onClick = {
                                    val link = buildWhatsAppLink(member.phone, member.name, MONTHS[currentMonth], year, formatAmount(dueAmount))
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("WhatsApp", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
