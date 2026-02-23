package com.mikeisesele.clearr.ui.feature.analytics

import com.mikeisesele.clearr.data.model.Member
import com.mikeisesele.clearr.data.model.PaymentRecord
import com.mikeisesele.clearr.data.model.YearConfig
import com.mikeisesele.clearr.domain.repository.DuesRepository
import com.mikeisesele.clearr.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<DuesRepository>()

    @Test
    fun `init combines analytics streams into ui state`() = runTest {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val membersFlow = MutableStateFlow(listOf(Member(id = 1, name = "Henry")))
        val paymentsByYear = mutableMapOf(
            currentYear to MutableStateFlow(listOf(PaymentRecord(memberId = 1, year = currentYear, monthIndex = 0, amountPaid = 5000.0, expectedAmount = 5000.0))),
            (currentYear - 1) to MutableStateFlow(listOf(PaymentRecord(memberId = 1, year = currentYear - 1, monthIndex = 11, amountPaid = 4000.0, expectedAmount = 5000.0)))
        )
        val configByYear = mutableMapOf(
            currentYear to MutableStateFlow<YearConfig?>(YearConfig(year = currentYear, dueAmountPerMonth = 5000.0)),
            (currentYear - 1) to MutableStateFlow<YearConfig?>(YearConfig(year = currentYear - 1, dueAmountPerMonth = 4500.0))
        )

        every { repository.getAllMembers() } returns membersFlow
        every { repository.getPaymentsForYear(any()) } answers { paymentsByYear[firstArg()]!! }
        every { repository.getYearConfigFlow(any()) } answers { configByYear[firstArg()]!! }

        val viewModel = AnalyticsViewModel(repository)
        advanceUntilIdle()

        assertEquals(currentYear, viewModel.uiState.value.selectedYear)
        assertEquals(1, viewModel.uiState.value.members.size)
        assertEquals(1, viewModel.uiState.value.payments.size)
        assertEquals(1, viewModel.uiState.value.prevYearPayments.size)
        assertEquals(5000.0, viewModel.uiState.value.yearConfig?.dueAmountPerMonth)
    }

    @Test
    fun `select year switches backing flows`() = runTest {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val nextYear = currentYear + 1
        val membersFlow = MutableStateFlow(emptyList<Member>())
        val paymentsByYear = mutableMapOf(
            currentYear to MutableStateFlow(emptyList<PaymentRecord>()),
            (currentYear - 1) to MutableStateFlow(emptyList<PaymentRecord>()),
            nextYear to MutableStateFlow(listOf(PaymentRecord(memberId = 8, year = nextYear, monthIndex = 2, amountPaid = 8000.0, expectedAmount = 8000.0))),
            currentYear to MutableStateFlow(emptyList<PaymentRecord>())
        )
        val configByYear = mutableMapOf(
            currentYear to MutableStateFlow<YearConfig?>(null),
            (currentYear - 1) to MutableStateFlow<YearConfig?>(null),
            nextYear to MutableStateFlow<YearConfig?>(YearConfig(year = nextYear, dueAmountPerMonth = 8000.0))
        )

        every { repository.getAllMembers() } returns membersFlow
        every { repository.getPaymentsForYear(any()) } answers {
            paymentsByYear.getOrPut(firstArg()) { MutableStateFlow(emptyList()) }
        }
        every { repository.getYearConfigFlow(any()) } answers {
            configByYear.getOrPut(firstArg()) { MutableStateFlow(null) }
        }

        val viewModel = AnalyticsViewModel(repository)
        viewModel.onAction(AnalyticsAction.SelectYear(nextYear))
        advanceUntilIdle()

        assertEquals(nextYear, viewModel.uiState.value.selectedYear)
        assertEquals(1, viewModel.uiState.value.payments.size)
        assertEquals(8000.0, viewModel.uiState.value.yearConfig?.dueAmountPerMonth)
    }
}
