package com.mikeisesele.clearr.ui.feature.trackerlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDS
import com.mikeisesele.clearr.ui.theme.ClearrTheme

@Composable
internal fun EmptyTrackerState(onCreate: () -> Unit) {
    val spacing = ClearrDS.spacing
    val radii = ClearrDS.radii
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = spacing.xxxl)
        ) {
            // Illustration
            Box(
                modifier = Modifier
                    .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp80)
                    .clip(RoundedCornerShape(radii.xxl))
                    .background(ClearrColors.VioletBg),
                contentAlignment = Alignment.Center
            ) {
                Text("📋", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp36)
            }

            Spacer(Modifier.height(spacing.xl))

            Text(
                "No trackers yet",
                fontWeight = FontWeight.ExtraBold,
                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp20,
                color = ClearrColors.BrandText
            )

            Spacer(Modifier.height(spacing.sm))

            Text(
                "Create your first remittance to start tracking payments for your group.",
                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14,
                color = ClearrColors.TextSecondary,
                lineHeight = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp22,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(spacing.xxl + com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4))

            Button(
                onClick = onCreate,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(radii.md + com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2),
                contentPadding = PaddingValues(horizontal = spacing.xxl + com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4, vertical = spacing.md + com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2)
            ) {
                Text("+ New Remittance", color = ClearrColors.Surface, fontWeight = FontWeight.Bold, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 500)
@Composable
private fun EmptyTrackerStatePreview() {
    ClearrTheme {
        EmptyTrackerState(onCreate = {})
    }
}
