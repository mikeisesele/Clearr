package com.mikeisesele.clearr.ui.navigation

import com.mikeisesele.clearr.data.model.AppConfig
import com.mikeisesele.clearr.di.AppStateHolder
import com.mikeisesele.clearr.domain.repository.DuesRepository
import com.mikeisesele.clearr.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppConfigViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<DuesRepository>()
    private val appState = AppStateHolder()

    @Test
    fun `observe updates ui state and app state`() = runTest {
        val configFlow = MutableStateFlow<AppConfig?>(null)
        every { repository.getAppConfigFlow() } returns configFlow

        val viewModel = AppConfigViewModel(repository, appState)
        val config = AppConfig(groupName = "Clearr Group", setupComplete = true)

        configFlow.value = config
        advanceUntilIdle()

        assertEquals(config, viewModel.uiState.value.appConfig)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(config, appState.appConfig.value)
    }
}
