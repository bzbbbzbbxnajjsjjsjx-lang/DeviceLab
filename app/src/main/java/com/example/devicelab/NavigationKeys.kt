package com.example.devicelab

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey

@Serializable data object Dashboard : NavKey

@Serializable data object CpuDetail : NavKey

@Serializable data object MemoryDetail : NavKey

@Serializable data object BatteryDetail : NavKey

@Serializable data object StorageDetail : NavKey

@Serializable data object DisplayDetail : NavKey

@Serializable data object SensorsDetail : NavKey

@Serializable data object NetworkDetail : NavKey

@Serializable data object CapabilitiesDetail : NavKey

@Serializable data object DiagnosticsDetail : NavKey
