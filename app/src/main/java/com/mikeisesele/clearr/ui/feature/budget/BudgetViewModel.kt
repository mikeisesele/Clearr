package com.mikeisesele.clearr.ui.feature.budget

import androidx.lifecycle.SavedStateHandle
import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetEntry
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetStatus
import com.mikeisesele.clearr.data.model.BudgetSummary
import com.mikeisesele.clearr.data.model.CategorySummary
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.domain.repository.DuesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlin.math.roundToLong
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: DuesRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<BudgetUiState, BudgetAction, BudgetEvent>(
    initialState = BudgetUiState(trackerId = checkNotNull(savedStateHandle.get<Long>("trackerId")))
) {

    private val frequencyFlow = MutableStateFlow(BudgetFrequency.MONTHLY)

    init {
        observeTracker()
        observeBudgetData()
    }

    override fun onAction(action: BudgetAction) {
        when (action) {
            is BudgetAction.SetFrequency -> setFrequency(action.frequency)
            is BudgetAction.SelectPeriod -> updateState { it.copy(selectedPeriodId = action.periodId) }
            is BudgetAction.LogExpense -> logExpense(action.categoryId, action.amountNaira, action.note)
            is BudgetAction.DeleteCategory -> deleteCategory(action.categoryId)
            is BudgetAction.AddCategory -> addCategory(
                name = action.name,
                icon = action.icon,
                colorToken = action.colorToken,
                plannedAmountNaira = action.plannedAmountNaira
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
                val initialFrequency = when (tracker.frequency) {
                    Frequency.WEEKLY -> BudgetFrequency.WEEKLY
                    else -> BudgetFrequency.MONTHLY
                }
                frequencyFlow.value = initialFrequency
                updateState {
                    it.copy(
                        trackerName = if (tracker.type == TrackerType.BUDGET) tracker.name else "Budget Tracker"
                    )
                }
            }
        }
    }

    private fun observeBudgetData() {
        launch {
            frequencyFlow
                .flatMapLatest { frequency ->
                    ensureBudgetFrequencySeeded(frequency)
                    combine(
                        repository.getBudgetPeriods(currentState.trackerId, frequency),
                        repository.getBudgetCategories(currentState.trackerId, frequency),
                        repository.getBudgetEntriesForTracker(currentState.trackerId)
                    ) { periods, categories, entries ->
                        BudgetSlice(frequency, periods, categories, entries)
                    }
                }
                .collectLatest { slice ->
                    val selectedPeriod = resolveSelectedPeriod(
                        previousSelected = currentState.selectedPeriodId,
                        periods = slice.periods
                    )
                    val summaries = computeCategorySummaries(
                        categories = slice.categories,
                        entries = slice.entries,
                        periodId = selectedPeriod
                    )
                    val aiInsight = ClearrEdgeAi.budgetInsightNanoAware(summaries)
                    updateState {
                        it.copy(
                            frequency = slice.frequency,
                            periods = slice.periods,
                            selectedPeriodId = selectedPeriod,
                            categorySummaries = summaries,
                            aiInsight = aiInsight,
                            budgetSummary = computeBudgetSummary(summaries),
                            isLoading = false
                        )
                    }
                }
        }
    }

    private suspend fun ensureBudgetFrequencySeeded(frequency: BudgetFrequency) {
        repository.ensureBudgetPeriods(currentState.trackerId, frequency)
        val existingCategories = repository.getBudgetCategories(currentState.trackerId, frequency).first()
        if (existingCategories.isNotEmpty()) {
            normalizeLegacySeededBudgetAmounts(existingCategories)
            return
        }

        defaultCategoryPresets.forEachIndexed { index, preset ->
            repository.addBudgetCategory(
                BudgetCategory(
                    trackerId = currentState.trackerId,
                    frequency = frequency,
                    name = preset.name,
                    icon = preset.icon,
                    colorToken = preset.colorToken,
                    plannedAmountKobo = preset.plannedAmountKobo,
                    sortOrder = index
                )
            )
        }
    }

    /**
     * One-time cleanup for previously seeded fake amounts so default categories start at 0.
     * Only legacy preset amounts are normalized; user-entered values are preserved.
     */
    private suspend fun normalizeLegacySeededBudgetAmounts(existingCategories: List<BudgetCategory>) {
        existingCategories.forEach { category ->
            val legacyAmount = legacySeededAmountByName[category.name] ?: return@forEach
            if (category.plannedAmountKobo == legacyAmount) {
                repository.updateBudgetCategory(category.copy(plannedAmountKobo = 0L))
            }
        }
    }

    private fun resolveSelectedPeriod(previousSelected: Long?, periods: List<com.mikeisesele.clearr.data.model.BudgetPeriod>): Long? {
        if (periods.isEmpty()) return null
        val previousStillExists = previousSelected != null && periods.any { it.id == previousSelected }
        return if (previousStillExists) previousSelected else periods.last().id
    }

    private fun setFrequency(frequency: BudgetFrequency) {
        frequencyFlow.value = frequency
        updateState { it.copy(isLoading = true, selectedPeriodId = null) }
    }

    private fun logExpense(categoryId: Long, amountNaira: Double, note: String?) {
        launch {
            val periodId = currentState.selectedPeriodId ?: return@launch
            val kobo = nairaToKobo(amountNaira)
            if (kobo <= 0L) return@launch
            val suggestedCategoryId = ClearrEdgeAi.inferBudgetCategoryIdNanoAware(
                note = note,
                categories = currentState.categorySummaries.map { it.category }
            )
            val resolvedCategoryId = suggestedCategoryId ?: categoryId
            repository.addBudgetEntry(
                BudgetEntry(
                    trackerId = currentState.trackerId,
                    categoryId = resolvedCategoryId,
                    periodId = periodId,
                    amountKobo = kobo,
                    note = note?.trim()?.ifBlank { null },
                    loggedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun addCategory(name: String, icon: String, colorToken: String, plannedAmountNaira: Double) {
        launch {
            val plannedKobo = nairaToKobo(plannedAmountNaira)
            if (name.isBlank() || plannedKobo < 0L) return@launch
            val sortOrder = repository.getBudgetMaxSortOrder(currentState.trackerId, currentState.frequency) + 1
            repository.addBudgetCategory(
                BudgetCategory(
                    trackerId = currentState.trackerId,
                    frequency = currentState.frequency,
                    name = name.trim(),
                    icon = icon,
                    colorToken = colorToken,
                    plannedAmountKobo = plannedKobo,
                    sortOrder = sortOrder,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun deleteCategory(categoryId: Long) {
        launch {
            repository.deleteBudgetCategory(categoryId)
        }
    }

    private fun computeCategorySummaries(
        categories: List<BudgetCategory>,
        entries: List<BudgetEntry>,
        periodId: Long?
    ): List<CategorySummary> {
        val periodEntries = if (periodId == null) emptyList() else entries.filter { it.periodId == periodId }
        return categories.map { category ->
            val spent = periodEntries
                .asSequence()
                .filter { it.categoryId == category.id }
                .sumOf { it.amountKobo }
            val planned = category.plannedAmountKobo
            val percentUsed = if (planned > 0L) spent.toFloat() / planned else 0f
            CategorySummary(
                category = category,
                plannedAmountKobo = planned,
                spentAmountKobo = spent,
                remainingAmountKobo = planned - spent,
                percentUsed = percentUsed,
                status = when {
                    percentUsed > 1f -> BudgetStatus.OVER_BUDGET
                    percentUsed == 1f -> BudgetStatus.CLEARED
                    percentUsed >= 0.9f -> BudgetStatus.NEAR_LIMIT
                    else -> BudgetStatus.ON_TRACK
                }
            )
        }.sortedBy { it.category.sortOrder }
    }

    private fun computeBudgetSummary(summaries: List<CategorySummary>): BudgetSummary {
        val totalPlanned = summaries.sumOf { it.plannedAmountKobo }
        val totalSpent = summaries.sumOf { it.spentAmountKobo }
        return BudgetSummary(
            totalPlannedKobo = totalPlanned,
            totalSpentKobo = totalSpent,
            totalRemainingKobo = totalPlanned - totalSpent,
            percentUsed = if (totalPlanned > 0L) totalSpent.toFloat() / totalPlanned else 0f,
            isOverBudget = totalSpent > totalPlanned,
            overBudgetCategories = summaries.filter { it.status == BudgetStatus.OVER_BUDGET }
        )
    }

    private fun nairaToKobo(naira: Double): Long = (naira * 100.0).roundToLong()

    private data class BudgetSlice(
        val frequency: BudgetFrequency,
        val periods: List<com.mikeisesele.clearr.data.model.BudgetPeriod>,
        val categories: List<BudgetCategory>,
        val entries: List<BudgetEntry>
    )

    private data class CategoryPreset(
        val name: String,
        val icon: String,
        val colorToken: String,
        val plannedAmountKobo: Long
    )

    private companion object {
        val defaultCategoryPresets = listOf(
            CategoryPreset("Housing", "🏠", "Violet", 0L),
            CategoryPreset("Food", "🍔", "Orange", 0L),
            CategoryPreset("Transport", "🚗", "Blue", 0L),
            CategoryPreset("Savings", "💰", "Teal", 0L),
            CategoryPreset("Entertainment", "🎬", "Purple", 0L),
            CategoryPreset("Health", "💊", "Teal", 0L)
        )

        val legacySeededAmountByName = mapOf(
            "Housing" to 150_000_00L,
            "Food" to 60_000_00L,
            "Transport" to 30_000_00L,
            "Savings" to 50_000_00L,
            "Entertainment" to 20_000_00L,
            "Health" to 15_000_00L
        )
    }
}
