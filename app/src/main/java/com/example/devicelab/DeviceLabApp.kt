package com.example.devicelab

import android.app.Application
import com.example.devicelab.di.AppContainer
import com.example.devicelab.di.DefaultAppContainer

class DeviceLabApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
