package com.mikeisesele.clearr.ui.feature.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.ClearrTheme

@Composable
fun CompletionScreen(onOpenApp: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(400, easing = FastOutSlowInEasing), label = "completion_alpha")
    val offsetY by animateDpAsState(targetValue = if (visible) ClearrDimens.dp0 else ClearrDimens.dp12, animationSpec = tween(400, easing = FastOutSlowInEasing), label = "completion_offset")

    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize().background(ClearrColors.Background), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.alpha(alpha).offset(y = offsetY).padding(horizontal = ClearrDimens.dp36),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(ClearrDimens.dp80).clip(RoundedCornerShape(ClearrDimens.dp24)).background(ClearrColors.EmeraldBg),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = ClearrTextSizes.sp36, color = ClearrColors.Emerald, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(Modifier.height(ClearrDimens.dp28))
            Text("You're all set.", fontSize = ClearrTextSizes.sp24, fontWeight = FontWeight.Black, color = ClearrColors.TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(ClearrDimens.dp10))
            Text(
                "Budget, goals, and todos are ready. Open Clearr and start tracking.",
                fontSize = ClearrTextSizes.sp14,
                color = ClearrColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = (14 * 1.7).sp
            )
            Spacer(Modifier.height(ClearrDimens.dp40))
            Button(
                onClick = onOpenApp,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ClearrDimens.dp16),
                colors = ButtonDefaults.buttonColors(containerColor = ClearrColors.Violet, contentColor = ClearrColors.Surface),
                contentPadding = PaddingValues(vertical = ClearrDimens.dp16)
            ) {
                Text("Open Clearr", fontSize = ClearrTextSizes.sp15, fontWeight = FontWeight.ExtraBold, color = ClearrColors.Surface)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompletionScreenPreview() {
    ClearrTheme { CompletionScreen(onOpenApp = {}) }
}
