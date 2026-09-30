package com.example.devicelab.domain.diagnostics

import com.example.devicelab.domain.model.BatteryHealth
import com.example.devicelab.domain.model.BatterySnapshot
import com.example.devicelab.domain.model.DiagnosticIssue
import com.example.devicelab.domain.model.DiagnosticSeverity
import com.example.devicelab.domain.model.MemorySnapshot
import com.example.devicelab.domain.model.NetworkSnapshot
import com.example.devicelab.domain.model.PluggedType
import com.example.devicelab.domain.model.StorageSnapshot
import com.example.devicelab.domain.model.ThermalSnapshot
import com.example.devicelab.domain.model.ThermalStatus

/**
 * Pure Kotlin rule engine that inspects hardware telemetry snapshots to detect
 * meaningful system health anomalies and hardware warnings without false positives.
 */
object HardwareDiagnosticEngine {

    fun analyze(
        memory: MemorySnapshot,
        storage: StorageSnapshot,
        battery: BatterySnapshot,
        thermal: ThermalSnapshot,
        network: NetworkSnapshot
    ): List<DiagnosticIssue> {
        val issues = mutableListOf<DiagnosticIssue>()

        // 1. Memory Analysis
        if (memory.usedPercentage >= 95.0f || memory.isLowMemory) {
            issues.add(
                DiagnosticIssue(
                    id = "diag_mem_critical",
                    title = "Extreme RAM Exhaustion",
                    description = "System RAM utilization is at ${"%.1f".format(memory.usedPercentage)}%. Low-memory killer (LMK) active.",
                    severity = DiagnosticSeverity.CRITICAL,
                    subsystem = "Compute / Memory"
                )
            )
        } else if (memory.usedPercentage >= 85.0f) {
            issues.add(
                DiagnosticIssue(
                    id = "diag_mem_warning",
                    title = "High RAM Pressure",
                    description = "RAM utilization is at ${"%.1f".format(memory.usedPercentage)}%. Background tasks may be evicted.",
                    severity = DiagnosticSeverity.WARNING,
                    subsystem = "Compute / Memory"
                )
            )
        }

        // 2. Storage Analysis
        if (storage.internalUsedPercentage >= 95.0f) {
            issues.add(
                DiagnosticIssue(
                    id = "diag_storage_critical",
                    title = "Critical Storage Exhaustion",
                    description = "Internal storage is at ${"%.1f".format(storage.internalUsedPercentage)}% capacity (<5% free). IO latency may increase.",
                    severity = DiagnosticSeverity.CRITICAL,
                    subsystem = "Storage"
                )
            )
        } else if (storage.internalUsedPercentage >= 90.0f) {
            issues.add(
                DiagnosticIssue(
                    id = "diag_storage_warning",
                    title = "Low Storage Space",
                    description = "Internal storage is at ${"%.1f".format(storage.internalUsedPercentage)}% capacity. Consider freeing space.",
                    severity = DiagnosticSeverity.WARNING,
                    subsystem = "Storage"
                )
            )
        }

        // 3. Thermal Analysis
        when (thermal.status) {
            ThermalStatus.SEVERE,
            ThermalStatus.CRITICAL,
            ThermalStatus.EMERGENCY,
            ThermalStatus.SHUTDOWN -> {
                issues.add(
                    DiagnosticIssue(
                        id = "diag_thermal_critical",
                        title = "Active Thermal Throttling",
                        description = "System thermal state is ${thermal.status.name}. CPU and GPU performance are actively reduced to protect silicon.",
                        severity = DiagnosticSeverity.CRITICAL,
                        subsystem = "Thermal"
                    )
                )
            }
            ThermalStatus.LIGHT,
            ThermalStatus.MODERATE -> {
                issues.add(
                    DiagnosticIssue(
                        id = "diag_thermal_warning",
                        title = "Elevated Device Temperature",
                        description = "Thermal state is ${thermal.status.name}. Device approaching throttling limits under sustained workload.",
                        severity = DiagnosticSeverity.WARNING,
                        subsystem = "Thermal"
                    )
                )
            }
            else -> { /* Nominal */ }
        }

        // 4. Battery Analysis
        val batteryTemp = battery.temperatureCelsius
        if (batteryTemp != null && batteryTemp >= 48.0f) {
            issues.add(
                DiagnosticIssue(
                    id = "diag_battery_overheat",
                    title = "Battery Overheat Alert",
                    description = "Battery thermistor reads ${"%.1f".format(batteryTemp)}°C. High temperature accelerates cell wear.",
                    severity = DiagnosticSeverity.CRITICAL,
                    subsystem = "Power / Battery"
                )
            )
        } else if (batteryTemp != null && batteryTemp >= 42.0f) {
            issues.add(
                DiagnosticIssue(
                    id = "diag_battery_warm",
                    title = "Elevated Battery Temperature",
                    description = "Battery thermistor reads ${"%.1f".format(batteryTemp)}°C. Charging rate may be throttled.",
                    severity = DiagnosticSeverity.WARNING,
                    subsystem = "Power / Battery"
                )
            )
        }

        if (battery.health != BatteryHealth.GOOD && battery.health != BatteryHealth.UNKNOWN) {
            issues.add(
                DiagnosticIssue(
                    id = "diag_battery_health",
                    title = "Abnormal Battery Health",
                    description = "Hardware report flagged battery status as ${battery.health.name}.",
                    severity = DiagnosticSeverity.CRITICAL,
                    subsystem = "Power / Battery"
                )
            )
        }

        if (battery.percentage <= 15 && battery.pluggedType == PluggedType.UNPLUGGED) {
            issues.add(
                DiagnosticIssue(
                    id = "diag_battery_low",
                    title = "Reserve Capacity Depleted",
                    description = "Battery is at ${battery.percentage}% while discharging.",
                    severity = DiagnosticSeverity.WARNING,
                    subsystem = "Power / Battery"
                )
            )
        }

        // 5. Network Analysis
        if (network.isConnected && !network.isValidated) {
            issues.add(
                DiagnosticIssue(
                    id = "diag_net_no_internet",
                    title = "Connected Without Internet",
                    description = "Active network transport is online but gateway reachability / captive portal validation failed.",
                    severity = DiagnosticSeverity.WARNING,
                    subsystem = "Connectivity"
                )
            )
        } else if (!network.isConnected) {
            issues.add(
                DiagnosticIssue(
                    id = "diag_net_offline",
                    title = "All Network Radios Idle",
                    description = "Device currently has no active network link.",
                    severity = DiagnosticSeverity.INFO,
                    subsystem = "Connectivity"
                )
            )
        }

        return issues
    }
}
