package com.example.devicelab.domain.util

import java.util.Locale
import kotlin.math.roundToInt

/**
 * Pure Kotlin mathematical functions and formatters for hardware telemetry.
 * Safe to test on host JVM without mocking Android framework classes.
 */
object TelemetryMath {

    /**
     * Calculates the used percentage in [0.0f, 100.0f].
     * Returns 0.0f safely if [total] <= 0 or [used] <= 0.
     */
    fun calculatePercentage(used: Long, total: Long): Float {
        if (total <= 0L || used <= 0L) return 0.0f
        val clampedUsed = used.coerceAtMost(total)
        val ratio = (clampedUsed.toDouble() / total.toDouble()) * 100.0
        return ((ratio * 10.0).roundToInt() / 10.0).toFloat().coerceIn(0.0f, 100.0f)
    }

    /**
     * Formats raw bytes into human-readable binary prefix units (B, KB, MB, GB, TB).
     */
    fun formatBytes(bytes: Long, decimals: Int = 2): String {
        if (bytes < 0L) return "0 B"
        if (bytes < 1024L) return "$bytes B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
        var value = bytes.toDouble()
        var unitIndex = 0

        while (value >= 1024.0 && unitIndex < units.lastIndex) {
            value /= 1024.0
            unitIndex++
        }

        return String.format(Locale.US, "%.${decimals}f %s", value, units[unitIndex])
    }

    /**
     * Formats a percentage value (e.g. 68.4%).
     */
    fun formatPercentage(percent: Float): String {
        val clamped = percent.coerceIn(0.0f, 100.0f)
        return String.format(Locale.US, "%.1f%%", clamped)
    }

    /**
     * Formats refresh rate in Hertz (e.g. 120.0 Hz).
     */
    fun formatHz(rate: Float): String {
        if (rate <= 0f) return "Unknown"
        return String.format(Locale.US, "%.1f Hz", rate)
    }

    /**
     * Formats battery temperature in degrees Celsius (e.g. "31.4 °C").
     */
    fun formatCelsius(temp: Float?): String {
        if (temp == null) return "N/A"
        return String.format(Locale.US, "%.1f °C", temp)
    }

    /**
     * Formats battery voltage in Volts (e.g. "4.12 V").
     */
    fun formatVoltage(voltageMv: Int?): String {
        if (voltageMv == null || voltageMv <= 0) return "N/A"
        val volts = voltageMv / 1000.0
        return String.format(Locale.US, "%,d mV (%.2f V)", voltageMv, volts)
    }

    /**
     * Normalizes raw battery level and scale into an integer percentage in [0, 100].
     */
    fun normalizeBatteryLevel(rawLevel: Int, rawScale: Int): Int {
        if (rawScale <= 0 || rawLevel <= 0) return 0
        val clampedLevel = rawLevel.coerceIn(0, rawScale)
        return ((clampedLevel.toDouble() / rawScale.toDouble()) * 100.0).roundToInt().coerceIn(0, 100)
    }
}
