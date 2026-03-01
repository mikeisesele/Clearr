package com.mikeisesele.clearr.ui.feature.goals.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors

@Composable
internal fun GoalSectionTitle(label: String) {
    val colors = LocalClearrUiColors.current
    Text(
        text = label,
        fontSize = ClearrTextSizes.sp12,
        fontWeight = FontWeight.SemiBold,
        color = colors.muted,
        modifier = Modifier.padding(start = ClearrDimens.dp4)
    )
}

@Composable
internal fun GoalSheetInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    val colors = LocalClearrUiColors.current
    Surface(
        shape = RoundedCornerShape(ClearrDimens.dp12),
        color = colors.card,
        border = BorderStroke(ClearrDimens.dp1, colors.border),
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(horizontal = ClearrDimens.dp14, vertical = ClearrDimens.dp13)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                keyboardOptions = keyboardOptions,
                cursorBrush = SolidColor(colors.muted),
                textStyle = TextStyle(color = colors.text, fontSize = ClearrTextSizes.sp15),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isBlank()) {
                        Text(placeholder, color = colors.muted, fontSize = ClearrTextSizes.sp15)
                    }
                    inner()
                }
            )
        }
    }
}
