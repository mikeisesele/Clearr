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
                    .size(80.dp)
                    .clip(RoundedCornerShape(radii.xxl))
                    .background(ClearrColors.VioletBg),
                contentAlignment = Alignment.Center
            ) {
                Text("📋", fontSize = 36.sp)
            }

            Spacer(Modifier.height(spacing.xl))

            Text(
                "No trackers yet",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = ClearrColors.BrandText
            )

            Spacer(Modifier.height(spacing.sm))

            Text(
                "Create your first tracker to start managing dues, attendance, tasks, or events for your group.",
                fontSize = 14.sp,
                color = ClearrColors.TextSecondary,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(spacing.xxl + 4.dp))

            Button(
                onClick = onCreate,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(radii.md + 2.dp),
                contentPadding = PaddingValues(horizontal = spacing.xxl + 4.dp, vertical = spacing.md + 2.dp)
            ) {
                Text("+ Create Tracker", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
