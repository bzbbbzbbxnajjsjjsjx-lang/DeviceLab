package com.example.devicelab.domain.model

/**
 * Immutable domain models for device hardware diagnostics.
 * NOTE: This package must NEVER import any `android.*` APIs to guarantee 100% JVM testability.
 */

data class DeviceSpecs(
    val manufacturer: String,
    val model: String,
    val device: String,
    val androidVersion: String,
    val apiLevel: Int,
    val supportedAbis: List<String>,
    val processorCount: Int
)

data class MemorySpecs(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usedPercentage: Float,
    val isLowMemory: Boolean
)

data class StorageSpecs(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usedPercentage: Float
)

enum class ChargingStatus {
    CHARGING,
    DISCHARGING,
    FULL,
    NOT_CHARGING,
    UNKNOWN
}

enum class PluggedType {
    AC,
    USB,
    WIRELESS,
    UNPLUGGED,
    UNKNOWN
}

enum class BatteryHealth {
    GOOD,
    OVERHEAT,
    DEAD,
    OVER_VOLTAGE,
    UNSPECIFIED_FAILURE,
    COLD,
    UNKNOWN
}

data class BatterySpecs(
    val percentage: Int,
    val chargingStatus: ChargingStatus,
    val pluggedType: PluggedType,
    val health: BatteryHealth,
    val temperatureCelsius: Float?,
    val voltageMillivolts: Int?
)

enum class ThermalStatus {
    NONE,
    LIGHT,
    MODERATE,
    SEVERE,
    CRITICAL,
    EMERGENCY,
    SHUTDOWN,
    NOT_SUPPORTED
}

data class ThermalSpecs(
    val status: ThermalStatus
)

data class DisplayBasicSpecs(
    val widthPixels: Int,
    val heightPixels: Int,
    val densityDpi: Int,
    val densityScale: Float,
    val refreshRateHz: Float
)

data class DashboardTelemetry(
    val device: DeviceSpecs,
    val memory: MemorySpecs,
    val storage: StorageSpecs,
    val battery: BatterySpecs,
    val thermal: ThermalSpecs,
    val display: DisplayBasicSpecs,
    val timestampMillis: Long
)
