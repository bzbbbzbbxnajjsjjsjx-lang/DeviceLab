package com.example.devicelab.platform.system

import android.os.Environment
import android.os.StatFs
import com.example.devicelab.domain.model.StorageSnapshot
import com.example.devicelab.domain.model.StorageSpecs
import com.example.devicelab.domain.repository.StorageDataSource
import com.example.devicelab.domain.util.TelemetryMath

class AndroidStorageDataSource : StorageDataSource {

    override fun getStorageSpecs(): StorageSpecs {
        val snapshot = getStorageSnapshot()
        return StorageSpecs(
            totalBytes = snapshot.internalTotalBytes,
            availableBytes = snapshot.internalAvailableBytes,
            usedBytes = snapshot.internalUsedBytes,
            usedPercentage = snapshot.internalUsedPercentage
        )
    }

    override fun getStorageSnapshot(): StorageSnapshot {
        val (intTotal, intAvail, intUsed, intPct) = try {
            val path = Environment.getDataDirectory().path
            val statFs = StatFs(path)
            val blockSize = statFs.blockSizeLong
            val totalBlocks = statFs.blockCountLong
            val availableBlocks = statFs.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val availableBytes = availableBlocks * blockSize
            val usedBytes = (totalBytes - availableBytes).coerceAtLeast(0L)
            val usedPercentage = TelemetryMath.calculatePercentage(usedBytes, totalBytes)
            Tuple4(totalBytes, availableBytes, usedBytes, usedPercentage)
        } catch (_: Exception) {
            Tuple4(0L, 0L, 0L, 0f)
        }

        var extAvailable = false
        var extTotal: Long? = null
        var extAvail: Long? = null

        try {
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val extPath = Environment.getExternalStorageDirectory().path
                val statExt = StatFs(extPath)
                val blockSize = statExt.blockSizeLong
                extTotal = statExt.blockCountLong * blockSize
                extAvail = statExt.availableBlocksLong * blockSize
                extAvailable = true
            }
        } catch (_: Exception) {
            extAvailable = false
        }

        return StorageSnapshot(
            internalTotalBytes = intTotal,
            internalAvailableBytes = intAvail,
            internalUsedBytes = intUsed,
            internalUsedPercentage = intPct,
            externalStorageAvailable = extAvailable,
            externalTotalBytes = extTotal,
            externalAvailableBytes = extAvail
        )
    }

    private data class Tuple4(
        val totalBytes: Long,
        val availableBytes: Long,
        val usedBytes: Long,
        val usedPercentage: Float
    )
}
