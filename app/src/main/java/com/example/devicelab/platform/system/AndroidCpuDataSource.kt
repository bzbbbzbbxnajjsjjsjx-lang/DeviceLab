package com.example.devicelab.platform.system

import android.os.Build
import com.example.devicelab.domain.model.CpuSnapshot
import com.example.devicelab.domain.repository.CpuDataSource
import java.io.File

class AndroidCpuDataSource : CpuDataSource {

    override fun getCpuSnapshot(): CpuSnapshot {
        val coreCount = Runtime.getRuntime().availableProcessors()
        val supportedAbis = Build.SUPPORTED_ABIS?.toList() ?: emptyList()
        val architecture = supportedAbis.firstOrNull() ?: "Unknown"

        val perCoreFrequencies = mutableListOf<Long?>()
        var hasAtLeastOneFreq = false

        for (i in 0 until coreCount) {
            val freq = readSysLong("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
                ?: readSysLong("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_cur_freq")
            if (freq != null) {
                hasAtLeastOneFreq = true
            }
            perCoreFrequencies.add(freq)
        }

        val governor = readSysString("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
        val scalingCurFreq = perCoreFrequencies.firstOrNull { it != null }

        return CpuSnapshot(
            coreCount = coreCount,
            architecture = architecture,
            supportedAbis = supportedAbis,
            perCoreFrequenciesKHz = perCoreFrequencies,
            governor = governor,
            scalingCurFreqKHz = scalingCurFreq,
            isFrequencyAvailable = hasAtLeastOneFreq
        )
    }

    private fun readSysLong(path: String): Long? {
        return try {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                file.readText().trim().toLongOrNull()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun readSysString(path: String): String? {
        return try {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                file.readText().trim().ifBlank { null }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
