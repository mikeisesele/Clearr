package com.mikeisesele.clearr.ui.feature.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.R
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import kotlinx.coroutines.delay

/**
 * Splash Screen — Screen 1 of onboarding.
 *
 * Phase 1 (0 → 1800ms): Large centred logo group fades in.
 * Phase 2 (1800ms → exit): Logo group fades out + scales down.
 * Phase 3 (after exit): "Get Started" state fades in — compact logo/tagline
 *   at the top + CTA button at the bottom, both on the same violet canvas.
 *
 * No back navigation from this screen.
 */
@Composable
fun SplashScreen(onGetStarted: () -> Unit) {

    // ── State flags ───────────────────────────────────────────────────────────
    var splashLogoVisible by remember { mutableStateOf(false) }
    var ctaVisible        by remember { mutableStateOf(false) }

    // ── Splash logo animations ────────────────────────────────────────────────
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

    // ── CTA state animations (compact logo + button) ──────────────────────────
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

    // ── Sequence ──────────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        splashLogoVisible = true      // logo fades in
        delay(1_800)                  // dwell
        splashLogoVisible = false     // logo fades out + shrinks
        delay(500)                    // wait for exit to finish
        ctaVisible = true             // CTA state fades in
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // ── Decorative background circles ─────────────────────────────────────
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-60).dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(ClearrColors.Violet.copy(alpha = 0.08f))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 60.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(ClearrColors.Violet.copy(alpha = 0.06f))
        )

        // ── Phase 1: Large centred splash logo ────────────────────────────────
        Column(
            modifier = Modifier
                .alpha(splashAlpha)
                .offset(y = splashOffsetY.dp)
                .scale(splashScale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.clear_icon_vector),
                contentDescription = "Clearr icon",
                modifier = Modifier.size(200.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "Clearr",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = ClearrColors.Violet,
                letterSpacing = (-1).sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Clear your obligations.",
                fontSize = 14.sp,
                color = ClearrColors.Violet.copy(alpha = 0.72f)
            )
        }

        // ── Phase 2: CTA state — compact logo + tagline + button ──────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .alpha(ctaAlpha)
                .offset(y = ctaOffsetY.dp)
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.clear_icon_vector),
                contentDescription = "Clearr icon",
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Clearr",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = ClearrColors.Violet,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Clear your obligations.",
                fontSize = 13.sp,
                color = ClearrColors.Violet.copy(alpha = 0.72f)
            )
            Spacer(Modifier.height(36.dp))
            Button(
                onClick = onGetStarted,
                enabled = ctaVisible,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClearrColors.Violet,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 40.dp, vertical = 16.dp)
            ) {
                Text(
                    "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    ClearrTheme {
        SplashScreen(onGetStarted = {})
    }
}
