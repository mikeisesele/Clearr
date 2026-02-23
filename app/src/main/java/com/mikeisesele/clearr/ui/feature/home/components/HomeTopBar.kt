package com.mikeisesele.clearr.ui.feature.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.ui.commons.util.formatAmount
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import com.mikeisesele.clearr.ui.theme.WhatsAppGreen

@Composable
internal fun HomeTopBar(
    trackerName: String,
    layoutStyle: LayoutStyle,
    selectedYear: Int,
    dueAmount: Double,
    onBack: (() -> Unit)?,
    onLayoutClick: () -> Unit,
    onShareClick: () -> Unit,
    colors: DuesColors = LocalDuesColors.current
) {
    Surface(color = colors.surface, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.text
                    )
                }
                Spacer(Modifier.width(4.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    trackerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.text
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = colors.accent.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable { onLayoutClick() }
                    ) {
                        Text(
                            layoutLabel(layoutStyle),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.accent,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        "$selectedYear  ·  ${formatAmount(dueAmount)}/member",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = colors.muted
                    )
                }
            }
            OutlinedButton(
                onClick = onShareClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WhatsAppGreen),
                border = BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.5f)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("📤", fontSize = 12.sp)
                Spacer(Modifier.width(4.dp))
                Text("Share", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun layoutLabel(style: LayoutStyle) = when (style) {
    LayoutStyle.GRID    -> "⊞ Grid"
    LayoutStyle.KANBAN  -> "🗂 Kanban"
    LayoutStyle.CARDS   -> "🃏 Cards"
    LayoutStyle.RECEIPT -> "🧾 Receipt"
}

@Preview(showBackground = true)
@Composable
private fun HomeTopBarPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        HomeTopBar(
            trackerName = "JSS Monthly Dues",
            layoutStyle = LayoutStyle.GRID,
            selectedYear = 2026,
            dueAmount = 5000.0,
            onBack = {},
            onLayoutClick = {},
            onShareClick = {},
            colors = colors
        )
    }
}
