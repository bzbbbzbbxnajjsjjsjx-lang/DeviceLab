package com.example.devicelab.domain.repository

import com.example.devicelab.domain.model.BatterySpecs
import com.example.devicelab.domain.model.DashboardTelemetry
import com.example.devicelab.domain.model.DeviceSpecs
import com.example.devicelab.domain.model.DisplayBasicSpecs
import com.example.devicelab.domain.model.MemorySpecs
import com.example.devicelab.domain.model.StorageSpecs
import com.example.devicelab.domain.model.ThermalSpecs
import kotlinx.coroutines.flow.Flow

interface DeviceSpecsProvider {
    fun getDeviceSpecs(): DeviceSpecs
}

interface MemoryDataSource {
    fun getMemorySpecs(): MemorySpecs
}

interface StorageDataSource {
    fun getStorageSpecs(): StorageSpecs
}

interface DisplayDataSource {
    fun getDisplaySpecs(): DisplayBasicSpecs
}

interface BatteryThermalDataSource {
    fun observeBatteryAndThermal(): Flow<Pair<BatterySpecs, ThermalSpecs>>
    fun getCurrentBatteryAndThermal(): Pair<BatterySpecs, ThermalSpecs>
}

interface DashboardRepository {
    fun observeDashboardTelemetry(): Flow<DashboardTelemetry>
    suspend fun refreshTelemetry(): DashboardTelemetry
}
