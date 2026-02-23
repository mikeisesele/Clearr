package com.mikeisesele.clearr.ui.feature.analytics

import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.domain.repository.DuesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: DuesRepository
) : BaseViewModel<AnalyticsUiState, AnalyticsAction, AnalyticsEvent>(
    initialState = AnalyticsUiState()
) {

    private val selectedYearFlow = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    init {
        launch {
            selectedYearFlow.flatMapLatest { year ->
                combine(
                    repository.getAllMembers(),
                    repository.getPaymentsForYear(year),
                    repository.getPaymentsForYear(year - 1),
                    repository.getYearConfigFlow(year),
                    repository.getYearConfigFlow(year - 1)
                ) { members, payments, prevPayments, config, prevConfig ->
                    AnalyticsUiState(
                        selectedYear = year,
                        members = members,
                        payments = payments,
                        prevYearPayments = prevPayments,
                        yearConfig = config,
                        prevYearConfig = prevConfig
                    )
                }
            }.collectLatest { newState ->
                updateState { newState }
            }
        }
    }

    override fun onAction(action: AnalyticsAction) {
        when (action) {
            is AnalyticsAction.SelectYear -> {
                selectedYearFlow.value = action.year
            }
        }
    }

}
