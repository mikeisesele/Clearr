package com.mikeisesele.clearr.ui.feature.todo.utils

import androidx.compose.ui.graphics.Color
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoStatus
import com.mikeisesele.clearr.ui.theme.ClearrColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun priorityDotColor(todo: TodoItem, derived: TodoStatus): Color {
    if (derived == TodoStatus.DONE) return ClearrColors.Emerald
    return when (todo.priority) {
        com.mikeisesele.clearr.data.model.TodoPriority.HIGH -> ClearrColors.Coral
        com.mikeisesele.clearr.data.model.TodoPriority.MEDIUM -> ClearrColors.Orange
        com.mikeisesele.clearr.data.model.TodoPriority.LOW -> ClearrColors.Blue
    }
}

fun dueLabelColor(todo: TodoItem, derived: TodoStatus, mutedColor: Color): Color = when {
    derived == TodoStatus.DONE -> mutedColor
    derived == TodoStatus.OVERDUE -> ClearrColors.Coral
    todo.dueDate == LocalDate.now() -> ClearrColors.Orange
    else -> mutedColor
}

fun dueLabel(dueDate: LocalDate?): String {
    val today = LocalDate.now()
    return when (dueDate) {
        null -> "No due date"
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        else -> dueDate.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))
    }
}

fun dueDateFromOption(option: String, customDate: LocalDate? = null): LocalDate? {
    val today = LocalDate.now()
    return when (option) {
        "Today" -> today
        "Tomorrow" -> today.plusDays(1)
        "This week" -> {
            val saturday = today.with(DayOfWeek.SATURDAY)
            if (saturday.isBefore(today)) saturday.plusWeeks(1) else saturday
        }
        "Next week" -> today.plusWeeks(1).with(DayOfWeek.MONDAY)
        "Custom" -> customDate ?: today.plusDays(1)
        "No due date" -> null
        else -> today
    }
}
