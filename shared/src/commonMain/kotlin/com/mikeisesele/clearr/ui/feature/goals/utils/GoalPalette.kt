package com.mikeisesele.clearr.ui.feature.goals.utils

import androidx.compose.ui.graphics.Color
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.fromToken

data class GoalPalette(
    val color: Color,
    val background: Color
)

fun goalPalette(token: String): GoalPalette {
    val scheme = ClearrColors.fromToken(token)
    return GoalPalette(color = scheme.color, background = scheme.background)
}
