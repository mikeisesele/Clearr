package com.mikeisesele.clearr.ui.feature.goals

import com.mikeisesele.clearr.core.time.randomId
import com.mikeisesele.clearr.data.model.Goal
import com.mikeisesele.clearr.data.model.GoalCompletion
import com.mikeisesele.clearr.data.model.GoalFrequency
import com.mikeisesele.clearr.data.model.GoalPeriodKey
import com.mikeisesele.clearr.data.model.GoalSummary
import com.mikeisesele.clearr.data.model.HistoryEntry
import com.mikeisesele.clearr.data.model.computeBestStreak
import com.mikeisesele.clearr.data.model.computeCurrentStreak
import com.mikeisesele.clearr.domain.repository.GoalsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GoalsStore(
    private val trackerId: Long,
    private val repository: GoalsRepository,
    private val goalsAiService: GoalsAiService,
    private val scope: CoroutineScope,
    private val nowMillis: () -> Long
) {
    private val mutableState = MutableStateFlow(GoalsUiState(trackerId = trackerId))
    val uiState: StateFlow<GoalsUiState> = mutableState.asStateFlow()

    private val eventChannel = Channel<GoalsEvent>(capacity = Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        observeTracker()
        observeGoals()
    }

    fun onAction(action: GoalsAction) {
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

    private val currentState: GoalsUiState
        get() = mutableState.value

    private fun updateState(transform: (GoalsUiState) -> GoalsUiState) {
        mutableState.update(transform)
    }

    private fun observeTracker() {
        scope.launch {
            repository.getTrackerByIdFlow(trackerId).collect { tracker ->
                if (tracker == null) {
                    updateState { it.copy(isLoading = false) }
                    return@collect
                }
                updateState { it.copy(trackerName = tracker.name.ifBlank { "My Goals" }) }
            }
        }
    }

    private fun observeGoals() {
        scope.launch {
            combine(
                repository.getGoalsForTracker(trackerId),
                repository.getGoalCompletionsForTracker(trackerId)
            ) { goals, completions ->
                val summaries = buildGoalSummaries(goals, completions)
                val doneCount = summaries.count { it.isDoneThisPeriod }
                GoalsUiState(
                    trackerId = trackerId,
                    trackerName = currentState.trackerName,
                    summaries = summaries,
                    doneCount = doneCount,
                    totalCount = summaries.size,
                    allDoneThisPeriod = summaries.isNotEmpty() && doneCount == summaries.size,
                    aiInsight = goalsAiService.goalsInsight(summaries),
                    isLoading = false
                )
            }.collect { next ->
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
        scope.launch {
            val goal = currentState.summaries.firstOrNull { it.goal.id == goalId }?.goal ?: return@launch
            repository.addGoalCompletion(
                GoalCompletion(
                    id = randomId(),
                    goalId = goalId,
                    periodKey = GoalPeriodKey.currentKey(goal.frequency),
                    completedAt = nowMillis()
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
        scope.launch {
            val ai = goalsAiService.inferGoal(
                title = title,
                target = target,
                frequency = frequency,
                emoji = emoji,
                colorToken = colorToken
            )
            if (ai.normalizedTitle.isEmpty()) return@launch
            repository.insertGoal(
                Goal(
                    id = randomId(),
                    trackerId = trackerId,
                    title = ai.normalizedTitle,
                    emoji = ai.suggestedEmoji,
                    colorToken = ai.suggestedColorToken,
                    target = ai.suggestedTarget?.ifBlank { null },
                    frequency = ai.suggestedFrequency,
                    createdAt = nowMillis()
                )
            )
        }
    }

    private fun deleteGoal(goalId: String) {
        scope.launch {
            repository.deleteGoal(goalId)
        }
    }

    private fun renameGoal(goalId: String, title: String) {
        scope.launch {
            val normalizedTitle = goalsAiService.normalizeTitle(title)
            if (normalizedTitle.isBlank()) return@launch
            val existing = currentState.summaries.firstOrNull { it.goal.id == goalId }?.goal ?: return@launch
            repository.insertGoal(existing.copy(title = normalizedTitle))
        }
    }
}
