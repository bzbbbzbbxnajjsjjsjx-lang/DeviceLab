package com.example.devicelab.platform.repository

import com.example.devicelab.domain.model.BatteryHealth
import com.example.devicelab.domain.model.BatterySpecs
import com.example.devicelab.domain.model.ChargingStatus
import com.example.devicelab.domain.model.DeviceSpecs
import com.example.devicelab.domain.model.DisplayBasicSpecs
import com.example.devicelab.domain.model.MemorySpecs
import com.example.devicelab.domain.model.PluggedType
import com.example.devicelab.domain.model.StorageSpecs
import com.example.devicelab.domain.model.ThermalSpecs
import com.example.devicelab.domain.model.ThermalStatus
import com.example.devicelab.domain.repository.BatteryThermalDataSource
import com.example.devicelab.domain.repository.DeviceSpecsProvider
import com.example.devicelab.domain.repository.DisplayDataSource
import com.example.devicelab.domain.repository.MemoryDataSource
import com.example.devicelab.domain.repository.StorageDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultDashboardRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakeDeviceSpecs = DeviceSpecs(
        manufacturer = "Google",
        model = "Pixel 8",
        device = "shiba",
        androidVersion = "15",
        apiLevel = 35,
        supportedAbis = listOf("arm64-v8a"),
        processorCount = 8
    )

    private val fakeMemorySpecs = MemorySpecs(
        totalBytes = 8_000_000_000L,
        availableBytes = 4_000_000_000L,
        usedBytes = 4_000_000_000L,
        usedPercentage = 50.0f,
        isLowMemory = false
    )

    private val fakeStorageSpecs = StorageSpecs(
        totalBytes = 128_000_000_000L,
        availableBytes = 64_000_000_000L,
        usedBytes = 64_000_000_000L,
        usedPercentage = 50.0f
    )

    private val fakeDisplaySpecs = DisplayBasicSpecs(
        widthPixels = 1080,
        heightPixels = 2400,
        densityDpi = 420,
        densityScale = 2.625f,
        refreshRateHz = 120.0f
    )

    private val fakeBatterySpecs = BatterySpecs(
        percentage = 90,
        chargingStatus = ChargingStatus.CHARGING,
        pluggedType = PluggedType.AC,
        health = BatteryHealth.GOOD,
        temperatureCelsius = 28.0f,
        voltageMillivolts = 4200
    )

    private val fakeThermalSpecs = ThermalSpecs(ThermalStatus.NONE)

    private val fakeBatteryThermalDataSource = object : BatteryThermalDataSource {
        val flow = MutableSharedFlow<Pair<BatterySpecs, ThermalSpecs>>(replay = 1)

        init {
            flow.tryEmit(fakeBatterySpecs to fakeThermalSpecs)
        }

        override fun observeBatteryAndThermal(): Flow<Pair<BatterySpecs, ThermalSpecs>> = flow

        override fun getCurrentBatteryAndThermal(): Pair<BatterySpecs, ThermalSpecs> =
            fakeBatterySpecs to fakeThermalSpecs
    }

    private val repository = DefaultDashboardRepository(
        deviceSpecsProvider = object : DeviceSpecsProvider {
            override fun getDeviceSpecs(): DeviceSpecs = fakeDeviceSpecs
        },
        memoryDataSource = object : MemoryDataSource {
            override fun getMemorySpecs(): MemorySpecs = fakeMemorySpecs
        },
        storageDataSource = object : StorageDataSource {
            override fun getStorageSpecs(): StorageSpecs = fakeStorageSpecs
        },
        displayDataSource = object : DisplayDataSource {
            override fun getDisplaySpecs(): DisplayBasicSpecs = fakeDisplaySpecs
        },
        batteryThermalDataSource = fakeBatteryThermalDataSource,
        ioDispatcher = testDispatcher
    )

    @Test
    fun observeDashboardTelemetry_aggregatesAllDataSources() = runTest(testDispatcher) {
        val telemetry = repository.observeDashboardTelemetry().first()

        assertEquals("Google", telemetry.device.manufacturer)
        assertEquals("Pixel 8", telemetry.device.model)
        assertEquals(8_000_000_000L, telemetry.memory.totalBytes)
        assertEquals(128_000_000_000L, telemetry.storage.totalBytes)
        assertEquals(1080, telemetry.display.widthPixels)
        assertEquals(90, telemetry.battery.percentage)
        assertEquals(ThermalStatus.NONE, telemetry.thermal.status)
    }

    @Test
    fun refreshTelemetry_returnsSnapshotAndTriggersFlow() = runTest(testDispatcher) {
        val telemetry = repository.refreshTelemetry()

        assertEquals("Google", telemetry.device.manufacturer)
        assertEquals(90, telemetry.battery.percentage)
        assertEquals(ChargingStatus.CHARGING, telemetry.battery.chargingStatus)
    }
}
