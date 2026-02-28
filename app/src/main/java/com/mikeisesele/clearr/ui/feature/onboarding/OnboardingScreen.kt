package com.mikeisesele.clearr.ui.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.mikeisesele.clearr.R
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.ClearrTheme

@Composable
fun OnboardingScreen(
    initialSlide: Int = 0,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    var currentSlide by remember { mutableIntStateOf(initialSlide.coerceIn(0, slides.lastIndex)) }
    var goingForward by remember { mutableStateOf(true) }
    val slide = slides[currentSlide]

    val topBgColor by animateColorAsState(
        targetValue = slide.bgColor,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "top_bg"
    )

    Box(modifier = Modifier.fillMaxSize().background(ClearrColors.Surface)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(0.48f).background(topBgColor)) {
                Box(modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(ClearrDimens.dp16)) {
                    Text(
                        "Skip",
                        color = slide.accentColor.copy(alpha = 0.7f),
                        fontSize = ClearrTextSizes.sp13,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clip(RoundedCornerShape(ClearrDimens.dp8)).clickable { onSkip() }.padding(horizontal = ClearrDimens.dp10, vertical = ClearrDimens.dp6)
                    )
                }

                AnimatedContent(
                    targetState = currentSlide,
                    transitionSpec = {
                        val offsetX = if (goingForward) 24 else -24
                        (fadeIn(tween(280)) + slideInHorizontally(tween(280)) { offsetX }) togetherWith
                            (fadeOut(tween(280)) + slideOutHorizontally(tween(280)) { -offsetX })
                    },
                    label = "slide_content",
                    modifier = Modifier.fillMaxSize()
                ) { idx ->
                    val s = slides[idx]
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = ClearrDimens.dp32).padding(top = ClearrDimens.dp60, bottom = ClearrDimens.dp24),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(ClearrDimens.dp64).clip(RoundedCornerShape(ClearrDimens.dp18)).background(s.accentColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.clear_icon_vector),
                                contentDescription = "Clearr icon",
                                modifier = Modifier.size(ClearrDimens.dp200)
                            )
                        }
                        Spacer(Modifier.height(ClearrDimens.dp20))
                        Text(s.headline, fontSize = ClearrTextSizes.sp24, fontWeight = FontWeight.ExtraBold, color = ClearrColors.TextPrimary, textAlign = TextAlign.Center, lineHeight = ClearrTextSizes.sp30)
                        Spacer(Modifier.height(ClearrDimens.dp12))
                        Text(s.subtext, fontSize = ClearrTextSizes.sp14, color = ClearrColors.TextSecondary, textAlign = TextAlign.Center, lineHeight = ClearrTextSizes.sp22)
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().weight(0.36f).background(ClearrColors.Surface), contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = currentSlide,
                    transitionSpec = {
                        val offsetX = if (goingForward) 24 else -24
                        (fadeIn(tween(280, delayMillis = 60)) + slideInHorizontally(tween(280, delayMillis = 60)) { offsetX }) togetherWith
                            (fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -offsetX })
                    },
                    label = "slide_visual"
                ) { idx ->
                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = ClearrDimens.dp24, vertical = ClearrDimens.dp12), contentAlignment = Alignment.Center) {
                        when (idx) {
                            0 -> Slide1Visual()
                            1 -> Slide2Visual()
                            else -> Slide3Visual()
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().background(ClearrColors.Surface).navigationBarsPadding().padding(horizontal = ClearrDimens.dp24).padding(bottom = ClearrDimens.dp32, top = ClearrDimens.dp8)) {
                Row(modifier = Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8), verticalAlignment = Alignment.CenterVertically) {
                    slides.indices.forEach { idx ->
                        val isActive = idx == currentSlide
                        val dotWidth by animateDpAsState(targetValue = if (isActive) ClearrDimens.dp24 else ClearrDimens.dp8, animationSpec = tween(300), label = "dot_width")
                        Box(
                            modifier = Modifier.height(ClearrDimens.dp8).width(dotWidth).clip(CircleShape).background(if (isActive) slide.accentColor else ClearrColors.Inactive).clickable {
                                goingForward = idx > currentSlide
                                currentSlide = idx
                            }
                        )
                    }
                }

                if (currentSlide > 0) {
                    IconButton(
                        onClick = { goingForward = false; currentSlide-- },
                        modifier = Modifier.align(Alignment.CenterStart).size(ClearrDimens.dp48).clip(RoundedCornerShape(ClearrDimens.dp14)).background(ClearrColors.NavBg)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ClearrColors.TextPrimary)
                    }
                }

                val isLastSlide = currentSlide == slides.lastIndex
                Button(
                    onClick = {
                        if (isLastSlide) onComplete() else { goingForward = true; currentSlide++ }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = slide.accentColor, contentColor = ClearrColors.Surface),
                    shape = RoundedCornerShape(ClearrDimens.dp14),
                    contentPadding = PaddingValues(horizontal = ClearrDimens.dp24),
                    modifier = Modifier.align(Alignment.CenterEnd).height(ClearrDimens.dp48)
                ) {
                    Text(if (isLastSlide) "Let's go →" else "Next →", fontWeight = FontWeight.ExtraBold, fontSize = ClearrTextSizes.sp14)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    ClearrTheme { OnboardingScreen(onComplete = {}, onSkip = {}) }
}
