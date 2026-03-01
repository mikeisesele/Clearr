package com.mikeisesele.clearr.ui.feature.todo.utils

import androidx.compose.ui.graphics.Color
import com.mikeisesele.clearr.core.time.dayOfWeekNumber
import com.mikeisesele.clearr.core.time.formatWeekdayMonthDay
import com.mikeisesele.clearr.core.time.plusDays
import com.mikeisesele.clearr.core.time.plusWeeks
import com.mikeisesele.clearr.core.time.todayLocalDate
import com.mikeisesele.clearr.data.model.TodoItem
import com.mikeisesele.clearr.data.model.TodoStatus
import com.mikeisesele.clearr.ui.theme.ClearrColors
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

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
    todo.dueDate == todayLocalDate() -> ClearrColors.Orange
    else -> mutedColor
}

fun dueLabel(dueDate: LocalDate?): String {
    val today = todayLocalDate()
    return when (dueDate) {
        null -> "No due date"
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        else -> formatWeekdayMonthDay(dueDate)
    }
}

fun dueDateFromOption(option: String, customDate: LocalDate? = null): LocalDate? {
    val today = todayLocalDate()
    return when (option) {
        "Today" -> today
        "Tomorrow" -> today.plusDays(1)
        "This week" -> {
            val saturday = today.plusDays(dayOfWeekNumber(DayOfWeek.SATURDAY) - dayOfWeekNumber(today.dayOfWeek))
            if (saturday < today) saturday.plusWeeks(1) else saturday
        }
        "Next week" -> {
            val nextWeek = today.plusWeeks(1)
            nextWeek.plusDays(dayOfWeekNumber(DayOfWeek.MONDAY) - dayOfWeekNumber(nextWeek.dayOfWeek))
        }
        "Custom" -> customDate ?: today.plusDays(1)
        "No due date" -> null
        else -> today
    }
}
