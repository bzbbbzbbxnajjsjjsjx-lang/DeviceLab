package com.example.devicelab.platform.system

import android.os.Environment
import android.os.StatFs
import com.example.devicelab.domain.model.StorageSpecs
import com.example.devicelab.domain.repository.StorageDataSource
import com.example.devicelab.domain.util.TelemetryMath

class AndroidStorageDataSource : StorageDataSource {

    override fun getStorageSpecs(): StorageSpecs {
        return try {
            val path = Environment.getDataDirectory().path
            val statFs = StatFs(path)

            val blockSize = statFs.blockSizeLong
            val totalBlocks = statFs.blockCountLong
            val availableBlocks = statFs.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val availableBytes = availableBlocks * blockSize
            val usedBytes = (totalBytes - availableBytes).coerceAtLeast(0L)
            val usedPercentage = TelemetryMath.calculatePercentage(usedBytes, totalBytes)

            StorageSpecs(
                totalBytes = totalBytes,
                availableBytes = availableBytes,
                usedBytes = usedBytes,
                usedPercentage = usedPercentage
            )
        } catch (_: Exception) {
            StorageSpecs(
                totalBytes = 0L,
                availableBytes = 0L,
                usedBytes = 0L,
                usedPercentage = 0f
            )
        }
    }
}
