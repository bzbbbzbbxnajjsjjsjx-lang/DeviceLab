package com.example.devicelab.platform.system

import android.app.ActivityManager
import android.content.Context
import com.example.devicelab.domain.model.MemorySpecs
import com.example.devicelab.domain.repository.MemoryDataSource
import com.example.devicelab.domain.util.TelemetryMath

class AndroidMemoryDataSource(
    private val context: Context
) : MemoryDataSource {

    override fun getMemorySpecs(): MemorySpecs {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()

        if (activityManager != null) {
            activityManager.getMemoryInfo(memoryInfo)
            val total = memoryInfo.totalMem
            val available = memoryInfo.availMem
            val used = (total - available).coerceAtLeast(0L)
            val percentage = TelemetryMath.calculatePercentage(used, total)

            return MemorySpecs(
                totalBytes = total,
                availableBytes = available,
                usedBytes = used,
                usedPercentage = percentage,
                isLowMemory = memoryInfo.lowMemory
            )
        }

        return MemorySpecs(
            totalBytes = 0L,
            availableBytes = 0L,
            usedBytes = 0L,
            usedPercentage = 0f,
            isLowMemory = false
        )
    }
}
