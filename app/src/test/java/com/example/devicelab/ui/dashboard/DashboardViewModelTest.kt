package com.example.devicelab.ui.dashboard

import com.example.devicelab.domain.model.BatteryHealth
import com.example.devicelab.domain.model.BatterySpecs
import com.example.devicelab.domain.model.ChargingStatus
import com.example.devicelab.domain.model.DashboardTelemetry
import com.example.devicelab.domain.model.DeviceSpecs
import com.example.devicelab.domain.model.DisplayBasicSpecs
import com.example.devicelab.domain.model.MemorySpecs
import com.example.devicelab.domain.model.PluggedType
import com.example.devicelab.domain.model.StorageSpecs
import com.example.devicelab.domain.model.ThermalSpecs
import com.example.devicelab.domain.model.ThermalStatus
import com.example.devicelab.domain.repository.DashboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleTelemetry = DashboardTelemetry(
        device = DeviceSpecs(
            manufacturer = "Google",
            model = "Pixel 8",
            device = "shiba",
            androidVersion = "15",
            apiLevel = 35,
            supportedAbis = listOf("arm64-v8a"),
            processorCount = 8
        ),
        memory = MemorySpecs(
            totalBytes = 8_000_000_000L,
            availableBytes = 4_000_000_000L,
            usedBytes = 4_000_000_000L,
            usedPercentage = 50.0f,
            isLowMemory = false
        ),
        storage = StorageSpecs(
            totalBytes = 128_000_000_000L,
            availableBytes = 64_000_000_000L,
            usedBytes = 64_000_000_000L,
            usedPercentage = 50.0f
        ),
        battery = BatterySpecs(
            percentage = 80,
            chargingStatus = ChargingStatus.DISCHARGING,
            pluggedType = PluggedType.UNPLUGGED,
            health = BatteryHealth.GOOD,
            temperatureCelsius = 25.0f,
            voltageMillivolts = 3900
        ),
        thermal = ThermalSpecs(ThermalStatus.NONE),
        display = DisplayBasicSpecs(
            widthPixels = 1080,
            heightPixels = 2400,
            densityDpi = 420,
            densityScale = 2.625f,
            refreshRateHz = 120.0f
        ),
        timestampMillis = 1000L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_initialLoading_thenEmitsSuccess() = runTest(testDispatcher) {
        val flow = MutableSharedFlow<DashboardTelemetry>(replay = 1)
        flow.tryEmit(sampleTelemetry)

        val repository = object : DashboardRepository {
            override fun observeDashboardTelemetry(): Flow<DashboardTelemetry> = flow
            override suspend fun refreshTelemetry(): DashboardTelemetry = sampleTelemetry
        }

        val viewModel = DashboardViewModel(repository)

        val collectJob = launch { viewModel.uiState.collect {} }

        val state = viewModel.uiState.value
        assertTrue("Expected Success state, but was $state", state is DashboardUiState.Success)
        val successState = state as DashboardUiState.Success
        assertEquals("Google", successState.telemetry.device.manufacturer)
        assertEquals(80, successState.telemetry.battery.percentage)

        collectJob.cancel()
    }

    @Test
    fun refresh_invokesRepositoryRefresh() = runTest(testDispatcher) {
        val flow = MutableSharedFlow<DashboardTelemetry>(replay = 1)
        flow.tryEmit(sampleTelemetry)

        var refreshCallCount = 0
        val repository = object : DashboardRepository {
            override fun observeDashboardTelemetry(): Flow<DashboardTelemetry> = flow
            override suspend fun refreshTelemetry(): DashboardTelemetry {
                refreshCallCount++
                return sampleTelemetry
            }
        }

        val viewModel = DashboardViewModel(repository)
        val collectJob = launch { viewModel.uiState.collect {} }

        viewModel.refresh()
        assertEquals(1, refreshCallCount)

        collectJob.cancel()
    }

    @Test
    fun uiState_repositoryError_emitsErrorState() = runTest(testDispatcher) {
        val errorFlow = flow<DashboardTelemetry> {
            throw RuntimeException("Hardware sensor access denied")
        }

        val repository = object : DashboardRepository {
            override fun observeDashboardTelemetry(): Flow<DashboardTelemetry> = errorFlow
            override suspend fun refreshTelemetry(): DashboardTelemetry = sampleTelemetry
        }

        val viewModel = DashboardViewModel(repository)
        val collectJob = launch { viewModel.uiState.collect {} }

        val state = viewModel.uiState.value
        assertTrue("Expected Error state, but was $state", state is DashboardUiState.Error)
        assertEquals("Hardware sensor access denied", (state as DashboardUiState.Error).message)

        collectJob.cancel()
    }
}
