package com.example.devicelab.domain.model

/**
 * Immutable domain models for device hardware diagnostics.
 * NOTE: This package must NEVER import any `android.*` APIs to guarantee 100% JVM testability.
 */

// ==========================================
// 1. Device Identity
// ==========================================
data class DeviceIdentity(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val device: String,
    val product: String,
    val board: String,
    val hardware: String,
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val supportedAbis: List<String>,
    val buildFingerprint: String,
    val kernelVersion: String,
    val processorCount: Int
)

// Legacy DeviceSpecs for backward-compatibility with Phase 1.6
data class DeviceSpecs(
    val manufacturer: String,
    val model: String,
    val device: String,
    val androidVersion: String,
    val apiLevel: Int,
    val supportedAbis: List<String>,
    val processorCount: Int
)

// ==========================================
// 2. CPU Intelligence
// ==========================================
data class CpuSnapshot(
    val coreCount: Int,
    val architecture: String,
    val supportedAbis: List<String>,
    val perCoreFrequenciesKHz: List<Long?>,
    val governor: String?,
    val scalingCurFreqKHz: Long?,
    val isFrequencyAvailable: Boolean
)

// ==========================================
// 3. Memory Intelligence
// ==========================================
data class MemorySnapshot(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usedPercentage: Float,
    val isLowMemory: Boolean,
    val lowMemoryThresholdBytes: Long,
    val jvmTotalHeapBytes: Long,
    val jvmFreeHeapBytes: Long,
    val jvmMaxHeapBytes: Long
)

// Legacy MemorySpecs for Phase 1.6 composables
data class MemorySpecs(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usedPercentage: Float,
    val isLowMemory: Boolean
)

// ==========================================
// 4. Storage Intelligence
// ==========================================
data class StorageSnapshot(
    val internalTotalBytes: Long,
    val internalAvailableBytes: Long,
    val internalUsedBytes: Long,
    val internalUsedPercentage: Float,
    val externalStorageAvailable: Boolean,
    val externalTotalBytes: Long?,
    val externalAvailableBytes: Long?
)

// Legacy StorageSpecs for Phase 1.6 composables
data class StorageSpecs(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usedPercentage: Float
)

// ==========================================
// 5. Battery Intelligence
// ==========================================
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

data class BatterySnapshot(
    val percentage: Int,
    val chargingStatus: ChargingStatus,
    val pluggedType: PluggedType,
    val health: BatteryHealth,
    val temperatureCelsius: Float?,
    val voltageMillivolts: Int?,
    val currentMicroamps: Int?,
    val technology: String?,
    val capacityMah: Double?
)

// Legacy BatterySpecs for Phase 1.6 composables
data class BatterySpecs(
    val percentage: Int,
    val chargingStatus: ChargingStatus,
    val pluggedType: PluggedType,
    val health: BatteryHealth,
    val temperatureCelsius: Float?,
    val voltageMillivolts: Int?
)

// ==========================================
// 6. Thermal Intelligence
// ==========================================
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

data class ThermalSnapshot(
    val status: ThermalStatus,
    val statusDescription: String,
    val isThrottling: Boolean
)

// Legacy ThermalSpecs for Phase 1.6 composables
data class ThermalSpecs(
    val status: ThermalStatus
)

// ==========================================
// 7. Display Intelligence
// ==========================================
data class DisplaySnapshot(
    val widthPixels: Int,
    val heightPixels: Int,
    val densityDpi: Int,
    val densityScale: Float,
    val currentRefreshRateHz: Float,
    val supportedRefreshRates: List<Float>,
    val isHdr: Boolean,
    val isWideColorGamut: Boolean
)

// Legacy DisplayBasicSpecs for Phase 1.6 composables
data class DisplayBasicSpecs(
    val widthPixels: Int,
    val heightPixels: Int,
    val densityDpi: Int,
    val densityScale: Float,
    val refreshRateHz: Float
)

// ==========================================
// 8. Network Diagnostics
// ==========================================
enum class NetworkTransport {
    WIFI,
    CELLULAR,
    ETHERNET,
    VPN,
    NONE,
    UNKNOWN
}

data class NetworkSnapshot(
    val transport: NetworkTransport,
    val isConnected: Boolean,
    val isValidated: Boolean,
    val isMetered: Boolean,
    val downlinkBandwidthKbps: Int?,
    val uplinkBandwidthKbps: Int?
)

// ==========================================
// 9. Sensor Inventory & Telemetry
// ==========================================
enum class SensorCategory {
    MOTION,
    POSITION,
    ENVIRONMENT,
    BIOMETRIC,
    OTHER
}

data class SensorDescriptor(
    val type: Int,
    val name: String,
    val vendor: String,
    val typeString: String,
    val category: SensorCategory,
    val version: Int,
    val resolution: Float,
    val maxRange: Float,
    val powerMa: Float,
    val reportingMode: String
)

data class SensorReading(
    val sensorType: Int,
    val sensorName: String,
    val values: List<Float>,
    val accuracy: Int,
    val timestampNanos: Long
)

// ==========================================
// 10. Hardware Capabilities Matrix
// ==========================================
enum class CapabilityStatus {
    SUPPORTED,
    NOT_SUPPORTED,
    UNAVAILABLE
}

data class HardwareCapability(
    val key: String,
    val name: String,
    val category: String,
    val status: CapabilityStatus,
    val detail: String? = null
)

// ==========================================
// 11. Diagnostics Engine
// ==========================================
enum class DiagnosticSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class DiagnosticIssue(
    val id: String,
    val title: String,
    val description: String,
    val severity: DiagnosticSeverity,
    val subsystem: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

// ==========================================
// 12. Complete Hardware Intelligence Master Model
// ==========================================
data class HardwareIntelligenceSnapshot(
    val identity: DeviceIdentity,
    val cpu: CpuSnapshot,
    val memory: MemorySnapshot,
    val storage: StorageSnapshot,
    val battery: BatterySnapshot,
    val thermal: ThermalSnapshot,
    val display: DisplaySnapshot,
    val network: NetworkSnapshot,
    val sensors: List<SensorDescriptor>,
    val capabilities: List<HardwareCapability>,
    val diagnostics: List<DiagnosticIssue>,
    val timestampMillis: Long
) {
    // Convenience adapter to Phase 1.6 DashboardTelemetry
    fun toLegacyTelemetry(): DashboardTelemetry = DashboardTelemetry(
        device = DeviceSpecs(
            manufacturer = identity.manufacturer,
            model = identity.model,
            device = identity.device,
            androidVersion = identity.androidVersion,
            apiLevel = identity.apiLevel,
            supportedAbis = identity.supportedAbis,
            processorCount = identity.processorCount
        ),
        memory = MemorySpecs(
            totalBytes = memory.totalBytes,
            availableBytes = memory.availableBytes,
            usedBytes = memory.usedBytes,
            usedPercentage = memory.usedPercentage,
            isLowMemory = memory.isLowMemory
        ),
        storage = StorageSpecs(
            totalBytes = storage.internalTotalBytes,
            availableBytes = storage.internalAvailableBytes,
            usedBytes = storage.internalUsedBytes,
            usedPercentage = storage.internalUsedPercentage
        ),
        battery = BatterySpecs(
            percentage = battery.percentage,
            chargingStatus = battery.chargingStatus,
            pluggedType = battery.pluggedType,
            health = battery.health,
            temperatureCelsius = battery.temperatureCelsius,
            voltageMillivolts = battery.voltageMillivolts
        ),
        thermal = ThermalSpecs(
            status = thermal.status
        ),
        display = DisplayBasicSpecs(
            widthPixels = display.widthPixels,
            heightPixels = display.heightPixels,
            densityDpi = display.densityDpi,
            densityScale = display.densityScale,
            refreshRateHz = display.currentRefreshRateHz
        ),
        timestampMillis = timestampMillis
    )
}

// Legacy DashboardTelemetry
data class DashboardTelemetry(
    val device: DeviceSpecs,
    val memory: MemorySpecs,
    val storage: StorageSpecs,
    val battery: BatterySpecs,
    val thermal: ThermalSpecs,
    val display: DisplayBasicSpecs,
    val timestampMillis: Long
) {
    fun toHardwareIntelligence(): HardwareIntelligenceSnapshot = HardwareIntelligenceSnapshot(
        identity = DeviceIdentity(
            manufacturer = device.manufacturer,
            brand = device.manufacturer.lowercase(),
            model = device.model,
            device = device.device,
            product = device.device,
            board = "unknown",
            hardware = "unknown",
            androidVersion = device.androidVersion,
            apiLevel = device.apiLevel,
            securityPatch = "unknown",
            supportedAbis = device.supportedAbis,
            buildFingerprint = "unknown",
            kernelVersion = "unknown",
            processorCount = device.processorCount
        ),
        cpu = CpuSnapshot(
            coreCount = device.processorCount,
            architecture = device.supportedAbis.firstOrNull() ?: "unknown",
            supportedAbis = device.supportedAbis,
            perCoreFrequenciesKHz = emptyList(),
            governor = null,
            scalingCurFreqKHz = null,
            isFrequencyAvailable = false
        ),
        memory = MemorySnapshot(
            totalBytes = memory.totalBytes,
            availableBytes = memory.availableBytes,
            usedBytes = memory.usedBytes,
            usedPercentage = memory.usedPercentage,
            isLowMemory = memory.isLowMemory,
            lowMemoryThresholdBytes = 0L,
            jvmTotalHeapBytes = 0L,
            jvmFreeHeapBytes = 0L,
            jvmMaxHeapBytes = 0L
        ),
        storage = StorageSnapshot(
            internalTotalBytes = storage.totalBytes,
            internalAvailableBytes = storage.availableBytes,
            internalUsedBytes = storage.usedBytes,
            internalUsedPercentage = storage.usedPercentage,
            externalStorageAvailable = false,
            externalTotalBytes = null,
            externalAvailableBytes = null
        ),
        battery = BatterySnapshot(
            percentage = battery.percentage,
            chargingStatus = battery.chargingStatus,
            pluggedType = battery.pluggedType,
            health = battery.health,
            temperatureCelsius = battery.temperatureCelsius,
            voltageMillivolts = battery.voltageMillivolts,
            currentMicroamps = null,
            technology = null,
            capacityMah = null
        ),
        thermal = ThermalSnapshot(
            status = thermal.status,
            statusDescription = thermal.status.name,
            isThrottling = false
        ),
        display = DisplaySnapshot(
            widthPixels = display.widthPixels,
            heightPixels = display.heightPixels,
            densityDpi = display.densityDpi,
            densityScale = display.densityScale,
            currentRefreshRateHz = display.refreshRateHz,
            supportedRefreshRates = listOf(display.refreshRateHz),
            isHdr = false,
            isWideColorGamut = false
        ),
        network = NetworkSnapshot(
            transport = NetworkTransport.NONE,
            isConnected = false,
            isValidated = false,
            isMetered = false,
            downlinkBandwidthKbps = null,
            uplinkBandwidthKbps = null
        ),
        sensors = emptyList(),
        capabilities = emptyList(),
        diagnostics = emptyList(),
        timestampMillis = timestampMillis
    )
}
