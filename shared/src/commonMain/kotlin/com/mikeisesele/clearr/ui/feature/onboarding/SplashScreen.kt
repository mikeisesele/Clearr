package com.mikeisesele.clearr.ui.feature.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onGetStarted: () -> Unit) {
    var splashLogoVisible by remember { mutableStateOf(false) }
    var ctaVisible by remember { mutableStateOf(false) }

    val splashAlpha by animateFloatAsState(
        targetValue = if (splashLogoVisible) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "splash_alpha"
    )
    val splashOffsetY by animateFloatAsState(
        targetValue = if (splashLogoVisible) 0f else 8f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "splash_offsetY"
    )
    val splashScale by animateFloatAsState(
        targetValue = if (splashLogoVisible) 1f else 0.92f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "splash_scale"
    )

    val ctaAlpha by animateFloatAsState(
        targetValue = if (ctaVisible) 1f else 0f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "cta_alpha"
    )
    val ctaOffsetY by animateFloatAsState(
        targetValue = if (ctaVisible) 0f else 14f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "cta_offsetY"
    )

    LaunchedEffect(Unit) {
        splashLogoVisible = true
        delay(1_800)
        splashLogoVisible = false
        delay(500)
        ctaVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClearrColors.Surface),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(ClearrDimens.dp280)
                .align(Alignment.TopEnd)
                .offset(x = ClearrDimens.dp60, y = (-60).dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(ClearrColors.Violet.copy(alpha = 0.08f))
        )
        Box(
            modifier = Modifier
                .size(ClearrDimens.dp200)
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = ClearrDimens.dp60)
                .clip(RoundedCornerShape(percent = 50))
                .background(ClearrColors.Violet.copy(alpha = 0.06f))
        )

        Column(
            modifier = Modifier
                .alpha(splashAlpha)
                .offset(y = splashOffsetY.dp)
                .scale(splashScale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "◎",
                fontSize = ClearrTextSizes.sp40,
                fontWeight = FontWeight.Black,
                color = ClearrColors.Violet
            )
            Spacer(Modifier.height(ClearrDimens.dp20))
            Text(
                "Clearr",
                fontSize = ClearrTextSizes.sp36,
                fontWeight = FontWeight.Black,
                color = ClearrColors.Violet,
                letterSpacing = (-1).sp
            )
            Spacer(Modifier.height(ClearrDimens.dp6))
            Text(
                "Clear your obligations.",
                fontSize = ClearrTextSizes.sp14,
                color = ClearrColors.Violet.copy(alpha = 0.72f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .alpha(ctaAlpha)
                .offset(y = ctaOffsetY.dp)
                .padding(bottom = ClearrDimens.dp64),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp0)
        ) {
            Text(
                text = "◎",
                fontSize = ClearrTextSizes.sp28,
                fontWeight = FontWeight.Black,
                color = ClearrColors.Violet
            )
            Spacer(Modifier.height(ClearrDimens.dp12))
            Text(
                "Clearr",
                fontSize = ClearrTextSizes.sp28,
                fontWeight = FontWeight.Black,
                color = ClearrColors.Violet,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(ClearrDimens.dp4))
            Text(
                "Clear your obligations.",
                fontSize = ClearrTextSizes.sp13,
                color = ClearrColors.Violet.copy(alpha = 0.72f)
            )
            Spacer(Modifier.height(ClearrDimens.dp36))
            Button(
                onClick = onGetStarted,
                enabled = ctaVisible,
                shape = RoundedCornerShape(ClearrDimens.dp16),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClearrColors.Violet,
                    contentColor = ClearrColors.Surface
                ),
                contentPadding = PaddingValues(horizontal = ClearrDimens.dp40, vertical = ClearrDimens.dp16)
            ) {
                Text(
                    "Get Started",
                    fontSize = ClearrTextSizes.sp16,
                    fontWeight = FontWeight.ExtraBold,
                    color = ClearrColors.Surface
                )
            }
        }
    }
}
