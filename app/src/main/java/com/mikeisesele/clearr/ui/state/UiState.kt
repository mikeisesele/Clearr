package com.mikeisesele.clearr.ui.state

import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.data.model.PaymentRecord
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.data.model.YearConfig

data class HomeUiState(
    val selectedYear: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
    val members: List<Member> = emptyList(),
    val payments: List<PaymentRecord> = emptyList(),
    val yearConfig: YearConfig? = null,
    val showArchived: Boolean = false,
    val confettiMonth: Int? = null,
    val snackbarMessage: SnackbarData? = null,
    val layoutStyle: LayoutStyle = LayoutStyle.GRID,
    val trackerName: String = "Dues Tracker",
    val trackerType: TrackerType = TrackerType.DUES,
    val currentPeriodId: Long? = null
)

data class SnackbarData(
    val message: String,
    val undoPaymentId: Long? = null,
    val undoMemberId: Long? = null,
    val undoYear: Int? = null,
    val undoMonthIndex: Int? = null
)

data class AnalyticsUiState(
    val selectedYear: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
    val members: List<Member> = emptyList(),
    val payments: List<PaymentRecord> = emptyList(),
    val prevYearPayments: List<PaymentRecord> = emptyList(),
    val yearConfig: YearConfig? = null,
    val prevYearConfig: YearConfig? = null
)

data class RemindersUiState(
    val selectedYear: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
    val members: List<Member> = emptyList(),
    val payments: List<PaymentRecord> = emptyList(),
    val yearConfig: YearConfig? = null
)

data class SettingsUiState(
    val selectedYear: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
    val allMembers: List<Member> = emptyList(),
    val yearConfigs: List<YearConfig> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val layoutStyle: LayoutStyle = LayoutStyle.GRID,
    val currentTrackerType: TrackerType? = null,
    val currentTrackerDueAmount: Double? = null
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }
