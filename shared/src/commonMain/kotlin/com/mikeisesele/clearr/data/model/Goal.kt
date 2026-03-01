package com.mikeisesele.clearr.data.model

import com.mikeisesele.clearr.core.time.epochMillisToLocalDate
import com.mikeisesele.clearr.core.time.isoWeekKey
import com.mikeisesele.clearr.core.time.minusDays
import com.mikeisesele.clearr.core.time.minusWeeks
import com.mikeisesele.clearr.core.time.plusDays
import com.mikeisesele.clearr.core.time.plusWeeks
import com.mikeisesele.clearr.core.time.todayLocalDate
import com.mikeisesele.clearr.core.time.weekdayShortName
import kotlinx.datetime.LocalDate

data class Goal(
    val id: String,
    val trackerId: Long,
    val title: String,
    val emoji: String,
    val colorToken: String,
    val target: String? = null,
    val frequency: GoalFrequency,
    val createdAt: Long
)

data class GoalCompletion(
    val id: String,
    val goalId: String,
    val periodKey: String,
    val completedAt: Long
)

enum class GoalFrequency { DAILY, WEEKLY }

data class HistoryEntry(
    val periodKey: String,
    val label: String,
    val isDone: Boolean
)

data class GoalSummary(
    val goal: Goal,
    val isDoneThisPeriod: Boolean,
    val currentStreak: Int,
    val bestStreak: Int,
    val recentHistory: List<HistoryEntry>,
    val completionRate: Float,
    val completedAtForCurrentPeriod: Long? = null
)

object GoalPeriodKey {
    fun dailyKey(date: LocalDate = todayLocalDate()): String = date.toString()

    fun weeklyKey(date: LocalDate = todayLocalDate()): String = isoWeekKey(date)

    fun currentKey(frequency: GoalFrequency): String = when (frequency) {
        GoalFrequency.DAILY -> dailyKey()
        GoalFrequency.WEEKLY -> weeklyKey()
    }

    fun recentKeys(frequency: GoalFrequency, count: Int = 7): List<String> {
        val today = todayLocalDate()
        return when (frequency) {
            GoalFrequency.DAILY -> (count - 1 downTo 0).map { dailyKey(today.minusDays(it)) }
            GoalFrequency.WEEKLY -> (count - 1 downTo 0).map { weeklyKey(today.minusWeeks(it)) }
        }
    }

    fun label(key: String, frequency: GoalFrequency): String = when (frequency) {
        GoalFrequency.DAILY -> weekdayShortName(LocalDate.parse(key).dayOfWeek).lowercase()
            .replaceFirstChar { it.uppercase() }
        GoalFrequency.WEEKLY -> "W${key.substringAfter("-W").trimStart('0').ifBlank { "0" }}"
    }
}

fun computeCurrentStreak(
    completionKeys: Set<String>,
    frequency: GoalFrequency,
    today: LocalDate = todayLocalDate()
): Int {
    var streak = 0
    var cursor = today
    val currentKey = GoalPeriodKey.currentKey(frequency)
    if (!completionKeys.contains(currentKey)) {
        cursor = when (frequency) {
            GoalFrequency.DAILY -> cursor.minusDays(1)
            GoalFrequency.WEEKLY -> cursor.minusWeeks(1)
        }
    }
    while (true) {
        val key = when (frequency) {
            GoalFrequency.DAILY -> GoalPeriodKey.dailyKey(cursor)
            GoalFrequency.WEEKLY -> GoalPeriodKey.weeklyKey(cursor)
        }
        if (!completionKeys.contains(key)) break
        streak++
        cursor = when (frequency) {
            GoalFrequency.DAILY -> cursor.minusDays(1)
            GoalFrequency.WEEKLY -> cursor.minusWeeks(1)
        }
    }
    return streak
}

fun computeBestStreak(
    completionKeys: Set<String>,
    frequency: GoalFrequency,
    createdAtMs: Long,
    today: LocalDate = todayLocalDate()
): Int {
    val created = epochMillisToLocalDate(createdAtMs)
    var best = 0
    var current = 0
    var cursor = created
    while (cursor <= today) {
        val key = when (frequency) {
            GoalFrequency.DAILY -> GoalPeriodKey.dailyKey(cursor)
            GoalFrequency.WEEKLY -> GoalPeriodKey.weeklyKey(cursor)
        }
        if (completionKeys.contains(key)) {
            current++
            if (current > best) best = current
        } else {
            current = 0
        }
        cursor = when (frequency) {
            GoalFrequency.DAILY -> cursor.plusDays(1)
            GoalFrequency.WEEKLY -> cursor.plusWeeks(1)
        }
    }
    return best
}
