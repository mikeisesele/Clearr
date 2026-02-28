package com.mikeisesele.clearr.ui.feature.onboarding.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import kotlinx.coroutines.delay

internal data class SlideData(
    val icon: String,
    val accentColor: Color,
    val bgColor: Color,
    val headline: String,
    val subtext: String
)

internal val slides = listOf(
    SlideData("◎", ClearrColors.Violet, ClearrColors.VioletBg, "Clear your obligations.", "with clarity and proof, clear remittance, goals, todos, and budget tracking in one app."),
    SlideData("◈", ClearrColors.Emerald, ClearrColors.EmeraldBg, "Every tracker,\nEvery period.", "Create independent trackers for remittance, goals, todos, or budget — with weekly, monthly, quarterly, or custom periods."),
    SlideData("⬡", ClearrColors.Amber, ClearrColors.AmberBg, "At a glance, always.", "See what’s cleared, pending, or overdue across your obligations — without guesswork.")
)

private val memberNames = listOf("Henry", "Simon", "Dare", "Tobi", "Michael")
private data class MockTracker(val name: String, val color: Color, val bg: Color, val icon: String, val paid: Int, val total: Int)
private val mockTrackers = listOf(
    MockTracker("Client Remittance Status", ClearrColors.Violet, ClearrColors.VioletBg, "₦", 7, 12),
    MockTracker("Weekly Goals Progress", ClearrColors.Emerald, ClearrColors.EmeraldBg, "✓", 18, 23),
    MockTracker("Todo Completion Tracker", ClearrColors.Amber, ClearrColors.AmberBg, "⬡", 4, 9)
)
private val slide3Names = listOf("John", "Simon", "Jessy", "Chelsea", "Mike", "Ola.")
private val slide3Cleared = setOf(0, 1, 3, 5)

@Composable
internal fun Slide1Visual() {
    var clearedIndices by remember { mutableStateOf(setOf(0, 2)) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_200)
            clearedIndices = buildSet {
                memberNames.indices.forEach { i -> if ((i + clearedIndices.size) % 2 == 0) add(i) }
                if (size < 2) add(0)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp6), modifier = Modifier.fillMaxWidth()) {
        memberNames.forEachIndexed { i, name ->
            val cleared = i in clearedIndices
            val rowOffset by animateDpAsState(targetValue = if (cleared) (-2).dp else ClearrDimens.dp2, animationSpec = tween(400), label = "row_offset_$i")
            val avatarBg = if (cleared) ClearrColors.EmeraldBg else ClearrColors.Border
            val statusColor = if (cleared) ClearrColors.Emerald else ClearrColors.TextMuted

            Surface(color = ClearrColors.Surface, shape = RoundedCornerShape(ClearrDimens.dp14), shadowElevation = ClearrDimens.dp1, modifier = Modifier.fillMaxWidth().offset(x = rowOffset)) {
                Row(modifier = Modifier.padding(horizontal = ClearrDimens.dp14, vertical = ClearrDimens.dp10), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp10)) {
                    Box(modifier = Modifier.size(ClearrDimens.dp30).clip(CircleShape).background(avatarBg), contentAlignment = Alignment.Center) {
                        Text(name.first().toString(), fontSize = ClearrTextSizes.sp13, color = if (cleared) ClearrColors.Emerald else ClearrColors.TextSecondary)
                    }
                    Text(name, fontSize = ClearrTextSizes.sp13, color = ClearrColors.TextPrimary, modifier = Modifier.weight(1f))
                    Text(if (cleared) "Cleared ✓" else "Pending...", fontSize = ClearrTextSizes.sp11, color = statusColor)
                    Box(modifier = Modifier.size(ClearrDimens.dp7).clip(CircleShape).background(statusColor))
                }
            }
        }
    }
}

@Composable
internal fun Slide2Visual() {
    var animated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); animated = true }

    Column(verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp8), modifier = Modifier.fillMaxWidth()) {
        mockTrackers.forEachIndexed { i, tracker ->
            val pct = tracker.paid.toFloat() / tracker.total
            val animatedPct by animateFloatAsState(targetValue = if (animated) pct else 0f, animationSpec = tween(1000, delayMillis = i * 100, easing = FastOutSlowInEasing), label = "bar_$i")
            val cardAlpha by animateFloatAsState(targetValue = if (animated) 1f else 0f, animationSpec = tween(300, delayMillis = i * 100), label = "card_alpha_$i")
            val cardOffset by animateDpAsState(targetValue = if (animated) ClearrDimens.dp0 else ClearrDimens.dp12, animationSpec = tween(300, delayMillis = i * 100), label = "card_offset_$i")

            Surface(color = ClearrColors.Surface, shape = RoundedCornerShape(ClearrDimens.dp14), shadowElevation = ClearrDimens.dp1, modifier = Modifier.fillMaxWidth().alpha(cardAlpha).offset(y = cardOffset)) {
                Row(modifier = Modifier.padding(horizontal = ClearrDimens.dp14, vertical = ClearrDimens.dp10), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp10)) {
                    Box(modifier = Modifier.size(ClearrDimens.dp32).clip(RoundedCornerShape(ClearrDimens.dp10)).background(tracker.bg), contentAlignment = Alignment.Center) {
                        Text(tracker.icon, fontSize = ClearrTextSizes.sp15, color = tracker.color)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(tracker.name, fontSize = ClearrTextSizes.sp12, color = ClearrColors.TextPrimary)
                            Text("${tracker.paid}/${tracker.total}", fontSize = ClearrTextSizes.sp11, color = tracker.color)
                        }
                        Spacer(Modifier.height(ClearrDimens.dp5))
                        Box(modifier = Modifier.fillMaxWidth().height(ClearrDimens.dp5).clip(RoundedCornerShape(ClearrDimens.dp99)).background(ClearrColors.Border)) {
                            Box(modifier = Modifier.fillMaxWidth(animatedPct).height(ClearrDimens.dp5).clip(RoundedCornerShape(ClearrDimens.dp99)).background(tracker.color))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Slide3Visual() {
    val clearedCount = slide3Cleared.size
    val totalCount = slide3Names.size
    val pct = clearedCount.toFloat() / totalCount

    var animated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); animated = true }
    val animatedPct by animateFloatAsState(targetValue = if (animated) pct else 0f, animationSpec = tween(1000, easing = FastOutSlowInEasing), label = "period_bar")

    Surface(color = ClearrColors.Surface, shape = RoundedCornerShape(ClearrDimens.dp16), shadowElevation = ClearrDimens.dp2, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(ClearrDimens.dp16)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("February 2026", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = ClearrTextSizes.sp14, color = ClearrColors.TextPrimary)
                Text("${(pct * 100).toInt()}%", fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, fontSize = ClearrTextSizes.sp14, color = ClearrColors.Violet)
            }
            Spacer(Modifier.height(ClearrDimens.dp8))
            Box(modifier = Modifier.fillMaxWidth().height(ClearrDimens.dp6).clip(RoundedCornerShape(ClearrDimens.dp99)).background(ClearrColors.Border)) {
                Box(modifier = Modifier.fillMaxWidth(animatedPct).height(ClearrDimens.dp6).clip(RoundedCornerShape(ClearrDimens.dp99)).background(ClearrColors.Violet))
            }
            Spacer(Modifier.height(ClearrDimens.dp12))
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp6), verticalArrangement = Arrangement.spacedBy(ClearrDimens.dp5)) {
                slide3Names.forEachIndexed { index, name ->
                    val cleared = index in slide3Cleared
                    Box(modifier = Modifier.clip(RoundedCornerShape(ClearrDimens.dp20)).background(if (cleared) ClearrColors.EmeraldBg else ClearrColors.CoralBg).padding(horizontal = ClearrDimens.dp10, vertical = ClearrDimens.dp4)) {
                        Text(name, fontSize = ClearrTextSizes.sp10, color = if (cleared) ClearrColors.Emerald else ClearrColors.Coral)
                    }
                }
            }
            Spacer(Modifier.height(ClearrDimens.dp10))
            Row(horizontalArrangement = Arrangement.spacedBy(ClearrDimens.dp8)) {
                OnboardingStatTile("Cleared", "$clearedCount", ClearrColors.Emerald, ClearrColors.EmeraldBg, Modifier.weight(1f))
                OnboardingStatTile("Pending", "${totalCount - clearedCount}", ClearrColors.Amber, ClearrColors.AmberBg, Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun OnboardingStatTile(label: String, value: String, color: Color, bg: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(ClearrDimens.dp10)).background(bg).padding(horizontal = ClearrDimens.dp12, vertical = ClearrDimens.dp8)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = ClearrTextSizes.sp18, color = color)
            Text(label, fontSize = ClearrTextSizes.sp10, color = color.copy(alpha = 0.7f))
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun Slide1VisualPreview() {
    ClearrTheme { Slide1Visual() }
}
