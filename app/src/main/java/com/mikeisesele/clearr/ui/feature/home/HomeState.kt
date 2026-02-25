package com.mikeisesele.clearr.ui.feature.home

import com.mikeisesele.clearr.core.base.contract.BaseState
import com.mikeisesele.clearr.core.base.contract.ViewEvent
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
    val currentPeriodId: Long? = null,
    val aiRiskHint: String? = null
) : BaseState

data class SnackbarData(
    val message: String,
    val undoPaymentId: Long? = null,
    val undoMemberId: Long? = null,
    val undoYear: Int? = null,
    val undoMonthIndex: Int? = null
)

sealed interface HomeAction {
    data class SetShowArchived(val show: Boolean) : HomeAction
    data class TogglePayment(
        val member: Member,
        val year: Int,
        val monthIndex: Int,
        val dueAmount: Double
    ) : HomeAction
    data class RecordPartialPayment(
        val memberId: Long,
        val year: Int,
        val monthIndex: Int,
        val amount: Double,
        val note: String?,
        val dueAmount: Double
    ) : HomeAction
    data object DismissConfetti : HomeAction
    data class UndoLastRemoval(
        val paymentId: Long,
        val memberId: Long,
        val year: Int,
        val monthIndex: Int,
        val dueAmount: Double
    ) : HomeAction
    data object DismissSnackbar : HomeAction
    data class AddMember(val name: String, val phone: String?) : HomeAction
    data class UpdateMember(val member: Member) : HomeAction
    data class SetMemberArchived(val id: Long, val archived: Boolean) : HomeAction
    data class DeleteMember(val id: Long, val trackerIdOverride: Long? = null) : HomeAction
    data class SetCurrentTrackerId(val trackerId: Long?) : HomeAction
    data class SetLayoutStyleForCurrentTracker(val style: LayoutStyle) : HomeAction
    data class MarkOutstandingMonthsPaid(
        val memberId: Long,
        val year: Int,
        val dueAmount: Double,
        val trackerIdOverride: Long? = null
    ) : HomeAction
}

sealed interface HomeEvent : ViewEvent
