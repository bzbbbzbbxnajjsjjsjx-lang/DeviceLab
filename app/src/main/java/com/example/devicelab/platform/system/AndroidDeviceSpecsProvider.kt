package com.example.devicelab.platform.system

import android.os.Build
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
}
