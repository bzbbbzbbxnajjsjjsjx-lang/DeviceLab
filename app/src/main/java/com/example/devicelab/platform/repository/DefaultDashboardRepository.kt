package com.example.devicelab.platform.repository

import com.example.devicelab.domain.diagnostics.HardwareDiagnosticEngine
import com.example.devicelab.domain.model.BatterySnapshot
import com.example.devicelab.domain.model.CpuSnapshot
import com.example.devicelab.domain.model.DashboardTelemetry
import com.example.devicelab.domain.model.DeviceIdentity
import com.example.devicelab.domain.model.DisplaySnapshot
import com.example.devicelab.domain.model.HardwareCapability
import com.example.devicelab.domain.model.HardwareIntelligenceSnapshot
import com.example.devicelab.domain.model.MemorySnapshot
import com.example.devicelab.domain.model.NetworkSnapshot
import com.example.devicelab.domain.model.NetworkTransport
import com.example.devicelab.domain.model.SensorDescriptor
import com.example.devicelab.domain.model.SensorReading
import com.example.devicelab.domain.model.StorageSnapshot
import com.example.devicelab.domain.model.ThermalSnapshot
import com.example.devicelab.domain.repository.BatteryThermalDataSource
import com.example.devicelab.domain.repository.CpuDataSource
import com.example.devicelab.domain.repository.DashboardRepository
import com.example.devicelab.domain.repository.DeviceSpecsProvider
import com.example.devicelab.domain.repository.DisplayDataSource
import com.example.devicelab.domain.repository.HardwareCapabilityDataSource
import com.example.devicelab.domain.repository.MemoryDataSource
import com.example.devicelab.domain.repository.NetworkDataSource
import com.example.devicelab.domain.repository.SensorDataSource
import com.example.devicelab.domain.repository.StorageDataSource
import com.example.devicelab.platform.system.AndroidCpuDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
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
    private val cpuDataSource: CpuDataSource = AndroidCpuDataSource(),
    private val sensorDataSource: SensorDataSource? = null,
    private val networkDataSource: NetworkDataSource? = null,
    private val capabilityDataSource: HardwareCapabilityDataSource? = null,
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

    override fun observeHardwareIntelligence(): Flow<HardwareIntelligenceSnapshot> {
        val networkFlow = networkDataSource?.observeNetworkSnapshot() ?: flowOf(
            NetworkSnapshot(
                transport = NetworkTransport.UNKNOWN,
                isConnected = false,
                isValidated = false,
                isMetered = false,
                downlinkBandwidthKbps = null,
                uplinkBandwidthKbps = null
            )
        )

        return combine(
            batteryThermalDataSource.observeBatteryAndThermal(),
            networkFlow,
            refreshSignal.onStart { emit(Unit) }
        ) { _, network, _ ->
            assembleHardwareIntelligence(network)
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

    override suspend fun refreshHardwareIntelligence(): HardwareIntelligenceSnapshot = withContext(ioDispatcher) {
        val network = networkDataSource?.getNetworkSnapshot() ?: NetworkSnapshot(
            transport = NetworkTransport.UNKNOWN,
            isConnected = false,
            isValidated = false,
            isMetered = false,
            downlinkBandwidthKbps = null,
            uplinkBandwidthKbps = null
        )
        val snapshot = assembleHardwareIntelligence(network)
        refreshSignal.tryEmit(Unit)
        snapshot
    }

    override fun getInitialTelemetry(): DashboardTelemetry? {
        return try {
            val (battery, thermal) = batteryThermalDataSource.getCurrentBatteryAndThermal()
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
        } catch (_: Exception) {
            null
        }
    }

    override fun getInitialHardwareIntelligence(): HardwareIntelligenceSnapshot? {
        return try {
            val network = networkDataSource?.getNetworkSnapshot() ?: NetworkSnapshot(
                transport = NetworkTransport.UNKNOWN,
                isConnected = false,
                isValidated = false,
                isMetered = false,
                downlinkBandwidthKbps = null,
                uplinkBandwidthKbps = null
            )
            assembleHardwareIntelligence(network)
        } catch (_: Exception) {
            null
        }
    }

    override fun observeLiveSensor(sensorType: Int): Flow<SensorReading> {
        return sensorDataSource?.observeSensorReadings(sensorType) ?: emptyFlow()
    }

    private fun assembleHardwareIntelligence(network: NetworkSnapshot): HardwareIntelligenceSnapshot {
        val identity = deviceSpecsProvider.getDeviceIdentity()
        val cpu = cpuDataSource.getCpuSnapshot()
        val memory = memoryDataSource.getMemorySnapshot()
        val storage = storageDataSource.getStorageSnapshot()
        val battery = batteryThermalDataSource.getBatterySnapshot()
        val thermal = batteryThermalDataSource.getThermalSnapshot()
        val display = displayDataSource.getDisplaySnapshot()
        val sensors = sensorDataSource?.getSensorInventory() ?: emptyList()
        val capabilities = capabilityDataSource?.getHardwareCapabilities() ?: emptyList()

        val diagnostics = HardwareDiagnosticEngine.analyze(
            memory = memory,
            storage = storage,
            battery = battery,
            thermal = thermal,
            network = network
        )

        return HardwareIntelligenceSnapshot(
            identity = identity,
            cpu = cpu,
            memory = memory,
            storage = storage,
            battery = battery,
            thermal = thermal,
            display = display,
            network = network,
            sensors = sensors,
            capabilities = capabilities,
            diagnostics = diagnostics,
            timestampMillis = System.currentTimeMillis()
        )
    }
}
