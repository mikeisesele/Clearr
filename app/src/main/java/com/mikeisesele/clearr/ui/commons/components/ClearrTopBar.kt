package com.mikeisesele.clearr.ui.commons.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes

@Composable
fun ClearrTopBar(
    title: String,
    subtitle: String? = null,
    leadingIcon: String = "←",
    onLeadingClick: (() -> Unit)? = null,
    actionIcon: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionContainerColor: androidx.compose.ui.graphics.Color = ClearrColors.NavBg,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ClearrColors.Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp10),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(ClearrDimens.dp34),
                shape = RoundedCornerShape(ClearrDimens.dp10),
                color = ClearrColors.NavBg
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = if (onLeadingClick != null) Modifier.clickable { onLeadingClick() } else Modifier
                ) {
                    Text(leadingIcon, fontSize = ClearrTextSizes.sp15, color = ClearrColors.TextPrimary)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    fontSize = ClearrTextSizes.sp17,
                    fontWeight = FontWeight.SemiBold,
                    color = ClearrColors.TextPrimary
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = ClearrTextSizes.sp12,
                        color = ClearrColors.TextMuted
                    )
                }
            }

            if (!actionIcon.isNullOrBlank() && onActionClick != null) {
                Surface(
                    modifier = Modifier
                        .size(ClearrDimens.dp34)
                        .clickable { onActionClick() },
                    shape = RoundedCornerShape(ClearrDimens.dp10),
                    color = actionContainerColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            actionIcon,
                            fontSize = ClearrTextSizes.sp18,
                            color = ClearrColors.Surface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.size(ClearrDimens.dp34))
            }
        }
        HorizontalDivider(color = ClearrColors.Border)
    }
}
