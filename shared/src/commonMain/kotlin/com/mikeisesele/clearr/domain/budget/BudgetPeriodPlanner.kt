package com.mikeisesele.clearr.domain.budget

import com.mikeisesele.clearr.core.time.epochMillisToLocalDate
import com.mikeisesele.clearr.core.time.formatMonthYear
import com.mikeisesele.clearr.core.time.isoWeekKey
import com.mikeisesele.clearr.core.time.localDateAtEndOfDayEpochMillis
import com.mikeisesele.clearr.core.time.localDateAtStartOfDayEpochMillis
import com.mikeisesele.clearr.core.time.plusDays
import com.mikeisesele.clearr.core.time.plusMonths
import com.mikeisesele.clearr.core.time.plusWeeks
import com.mikeisesele.clearr.core.time.startOfIsoWeek
import com.mikeisesele.clearr.core.time.todayLocalDate
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetPeriod
import kotlinx.datetime.LocalDate

class BudgetPeriodPlanner {
    fun initialPeriods(
        trackerId: Long,
        frequency: BudgetFrequency,
        today: LocalDate = todayLocalDate()
    ): List<BudgetPeriod> = when (frequency) {
        BudgetFrequency.MONTHLY -> {
            val baseMonth = LocalDate(today.year, today.monthNumber, 1).plusMonths(-4)
            (0 until 5).map { buildMonthlyPeriod(trackerId, baseMonth.plusMonths(it)) }
        }
        BudgetFrequency.WEEKLY -> {
            val baseWeek = startOfIsoWeek(today).plusWeeks(-4)
            (0 until 5).map { buildWeeklyPeriod(trackerId, baseWeek.plusWeeks(it)) }
        }
    }

    fun missingPeriods(
        trackerId: Long,
        frequency: BudgetFrequency,
        latest: BudgetPeriod,
        today: LocalDate = todayLocalDate()
    ): List<BudgetPeriod> = when (frequency) {
        BudgetFrequency.MONTHLY -> missingMonthlyPeriods(trackerId, latest, today)
        BudgetFrequency.WEEKLY -> missingWeeklyPeriods(trackerId, latest, today)
    }

    private fun missingMonthlyPeriods(
        trackerId: Long,
        latest: BudgetPeriod,
        today: LocalDate
    ): List<BudgetPeriod> {
        val latestMonthStart = LocalDate(
            epochMillisToLocalDate(latest.startDate).year,
            epochMillisToLocalDate(latest.startDate).monthNumber,
            1
        )
        val currentMonthStart = LocalDate(today.year, today.monthNumber, 1)
        if (latestMonthStart >= currentMonthStart) return emptyList()

        val periods = mutableListOf<BudgetPeriod>()
        var nextMonth = latestMonthStart.plusMonths(1)
        while (nextMonth <= currentMonthStart) {
            periods += buildMonthlyPeriod(trackerId, nextMonth)
            nextMonth = nextMonth.plusMonths(1)
        }
        return periods
    }

    private fun missingWeeklyPeriods(
        trackerId: Long,
        latest: BudgetPeriod,
        today: LocalDate
    ): List<BudgetPeriod> {
        val latestWeekStart = startOfIsoWeek(epochMillisToLocalDate(latest.startDate))
        val currentWeekStart = startOfIsoWeek(today)
        if (latestWeekStart >= currentWeekStart) return emptyList()

        val periods = mutableListOf<BudgetPeriod>()
        var nextWeekStart = latestWeekStart.plusWeeks(1)
        while (nextWeekStart <= currentWeekStart) {
            periods += buildWeeklyPeriod(trackerId, nextWeekStart)
            nextWeekStart = nextWeekStart.plusWeeks(1)
        }
        return periods
    }

    private fun buildMonthlyPeriod(trackerId: Long, monthStart: LocalDate): BudgetPeriod {
        val monthEnd = LocalDate(
            monthStart.year,
            monthStart.monthNumber,
            com.mikeisesele.clearr.core.time.daysInMonth(monthStart.year, monthStart.monthNumber)
        )
        return BudgetPeriod(
            trackerId = trackerId,
            frequency = BudgetFrequency.MONTHLY,
            label = formatMonthYear(monthStart),
            startDate = localDateAtStartOfDayEpochMillis(monthStart),
            endDate = localDateAtEndOfDayEpochMillis(monthEnd)
        )
    }

    private fun buildWeeklyPeriod(trackerId: Long, weekStart: LocalDate): BudgetPeriod {
        val weekEnd = weekStart.plusDays(6)
        val weekLabel = "Week ${isoWeekKey(weekStart).substringAfter("-W")}"
        return BudgetPeriod(
            trackerId = trackerId,
            frequency = BudgetFrequency.WEEKLY,
            label = weekLabel,
            startDate = localDateAtStartOfDayEpochMillis(weekStart),
            endDate = localDateAtEndOfDayEpochMillis(weekEnd)
        )
    }
}
