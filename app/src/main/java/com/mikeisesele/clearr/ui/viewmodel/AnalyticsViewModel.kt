package com.mikeisesele.clearr.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikeisesele.clearr.domain.repository.DuesRepository
import com.mikeisesele.clearr.ui.state.AnalyticsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: DuesRepository
) : ViewModel() {

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

    val uiState: StateFlow<AnalyticsUiState> = _selectedYear.flatMapLatest { year ->
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState())

    fun selectYear(year: Int) { _selectedYear.value = year }
}
