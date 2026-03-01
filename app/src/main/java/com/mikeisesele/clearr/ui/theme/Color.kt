package com.mikeisesele.clearr.ui.theme

import androidx.compose.ui.graphics.Color
import com.mikeisesele.clearr.data.model.TrackerType

object ClearrColors {
    val Violet = Color(0xFF6C63FF)
    val Emerald = Color(0xFF00A67E)
    val Amber = Color(0xFFF59E0B)
    val Blue = Color(0xFF3B82F6)
    val Coral = Color(0xFFEF4444)
    val Orange = Color(0xFFFF9500)

    val BrandPrimary = Violet
    val BrandSecondary = Emerald
    val BrandAccent = Amber
    val BrandDanger = Coral
    val BrandBackground = Color(0xFFF7F7FB)
    val BrandText = Color(0xFF1A1A2E)

    val VioletBg = Color(0xFFEEF0FF)
    val EmeraldBg = Color(0xFFE6F7F3)
    val AmberBg = Color(0xFFFEF3C7)
    val BlueBg = Color(0xFFEFF6FF)
    val CoralBg = Color(0xFFFEE2E2)

    val VioletSurface = Color(0xFFE8E6FF)
    val EmeraldSurface = Color(0xFFD1F5EA)
    val AmberSurface = Color(0xFFFEF3C7)
    val BlueSurface = Color(0xFFDCEEFF)
    val CoralSurface = Color(0xFFFFE4E4)

    val Background = Color(0xFFF7F7FB)
    val Surface = Color(0xFFFFFFFF)
    val Border = Color(0xFFF0F0F0)
    val TextPrimary = Color(0xFF1A1A2E)
    val TextSecondary = Color(0xFF888888)
    val TextMuted = Color(0xFFBBBBBB)
    val Inactive = Color(0xFFDDDDDD)
    val NavBg = Color(0xFFEBEBF0)
    val Transparent = Color.Transparent

    val DarkBackground = Color(0xFF0F0F1A)
    val DarkSurface = Color(0xFF1A1A2E)
    val DarkCard = Color(0xFF242438)
    val DarkBorder = Color(0xFF2A2A3E)
    val DarkTextPrimary = Color(0xFFF0F0F8)
    val DarkTextMuted = Color(0xFF888888)
    val DarkInactive = Color(0xFF3A3A50)

    val WhatsAppGreen = Color(0xFF25D366)
}

fun TrackerType.brandColor(): Color = when (this) {
    TrackerType.GOALS -> ClearrColors.Emerald
    TrackerType.TODO -> ClearrColors.Amber
    TrackerType.BUDGET -> ClearrColors.Blue
}

fun TrackerType.brandBackground(): Color = when (this) {
    TrackerType.GOALS -> ClearrColors.EmeraldBg
    TrackerType.TODO -> ClearrColors.AmberBg
    TrackerType.BUDGET -> ClearrColors.BlueBg
}

fun TrackerType.brandSurface(): Color = when (this) {
    TrackerType.GOALS -> ClearrColors.EmeraldSurface
    TrackerType.TODO -> ClearrColors.AmberSurface
    TrackerType.BUDGET -> ClearrColors.BlueSurface
}

fun TrackerType.brandIcon(): String = when (this) {
    TrackerType.GOALS -> "🎯"
    TrackerType.TODO -> "☑"
    TrackerType.BUDGET -> "💳"
}

data class BudgetColorScheme(
    val color: Color,
    val background: Color
)

fun ClearrColors.fromToken(token: String): BudgetColorScheme = when (token.lowercase()) {
    "teal", "emerald" -> BudgetColorScheme(Emerald, EmeraldBg)
    "coral" -> BudgetColorScheme(Coral, CoralBg)
    "amber" -> BudgetColorScheme(Amber, AmberBg)
    "violet", "purple" -> BudgetColorScheme(Violet, VioletBg)
    "blue" -> BudgetColorScheme(Blue, BlueBg)
    "orange" -> BudgetColorScheme(Color(0xFFF97316), Color(0xFFFFF3E8))
    else -> BudgetColorScheme(Violet, VioletBg)
}

val Violet = ClearrColors.Violet
val Emerald = ClearrColors.Emerald
val Amber = ClearrColors.Amber
val Coral = ClearrColors.Coral
val Indigo400 = ClearrColors.Violet
val Indigo500 = ClearrColors.Violet
val Indigo600 = Color(0xFF5652D6)
val Green400 = ClearrColors.Emerald
val Amber400 = ClearrColors.Amber
val Red400 = ClearrColors.Coral
val DarkBg = ClearrColors.DarkBackground
val DarkSurface = ClearrColors.DarkSurface
val DarkCard = ClearrColors.DarkCard
val DarkBorder = ClearrColors.DarkBorder
val DarkText = ClearrColors.DarkTextPrimary
val DarkMuted = ClearrColors.DarkTextMuted
val DarkDim = ClearrColors.DarkInactive
val LightBg = ClearrColors.Background
val LightSurface = ClearrColors.Surface
val LightCard = Color(0xFFF1F5F9)
val LightBorder = ClearrColors.Border
val LightText = ClearrColors.TextPrimary
val LightMuted = ClearrColors.TextSecondary
val LightDim = ClearrColors.Inactive
val WhatsAppGreen = ClearrColors.WhatsAppGreen
