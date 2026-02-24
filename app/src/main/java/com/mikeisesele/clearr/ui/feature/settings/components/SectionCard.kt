package com.mikeisesele.clearr.ui.feature.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikeisesele.clearr.ui.theme.ClearrTheme
import com.mikeisesele.clearr.ui.theme.DuesColors
import com.mikeisesele.clearr.ui.theme.LocalDuesColors

@Composable
internal fun SectionCard(
    title: String,
    colors: DuesColors = LocalDuesColors.current,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.card),
        shape = RoundedCornerShape(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp16)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = colors.text)
            Spacer(Modifier.height(com.mikeisesele.clearr.ui.theme.ClearrDimens.dp12))
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionCardPreview() {
    ClearrTheme {
        val colors = LocalDuesColors.current
        SectionCard(title = "Active Year", colors = colors) {
            Text("2026", color = colors.text)
        }
    }
}
