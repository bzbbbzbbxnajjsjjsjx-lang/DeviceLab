package com.example.devicelab.platform.repository

import com.example.devicelab.domain.model.DashboardTelemetry
import com.example.devicelab.domain.repository.BatteryThermalDataSource
import com.example.devicelab.domain.repository.DashboardRepository
import com.example.devicelab.domain.repository.DeviceSpecsProvider
import com.example.devicelab.domain.repository.DisplayDataSource
import com.example.devicelab.domain.repository.MemoryDataSource
import com.example.devicelab.domain.repository.StorageDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/**
 * Production implementation of [DashboardRepository] that aggregates hardware telemetry
 * from decoupled domain data sources and emits updates reactively.
 */
class DefaultDashboardRepository(
    private val deviceSpecsProvider: DeviceSpecsProvider,
    private val memoryDataSource: MemoryDataSource,
    private val storageDataSource: StorageDataSource,
    private val displayDataSource: DisplayDataSource,
    private val batteryThermalDataSource: BatteryThermalDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DashboardRepository {

    private val refreshSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun observeDashboardTelemetry(): Flow<DashboardTelemetry> {
        return combine(
            batteryThermalDataSource.observeBatteryAndThermal(),
            refreshSignal.onStart { emit(Unit) }
        ) { (battery, thermal), _ ->
            val device = deviceSpecsProvider.getDeviceSpecs()
            val memory = memoryDataSource.getMemorySpecs()
            val storage = storageDataSource.getStorageSpecs()
            val display = displayDataSource.getDisplaySpecs()
            DashboardTelemetry(
                device = device,
                memory = memory,
                storage = storage,
                battery = battery,
                thermal = thermal,
                display = display,
                timestampMillis = System.currentTimeMillis()
            )
        }.flowOn(ioDispatcher)
    }

    override suspend fun refreshTelemetry(): DashboardTelemetry = withContext(ioDispatcher) {
        val (battery, thermal) = batteryThermalDataSource.getCurrentBatteryAndThermal()
        val device = deviceSpecsProvider.getDeviceSpecs()
        val memory = memoryDataSource.getMemorySpecs()
        val storage = storageDataSource.getStorageSpecs()
        val display = displayDataSource.getDisplaySpecs()
        val telemetry = DashboardTelemetry(
            device = device,
            memory = memory,
            storage = storage,
            battery = battery,
            thermal = thermal,
            display = display,
            timestampMillis = System.currentTimeMillis()
        )
        refreshSignal.tryEmit(Unit)
        telemetry
    }
}
