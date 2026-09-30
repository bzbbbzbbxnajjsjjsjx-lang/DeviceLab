package com.example.devicelab.domain.diagnostics

import com.example.devicelab.domain.model.BatteryHealth
import com.example.devicelab.domain.model.BatterySnapshot
import com.example.devicelab.domain.model.ChargingStatus
import com.example.devicelab.domain.model.DiagnosticSeverity
import com.example.devicelab.domain.model.MemorySnapshot
import com.example.devicelab.domain.model.NetworkSnapshot
import com.example.devicelab.domain.model.NetworkTransport
import com.example.devicelab.domain.model.PluggedType
import com.example.devicelab.domain.model.StorageSnapshot
import com.example.devicelab.domain.model.ThermalSnapshot
import com.example.devicelab.domain.model.ThermalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HardwareDiagnosticEngineTest {

    private val nominalMemory = MemorySnapshot(
        totalBytes = 8_000_000_000L,
        availableBytes = 4_000_000_000L,
        usedBytes = 4_000_000_000L,
        usedPercentage = 50.0f,
        lowMemoryThresholdBytes = 500_000_000L,
        isLowMemory = false,
        jvmTotalHeapBytes = 64_000_000L,
        jvmFreeHeapBytes = 32_000_000L,
        jvmMaxHeapBytes = 512_000_000L
    )

    private val nominalStorage = StorageSnapshot(
        internalTotalBytes = 128_000_000_000L,
        internalAvailableBytes = 64_000_000_000L,
        internalUsedBytes = 64_000_000_000L,
        internalUsedPercentage = 50.0f,
        externalStorageAvailable = false,
        externalTotalBytes = null,
        externalAvailableBytes = null
    )

    private val nominalBattery = BatterySnapshot(
        percentage = 80,
        chargingStatus = ChargingStatus.DISCHARGING,
        pluggedType = PluggedType.UNPLUGGED,
        health = BatteryHealth.GOOD,
        temperatureCelsius = 29.0f,
        voltageMillivolts = 3950,
        currentMicroamps = -350000,
        technology = "Li-ion",
        capacityMah = 5000.0
    )

    private val nominalThermal = ThermalSnapshot(
        status = ThermalStatus.NONE,
        statusDescription = "Nominal",
        isThrottling = false
    )

    private val nominalNetwork = NetworkSnapshot(
        transport = NetworkTransport.WIFI,
        isConnected = true,
        isValidated = true,
        isMetered = false,
        downlinkBandwidthKbps = 150000,
        uplinkBandwidthKbps = 50000
    )

    @Test
    fun analyze_nominalHardware_producesZeroIssues() {
        val issues = HardwareDiagnosticEngine.analyze(
            nominalMemory,
            nominalStorage,
            nominalBattery,
            nominalThermal,
            nominalNetwork
        )
        assertTrue("Expected 0 issues for nominal hardware, found: ${issues.size}", issues.isEmpty())
    }

    @Test
    fun analyze_criticalMemoryExhaustion_triggersCriticalIssue() {
        val criticalMem = nominalMemory.copy(
            usedPercentage = 96.5f,
            isLowMemory = true
        )
        val issues = HardwareDiagnosticEngine.analyze(
            criticalMem,
            nominalStorage,
            nominalBattery,
            nominalThermal,
            nominalNetwork
        )
        assertTrue(issues.any { it.id == "diag_mem_critical" && it.severity == DiagnosticSeverity.CRITICAL })
    }

    @Test
    fun analyze_highRamPressure_triggersWarning() {
        val highMem = nominalMemory.copy(
            usedPercentage = 87.0f,
            isLowMemory = false
        )
        val issues = HardwareDiagnosticEngine.analyze(
            highMem,
            nominalStorage,
            nominalBattery,
            nominalThermal,
            nominalNetwork
        )
        assertTrue(issues.any { it.id == "diag_mem_warning" && it.severity == DiagnosticSeverity.WARNING })
    }

    @Test
    fun analyze_criticalStorage_triggersCriticalIssue() {
        val fullStorage = nominalStorage.copy(
            internalUsedPercentage = 97.0f
        )
        val issues = HardwareDiagnosticEngine.analyze(
            nominalMemory,
            fullStorage,
            nominalBattery,
            nominalThermal,
            nominalNetwork
        )
        assertTrue(issues.any { it.id == "diag_storage_critical" && it.severity == DiagnosticSeverity.CRITICAL })
    }

    @Test
    fun analyze_severeThermalThrottling_triggersCriticalIssue() {
        val hotThermal = nominalThermal.copy(
            status = ThermalStatus.SEVERE,
            isThrottling = true
        )
        val issues = HardwareDiagnosticEngine.analyze(
            nominalMemory,
            nominalStorage,
            nominalBattery,
            hotThermal,
            nominalNetwork
        )
        assertTrue(issues.any { it.id == "diag_thermal_critical" && it.severity == DiagnosticSeverity.CRITICAL })
    }

    @Test
    fun analyze_batteryOverheat_triggersCriticalIssue() {
        val hotBattery = nominalBattery.copy(
            temperatureCelsius = 49.5f
        )
        val issues = HardwareDiagnosticEngine.analyze(
            nominalMemory,
            nominalStorage,
            hotBattery,
            nominalThermal,
            nominalNetwork
        )
        assertTrue(issues.any { it.id == "diag_battery_overheat" && it.severity == DiagnosticSeverity.CRITICAL })
    }

    @Test
    fun analyze_unvalidatedNetwork_triggersWarning() {
        val unvalidatedNetwork = nominalNetwork.copy(
            isConnected = true,
            isValidated = false
        )
        val issues = HardwareDiagnosticEngine.analyze(
            nominalMemory,
            nominalStorage,
            nominalBattery,
            nominalThermal,
            unvalidatedNetwork
        )
        assertTrue(issues.any { it.id == "diag_net_no_internet" && it.severity == DiagnosticSeverity.WARNING })
    }

    @Test
    fun analyze_offlineNetwork_triggersInfoIssue() {
        val offlineNetwork = nominalNetwork.copy(
            isConnected = false,
            isValidated = false,
            transport = NetworkTransport.NONE
        )
        val issues = HardwareDiagnosticEngine.analyze(
            nominalMemory,
            nominalStorage,
            nominalBattery,
            nominalThermal,
            offlineNetwork
        )
        assertTrue(issues.any { it.id == "diag_net_offline" && it.severity == DiagnosticSeverity.INFO })
    }
}
