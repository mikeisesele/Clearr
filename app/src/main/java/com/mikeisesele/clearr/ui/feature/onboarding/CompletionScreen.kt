package com.mikeisesele.clearr.ui.feature.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrTheme

/**
 * Completion Screen — shown after the last slide or skip.
 * Animates in on mount. Single CTA → SetupWizardScreen.
 * No back navigation (back-stack cleared before arriving here).
 */
@Composable
fun CompletionScreen(onCreateTracker: () -> Unit) {

    var visible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "completion_alpha"
    )
    val offsetY by animateDpAsState(
        targetValue = if (visible) com.mikeisesele.clearr.ui.theme.ClearrDimens.dp0 else com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "completion_offset"
    )

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClearrColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .alpha(alpha)
                .offset(y = offsetY)
                .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp36),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp80)
                    .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24))
                    .background(ClearrColors.EmeraldBg),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp36, color = ClearrColors.Emerald, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp28))

            Text(
                "You're all set.",
                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp24,
                fontWeight = FontWeight.Black,
                color = ClearrColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10))

            Text(
                "Let's create your first tracker. It only takes a minute.",
                fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14,
                color = ClearrColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = (14 * 1.7).sp
            )

            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp40))

            Button(
                onClick = onCreateTracker,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClearrColors.Violet,
                    contentColor = ClearrColors.Surface
                ),
                contentPadding = PaddingValues(vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
            ) {
                Text(
                    "Create First Tracker →",
                    fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15,
                    fontWeight = FontWeight.ExtraBold,
                    color = ClearrColors.Surface
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompletionScreenPreview() {
    ClearrTheme {
        CompletionScreen(onCreateTracker = {})
    }
}
