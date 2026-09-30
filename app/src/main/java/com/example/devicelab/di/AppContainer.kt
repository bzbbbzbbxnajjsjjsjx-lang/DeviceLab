package com.example.devicelab.di

import android.content.Context
import com.example.devicelab.domain.repository.DashboardRepository
import com.example.devicelab.platform.repository.DefaultDashboardRepository
import com.example.devicelab.platform.system.AndroidBatteryThermalDataSource
import com.example.devicelab.platform.system.AndroidDeviceSpecsProvider
import com.example.devicelab.platform.system.AndroidDisplayDataSource
import com.example.devicelab.platform.system.AndroidMemoryDataSource
import com.example.devicelab.platform.system.AndroidStorageDataSource

interface AppContainer {
    val dashboardRepository: DashboardRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val dashboardRepository: DashboardRepository by lazy {
        DefaultDashboardRepository(
            deviceSpecsProvider = AndroidDeviceSpecsProvider(),
            memoryDataSource = AndroidMemoryDataSource(context),
            storageDataSource = AndroidStorageDataSource(),
            displayDataSource = AndroidDisplayDataSource(context),
            batteryThermalDataSource = AndroidBatteryThermalDataSource(context)
        )
    }
}
