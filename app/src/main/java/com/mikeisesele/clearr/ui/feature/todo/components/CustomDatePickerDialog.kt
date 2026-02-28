package com.mikeisesele.clearr.ui.feature.todo.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mikeisesele.clearr.ui.theme.ClearrColors
import com.mikeisesele.clearr.ui.theme.ClearrDimens
import com.mikeisesele.clearr.ui.theme.ClearrTextSizes
import com.mikeisesele.clearr.ui.theme.LocalDuesColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun CustomDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val colors = LocalDuesColors.current
    val minSelectableDate = LocalDate.now().plusDays(1)
    val initial = if (initialDate.isBefore(minSelectableDate)) minSelectableDate else initialDate
    var displayedMonth by remember { mutableStateOf(YearMonth.from(initial)) }
    var selectedDate by remember { mutableStateOf(initial) }
    val firstDayOfMonth = displayedMonth.atDay(1)
    val leadingSpaces = firstDayOfMonth.dayOfWeek.value - 1
    val monthDays = displayedMonth.lengthOfMonth()
    val cells = buildList<LocalDate?> {
        repeat(leadingSpaces) { add(null) }
        (1..monthDays).forEach { day -> add(displayedMonth.atDay(day)) }
        while (size % 7 != 0) add(null)
    }.chunked(7)

    BackHandler(onBack = onDismiss)
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(horizontal = ClearrDimens.dp16),
            contentAlignment = Alignment.Center
        ) {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(ClearrDimens.dp20), color = colors.surface) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = ClearrDimens.dp16, vertical = ClearrDimens.dp12)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.TextButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) { Text("‹", color = ClearrColors.Blue) }
                        Text(displayedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())), fontSize = ClearrTextSizes.sp16, color = colors.text)
                        androidx.compose.material3.TextButton(onClick = { displayedMonth = displayedMonth.plusMonths(1) }) { Text("›", color = ClearrColors.Blue) }
                    }
                    Spacer(Modifier.padding(vertical = ClearrDimens.dp4))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { label ->
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(label, color = colors.muted, fontSize = ClearrTextSizes.sp11)
                            }
                        }
                    }
                    Spacer(Modifier.padding(vertical = ClearrDimens.dp3))
                    cells.forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            week.forEach { date ->
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(ClearrDimens.dp2), contentAlignment = Alignment.Center) {
                                    if (date != null) {
                                        val selectable = !date.isBefore(minSelectableDate)
                                        val isSelected = date == selectedDate
                                        Surface(
                                            modifier = Modifier.fillMaxSize().clickable(enabled = selectable) { selectedDate = date },
                                            shape = RoundedCornerShape(ClearrDimens.dp10),
                                            color = when {
                                                isSelected -> ClearrColors.Blue
                                                selectable -> colors.card
                                                else -> colors.bg
                                            }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    date.dayOfMonth.toString(),
                                                    color = when {
                                                        isSelected -> ClearrColors.Surface
                                                        selectable -> colors.text
                                                        else -> colors.muted.copy(alpha = 0.6f)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.padding(vertical = ClearrDimens.dp6))
                    Button(
                        onClick = { onDateSelected(selectedDate) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ClearrDimens.dp14),
                        colors = ButtonDefaults.buttonColors(containerColor = ClearrColors.Blue)
                    ) {
                        Text("Use Date", color = ClearrColors.Surface)
                    }
                }
            }
        }
    }
}
