package com.mikeisesele.clearr.ui.feature.budget

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikeisesele.clearr.domain.repository.BudgetPreferencesRepository
import com.mikeisesele.clearr.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class BudgetViewModel @Inject constructor(
    repository: BudgetRepository,
    budgetPreferencesRepository: BudgetPreferencesRepository,
    budgetAiService: BudgetAiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val store = BudgetStore(
        trackerId = checkNotNull(savedStateHandle.get<Long>("trackerId")),
        repository = repository,
        budgetPreferencesRepository = budgetPreferencesRepository,
        budgetAiService = budgetAiService,
        scope = viewModelScope,
        nowMillis = System::currentTimeMillis
    )

    val uiState: StateFlow<BudgetUiState> = store.uiState
    val events = store.events

    fun onAction(action: BudgetAction) {
        store.onAction(action)
    }
}
