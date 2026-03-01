package com.mikeisesele.clearr.ui.feature.goals

import androidx.lifecycle.SavedStateHandle
import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.data.model.Goal
import com.mikeisesele.clearr.data.model.GoalCompletion
import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.data.model.GoalPeriodKey
import com.mikeisesele.clearr.data.model.GoalSummary
import com.mikeisesele.clearr.data.model.HistoryEntry
import com.mikeisesele.clearr.data.model.computeBestStreak
import com.mikeisesele.clearr.data.model.computeCurrentStreak
import com.mikeisesele.clearr.domain.repository.ClearrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repository: ClearrRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<GoalsUiState, GoalsAction, GoalsEvent>(
    initialState = GoalsUiState(trackerId = checkNotNull(savedStateHandle.get<Long>("trackerId")))
) {

    init {
        observeTracker()
        observeGoals()
    }

    override fun onAction(action: GoalsAction) {
        when (action) {
            is GoalsAction.MarkDone -> markDone(action.goalId)
            is GoalsAction.Delete -> deleteGoal(action.goalId)
            is GoalsAction.Rename -> renameGoal(action.goalId, action.title)
            is GoalsAction.AddGoal -> addGoal(
                title = action.title,
                emoji = action.emoji,
                colorToken = action.colorToken,
                target = action.target,
                frequency = action.frequency
            )
        }
    }

    private fun observeTracker() {
        launch {
            repository.getTrackerByIdFlow(currentState.trackerId).collectLatest { tracker ->
                if (tracker == null) {
                    updateState { it.copy(isLoading = false) }
                    return@collectLatest
                }
                updateState {
                    it.copy(
                        trackerName = tracker.name.ifBlank { "My Goals" }
                    )
                }
            }
        }
    }

    private fun observeGoals() {
        launch {
            combine(
                repository.getGoalsForTracker(currentState.trackerId),
                repository.getGoalCompletionsForTracker(currentState.trackerId)
            ) { goals, completions ->
                val summaries = buildGoalSummaries(goals, completions)
                val doneCount = summaries.count { it.isDoneThisPeriod }
                val aiInsight = ClearrEdgeAi.goalsInsightNanoAware(summaries)
                GoalsUiState(
                    trackerId = currentState.trackerId,
                    trackerName = currentState.trackerName,
                    summaries = summaries,
                    doneCount = doneCount,
                    totalCount = summaries.size,
                    allDoneThisPeriod = summaries.isNotEmpty() && doneCount == summaries.size,
                    aiInsight = aiInsight,
                    isLoading = false
                )
            }.collectLatest { next ->
                updateState { next }
            }
        }
    }

    private fun buildGoalSummaries(
        goals: List<Goal>,
        completions: List<GoalCompletion>
    ): List<GoalSummary> {
        val summaries = goals.map { goal ->
            val goalCompletions = completions.filter { it.goalId == goal.id }
            val completionByPeriod = goalCompletions
                .groupBy { it.periodKey }
                .mapValues { (_, list) -> list.maxOf { it.completedAt } }
            val completionKeys = completionByPeriod.keys
            val currentKey = GoalPeriodKey.currentKey(goal.frequency)
            val recentKeys = GoalPeriodKey.recentKeys(goal.frequency, count = 7)
            val completedAtCurrent = completionByPeriod[currentKey]

            GoalSummary(
                goal = goal,
                isDoneThisPeriod = completionKeys.contains(currentKey),
                currentStreak = computeCurrentStreak(completionKeys, goal.frequency),
                bestStreak = computeBestStreak(
                    completionKeys = completionKeys,
                    frequency = goal.frequency,
                    createdAtMs = goal.createdAt
                ),
                recentHistory = recentKeys.map { key ->
                    HistoryEntry(
                        periodKey = key,
                        label = GoalPeriodKey.label(key, goal.frequency),
                        isDone = completionKeys.contains(key)
                    )
                },
                completionRate = if (recentKeys.isEmpty()) 0f
                else recentKeys.count { completionKeys.contains(it) }.toFloat() / recentKeys.size,
                completedAtForCurrentPeriod = completedAtCurrent
            )
        }

        val pending = summaries
            .filterNot { it.isDoneThisPeriod }
            .sortedWith(compareByDescending<GoalSummary> { it.currentStreak }.thenBy { it.goal.title.lowercase() })
        val done = summaries
            .filter { it.isDoneThisPeriod }
            .sortedByDescending { it.completedAtForCurrentPeriod ?: 0L }
        return pending + done
    }

    private fun markDone(goalId: String) {
        launch {
            val goal = currentState.summaries.firstOrNull { it.goal.id == goalId }?.goal ?: return@launch
            repository.addGoalCompletion(
                GoalCompletion(
                    id = UUID.randomUUID().toString(),
                    goalId = goalId,
                    periodKey = GoalPeriodKey.currentKey(goal.frequency),
                    completedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun addGoal(
        title: String,
        emoji: String,
        colorToken: String,
        target: String?,
        frequency: GoalFrequency
    ) {
        launch {
            val ai = ClearrEdgeAi.inferGoalNanoAware(
                title = title,
                target = target,
                frequency = frequency,
                emoji = emoji,
                colorToken = colorToken
            )
            if (ai.normalizedTitle.isEmpty()) return@launch
            repository.insertGoal(
                Goal(
                    id = UUID.randomUUID().toString(),
                    trackerId = currentState.trackerId,
                    title = ai.normalizedTitle,
                    emoji = ai.suggestedEmoji,
                    colorToken = ai.suggestedColorToken,
                    target = ai.suggestedTarget?.ifBlank { null },
                    frequency = ai.suggestedFrequency,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun deleteGoal(goalId: String) {
        launch {
            repository.deleteGoal(goalId)
        }
    }

    private fun renameGoal(goalId: String, title: String) {
        launch {
            val normalizedTitle = ClearrEdgeAi.normalizeTitle(title)
            if (normalizedTitle.isBlank()) return@launch
            val existing = currentState.summaries.firstOrNull { it.goal.id == goalId }?.goal ?: return@launch
            repository.insertGoal(existing.copy(title = normalizedTitle))
        }
    }
}
