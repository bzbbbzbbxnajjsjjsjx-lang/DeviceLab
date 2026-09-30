package com.example.devicelab.platform.system

import android.os.Build
import com.example.devicelab.domain.model.DeviceIdentity
import com.example.devicelab.domain.model.DeviceSpecs
import com.example.devicelab.domain.repository.DeviceSpecsProvider

class AndroidDeviceSpecsProvider : DeviceSpecsProvider {

    override fun getDeviceSpecs(): DeviceSpecs {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        val device = Build.DEVICE
        val androidVersion = Build.VERSION.RELEASE
        val apiLevel = Build.VERSION.SDK_INT
        val supportedAbis = Build.SUPPORTED_ABIS?.toList() ?: emptyList()
        val processorCount = Runtime.getRuntime().availableProcessors()

        return DeviceSpecs(
            manufacturer = manufacturer,
            model = model,
            device = device,
            androidVersion = androidVersion,
            apiLevel = apiLevel,
            supportedAbis = supportedAbis,
            processorCount = processorCount
        )
    }

    override fun getDeviceIdentity(): DeviceIdentity {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val brand = Build.BRAND.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        val device = Build.DEVICE
        val product = Build.PRODUCT
        val board = Build.BOARD
        val hardware = Build.HARDWARE
        val androidVersion = Build.VERSION.RELEASE
        val apiLevel = Build.VERSION.SDK_INT
        val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH ?: "Unknown"
        } else {
            "Unavailable"
        }
        val supportedAbis = Build.SUPPORTED_ABIS?.toList() ?: emptyList()
        val buildFingerprint = Build.FINGERPRINT ?: "Unknown"
        val kernelVersion = try {
            System.getProperty("os.version") ?: "Linux"
        } catch (_: Exception) {
            "Linux"
        }
        val processorCount = Runtime.getRuntime().availableProcessors()

        return DeviceIdentity(
            manufacturer = manufacturer,
            brand = brand,
            model = model,
            device = device,
            product = product,
            board = board,
            hardware = hardware,
            androidVersion = androidVersion,
            apiLevel = apiLevel,
            securityPatch = securityPatch,
            supportedAbis = supportedAbis,
            buildFingerprint = buildFingerprint,
            kernelVersion = kernelVersion,
            processorCount = processorCount
        )
    }
}
