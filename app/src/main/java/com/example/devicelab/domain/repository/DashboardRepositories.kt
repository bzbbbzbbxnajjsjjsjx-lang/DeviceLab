package com.example.devicelab.domain.repository

import com.example.devicelab.domain.model.BatterySnapshot
import com.example.devicelab.domain.model.BatterySpecs
import com.example.devicelab.domain.model.CpuSnapshot
import com.example.devicelab.domain.model.DashboardTelemetry
import com.example.devicelab.domain.model.DeviceIdentity
import com.example.devicelab.domain.model.DeviceSpecs
import com.example.devicelab.domain.model.DisplayBasicSpecs
import com.example.devicelab.domain.model.DisplaySnapshot
import com.example.devicelab.domain.model.HardwareCapability
import com.example.devicelab.domain.model.HardwareIntelligenceSnapshot
import com.example.devicelab.domain.model.MemorySnapshot
import com.example.devicelab.domain.model.MemorySpecs
import com.example.devicelab.domain.model.NetworkSnapshot
import com.example.devicelab.domain.model.SensorDescriptor
import com.example.devicelab.domain.model.SensorReading
import com.example.devicelab.domain.model.StorageSnapshot
import com.example.devicelab.domain.model.StorageSpecs
import com.example.devicelab.domain.model.ThermalSnapshot
import com.example.devicelab.domain.model.ThermalSpecs
import kotlinx.coroutines.flow.Flow

interface DeviceSpecsProvider {
    fun getDeviceSpecs(): DeviceSpecs
    fun getDeviceIdentity(): DeviceIdentity
}

interface CpuDataSource {
    fun getCpuSnapshot(): CpuSnapshot
}

interface MemoryDataSource {
    fun getMemorySpecs(): MemorySpecs
    fun getMemorySnapshot(): MemorySnapshot
}

interface StorageDataSource {
    fun getStorageSpecs(): StorageSpecs
    fun getStorageSnapshot(): StorageSnapshot
}

interface DisplayDataSource {
    fun getDisplaySpecs(): DisplayBasicSpecs
    fun getDisplaySnapshot(): DisplaySnapshot
}

interface BatteryThermalDataSource {
    fun observeBatteryAndThermal(): Flow<Pair<BatterySpecs, ThermalSpecs>>
    fun getCurrentBatteryAndThermal(): Pair<BatterySpecs, ThermalSpecs>
    fun getBatterySnapshot(): BatterySnapshot
    fun getThermalSnapshot(): ThermalSnapshot
}

interface SensorDataSource {
    fun getSensorInventory(): List<SensorDescriptor>
    fun observeSensorReadings(sensorType: Int): Flow<SensorReading>
}

interface NetworkDataSource {
    fun getNetworkSnapshot(): NetworkSnapshot
    fun observeNetworkSnapshot(): Flow<NetworkSnapshot>
}

interface HardwareCapabilityDataSource {
    fun getHardwareCapabilities(): List<HardwareCapability>
}

interface DashboardRepository {
    fun observeDashboardTelemetry(): Flow<DashboardTelemetry>
    fun observeHardwareIntelligence(): Flow<HardwareIntelligenceSnapshot> =
        kotlinx.coroutines.flow.flow {
            observeDashboardTelemetry().collect { emit(it.toHardwareIntelligence()) }
        }
    suspend fun refreshTelemetry(): DashboardTelemetry
    suspend fun refreshHardwareIntelligence(): HardwareIntelligenceSnapshot =
        refreshTelemetry().toHardwareIntelligence()
    fun getInitialTelemetry(): DashboardTelemetry? = null
    fun getInitialHardwareIntelligence(): HardwareIntelligenceSnapshot? =
        getInitialTelemetry()?.toHardwareIntelligence()
    fun observeLiveSensor(sensorType: Int): Flow<SensorReading> = kotlinx.coroutines.flow.emptyFlow()
}
