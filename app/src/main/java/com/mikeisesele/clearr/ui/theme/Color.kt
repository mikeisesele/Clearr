package com.mikeisesele.clearr.ui.theme

import androidx.compose.ui.graphics.Color
import com.mikeisesele.clearr.data.model.RecordStatus
import com.mikeisesele.clearr.data.model.TrackerType

/**
 * Clearr Brand Color System
 * Four semantic primaries — every color carries meaning, never decorative.
 * Rule: Never hardcode a hex anywhere in the UI. Always reference ClearrColors.*
 */
object ClearrColors {

    // ── BRAND PRIMARIES ───────────────────────────────────────────────────────
    val Violet  = Color(0xFF6C63FF)  // Primary / Dues
    val Emerald = Color(0xFF00A67E)  // Success / Attendance / Cleared
    val Amber   = Color(0xFFF59E0B)  // Caution / Tasks / Pending
    val Blue    = Color(0xFF3B82F6)  // Info / Budget
    val Coral   = Color(0xFFEF4444)  // Danger / Events / Unpaid / Absent
    val Orange  = Color(0xFFFF9500)  // Emphasis for due-today / medium priority

    // ── BRAND TOKENS (for logo/marketing + app-level theming) ───────────────
    val BrandPrimary   = Violet
    val BrandSecondary = Emerald
    val BrandAccent    = Amber
    val BrandDanger    = Coral
    val BrandBackground = Color(0xFFF7F7FB)
    val BrandText      = Color(0xFF1A1A2E)

    // ── TINTED BACKGROUNDS (12% opacity on white) ─────────────────────────────
    val VioletBg  = Color(0xFFEEF0FF)
    val EmeraldBg = Color(0xFFE6F7F3)
    val AmberBg   = Color(0xFFFEF3C7)
    val BlueBg    = Color(0xFFEFF6FF)
    val CoralBg   = Color(0xFFFEE2E2)

    // ── TINTED SURFACES (18% opacity — chips, badges, icon containers) ────────
    val VioletSurface  = Color(0xFFE8E6FF)
    val EmeraldSurface = Color(0xFFD1F5EA)
    val AmberSurface   = Color(0xFFFEF3C7)
    val BlueSurface    = Color(0xFFDCEEFF)
    val CoralSurface   = Color(0xFFFFE4E4)

    // ── NEUTRALS ──────────────────────────────────────────────────────────────
    val Background   = Color(0xFFF7F7FB)   // App background
    val Surface      = Color(0xFFFFFFFF)   // Cards, sheets
    val Border       = Color(0xFFF0F0F0)   // Dividers, inactive progress track
    val TextPrimary  = Color(0xFF1A1A2E)   // Headlines, primary text
    val TextSecondary= Color(0xFF888888)   // Subtitles, hints
    val TextMuted    = Color(0xFFBBBBBB)   // Placeholders, disabled
    val Inactive     = Color(0xFFDDDDDD)   // Inactive dots, empty bars
    val NavBg        = Color(0xFFEBEBF0)   // Back button background
    val Transparent  = Color.Transparent

    // ── DARK MODE VARIANTS ────────────────────────────────────────────────────
    val DarkBackground  = Color(0xFF0F0F1A)
    val DarkSurface     = Color(0xFF1A1A2E)
    val DarkCard        = Color(0xFF242438)
    val DarkBorder      = Color(0xFF2A2A3E)
    val DarkTextPrimary = Color(0xFFF0F0F8)
    val DarkTextMuted   = Color(0xFF888888)
    val DarkInactive    = Color(0xFF3A3A50)

    // ── MISC LEGACY (kept for WhatsApp share button, not brand palette) ───────
    val WhatsAppGreen = Color(0xFF25D366)
}

// ── TrackerType extensions ────────────────────────────────────────────────────

fun TrackerType.brandColor(): Color = when (this) {
    TrackerType.DUES     -> ClearrColors.Violet
    TrackerType.GOALS    -> ClearrColors.Emerald
    TrackerType.TODO     -> ClearrColors.Amber
    TrackerType.BUDGET   -> ClearrColors.Blue
}

fun TrackerType.brandBackground(): Color = when (this) {
    TrackerType.DUES     -> ClearrColors.VioletBg
    TrackerType.GOALS    -> ClearrColors.EmeraldBg
    TrackerType.TODO     -> ClearrColors.AmberBg
    TrackerType.BUDGET   -> ClearrColors.BlueBg
}

fun TrackerType.brandSurface(): Color = when (this) {
    TrackerType.DUES     -> ClearrColors.VioletSurface
    TrackerType.GOALS    -> ClearrColors.EmeraldSurface
    TrackerType.TODO     -> ClearrColors.AmberSurface
    TrackerType.BUDGET   -> ClearrColors.BlueSurface
}

fun TrackerType.brandIcon(): String = when (this) {
    TrackerType.DUES     -> "₦"
    TrackerType.GOALS    -> "🎯"
    TrackerType.TODO     -> "☑"
    TrackerType.BUDGET   -> "💳"
}

data class BudgetColorScheme(
    val color: Color,
    val background: Color
)

fun ClearrColors.fromToken(token: String): BudgetColorScheme = when (token.lowercase()) {
    "teal" -> BudgetColorScheme(Emerald, EmeraldBg)
    "emerald" -> BudgetColorScheme(Emerald, EmeraldBg)
    "coral" -> BudgetColorScheme(Coral, CoralBg)
    "amber" -> BudgetColorScheme(Amber, AmberBg)
    "violet" -> BudgetColorScheme(Violet, VioletBg)
    "blue" -> BudgetColorScheme(Blue, BlueBg)
    "purple" -> BudgetColorScheme(Violet, VioletBg)
    "orange" -> BudgetColorScheme(Color(0xFFF97316), Color(0xFFFFF3E8))
    else -> BudgetColorScheme(Violet, VioletBg)
}

// ── RecordStatus extensions ───────────────────────────────────────────────────

fun RecordStatus.brandColor(): Color = when (this) {
    RecordStatus.PAID,
    RecordStatus.PRESENT,
    RecordStatus.DONE    -> ClearrColors.Emerald
    RecordStatus.PARTIAL,
    RecordStatus.PENDING -> ClearrColors.Amber
    RecordStatus.UNPAID,
    RecordStatus.ABSENT  -> ClearrColors.Coral
}

fun RecordStatus.brandBackground(): Color = when (this) {
    RecordStatus.PAID,
    RecordStatus.PRESENT,
    RecordStatus.DONE    -> ClearrColors.EmeraldBg
    RecordStatus.PARTIAL,
    RecordStatus.PENDING -> ClearrColors.AmberBg
    RecordStatus.UNPAID,
    RecordStatus.ABSENT  -> ClearrColors.CoralBg
}

fun RecordStatus.brandLabel(type: TrackerType): String = when (type) {
    TrackerType.DUES -> when (this) {
        RecordStatus.PAID    -> "Paid"
        RecordStatus.UNPAID  -> "Unpaid"
        RecordStatus.PARTIAL -> "Partial"
        else -> name.lowercase().replaceFirstChar { it.uppercase() }
    }
    TrackerType.GOALS -> when (this) {
        RecordStatus.DONE    -> "Done"
        RecordStatus.PENDING -> "Pending"
        else -> name.lowercase().replaceFirstChar { it.uppercase() }
    }
    TrackerType.TODO -> when (this) {
        RecordStatus.DONE    -> "Done"
        RecordStatus.PENDING -> "Pending"
        else -> name.lowercase().replaceFirstChar { it.uppercase() }
    }
    TrackerType.BUDGET -> when (this) {
        RecordStatus.PAID -> "On Track"
        RecordStatus.PARTIAL -> "Near Limit"
        RecordStatus.UNPAID -> "Over"
        else -> name.lowercase().replaceFirstChar { it.uppercase() }
    }
}

// ── Legacy aliases kept for backward compatibility with existing screens ───────
// These are the old Indigo/Green/Amber/Red tokens referenced in Theme.kt.
// Gradually migrate all call-sites to ClearrColors.*
val Violet  = ClearrColors.Violet
val Emerald = ClearrColors.Emerald
val Amber   = ClearrColors.Amber
val Coral   = ClearrColors.Coral

// Old Indigo aliases (map to Clearr Violet)
val Indigo400 = ClearrColors.Violet
val Indigo500 = ClearrColors.Violet
val Indigo600 = Color(0xFF5652D6)  // slightly darker shade, used in dark scheme

// Old semantic aliases
val Green400 = ClearrColors.Emerald
val Amber400 = ClearrColors.Amber
val Red400   = ClearrColors.Coral

// Old dark palette
val DarkBg      = ClearrColors.DarkBackground
val DarkSurface = ClearrColors.DarkSurface
val DarkCard    = ClearrColors.DarkCard
val DarkBorder  = ClearrColors.DarkBorder
val DarkText    = ClearrColors.DarkTextPrimary
val DarkMuted   = ClearrColors.DarkTextMuted
val DarkDim     = ClearrColors.DarkInactive

// Old light palette
val LightBg      = ClearrColors.Background
val LightSurface = ClearrColors.Surface
val LightCard    = Color(0xFFF1F5F9)
val LightBorder  = ClearrColors.Border
val LightText    = ClearrColors.TextPrimary
val LightMuted   = ClearrColors.TextSecondary
val LightDim     = ClearrColors.Inactive

val WhatsAppGreen = ClearrColors.WhatsAppGreen
