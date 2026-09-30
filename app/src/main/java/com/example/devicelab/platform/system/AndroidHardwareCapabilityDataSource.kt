package com.example.devicelab.platform.system

import android.content.Context
import android.content.pm.PackageManager
import com.example.devicelab.domain.model.CapabilityStatus
import com.example.devicelab.domain.model.HardwareCapability
import com.example.devicelab.domain.repository.DisplayDataSource
import com.example.devicelab.domain.repository.HardwareCapabilityDataSource

class AndroidHardwareCapabilityDataSource(
    private val context: Context,
    private val displayDataSource: DisplayDataSource
) : HardwareCapabilityDataSource {

    override fun getHardwareCapabilities(): List<HardwareCapability> {
        val pm = context.packageManager
        val capabilities = mutableListOf<HardwareCapability>()

        fun checkFeature(key: String, name: String, category: String, featureName: String) {
            val supported = pm.hasSystemFeature(featureName)
            capabilities.add(
                HardwareCapability(
                    key = key,
                    name = name,
                    category = category,
                    status = if (supported) CapabilityStatus.SUPPORTED else CapabilityStatus.NOT_SUPPORTED
                )
            )
        }

        // Connectivity & Radios
        checkFeature("nfc", "NFC Radio", "Connectivity", PackageManager.FEATURE_NFC)
        checkFeature("bluetooth", "Bluetooth Controller", "Connectivity", PackageManager.FEATURE_BLUETOOTH)
        checkFeature("ble", "Bluetooth Low Energy", "Connectivity", PackageManager.FEATURE_BLUETOOTH_LE)
        checkFeature("wifi", "Wi-Fi Subsystem", "Connectivity", PackageManager.FEATURE_WIFI)
        checkFeature("wifi_direct", "Wi-Fi Direct (P2P)", "Connectivity", PackageManager.FEATURE_WIFI_DIRECT)
        checkFeature("gps", "GNSS / GPS Hardware", "Location", PackageManager.FEATURE_LOCATION_GPS)

        // Optics & Media
        checkFeature("camera", "Integrated Camera", "Optics", PackageManager.FEATURE_CAMERA_ANY)
        checkFeature("flash", "Camera Flash Unit", "Optics", PackageManager.FEATURE_CAMERA_FLASH)

        // Security & Biometrics
        checkFeature("fingerprint", "Biometric Fingerprint", "Biometrics", PackageManager.FEATURE_FINGERPRINT)

        // Motion & Environmental Silicon
        checkFeature("gyroscope", "MEMS Gyroscope", "Sensors", PackageManager.FEATURE_SENSOR_GYROSCOPE)
        checkFeature("accelerometer", "MEMS Accelerometer", "Sensors", PackageManager.FEATURE_SENSOR_ACCELEROMETER)
        checkFeature("compass", "Magnetometer / Compass", "Sensors", PackageManager.FEATURE_SENSOR_COMPASS)
        checkFeature("barometer", "Barometric Altimeter", "Sensors", PackageManager.FEATURE_SENSOR_BAROMETER)

        // Bus & Interfaces
        checkFeature("usb_host", "USB Host / OTG", "Interfaces", PackageManager.FEATURE_USB_HOST)

        // Display Silicon Capabilities
        val display = displayDataSource.getDisplaySnapshot()
        val hasHighRefresh = display.supportedRefreshRates.any { it >= 90.0f }
        capabilities.add(
            HardwareCapability(
                key = "high_refresh",
                name = "High Refresh Rate (${"%.0f".format(display.currentRefreshRateHz)} Hz)",
                category = "Display",
                status = if (hasHighRefresh) CapabilityStatus.SUPPORTED else CapabilityStatus.NOT_SUPPORTED,
                detail = "Supported modes: ${display.supportedRefreshRates.joinToString { "${it.toInt()}Hz" }}"
            )
        )

        capabilities.add(
            HardwareCapability(
                key = "hdr_display",
                name = "HDR Display Pipeline",
                category = "Display",
                status = if (display.isHdr) CapabilityStatus.SUPPORTED else CapabilityStatus.NOT_SUPPORTED,
                detail = if (display.isWideColorGamut) "Wide Color Gamut active" else null
            )
        )

        return capabilities.sortedWith(compareBy({ it.category }, { it.name }))
    }
}
