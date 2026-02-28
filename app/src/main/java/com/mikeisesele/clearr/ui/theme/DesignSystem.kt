package com.mikeisesele.clearr.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

object ClearrDimens {
    val dp0 = 0.dp
    val dp0_5 = 0.5.dp
    val dp1 = 1.dp
    val dp1_5 = 1.5.dp
    val dp2 = 2.dp
    val dp3 = 3.dp
    val dp4 = 4.dp
    val dp5 = 5.dp
    val dp6 = 6.dp
    val dp7 = 7.dp
    val dp8 = 8.dp
    val dp9 = 9.dp
    val dp10 = 10.dp
    val dp11 = 11.dp

    val dp12 = 12.dp
    val dp13 = 13.dp
    val dp14 = 14.dp
    val dp15 = 15.dp
    val dp16 = 16.dp
    val dp18 = 18.dp
    val dp20 = 20.dp
    val dp22 = 22.dp
    val dp24 = 24.dp
    val dp28 = 28.dp
    val dp30 = 30.dp
    val dp32 = 32.dp
    val dp34 = 34.dp
    val dp36 = 36.dp
    val dp38 = 38.dp
    val dp40 = 40.dp
    val dp42 = 42.dp
    val dp44 = 44.dp
    val dp48 = 48.dp
    val dp52 = 52.dp
    val dp56 = 56.dp
    val dp60 = 60.dp
    val dp64 = 64.dp
    val dp72 = 72.dp
    val dp80 = 80.dp
    val dp84 = 84.dp
    val dp90 = 90.dp
    val dp96 = 96.dp
    val dp99 = 99.dp
    val dp100 = 100.dp
    val dp120 = 120.dp
    val dp130 = 130.dp
    val dp140 = 140.dp
    val dp160 = 160.dp
    val dp200 = 200.dp
    val dp210 = 210.dp
    val dp280 = 280.dp
}

object ClearrTextSizes {
    val sp0_5 = 0.5.sp
    val sp7 = 7.sp
    val sp8 = 8.sp
    val sp9 = 9.sp
    val sp10 = 10.sp
    val sp11 = 11.sp
    val sp12 = 12.sp
    val sp13 = 13.sp
    val sp14 = 14.sp
    val sp15 = 15.sp
    val sp16 = 16.sp
    val sp17 = 17.sp
    val sp18 = 18.sp
    val sp20 = 20.sp
    val sp22 = 22.sp
    val sp24 = 24.sp
    val sp26 = 26.sp
    val sp28 = 28.sp
    val sp30 = 30.sp
    val sp32 = 32.sp
    val sp36 = 36.sp
    val sp40 = 40.sp
}
