package com.mikeisesele.clearr.data.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

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
    private val weekFields = WeekFields.ISO

    fun dailyKey(date: LocalDate = LocalDate.now()): String =
        date.format(DateTimeFormatter.ISO_LOCAL_DATE)

    fun weeklyKey(date: LocalDate = LocalDate.now()): String {
        val week = date.get(weekFields.weekOfWeekBasedYear())
        val weekYear = date.get(weekFields.weekBasedYear())
        return "$weekYear-W${week.toString().padStart(2, '0')}"
    }

    fun currentKey(frequency: GoalFrequency): String = when (frequency) {
        GoalFrequency.DAILY -> dailyKey()
        GoalFrequency.WEEKLY -> weeklyKey()
    }

    fun recentKeys(frequency: GoalFrequency, count: Int = 7): List<String> {
        val today = LocalDate.now()
        return when (frequency) {
            GoalFrequency.DAILY -> (count - 1 downTo 0).map { dailyKey(today.minusDays(it.toLong())) }
            GoalFrequency.WEEKLY -> (count - 1 downTo 0).map { weeklyKey(today.minusWeeks(it.toLong())) }
        }
    }

    fun label(key: String, frequency: GoalFrequency): String = when (frequency) {
        GoalFrequency.DAILY -> LocalDate.parse(key).dayOfWeek.name.take(3).lowercase()
            .replaceFirstChar { it.uppercase() }
        GoalFrequency.WEEKLY -> "W${key.substringAfter("-W").trimStart('0').ifBlank { "0" }}"
    }
}

fun computeCurrentStreak(
    completionKeys: Set<String>,
    frequency: GoalFrequency,
    today: LocalDate = LocalDate.now()
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
    today: LocalDate = LocalDate.now()
): Int {
    val created = java.time.Instant.ofEpochMilli(createdAtMs)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
    var best = 0
    var current = 0
    var cursor = created
    while (!cursor.isAfter(today)) {
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
