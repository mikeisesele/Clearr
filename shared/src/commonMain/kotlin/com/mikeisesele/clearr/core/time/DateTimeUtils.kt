package com.mikeisesele.clearr.core.time

import kotlin.random.Random
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private val systemTimeZone: TimeZone
    get() = TimeZone.currentSystemDefault()

fun nowEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()

fun todayLocalDate(): LocalDate = Clock.System.now().toLocalDateTime(systemTimeZone).date

fun epochMillisToLocalDate(epochMillis: Long): LocalDate =
    Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(systemTimeZone).date

fun localDateAtStartOfDayEpochMillis(date: LocalDate): Long =
    LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 0, 0, 0, 0)
        .toInstant(systemTimeZone)
        .toEpochMilliseconds()

fun localDateAtEndOfDayEpochMillis(date: LocalDate): Long =
    LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 23, 59, 59, 999_000_000)
        .toInstant(systemTimeZone)
        .toEpochMilliseconds()

fun randomId(): String = "${nowEpochMillis()}-${Random.nextLong().toString(16)}"

fun LocalDate.minusDays(days: Int): LocalDate = this - DatePeriod(days = days)

fun LocalDate.plusDays(days: Int): LocalDate = this + DatePeriod(days = days)

fun LocalDate.minusWeeks(weeks: Int): LocalDate = this - DatePeriod(days = weeks * 7)

fun LocalDate.plusWeeks(weeks: Int): LocalDate = this + DatePeriod(days = weeks * 7)

fun LocalDate.plusMonths(months: Int): LocalDate = this + DatePeriod(months = months)

fun dayOfWeekNumber(dayOfWeek: DayOfWeek): Int = when (dayOfWeek) {
    DayOfWeek.MONDAY -> 1
    DayOfWeek.TUESDAY -> 2
    DayOfWeek.WEDNESDAY -> 3
    DayOfWeek.THURSDAY -> 4
    DayOfWeek.FRIDAY -> 5
    DayOfWeek.SATURDAY -> 6
    DayOfWeek.SUNDAY -> 7
}

fun startOfIsoWeek(date: LocalDate): LocalDate = date.minusDays(dayOfWeekNumber(date.dayOfWeek) - 1)

fun isoWeekKey(date: LocalDate): String {
    val currentWeekStart = startOfIsoWeek(date)
    val thursday = currentWeekStart.plusDays(3)
    val weekYear = thursday.year
    val firstWeekStart = startOfIsoWeek(LocalDate(weekYear, 1, 4))
    val weekNumber = firstWeekStart.daysUntil(currentWeekStart) / 7 + 1
    return "$weekYear-W${weekNumber.toString().padStart(2, '0')}"
}

fun monthShortName(monthNumber: Int): String = when (monthNumber) {
    1 -> "Jan"
    2 -> "Feb"
    3 -> "Mar"
    4 -> "Apr"
    5 -> "May"
    6 -> "Jun"
    7 -> "Jul"
    8 -> "Aug"
    9 -> "Sep"
    10 -> "Oct"
    11 -> "Nov"
    else -> "Dec"
}

fun monthFullName(monthNumber: Int): String = when (monthNumber) {
    1 -> "January"
    2 -> "February"
    3 -> "March"
    4 -> "April"
    5 -> "May"
    6 -> "June"
    7 -> "July"
    8 -> "August"
    9 -> "September"
    10 -> "October"
    11 -> "November"
    else -> "December"
}

fun weekdayShortName(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
    DayOfWeek.MONDAY -> "Mon"
    DayOfWeek.TUESDAY -> "Tue"
    DayOfWeek.WEDNESDAY -> "Wed"
    DayOfWeek.THURSDAY -> "Thu"
    DayOfWeek.FRIDAY -> "Fri"
    DayOfWeek.SATURDAY -> "Sat"
    DayOfWeek.SUNDAY -> "Sun"
}

fun formatMonthYear(date: LocalDate): String = "${monthShortName(date.monthNumber)} ${date.year}"

fun formatFullMonthYear(date: LocalDate): String = "${monthFullName(date.monthNumber)} ${date.year}"

fun formatMonthDay(date: LocalDate): String = "${monthShortName(date.monthNumber)} ${date.dayOfMonth}"

fun formatWeekdayMonthDay(date: LocalDate): String =
    "${weekdayShortName(date.dayOfWeek)}, ${monthShortName(date.monthNumber)} ${date.dayOfMonth}"

fun daysInMonth(year: Int, monthNumber: Int): Int = when (monthNumber) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (isLeapYear(year)) 29 else 28
    else -> 30
}

fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

val MaxLocalDate: LocalDate = LocalDate(9999, 12, 31)
