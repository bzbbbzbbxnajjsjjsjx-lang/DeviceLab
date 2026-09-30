package com.example.devicelab.platform.system

import android.app.ActivityManager
import android.content.Context
import com.example.devicelab.domain.model.MemorySnapshot
import com.example.devicelab.domain.model.MemorySpecs
import com.example.devicelab.domain.repository.MemoryDataSource
import com.example.devicelab.domain.util.TelemetryMath

class AndroidMemoryDataSource(
    private val context: Context
) : MemoryDataSource {

    override fun getMemorySpecs(): MemorySpecs {
        val snapshot = getMemorySnapshot()
        return MemorySpecs(
            totalBytes = snapshot.totalBytes,
            availableBytes = snapshot.availableBytes,
            usedBytes = snapshot.usedBytes,
            usedPercentage = snapshot.usedPercentage,
            isLowMemory = snapshot.isLowMemory
        )
    }

    override fun getMemorySnapshot(): MemorySnapshot {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()

        val runtime = Runtime.getRuntime()
        val jvmTotal = runtime.totalMemory()
        val jvmFree = runtime.freeMemory()
        val jvmMax = runtime.maxMemory()

        if (activityManager != null) {
            activityManager.getMemoryInfo(memoryInfo)
            val total = memoryInfo.totalMem
            val available = memoryInfo.availMem
            val used = (total - available).coerceAtLeast(0L)
            val percentage = TelemetryMath.calculatePercentage(used, total)

            return MemorySnapshot(
                totalBytes = total,
                availableBytes = available,
                usedBytes = used,
                usedPercentage = percentage,
                isLowMemory = memoryInfo.lowMemory,
                lowMemoryThresholdBytes = memoryInfo.threshold,
                jvmTotalHeapBytes = jvmTotal,
                jvmFreeHeapBytes = jvmFree,
                jvmMaxHeapBytes = jvmMax
            )
        }

        return MemorySnapshot(
            totalBytes = 0L,
            availableBytes = 0L,
            usedBytes = 0L,
            usedPercentage = 0f,
            isLowMemory = false,
            lowMemoryThresholdBytes = 0L,
            jvmTotalHeapBytes = jvmTotal,
            jvmFreeHeapBytes = jvmFree,
            jvmMaxHeapBytes = jvmMax
        )
    }
}
