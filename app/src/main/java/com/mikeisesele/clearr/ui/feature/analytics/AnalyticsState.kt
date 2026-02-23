package com.mikeisesele.clearr.ui.feature.analytics

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.data.model.PaymentRecord
import com.mikeisesele.clearr.data.model.YearConfig

data class AnalyticsUiState(
    val selectedYear: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
    val members: List<Member> = emptyList(),
    val payments: List<PaymentRecord> = emptyList(),
    val prevYearPayments: List<PaymentRecord> = emptyList(),
    val yearConfig: YearConfig? = null,
    val prevYearConfig: YearConfig? = null
) : BaseState

sealed interface AnalyticsAction {
    data class SelectYear(val year: Int) : AnalyticsAction
}

sealed interface AnalyticsEvent : ViewEvent
