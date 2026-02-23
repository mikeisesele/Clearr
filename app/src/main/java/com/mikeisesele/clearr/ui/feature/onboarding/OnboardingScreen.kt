package com.mikeisesele.clearr.ui.feature.onboarding

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
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
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
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
        headline = "Know who's cleared.",
        subtext = "Track dues, attendance, tasks and events across all your groups — in one place."
    ),
    SlideData(
        icon = "◈",
        accentColor = ClearrColors.Emerald,
        bgColor = ClearrColors.EmeraldBg,
        headline = "Every group.\nEvery period.",
        subtext = "Create independent trackers for each group. Weekly, monthly, quarterly — any frequency."
    ),
    SlideData(
        icon = "⬡",
        accentColor = ClearrColors.Amber,
        bgColor = ClearrColors.AmberBg,
        headline = "At a glance, always.",
        subtext = "See who's paid, who's absent, who's pending — without digging through spreadsheets."
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
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() as? ComponentActivity }
    val themeIsDark = LocalDuesColors.current.isDark

    DisposableEffect(activity, themeIsDark) {
        val transparent = android.graphics.Color.TRANSPARENT
        activity?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(transparent, transparent),
            navigationBarStyle = SystemBarStyle.light(transparent, transparent)
        )
        onDispose {
            activity?.enableEdgeToEdge(
                statusBarStyle = if (themeIsDark) {
                    SystemBarStyle.dark(transparent)
                } else {
                    SystemBarStyle.light(transparent, transparent)
                },
                navigationBarStyle = if (themeIsDark) {
                    SystemBarStyle.dark(transparent)
                } else {
                    SystemBarStyle.light(transparent, transparent)
                }
            )
        }
    }

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
                        .padding(16.dp)
                ) {
                    Text(
                        "Skip",
                        color = slide.accentColor.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSkip() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
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
                            .padding(horizontal = 32.dp)
                            .padding(top = 60.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(s.accentColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(s.icon, fontSize = 28.sp, color = s.accentColor)
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            s.headline,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ClearrColors.TextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 30.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            s.subtext,
                            fontSize = 14.sp,
                            color = ClearrColors.TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
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
                            .padding(horizontal = 24.dp, vertical = 12.dp),
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
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp, top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.indices.forEach { idx ->
                        val isActive = idx == currentSlide
                        val dotWidth by animateDpAsState(
                            targetValue = if (isActive) 24.dp else 8.dp,
                            animationSpec = tween(300),
                            label = "dot_width"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
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
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
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
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .height(48.dp)
                ) {
                    Text(
                        if (isLastSlide) "Let's go →" else "Next →",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
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
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        memberNames.forEachIndexed { i, name ->
            val cleared = i in clearedIndices
            val rowOffset by animateDpAsState(
                targetValue = if (cleared) (-2).dp else 2.dp,
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
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = rowOffset)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(avatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            name.first().toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cleared) ClearrColors.Emerald else ClearrColors.TextSecondary
                        )
                    }
                    Text(
                        name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ClearrColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (cleared) "Cleared ✓" else "Pending...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
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
    MockTracker("Term 1 Fees",     ClearrColors.Violet,  ClearrColors.VioletBg,  "₦", 7, 12),
    MockTracker("Sunday Service",  ClearrColors.Emerald, ClearrColors.EmeraldBg, "✓", 18, 23),
    MockTracker("Task Roster",     ClearrColors.Amber,   ClearrColors.AmberBg,   "⬡", 4, 9)
)

@Composable
private fun Slide2Visual() {
    var animated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); animated = true }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
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
                targetValue = if (animated) 0.dp else 12.dp,
                animationSpec = tween(300, delayMillis = i * 100),
                label = "card_offset_$i"
            )

            Surface(
                color = ClearrColors.Surface,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(cardAlpha)
                    .offset(y = cardOffset)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(tracker.bg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(tracker.icon, fontSize = 15.sp, color = tracker.color, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tracker.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ClearrColors.TextPrimary)
                            Text("${tracker.paid}/${tracker.total}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tracker.color)
                        }
                        Spacer(Modifier.height(5.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(ClearrColors.Border)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedPct)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(99.dp))
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

private val slide3Names = listOf("Henry", "Simon", "Dare", "Tobi", "Michael", "Faruk", "Chidi", "Olu", "Michael I.")
private val slide3Cleared = setOf(0, 2, 3, 5, 6, 8)

@Composable
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
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("February 2026", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ClearrColors.TextPrimary)
                Text("${(pct * 100).toInt()}%", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = ClearrColors.Violet)
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(ClearrColors.Border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedPct)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(99.dp))
                        .background(ClearrColors.Violet)
                )
            }
            Spacer(Modifier.height(12.dp))
            val rows = slide3Names.chunked(3)
            rows.forEach { rowNames ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 5.dp)
                ) {
                    rowNames.forEachIndexed { i, name ->
                        val globalIdx = slide3Names.indexOf(name)
                        val cleared = globalIdx in slide3Cleared
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (cleared) ClearrColors.EmeraldBg else ClearrColors.CoralBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (cleared) ClearrColors.Emerald else ClearrColors.Coral
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 10.sp, color = color.copy(alpha = 0.7f))
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
