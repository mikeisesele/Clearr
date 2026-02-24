package com.mikeisesele.clearr.ui.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikeisesele.clearr.R
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import kotlinx.coroutines.delay

// ── Slide data model ──────────────────────────────────────────────────────────

private data class SlideData(
    val icon: String,
    val accentColor: Color,
    val bgColor: Color,
    val headline: String,
    val subtext: String
)

private val slides = listOf(
    SlideData(
        icon = "◎",
        accentColor = ClearrColors.Violet,
        bgColor = ClearrColors.VioletBg,
        headline = "Clear your obligations.",
        subtext = "with clarity and proof, clear remittance, goals, todos, and budget tracking in one app."
    ),
    SlideData(
        icon = "◈",
        accentColor = ClearrColors.Emerald,
        bgColor = ClearrColors.EmeraldBg,
        headline = "Every tracker,\nEvery period.",
        subtext = "Create independent trackers for remittance, goals, todos, or budget — with weekly, monthly, quarterly, or custom periods."
    ),
    SlideData(
        icon = "⬡",
        accentColor = ClearrColors.Amber,
        bgColor = ClearrColors.AmberBg,
        headline = "At a glance, always.",
        subtext = "See what’s cleared, pending, or overdue across your obligations — without guesswork."
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// Root OnboardingScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    initialSlide: Int = 0,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    var currentSlide by remember { mutableIntStateOf(initialSlide.coerceIn(0, slides.lastIndex)) }
    var prevSlide by remember { mutableIntStateOf(currentSlide) }
    var goingForward by remember { mutableStateOf(true) }

    val slide = slides[currentSlide]

    val topBgColor by animateColorAsState(
        targetValue = slide.bgColor,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "top_bg"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClearrColors.Surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top zone (coloured) ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.48f)
                    .background(topBgColor)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)
                ) {
                    Text(
                        "Skip",
                        color = slide.accentColor.copy(alpha = 0.7f),
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
                            .clickable { onSkip() }
                            .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6)
                    )
                }

                AnimatedContent(
                    targetState = currentSlide,
                    transitionSpec = {
                        val offsetX = if (goingForward) 24 else -24
                        (fadeIn(tween(280)) + slideInHorizontally(tween(280)) { offsetX })
                            .togetherWith(
                                fadeOut(tween(280)) + slideOutHorizontally(tween(280)) { -offsetX }
                            )
                    },
                    label = "slide_content",
                    modifier = Modifier.fillMaxSize()
                ) { idx ->
                    val s = slides[idx]
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp32)
                            .padding(top = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp60, bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp64)
                                .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp18))
                                .background(s.accentColor.copy(alpha = 0.12f)),
//                                .background(if (idx == 0) ClearrColors.Transparent else s.accentColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                    painter = painterResource(id = R.drawable.clear_icon_vector),
                                    contentDescription = "Clearr icon",
                                    modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp200)
                                )
//                            if (idx == 0) {
//                                Image(
//                                    painter = painterResource(id = R.drawable.clear_icon_vector),
//                                    contentDescription = "Clearr icon",
//                                    modifier = Modifier.size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp200)
//                                )
//                            } else {
//                                Text(s.icon, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp28, color = s.accentColor)
//                            }
                        }
                        Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20))
                        Text(
                            s.headline,
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp24,
                            fontWeight = FontWeight.ExtraBold,
                            color = ClearrColors.TextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp30
                        )
                        Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
                        Text(
                            s.subtext,
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14,
                            color = ClearrColors.TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp22
                        )
                    }
                }
            }

            // ── Bottom zone (white) ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.36f)
                    .background(ClearrColors.Surface),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentSlide,
                    transitionSpec = {
                        val offsetX = if (goingForward) 24 else -24
                        (fadeIn(tween(280, delayMillis = 60)) + slideInHorizontally(tween(280, delayMillis = 60)) { offsetX })
                            .togetherWith(
                                fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -offsetX }
                            )
                    },
                    label = "slide_visual"
                ) { idx ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12),
                        contentAlignment = Alignment.Center
                    ) {
                        when (idx) {
                            0 -> Slide1Visual()
                            1 -> Slide2Visual()
                            2 -> Slide3Visual()
                        }
                    }
                }
            }

            // ── Navigation bar ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ClearrColors.Surface)
                    .navigationBarsPadding()
                    .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24)
                    .padding(bottom = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp32, top = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.indices.forEach { idx ->
                        val isActive = idx == currentSlide
                        val dotWidth by animateDpAsState(
                            targetValue = if (isActive) com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24 else com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8,
                            animationSpec = tween(300),
                            label = "dot_width"
                        )
                        Box(
                            modifier = Modifier
                                .height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)
                                .width(dotWidth)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) slide.accentColor
                                    else ClearrColors.Inactive
                                )
                                .clickable {
                                    goingForward = idx > currentSlide
                                    prevSlide = currentSlide
                                    currentSlide = idx
                                }
                        )
                    }
                }

                if (currentSlide > 0) {
                    IconButton(
                        onClick = {
                            goingForward = false
                            prevSlide = currentSlide
                            currentSlide--
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp48)
                            .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14))
                            .background(ClearrColors.NavBg)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ClearrColors.TextPrimary
                        )
                    }
                }

                val isLastSlide = currentSlide == slides.lastIndex
                Button(
                    onClick = {
                        if (isLastSlide) {
                            onComplete()
                        } else {
                            goingForward = true
                            prevSlide = currentSlide
                            currentSlide++
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = slide.accentColor,
                        contentColor = ClearrColors.Surface
                    ),
                    shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14),
                    contentPadding = PaddingValues(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp24),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp48)
                ) {
                    Text(
                        if (isLastSlide) "Let's go →" else "Next →",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Slide 1 Visual
// ─────────────────────────────────────────────────────────────────────────────

private val memberNames = listOf("Henry", "Simon", "Dare", "Tobi", "Michael")

@Composable
private fun Slide1Visual() {
    var clearedIndices by remember { mutableStateOf(setOf(0, 2)) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_200)
            clearedIndices = buildSet {
                memberNames.indices.forEach { i ->
                    if ((i + clearedIndices.size) % 2 == 0) add(i)
                }
                if (size < 2) add(0)
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6),
        modifier = Modifier.fillMaxWidth()
    ) {
        memberNames.forEachIndexed { i, name ->
            val cleared = i in clearedIndices
            val rowOffset by animateDpAsState(
                targetValue = if (cleared) (-2).dp else com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2,
                animationSpec = tween(400),
                label = "row_offset_$i"
            )
            val avatarBg by animateColorAsState(
                targetValue = if (cleared) ClearrColors.EmeraldBg else ClearrColors.Border,
                animationSpec = tween(400),
                label = "avatar_bg_$i"
            )
            val statusColor by animateColorAsState(
                targetValue = if (cleared) ClearrColors.Emerald else ClearrColors.TextMuted,
                animationSpec = tween(400),
                label = "status_color_$i"
            )

            Surface(
                color = ClearrColors.Surface,
                shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14),
                shadowElevation = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = rowOffset)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10)
                ) {
                    Box(
                        modifier = Modifier
                            .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp30)
                            .clip(CircleShape)
                            .background(avatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            name.first().toString(),
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13,
                            fontWeight = FontWeight.Bold,
                            color = if (cleared) ClearrColors.Emerald else ClearrColors.TextSecondary
                        )
                    }
                    Text(
                        name,
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp13,
                        fontWeight = FontWeight.SemiBold,
                        color = ClearrColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (cleared) "Cleared ✓" else "Pending...",
                        fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp11,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                    Box(
                        modifier = Modifier
                            .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp7)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Slide 2 Visual
// ─────────────────────────────────────────────────────────────────────────────

private data class MockTracker(val name: String, val color: Color, val bg: Color, val icon: String, val paid: Int, val total: Int)

private val mockTrackers = listOf(
    MockTracker("Client Remittance Status", ClearrColors.Violet,  ClearrColors.VioletBg,  "₦", 7, 12),
    MockTracker("Weekly Goals Progress",    ClearrColors.Emerald, ClearrColors.EmeraldBg, "✓", 18, 23),
    MockTracker("Todo Completion Tracker",  ClearrColors.Amber,   ClearrColors.AmberBg,   "⬡", 4, 9)
)

@Composable
private fun Slide2Visual() {
    var animated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); animated = true }

    Column(verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8), modifier = Modifier.fillMaxWidth()) {
        mockTrackers.forEachIndexed { i, tracker ->
            val pct = tracker.paid.toFloat() / tracker.total

            val animatedPct by animateFloatAsState(
                targetValue = if (animated) pct else 0f,
                animationSpec = tween(1000, delayMillis = i * 100, easing = FastOutSlowInEasing),
                label = "bar_$i"
            )
            val cardAlpha by animateFloatAsState(
                targetValue = if (animated) 1f else 0f,
                animationSpec = tween(300, delayMillis = i * 100),
                label = "card_alpha_$i"
            )
            val cardOffset by animateDpAsState(
                targetValue = if (animated) com.mikeisesele.clearr.ui.theme.ClearrDimens.dp0 else com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12,
                animationSpec = tween(300, delayMillis = i * 100),
                label = "card_offset_$i"
            )

            Surface(
                color = ClearrColors.Surface,
                shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14),
                shadowElevation = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp1,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(cardAlpha)
                    .offset(y = cardOffset)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp14, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10)
                ) {
                    Box(
                        modifier = Modifier
                            .size(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp32)
                            .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10))
                            .background(tracker.bg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(tracker.icon, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp15, color = tracker.color, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tracker.name, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp12, fontWeight = FontWeight.SemiBold, color = ClearrColors.TextPrimary)
                            Text("${tracker.paid}/${tracker.total}", fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp11, fontWeight = FontWeight.Bold, color = tracker.color)
                        }
                        Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp5))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp5)
                                .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp99))
                                .background(ClearrColors.Border)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedPct)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp99))
                                    .background(tracker.color)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Slide 3 Visual
// ─────────────────────────────────────────────────────────────────────────────

private val slide3Names = listOf("John", "Simon", "Jessy", "Chelsea", "Mike", "Ola.")
private val slide3Cleared = setOf(0, 1, 3, 5)

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun Slide3Visual() {
    val clearedCount = slide3Cleared.size
    val totalCount = slide3Names.size
    val pct = clearedCount.toFloat() / totalCount

    var animated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); animated = true }
    val animatedPct by animateFloatAsState(
        targetValue = if (animated) pct else 0f,
        animationSpec = tween(1_000, easing = FastOutSlowInEasing),
        label = "period_bar"
    )

    Surface(
        color = ClearrColors.Surface,
        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
        shadowElevation = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp2,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("February 2026", fontWeight = FontWeight.Bold, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14, color = ClearrColors.TextPrimary)
                Text("${(pct * 100).toInt()}%", fontWeight = FontWeight.ExtraBold, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp14, color = ClearrColors.Violet)
            }
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6)
                    .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp99))
                    .background(ClearrColors.Border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedPct)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp99))
                        .background(ClearrColors.Violet)
                )
            }
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp6),
                verticalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp5)
            ) {
                slide3Names.forEachIndexed { index, name ->
                    val cleared = index in slide3Cleared
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp20))
                            .background(if (cleared) ClearrColors.EmeraldBg else ClearrColors.CoralBg)
                            .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp4)
                    ) {
                        Text(
                            name,
                            fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp10,
                            fontWeight = FontWeight.SemiBold,
                            color = if (cleared) ClearrColors.Emerald else ClearrColors.Coral
                        )
                    }
                }
            }
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10))
            Row(horizontalArrangement = Arrangement.spacedBy(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)) {
                StatTile(label = "Cleared", value = "$clearedCount", color = ClearrColors.Emerald, bg = ClearrColors.EmeraldBg, modifier = Modifier.weight(1f))
                StatTile(label = "Pending", value = "${totalCount - clearedCount}", color = ClearrColors.Amber, bg = ClearrColors.AmberBg, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, color: Color, bg: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp10))
            .background(bg)
            .padding(horizontal = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12, vertical = com.mikeisesele.clearr.ui.theme.ClearrDimens.dp8)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp18, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = com.mikeisesele.clearr.ui.theme.ClearrTextSizes.sp10, color = color.copy(alpha = 0.7f))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    ClearrTheme {
        OnboardingScreen(initialSlide = 0, onComplete = {}, onSkip = {})
    }
}
