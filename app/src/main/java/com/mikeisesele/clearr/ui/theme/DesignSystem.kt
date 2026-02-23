package com.mikeisesele.clearr.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ClearrSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp
)

@Immutable
data class ClearrRadii(
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val pill: Dp = 99.dp
)

@Immutable
data class ClearrSizes(
    val iconSm: Dp = 16.dp,
    val iconMd: Dp = 20.dp,
    val iconLg: Dp = 24.dp,
    val chipHeight: Dp = 36.dp,
    val buttonHeight: Dp = 48.dp,
    val fab: Dp = 52.dp
)

val LocalClearrSpacing = staticCompositionLocalOf { ClearrSpacing() }
val LocalClearrRadii = staticCompositionLocalOf { ClearrRadii() }
val LocalClearrSizes = staticCompositionLocalOf { ClearrSizes() }

object ClearrDS {
    val spacing: ClearrSpacing
        @Composable get() = LocalClearrSpacing.current
    val radii: ClearrRadii
        @Composable get() = LocalClearrRadii.current
    val sizes: ClearrSizes
        @Composable get() = LocalClearrSizes.current
}

