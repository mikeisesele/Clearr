package com.mikeisesele.clearr.runtime

import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.data.model.GoalSummary
import com.mikeisesele.clearr.ui.feature.goals.GoalsAiResult
import com.mikeisesele.clearr.ui.feature.goals.GoalsAiService

class AndroidGoalsAiService : GoalsAiService {
    override suspend fun inferGoal(
        title: String,
        target: String?,
        frequency: GoalFrequency,
        emoji: String,
        colorToken: String
    ): GoalsAiResult = GoalsAiResult(
        normalizedTitle = normalizeTitle(title),
        suggestedTarget = target?.trim()?.ifBlank { null },
        suggestedFrequency = frequency,
        suggestedEmoji = emoji,
        suggestedColorToken = colorToken
    )

    override suspend fun goalsInsight(summaries: List<GoalSummary>): String? =
        when {
            summaries.isEmpty() -> "No goals yet."
            summaries.any { !it.isDoneThisPeriod } -> "There are goals still pending this period."
            else -> "All goals are cleared for this period."
        }

    override fun normalizeTitle(input: String): String =
        input.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
