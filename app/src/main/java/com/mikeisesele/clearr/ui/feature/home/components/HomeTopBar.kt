package com.mikeisesele.clearr.ui.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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

@Composable
internal fun HomeTopBar(
    trackerName: String,
    layoutStyle: LayoutStyle,
    selectedYear: Int,
    dueAmount: Double,
    showBlurToggle: Boolean,
    blurMemberNames: Boolean,
    onBlurToggle: () -> Unit,
    onBack: (() -> Unit)?,
    onLayoutClick: () -> Unit,
    onShareClick: () -> Unit,
    colors: DuesColors = LocalDuesColors.current
) {
    Surface(color = colors.surface, shadowElevation = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp32)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.text
                    )
                }
                Spacer(Modifier.width(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    trackerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp18,
                    color = colors.text
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = colors.accent.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4),
                        modifier = Modifier.clickable { onLayoutClick() }
                    ) {
                        Text(
                            layoutLabel(layoutStyle),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.accent,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp10,
                            modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp5, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)
                        )
                    }
                    Text(
                        "$selectedYear  ·  ${formatAmount(dueAmount)}/member",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp11,
                        color = colors.muted
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBlurToggle) {
                    IconButton(
                        onClick = onBlurToggle,
                        modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp32)
                    ) {
                        Icon(
                            imageVector = if (blurMemberNames) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (blurMemberNames) "Show names" else "Hide names",
                            tint = colors.muted
                        )
                    }
                }
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp32)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = colors.muted
                    )
                }
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
            showBlurToggle = true,
            blurMemberNames = false,
            onBlurToggle = {},
            onBack = {},
            onLayoutClick = {},
            onShareClick = {},
            colors = colors
        )
    }
}
