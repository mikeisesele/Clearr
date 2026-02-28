package com.mikeisesele.clearr.ui.feature.setup

import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.core.ai.ClearrEdgeAi
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.BudgetCategory
import com.mikeisesele.clearr.data.model.BudgetFrequency
import com.mikeisesele.clearr.data.model.Frequency
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.data.model.TrackerPeriod
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.di.AppStateHolder
import com.mikeisesele.clearr.domain.repository.DuesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val repository: DuesRepository,
    private val appState: AppStateHolder
) : BaseViewModel<SetupWizardState, SetupAction, SetupEvent>(
    initialState = SetupWizardState()
) {

    init {
        launch {
            val hasTrackers = repository.getAllTrackers().first().isNotEmpty()
            if (hasTrackers) {
                updateState { it.copy(step = 1) }
            }
        }
    }

    override fun onAction(action: SetupAction) {
        when (action) {
            SetupAction.NextStep -> handleNextStep()
            SetupAction.PrevStep -> handlePrevStep()
            is SetupAction.SetGroupName -> handleSetGroupName(action.value)
            is SetupAction.SetTrackerName -> handleSetTrackerName(action.value)
            is SetupAction.SetAdminName -> handleSetAdminName(action.value)
            is SetupAction.SetAdminPhone -> handleSetAdminPhone(action.value)
            is SetupAction.SetTrackerType -> handleSetTrackerType(action.value)
            is SetupAction.SetFrequency -> handleSetFrequency(action.value)
            is SetupAction.SetDefaultAmount -> handleSetDefaultAmount(action.value)
            is SetupAction.SetLayoutStyle -> handleSetLayoutStyle(action.value)
            is SetupAction.SetLoadSampleMembers -> handleSetLoadSampleMembers(action.value)
            is SetupAction.GoToStep -> handleGoToStep(action.step)
            is SetupAction.FinishSetup -> finishSetupInternal(action.onDone)
            is SetupAction.LoadExistingConfig -> handleLoadExistingConfig(action.config)
        }
    }

    private fun handleNextStep() = updateState { s -> s.copy(step = (s.step + 1).coerceAtMost(5)) }

    private fun handlePrevStep() = updateState { s -> s.copy(step = (s.step - 1).coerceAtLeast(1)) }

    private fun handleSetGroupName(value: String) = updateState { it.copy(groupName = value) }
    private fun handleSetTrackerName(value: String) {
        updateState { it.copy(trackerName = value) }
        launch {
            val ai = ClearrEdgeAi.parseSetupIntentNanoAware(value)
            updateState { state ->
                state.copy(
                    trackerType = ai.trackerType ?: state.trackerType,
                    frequency = ai.suggestedFrequency ?: state.frequency,
                    defaultAmount = ai.suggestedDefaultAmount?.toInt()?.toString() ?: state.defaultAmount
                )
            }
        }
    }
    private fun handleSetAdminName(value: String) = updateState { it.copy(adminName = value) }
    private fun handleSetAdminPhone(value: String) = updateState { it.copy(adminPhone = value) }

    private fun handleSetTrackerType(value: TrackerType) = updateState { s ->
        val oldDefault = defaultTrackerName(s.trackerType)
        val shouldAutoRename = s.trackerName.isBlank() || s.trackerName == oldDefault
        s.copy(
            trackerType = value,
            trackerName = if (shouldAutoRename) defaultTrackerName(value) else s.trackerName
        )
    }

    private fun handleSetFrequency(value: Frequency) = updateState { it.copy(frequency = value) }
    private fun handleSetDefaultAmount(value: String) = updateState { it.copy(defaultAmount = value) }
    private fun handleSetLayoutStyle(value: LayoutStyle) = updateState { it.copy(layoutStyle = value) }
    private fun handleSetLoadSampleMembers(value: Boolean) = updateState { it.copy(loadSampleMembers = value) }
    private fun handleGoToStep(step: Int) = updateState { it.copy(step = step.coerceIn(1, 5)) }

    private fun handleLoadExistingConfig(config: AppConfig) = updateState {
        it.copy(
            groupName = config.groupName,
            trackerName = defaultTrackerName(config.trackerType),
            adminName = config.adminName,
            adminPhone = config.adminPhone,
            trackerType = config.trackerType,
            frequency = config.frequency,
            defaultAmount = config.defaultAmount.toInt().toString(),
            layoutStyle = config.layoutStyle,
            loadSampleMembers = true
        )
    }

    private fun finishSetupInternal(onDone: () -> Unit) {
        val s = currentState
        val amount = s.defaultAmount.toDoubleOrNull()?.takeIf { it > 0 } ?: 5000.0
        val now = System.currentTimeMillis()
        val config = AppConfig(
            id = 1,
            groupName = s.groupName.trim().ifBlank { "JSS Durumi Brothers" },
            adminName = s.adminName.trim(),
            adminPhone = s.adminPhone.trim(),
            trackerType = s.trackerType,
            frequency = s.frequency,
            defaultAmount = amount,
            layoutStyle = s.layoutStyle,
            remindersEnabled = false,
            setupComplete = true
        )
        updateState { it.copy(isSaving = true) }
        launch {
            val trackerId = repository.insertTracker(
                Tracker(
                    name = s.trackerName.trim().ifBlank { defaultTrackerName(s.trackerType) },
                    type = s.trackerType,
                    frequency = s.frequency,
                    layoutStyle = s.layoutStyle,
                    defaultAmount = amount,
                    isNew = true,
                    createdAt = now
                )
            )
            val periodId = repository.insertPeriod(buildCurrentPeriod(trackerId, s.frequency, now))
            repository.setCurrentPeriod(trackerId, periodId)

            if ((s.trackerType == TrackerType.DUES || s.trackerType == TrackerType.EXPENSES) && s.loadSampleMembers) {
                SAMPLE_MEMBERS.forEach { name ->
                    repository.insertTrackerMember(
                        TrackerMember(
                            trackerId = trackerId,
                            name = name,
                            createdAt = now
                        )
                    )
                }
            }

            repeat(6) {
                if (repository.getTrackerById(trackerId) != null) return@repeat
                delay(50)
            }
            if (s.trackerType == TrackerType.BUDGET) {
                seedBudgetTracker(trackerId)
            }
            repository.upsertAppConfig(config)
            appState.setAppConfig(config)
            updateState { it.copy(isSaving = false) }
            onDone()
        }
    }

    private suspend fun seedBudgetTracker(trackerId: Long) {
        listOf(BudgetFrequency.MONTHLY, BudgetFrequency.WEEKLY).forEach { frequency ->
            repository.ensureBudgetPeriods(trackerId, frequency)
            if (repository.getBudgetMaxSortOrder(trackerId, frequency) >= 0) return@forEach
            defaultBudgetCategories.forEachIndexed { index, preset ->
                repository.addBudgetCategory(
                    BudgetCategory(
                        trackerId = trackerId,
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
    }
    private fun defaultTrackerName(type: TrackerType): String = when (type) {
        TrackerType.DUES -> "Remittance"
        TrackerType.EXPENSES -> "Remittance"
        TrackerType.GOALS -> "Goals Tracker"
        TrackerType.TODO -> "To-do Tracker"
        TrackerType.BUDGET -> "Budget Tracker"
    }

    private companion object {
        data class BudgetCategoryPreset(
            val name: String,
            val icon: String,
            val colorToken: String,
            val plannedAmountKobo: Long
        )

        val defaultBudgetCategories = listOf(
            BudgetCategoryPreset("Housing", "🏠", "Violet", 150_000_00),
            BudgetCategoryPreset("Food", "🍔", "Orange", 60_000_00),
            BudgetCategoryPreset("Transport", "🚗", "Blue", 30_000_00),
            BudgetCategoryPreset("Savings", "💰", "Teal", 50_000_00),
            BudgetCategoryPreset("Entertainment", "🎬", "Purple", 20_000_00),
            BudgetCategoryPreset("Utilities", "💡", "Violet", 15_000_00)
        )

        val SAMPLE_MEMBERS = listOf(
            "Henry Nwazuru",
            "Chidubem",
            "Simon Boniface",
            "Ikechukwu Udeh",
            "Oluwatobi Majekodunmi",
            "Dare Oladunjoye",
            "Michael Isesele",
            "Faruk Umar"
        )
    }

    private fun currentPeriodLabel(frequency: Frequency): String {
        val cal = Calendar.getInstance()
        return when (frequency) {
            Frequency.MONTHLY -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
            Frequency.WEEKLY -> "Week ${cal.get(Calendar.WEEK_OF_YEAR)}, ${cal.get(Calendar.YEAR)}"
            Frequency.QUARTERLY -> "Q${(cal.get(Calendar.MONTH) / 3) + 1} ${cal.get(Calendar.YEAR)}"
            Frequency.TERMLY -> "Term ${(cal.get(Calendar.MONTH) / 4) + 1} ${cal.get(Calendar.YEAR)}"
            Frequency.BIANNUAL -> "H${if (cal.get(Calendar.MONTH) < 6) 1 else 2} ${cal.get(Calendar.YEAR)}"
            Frequency.ANNUAL -> "${cal.get(Calendar.YEAR)}"
            Frequency.CUSTOM -> "Current Period"
        }
    }

    private fun buildCurrentPeriod(trackerId: Long, frequency: Frequency, now: Long): TrackerPeriod {
        val cal = Calendar.getInstance()
        val (start, end) = when (frequency) {
            Frequency.MONTHLY -> {
                val s = cal.apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val e = cal.apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis
                s to e
            }
            Frequency.WEEKLY -> {
                val s = cal.apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val e = cal.apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis
                s to e
            }
            Frequency.QUARTERLY -> {
                val quarter = cal.get(Calendar.MONTH) / 3
                val startMonth = quarter * 3
                val s = cal.apply {
                    set(Calendar.MONTH, startMonth)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val e = cal.apply {
                    set(Calendar.MONTH, startMonth + 2)
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis
                s to e
            }
            else -> {
                val yearStart = cal.apply {
                    set(Calendar.MONTH, 0)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val yearEnd = cal.apply {
                    set(Calendar.MONTH, 11)
                    set(Calendar.DAY_OF_MONTH, 31)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis
                yearStart to yearEnd
            }
        }

        return TrackerPeriod(
            trackerId = trackerId,
            label = currentPeriodLabel(frequency),
            startDate = start,
            endDate = end,
            isCurrent = true,
            createdAt = now
        )
    }
}
