package com.mikeisesele.clearr.ui.feature.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.ui.commons.util.MONTHS
import com.mikeisesele.clearr.ui.commons.util.isFuture
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
internal fun MonthlyBarChartCard(
    selectedYear: Int,
    activeMembers: List<Member>,
    paymentMap: Map<String, Double>,
    prevPaymentMap: Map<String, Double>,
    dueAmount: Double,
    colors: DuesColors = LocalDuesColors.current
) {
    var showPrevYear by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.card),
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
                    color = colors.text
                )
                TextButton(onClick = { showPrevYear = !showPrevYear }) {
                    Text(
                        if (showPrevYear) "Hide ${selectedYear - 1}" else "vs ${selectedYear - 1}",
                        color = colors.accent,
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
                                    .background(colors.muted.copy(alpha = 0.5f))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (showPrevYear) 0.6f else 0.8f)
                                .height((rate * maxBarHeightDp).dp.coerceAtLeast(3.dp))
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(
                                    when {
                                        isFutureMonth -> colors.border
                                        rate >= 1f -> colors.green
                                        rate > 0 -> colors.accent
                                        else -> colors.dim
                                    }
                                )
                        )
                        Text(
                            month,
                            fontSize = 8.sp,
                            color = colors.muted,
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
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(colors.accent))
                        Text("$selectedYear", fontSize = 11.sp, color = colors.muted)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(colors.muted.copy(alpha = 0.5f)))
                        Text("${selectedYear - 1}", fontSize = 11.sp, color = colors.muted)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MonthlyBarChartCardPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        MonthlyBarChartCard(
            selectedYear = 2026,
            activeMembers = emptyList(),
            paymentMap = emptyMap(),
            prevPaymentMap = emptyMap(),
            dueAmount = 5000.0,
            colors = colors
        )
    }
}
