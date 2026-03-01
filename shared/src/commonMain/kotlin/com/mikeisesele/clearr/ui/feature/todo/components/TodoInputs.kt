package com.mikeisesele.clearr.ui.feature.todo.components

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
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.LocalClearrUiColors

@Composable
internal fun TodoSheetInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val colors = LocalClearrUiColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(ClearrDimens.dp12),
        color = colors.card,
        border = BorderStroke(ClearrDimens.dp1, colors.border)
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
