package com.mikeisesele.clearr.ui.feature.goals.previews

import com.mikeisesele.clearr.data.model.Goal
import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.data.model.GoalSummary
import com.mikeisesele.clearr.data.model.HistoryEntry

internal val previewGoalSummary = GoalSummary(
    goal = Goal(
        id = "goal-1",
        trackerId = 1L,
        title = "Exercise",
        emoji = "🏃",
        colorToken = "Purple",
        target = "30 mins",
        frequency = GoalFrequency.DAILY,
        createdAt = 0L
    ),
    isDoneThisPeriod = false,
    currentStreak = 4,
    bestStreak = 12,
    recentHistory = listOf(
        HistoryEntry("1", "Wed", false),
        HistoryEntry("2", "Thu", true),
        HistoryEntry("3", "Fri", true),
        HistoryEntry("4", "Sat", false),
        HistoryEntry("5", "Sun", true),
        HistoryEntry("6", "Mon", true),
        HistoryEntry("7", "Tue", false)
    ),
    completionRate = 0.57f,
    completedAtForCurrentPeriod = null
)
