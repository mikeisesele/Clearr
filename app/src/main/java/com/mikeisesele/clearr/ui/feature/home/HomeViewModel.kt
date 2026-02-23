package com.mikeisesele.clearr.ui.feature.home

import com.mikeisesele.clearr.core.base.BaseViewModel
import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.data.model.LayoutStyle
import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.data.model.PaymentRecord
import com.mikeisesele.clearr.data.model.RecordStatus
import com.mikeisesele.clearr.data.model.Tracker
import com.mikeisesele.clearr.data.model.TrackerMember
import com.mikeisesele.clearr.data.model.TrackerPeriod
import com.mikeisesele.clearr.data.model.TrackerRecord
import com.mikeisesele.clearr.data.model.TrackerType
import com.mikeisesele.clearr.data.model.YearConfig
import com.mikeisesele.clearr.di.AppStateHolder
import com.mikeisesele.clearr.domain.repository.DuesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DuesRepository,
    private val appState: AppStateHolder
) : BaseViewModel<HomeUiState, HomeAction, HomeEvent>(
    initialState = HomeUiState()
) {

    private val showArchivedFlow = MutableStateFlow(false)
    private val confettiMonthFlow = MutableStateFlow<Int?>(null)
    private val snackbarFlow = MutableStateFlow<SnackbarData?>(null)

    private data class UiParams(
        val year: Int,
        val showArchived: Boolean,
        val confettiMonth: Int?,
        val snackbar: SnackbarData?,
        val appConfig: AppConfig?,
        val trackerId: Long?
    )

    init {
        launch {
            combine(
                appState.selectedYear,
                showArchivedFlow,
                confettiMonthFlow,
                snackbarFlow,
                appState.appConfig,
                appState.currentTrackerId
            ) { arr ->
                @Suppress("UNCHECKED_CAST")
                UiParams(
                    year = arr[0] as Int,
                    showArchived = arr[1] as Boolean,
                    confettiMonth = arr[2] as? Int,
                    snackbar = arr[3] as? SnackbarData,
                    appConfig = arr[4] as AppConfig?,
                    trackerId = arr[5] as Long?
                )
            }.flatMapLatest { p ->
                val trackerId = p.trackerId
                if (trackerId != null) {
                    val trackerFlow = repository.getTrackerByIdFlow(trackerId)
                    val membersFlow = repository.getAllMembersForTracker(trackerId)
                    val periodsFlow = repository.getPeriodsForTracker(trackerId)
                    val recordsFlow = repository.getRecordsForTracker(trackerId)

                    combine(trackerFlow, membersFlow, periodsFlow, recordsFlow) { tracker, members, periods, records ->
                        val mappedMembers = members.map { it.asUiMember() }
                        val periodById = periods.associateBy { it.id }
                        val mappedPayments = records.mapNotNull { rec ->
                            val period = periodById[rec.periodId] ?: return@mapNotNull null
                            val monthIndex = monthIndexFromLabel(period.label, p.year) ?: return@mapNotNull null
                            PaymentRecord(
                                memberId = rec.memberId,
                                year = p.year,
                                monthIndex = monthIndex,
                                amountPaid = rec.amountPaid,
                                expectedAmount = tracker?.defaultAmount ?: 5000.0,
                                paidAt = rec.updatedAt,
                                note = rec.note
                            )
                        }
                        val syntheticYearConfig = YearConfig(
                            year = p.year,
                            dueAmountPerMonth = tracker?.defaultAmount ?: 5000.0
                        )
                        HomeUiState(
                            selectedYear = p.year,
                            members = mappedMembers,
                            payments = mappedPayments,
                            yearConfig = syntheticYearConfig,
                            showArchived = p.showArchived,
                            confettiMonth = p.confettiMonth,
                            snackbarMessage = p.snackbar,
                            layoutStyle = tracker?.layoutStyle ?: p.appConfig?.layoutStyle ?: LayoutStyle.GRID,
                            trackerName = tracker?.name ?: "Tracker",
                            trackerType = tracker?.type ?: TrackerType.DUES,
                            currentPeriodId = periods.firstOrNull { it.isCurrent }?.id
                        )
                    }
                } else {
                    combine(
                        repository.getAllMembers(),
                        repository.getPaymentsForYear(p.year),
                        repository.getYearConfigFlow(p.year)
                    ) { members, payments, config ->
                        HomeUiState(
                            selectedYear = p.year,
                            members = members,
                            payments = payments,
                            yearConfig = config,
                            showArchived = p.showArchived,
                            confettiMonth = p.confettiMonth,
                            snackbarMessage = p.snackbar,
                            layoutStyle = p.appConfig?.layoutStyle ?: LayoutStyle.GRID,
                            trackerName = "Dues Tracker",
                            trackerType = TrackerType.DUES
                        )
                    }
                }
            }.collectLatest { newState -> updateState { newState } }
        }

        launch {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            repository.ensureYearConfig(currentYear)
        }
    }

    override fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.SetShowArchived -> setShowArchived(action.show)
            is HomeAction.TogglePayment -> togglePayment(action.member, action.year, action.monthIndex, action.dueAmount)
            is HomeAction.RecordPartialPayment -> recordPartialPayment(action.memberId, action.year, action.monthIndex, action.amount, action.note, action.dueAmount)
            HomeAction.DismissConfetti -> dismissConfetti()
            is HomeAction.UndoLastRemoval -> undoLastRemoval(action.paymentId, action.memberId, action.year, action.monthIndex, action.dueAmount)
            HomeAction.DismissSnackbar -> dismissSnackbar()
            is HomeAction.AddMember -> addMember(action.name, action.phone)
            is HomeAction.UpdateMember -> updateMember(action.member)
            is HomeAction.SetMemberArchived -> setMemberArchived(action.id, action.archived)
            is HomeAction.DeleteMember -> deleteMember(action.id, action.trackerIdOverride)
            is HomeAction.SetCurrentTrackerId -> setCurrentTrackerId(action.trackerId)
            is HomeAction.SetLayoutStyleForCurrentTracker -> setLayoutStyleForCurrentTracker(action.style)
            is HomeAction.MarkOutstandingMonthsPaid -> markOutstandingMonthsPaid(action.memberId, action.year, action.dueAmount, action.trackerIdOverride)
        }
    }

    private fun setShowArchived(show: Boolean) { showArchivedFlow.value = show }

    private fun togglePayment(member: Member, year: Int, monthIndex: Int, dueAmount: Double) {
        launch {
            val trackerId = appState.currentTrackerId.value
            if (trackerId == null) {
                val totalPaid = repository.getTotalPaidForMonth(member.id, year, monthIndex)
                if (totalPaid >= dueAmount) {
                    val latest = repository.getLatestPayment(member.id, year, monthIndex)
                    if (latest != null) {
                        repository.undoPayment(latest.id)
                        snackbarFlow.value = SnackbarData(
                            message = "Payment removed for ${member.name}",
                            undoPaymentId = latest.id,
                            undoMemberId = member.id,
                            undoYear = year,
                            undoMonthIndex = monthIndex
                        )
                    }
                } else {
                    repository.insertPayment(
                        PaymentRecord(
                            memberId = member.id,
                            year = year,
                            monthIndex = monthIndex,
                            amountPaid = dueAmount - totalPaid,
                            expectedAmount = dueAmount,
                            paidAt = System.currentTimeMillis()
                        )
                    )
                    checkAndTriggerConfetti(year, monthIndex, dueAmount)
                }
                return@launch
            }

            val tracker = repository.getTrackerById(trackerId) ?: return@launch
            val period = if (tracker.type == TrackerType.DUES) {
                ensureMonthlyPeriod(trackerId, year, monthIndex)
            } else {
                repository.getCurrentPeriod(trackerId) ?: return@launch
            }
            val existing = repository.getRecord(trackerId, period.id, member.id)
            val now = System.currentTimeMillis()
            val updated = when (tracker.type) {
                TrackerType.DUES -> {
                    val currentlyCompleted = existing?.status == RecordStatus.PAID
                    val newStatus = if (currentlyCompleted) RecordStatus.UNPAID else RecordStatus.PAID
                    val newAmount = if (newStatus == RecordStatus.PAID) tracker.defaultAmount else 0.0
                    (existing ?: TrackerRecord(
                        trackerId = trackerId,
                        periodId = period.id,
                        memberId = member.id
                    )).copy(status = newStatus, amountPaid = newAmount, updatedAt = now)
                }
                TrackerType.EXPENSES -> {
                    val next = if (existing?.status == RecordStatus.PRESENT) RecordStatus.ABSENT else RecordStatus.PRESENT
                    (existing ?: TrackerRecord(
                        trackerId = trackerId,
                        periodId = period.id,
                        memberId = member.id
                    )).copy(status = next, amountPaid = 0.0, updatedAt = now)
                }
                TrackerType.GOALS, TrackerType.TODO -> {
                    val next = if (existing?.status == RecordStatus.DONE) RecordStatus.PENDING else RecordStatus.DONE
                    (existing ?: TrackerRecord(
                        trackerId = trackerId,
                        periodId = period.id,
                        memberId = member.id
                    )).copy(status = next, amountPaid = 0.0, updatedAt = now)
                }
                TrackerType.BUDGET -> {
                    val next = if (existing?.status == RecordStatus.PAID) RecordStatus.UNPAID else RecordStatus.PAID
                    (existing ?: TrackerRecord(
                        trackerId = trackerId,
                        periodId = period.id,
                        memberId = member.id
                    )).copy(status = next, amountPaid = 0.0, updatedAt = now)
                }
            }
            if (existing == null) repository.insertRecord(updated) else repository.updateRecord(updated)
            checkAndTriggerTrackerConfetti(trackerId, period.id, tracker.type)
        }
    }

    private fun recordPartialPayment(memberId: Long, year: Int, monthIndex: Int, amount: Double, note: String?, dueAmount: Double) {
        launch {
            val trackerId = appState.currentTrackerId.value
            if (trackerId == null) {
                repository.insertPayment(
                    PaymentRecord(
                        memberId = memberId,
                        year = year,
                        monthIndex = monthIndex,
                        amountPaid = amount,
                        expectedAmount = dueAmount,
                        paidAt = System.currentTimeMillis(),
                        note = note
                    )
                )
                checkAndTriggerConfetti(year, monthIndex, dueAmount)
                return@launch
            }
            val tracker = repository.getTrackerById(trackerId) ?: return@launch
            val period = if (tracker.type == TrackerType.DUES) {
                ensureMonthlyPeriod(trackerId, year, monthIndex)
            } else {
                repository.getCurrentPeriod(trackerId) ?: return@launch
            }
            val existing = repository.getRecord(trackerId, period.id, memberId)
            val status = if (amount >= tracker.defaultAmount) RecordStatus.PAID else RecordStatus.PARTIAL
            val updated = (existing ?: TrackerRecord(
                trackerId = trackerId,
                periodId = period.id,
                memberId = memberId
            )).copy(
                status = status,
                amountPaid = amount,
                note = note,
                updatedAt = System.currentTimeMillis()
            )
            if (existing == null) repository.insertRecord(updated) else repository.updateRecord(updated)
            checkAndTriggerTrackerConfetti(trackerId, period.id, tracker.type)
        }
    }

    private suspend fun checkAndTriggerConfetti(year: Int, monthIndex: Int, dueAmount: Double) {
        val allMembers = repository.getActiveMembers().first()
        val allPaid = allMembers.isNotEmpty() && allMembers.all { m ->
            repository.getTotalPaidForMonth(m.id, year, monthIndex) >= dueAmount
        }
        if (allPaid) confettiMonthFlow.value = monthIndex
    }

    private fun dismissConfetti() { confettiMonthFlow.value = null }

    private fun undoLastRemoval(paymentId: Long, memberId: Long, year: Int, monthIndex: Int, dueAmount: Double) {
        launch {
            repository.insertPayment(
                PaymentRecord(
                    memberId = memberId,
                    year = year,
                    monthIndex = monthIndex,
                    amountPaid = dueAmount,
                    expectedAmount = dueAmount,
                    paidAt = System.currentTimeMillis()
                )
            )
            snackbarFlow.value = null
        }
    }

    private fun dismissSnackbar() { snackbarFlow.value = null }

    private fun addMember(name: String, phone: String?) {
        launch {
            val trackerId = appState.currentTrackerId.value
            if (trackerId != null) {
                repository.insertTrackerMember(
                    TrackerMember(
                        trackerId = trackerId,
                        name = name.trim(),
                        phone = if (phone.isNullOrBlank()) null else phone.trim(),
                        createdAt = System.currentTimeMillis()
                    )
                )
            } else {
                repository.insertMember(
                    Member(
                        name = name.trim(),
                        phone = if (phone.isNullOrBlank()) null else phone.trim(),
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    private fun updateMember(member: Member) {
        launch {
            val trackerId = appState.currentTrackerId.value
            if (trackerId != null) {
                repository.updateTrackerMember(
                    TrackerMember(
                        id = member.id,
                        trackerId = trackerId,
                        name = member.name,
                        phone = member.phone,
                        isArchived = member.isArchived,
                        createdAt = member.createdAt
                    )
                )
            } else {
                repository.updateMember(member)
            }
        }
    }

    private fun setMemberArchived(id: Long, archived: Boolean) {
        launch {
            val trackerId = appState.currentTrackerId.value
            if (trackerId != null) {
                repository.setTrackerMemberArchived(id, archived)
            } else {
                repository.setMemberArchived(id, archived)
            }
        }
    }

    private fun deleteMember(id: Long, trackerIdOverride: Long? = null) {
        launch {
            val trackerId = trackerIdOverride ?: appState.currentTrackerId.value
            if (trackerId != null) {
                repository.deleteTrackerMember(trackerId, id)
            } else {
                repository.deleteMember(id)
            }
        }
    }

    private fun setCurrentTrackerId(trackerId: Long?) {
        appState.setCurrentTrackerId(trackerId)
    }

    private fun setLayoutStyleForCurrentTracker(style: LayoutStyle) {
        launch {
            val trackerId = appState.currentTrackerId.value ?: return@launch
            val tracker = repository.getTrackerById(trackerId) ?: return@launch
            repository.updateTracker(tracker.copy(layoutStyle = style))
        }
    }

    private suspend fun checkAndTriggerTrackerConfetti(trackerId: Long, periodId: Long, type: TrackerType) {
        val allMembers = repository.getActiveMembersForTracker(trackerId).first()
        val records = repository.getRecordsForPeriod(trackerId, periodId).first()
        val completed = when (type) {
            TrackerType.DUES -> setOf(RecordStatus.PAID)
            TrackerType.GOALS -> setOf(RecordStatus.DONE)
            TrackerType.TODO -> setOf(RecordStatus.DONE)
            TrackerType.BUDGET -> setOf(RecordStatus.PAID)
            TrackerType.EXPENSES -> setOf(RecordStatus.DONE, RecordStatus.PAID, RecordStatus.PRESENT)
        }
        val recordByMember = records.associateBy { it.memberId }
        val allDone = allMembers.isNotEmpty() && allMembers.all { m ->
            val status = recordByMember[m.id]?.status
            status != null && status in completed
        }
        if (allDone) confettiMonthFlow.value = Calendar.getInstance().get(Calendar.MONTH)
    }

    private fun TrackerMember.asUiMember(): Member = Member(
        id = id,
        name = name,
        phone = phone,
        isArchived = isArchived,
        createdAt = createdAt
    )

    private fun monthIndexFromLabel(label: String, year: Int): Int? {
        val fmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val date = runCatching { fmt.parse(label) }.getOrNull() ?: return null
        val cal = Calendar.getInstance().apply { time = date }
        if (cal.get(Calendar.YEAR) != year) return null
        return cal.get(Calendar.MONTH)
    }

    private suspend fun ensureMonthlyPeriod(trackerId: Long, year: Int, monthIndex: Int): TrackerPeriod {
        val label = monthlyLabel(year, monthIndex)
        val existing = repository.getPeriodByLabel(trackerId, label)
        if (existing != null) return existing

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = cal.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        val createdAt = System.currentTimeMillis()
        val periodId = repository.insertPeriod(
            TrackerPeriod(
                trackerId = trackerId,
                label = label,
                startDate = start,
                endDate = end,
                isCurrent = false,
                createdAt = createdAt
            )
        )
        return TrackerPeriod(
            id = periodId,
            trackerId = trackerId,
            label = label,
            startDate = start,
            endDate = end,
            isCurrent = false,
            createdAt = createdAt
        )
    }

    private fun monthlyLabel(year: Int, monthIndex: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    private fun markOutstandingMonthsPaid(
        memberId: Long,
        year: Int,
        dueAmount: Double,
        trackerIdOverride: Long? = null
    ) {
        launch {
            val trackerId = trackerIdOverride ?: appState.currentTrackerId.value
            val current = Calendar.getInstance()
            val endMonth = when {
                year < current.get(Calendar.YEAR) -> 11
                year == current.get(Calendar.YEAR) -> current.get(Calendar.MONTH)
                else -> -1
            }
            if (endMonth < 0) return@launch

            if (trackerId != null) {
                val tracker = repository.getTrackerById(trackerId) ?: return@launch
                if (tracker.type != TrackerType.DUES) return@launch
                for (mi in 0..endMonth) {
                    val period = ensureMonthlyPeriod(trackerId, year, mi)
                    val existing = repository.getRecord(trackerId, period.id, memberId)
                    val paid = existing?.amountPaid ?: 0.0
                    if (paid < tracker.defaultAmount) {
                        val updated = (existing ?: TrackerRecord(
                            trackerId = trackerId,
                            periodId = period.id,
                            memberId = memberId
                        )).copy(
                            status = RecordStatus.PAID,
                            amountPaid = tracker.defaultAmount,
                            updatedAt = System.currentTimeMillis()
                        )
                        if (existing == null) repository.insertRecord(updated) else repository.updateRecord(updated)
                    }
                }
                val currentPeriod = repository.getCurrentPeriod(trackerId)
                if (currentPeriod != null) {
                    checkAndTriggerTrackerConfetti(trackerId, currentPeriod.id, TrackerType.DUES)
                }
                return@launch
            }

            for (mi in 0..endMonth) {
                val totalPaid = repository.getTotalPaidForMonth(memberId, year, mi)
                if (totalPaid < dueAmount) {
                    repository.insertPayment(
                        PaymentRecord(
                            memberId = memberId,
                            year = year,
                            monthIndex = mi,
                            amountPaid = dueAmount - totalPaid,
                            expectedAmount = dueAmount,
                            paidAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }
}
