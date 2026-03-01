package com.mikeisesele.clearr.ui.feature.goals

import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.data.model.GoalSummary
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidGoalsAiService @Inject constructor() : GoalsAiService {
    override suspend fun inferGoal(
        title: String,
        target: String?,
        frequency: GoalFrequency,
        emoji: String,
        colorToken: String
    ): GoalsAiResult {
        val result = ClearrEdgeAi.inferGoalNanoAware(
            title = title,
            target = target,
            frequency = frequency,
            emoji = emoji,
            colorToken = colorToken
        )
        return GoalsAiResult(
            normalizedTitle = result.normalizedTitle,
            suggestedTarget = result.suggestedTarget,
            suggestedFrequency = result.suggestedFrequency,
            suggestedEmoji = result.suggestedEmoji,
            suggestedColorToken = result.suggestedColorToken
        )
    }

    override suspend fun goalsInsight(summaries: List<GoalSummary>): String? =
        ClearrEdgeAi.goalsInsightNanoAware(summaries)

    override fun normalizeTitle(input: String): String = ClearrEdgeAi.normalizeTitle(input)
}
