package com.mikeisesele.clearr.ui.feature.goals

import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.data.model.GoalSummary

data class GoalsAiResult(
    val normalizedTitle: String,
    val suggestedTarget: String?,
    val suggestedFrequency: GoalFrequency,
    val suggestedEmoji: String,
    val suggestedColorToken: String
)

interface GoalsAiService {
    suspend fun inferGoal(
        title: String,
        target: String?,
        frequency: GoalFrequency,
        emoji: String,
        colorToken: String
    ): GoalsAiResult

    suspend fun goalsInsight(summaries: List<GoalSummary>): String?

    fun normalizeTitle(input: String): String
}
