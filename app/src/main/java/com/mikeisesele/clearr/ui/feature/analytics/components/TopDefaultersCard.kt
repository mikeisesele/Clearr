package com.mikeisesele.clearr.ui.feature.analytics.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.ui.commons.util.MONTHS
import com.mikeisesele.clearr.ui.commons.util.buildWhatsAppLink
import com.mikeisesele.clearr.ui.commons.util.currentMonth
import com.mikeisesele.clearr.ui.commons.util.formatAmount
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import com.mikeisesele.clearr.ui.theme.WhatsAppGreen

@Composable
internal fun TopDefaultersCard(
    defaulters: List<Pair<Member, Int>>,
    dueAmount: Double,
    year: Int,
    context: android.content.Context,
    colors: DuesColors = LocalDuesColors.current
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.card),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🏆 Top Defaulters",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
            Spacer(Modifier.height(12.dp))

            if (defaulters.isEmpty()) {
                Text(
                    "🎉 No defaulters! Everyone is up to date.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.green
                )
            } else {
                defaulters.take(5).forEachIndexed { i, (member, unpaidCount) ->
                    if (i > 0) HorizontalDivider(color = colors.border)
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
                                        0 -> colors.red
                                        1 -> colors.amber
                                        else -> colors.dim
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${i + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = ClearrColors.BrandText
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = colors.text)
                            Text(
                                "$unpaidCount month${if (unpaidCount > 1) "s" else ""} unpaid · ${formatAmount(unpaidCount * dueAmount)} owed",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.muted
                            )
                        }
                        if (!member.phone.isNullOrBlank()) {
                            val cm = currentMonth()
                            Button(
                                onClick = {
                                    val link = buildWhatsAppLink(member.phone, member.name, MONTHS[cm], year, formatAmount(dueAmount))
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

@Preview(showBackground = true)
@Composable
private fun TopDefaultersCardPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        TopDefaultersCard(
            defaulters = emptyList(),
            dueAmount = 5000.0,
            year = 2026,
            context = androidx.compose.ui.platform.LocalContext.current,
            colors = colors
        )
    }
}
