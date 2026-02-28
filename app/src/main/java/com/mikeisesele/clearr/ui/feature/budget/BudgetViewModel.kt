package com.mikeisesele.clearr.ui.feature.budget

import androidx.lifecycle.SavedStateHandle
import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetCategoryPlan
import com.mikeisesele.clearr.data.model.BudgetEntry
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.BudgetStatus
import com.mikeisesele.clearr.data.model.BudgetSummary
import com.mikeisesele.clearr.data.model.CategorySummary
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.data.repository.BudgetPreferencesRepository
import com.mikeisesele.clearr.domain.repository.DuesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlin.math.roundToLong
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: DuesRepository,
    private val budgetPreferencesRepository: BudgetPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<BudgetUiState, BudgetAction, BudgetEvent>(
    initialState = BudgetUiState(trackerId = checkNotNull(savedStateHandle.get<Long>("trackerId")))
) {

    private val frequencyFlow = MutableStateFlow(BudgetFrequency.MONTHLY)
    private val selectedPeriodIdFlow = MutableStateFlow<Long?>(null)
    private var lastSetupPromptedPeriodId: Long? = null

    init {
        observeTracker()
        observeBudgetData()
        evaluateSwipeHint()
    }

    override fun onAction(action: BudgetAction) {
        when (action) {
            is BudgetAction.SetFrequency -> setFrequency(action.frequency)
            is BudgetAction.SelectPeriod -> selectPeriod(action.periodId)
            BudgetAction.OpenBudgetSetup -> openBudgetSetup(force = true)
            BudgetAction.DismissBudgetSetup -> dismissBudgetSetup()
            is BudgetAction.UpdateBudgetDraft -> updateBudgetDraft(action.categoryId, action.amountNaira)
            BudgetAction.ConfirmBudgetSetup -> confirmBudgetSetup()
            BudgetAction.OnSwipeHintDisplayed -> markSwipeHintDisplayed()
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
                selectedPeriodIdFlow.value = null
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
                        repository.getBudgetEntriesForTracker(currentState.trackerId),
                        repository.getBudgetCategoryPlansForTracker(currentState.trackerId),
                        selectedPeriodIdFlow
                    ) { periods, categories, entries, plans, selectedPeriodId ->
                        BudgetSlice(frequency, periods, categories, entries, plans, selectedPeriodId)
                    }
                }
                .collectLatest { slice ->
                    val selectedPeriod = resolveSelectedPeriod(
                        previousSelected = slice.selectedPeriodId,
                        periods = slice.periods
                    )
                    if (selectedPeriodIdFlow.value != selectedPeriod) {
                        selectedPeriodIdFlow.value = selectedPeriod
                    }
                    val planMap = slice.plans
                        .asSequence()
                        .filter { it.periodId == selectedPeriod }
                        .associateBy { it.categoryId }
                    val summaries = computeCategorySummaries(
                        categories = slice.categories,
                        entries = slice.entries,
                        periodId = selectedPeriod,
                        plansByCategoryId = planMap
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
                    maybeOpenBudgetSetup(
                        periodId = selectedPeriod,
                        periods = slice.periods,
                        categories = slice.categories,
                        plans = slice.plans,
                        force = false
                    )
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
        selectedPeriodIdFlow.value = null
        lastSetupPromptedPeriodId = null
        updateState { it.copy(isLoading = true, selectedPeriodId = null) }
    }

    private fun selectPeriod(periodId: Long) {
        selectedPeriodIdFlow.value = periodId
        updateState { it.copy(selectedPeriodId = periodId) }
        openBudgetSetup(force = true)
    }

    private fun openBudgetSetup(force: Boolean) {
        launch {
            maybeOpenBudgetSetup(
                periodId = currentState.selectedPeriodId,
                periods = currentState.periods,
                categories = currentState.categorySummaries.map { it.category },
                plans = repository.getBudgetCategoryPlansForTracker(currentState.trackerId).first(),
                force = force
            )
        }
    }

    private fun evaluateSwipeHint() {
        launch {
            val showHint = budgetPreferencesRepository.shouldShowSwipeHint()
            updateState { it.copy(showSwipeHint = showHint) }
        }
    }

    private fun markSwipeHintDisplayed() {
        launch {
            budgetPreferencesRepository.markSwipeHintShown()
            updateState { it.copy(showSwipeHint = false) }
        }
    }

    private fun dismissBudgetSetup() {
        lastSetupPromptedPeriodId = currentState.selectedPeriodId
        updateState {
            it.copy(
                showBudgetSetup = false,
                budgetSetupPeriodLabel = null,
                budgetSetupSourceLabel = null,
                budgetSetupDrafts = emptyList()
            )
        }
    }

    private fun updateBudgetDraft(categoryId: Long, amountNaira: Double) {
        val nextAmountKobo = nairaToKobo(amountNaira).coerceAtLeast(0L)
        updateState { state ->
            state.copy(
                budgetSetupDrafts = state.budgetSetupDrafts.map { draft ->
                    if (draft.categoryId == categoryId) {
                        draft.copy(plannedAmountKobo = nextAmountKobo)
                    } else {
                        draft
                    }
                }
            )
        }
    }

    private fun confirmBudgetSetup() {
        launch {
            val periodId = currentState.selectedPeriodId ?: return@launch
            val drafts = currentState.budgetSetupDrafts
            if (drafts.isEmpty()) {
                dismissBudgetSetup()
                return@launch
            }
            repository.saveBudgetCategoryPlans(
                periodId = periodId,
                plans = drafts.map { draft ->
                    BudgetCategoryPlan(
                        trackerId = currentState.trackerId,
                        categoryId = draft.categoryId,
                        periodId = periodId,
                        plannedAmountKobo = draft.plannedAmountKobo
                    )
                }
            )
            lastSetupPromptedPeriodId = periodId
            dismissBudgetSetup()
        }
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
            val categoryId = repository.addBudgetCategory(
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
            val periodId = currentState.selectedPeriodId
            if (periodId != null) {
                repository.saveBudgetCategoryPlans(
                    periodId = periodId,
                    plans = repository.getBudgetCategoryPlansForPeriod(periodId) + BudgetCategoryPlan(
                        trackerId = currentState.trackerId,
                        categoryId = categoryId,
                        periodId = periodId,
                        plannedAmountKobo = plannedKobo
                    )
                )
            }
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
        periodId: Long?,
        plansByCategoryId: Map<Long, BudgetCategoryPlan>
    ): List<CategorySummary> {
        val periodEntries = if (periodId == null) emptyList() else entries.filter { it.periodId == periodId }
        return categories.map { category ->
            val spent = periodEntries
                .asSequence()
                .filter { it.categoryId == category.id }
                .sumOf { it.amountKobo }
            val planned = plansByCategoryId[category.id]?.plannedAmountKobo ?: category.plannedAmountKobo
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
        }.sortedBy { it.category.name.lowercase() }
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

    private suspend fun maybeOpenBudgetSetup(
        periodId: Long?,
        periods: List<com.mikeisesele.clearr.data.model.BudgetPeriod>,
        categories: List<BudgetCategory>,
        plans: List<BudgetCategoryPlan>,
        force: Boolean
    ) {
        if (periodId == null || categories.isEmpty()) return
        val existingPlans = plans.filter { it.periodId == periodId }
        if (existingPlans.isNotEmpty() && !force) return
        if (!force && lastSetupPromptedPeriodId == periodId) return

        val selectedPeriod = periods.firstOrNull { it.id == periodId }
        val sourcePeriod = periods
            .filter { it.id != periodId && it.endDate <= (selectedPeriod?.endDate ?: Long.MAX_VALUE) }
            .sortedByDescending { it.endDate }
            .firstOrNull { candidate ->
                plans.any { it.periodId == candidate.id }
            }
        val sourcePlans = if (existingPlans.isNotEmpty()) {
            existingPlans.associateBy { it.categoryId }
        } else {
            sourcePeriod?.let { period ->
            plans.filter { it.periodId == period.id }.associateBy { it.categoryId }
            }.orEmpty()
        }

        val drafts = categories.sortedBy { it.name.lowercase() }.map { category ->
            BudgetPlanDraft(
                categoryId = category.id,
                icon = category.icon,
                name = category.name,
                colorToken = category.colorToken,
                plannedAmountKobo = sourcePlans[category.id]?.plannedAmountKobo ?: category.plannedAmountKobo
            )
        }

        if (drafts.isEmpty()) return

        updateState {
            it.copy(
                showBudgetSetup = true,
                budgetSetupPeriodLabel = selectedPeriod?.label,
                budgetSetupSourceLabel = if (existingPlans.isNotEmpty()) null else sourcePeriod?.label,
                budgetSetupDrafts = drafts
            )
        }
    }

    private data class BudgetSlice(
        val frequency: BudgetFrequency,
        val periods: List<com.mikeisesele.clearr.data.model.BudgetPeriod>,
        val categories: List<BudgetCategory>,
        val entries: List<BudgetEntry>,
        val plans: List<BudgetCategoryPlan>,
        val selectedPeriodId: Long?
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
            CategoryPreset("Utilities", "💡", "Violet", 0L)
        )

        val legacySeededAmountByName = mapOf(
            "Housing" to 150_000_00L,
            "Food" to 60_000_00L,
            "Transport" to 30_000_00L,
            "Savings" to 50_000_00L,
            "Entertainment" to 20_000_00L,
            "Health" to 15_000_00L,
            "Utilities" to 15_000_00L
        )
    }
}
