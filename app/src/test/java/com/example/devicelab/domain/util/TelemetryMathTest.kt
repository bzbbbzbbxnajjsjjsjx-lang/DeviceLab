package com.example.devicelab.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TelemetryMathTest {

    @Test
    fun calculatePercentage_standardValues_calculatesAccurately() {
        assertEquals(0.0f, TelemetryMath.calculatePercentage(0L, 100L), 0.001f)
        assertEquals(50.0f, TelemetryMath.calculatePercentage(50L, 100L), 0.001f)
        assertEquals(100.0f, TelemetryMath.calculatePercentage(100L, 100L), 0.001f)
        assertEquals(33.3f, TelemetryMath.calculatePercentage(1L, 3L), 0.001f)
    }

    @Test
    fun calculatePercentage_edgeCases_returnsZeroOrClamped() {
        assertEquals(0.0f, TelemetryMath.calculatePercentage(50L, 0L), 0.001f)
        assertEquals(0.0f, TelemetryMath.calculatePercentage(50L, -10L), 0.001f)
        assertEquals(0.0f, TelemetryMath.calculatePercentage(-5L, 100L), 0.001f)
        assertEquals(100.0f, TelemetryMath.calculatePercentage(150L, 100L), 0.001f)
    }

    @Test
    fun formatBytes_binaryUnits_formatsCorrectly() {
        assertEquals("0 B", TelemetryMath.formatBytes(0L))
        assertEquals("0 B", TelemetryMath.formatBytes(-50L))
        assertEquals("500 B", TelemetryMath.formatBytes(500L))
        assertEquals("1.00 KB", TelemetryMath.formatBytes(1024L))
        assertEquals("1.50 KB", TelemetryMath.formatBytes(1536L))
        assertEquals("1.00 MB", TelemetryMath.formatBytes(1024L * 1024L))
        assertEquals("2.50 GB", TelemetryMath.formatBytes((2.5 * 1024 * 1024 * 1024).toLong()))
        assertEquals("1.00 TB", TelemetryMath.formatBytes(1024L * 1024L * 1024L * 1024L))
    }

    @Test
    fun formatPercentage_formatsWithOneDecimal() {
        assertEquals("0.0%", TelemetryMath.formatPercentage(0.0f))
        assertEquals("54.2%", TelemetryMath.formatPercentage(54.23f))
        assertEquals("100.0%", TelemetryMath.formatPercentage(100.0f))
    }

    @Test
    fun formatHz_formatsWithOneDecimal() {
        assertEquals("60.0 Hz", TelemetryMath.formatHz(60.0f))
        assertEquals("120.0 Hz", TelemetryMath.formatHz(120.0f))
        assertEquals("90.0 Hz", TelemetryMath.formatHz(89.999f))
    }

    @Test
    fun formatCelsius_validAndNull() {
        assertEquals("N/A", TelemetryMath.formatCelsius(null))
        assertEquals("28.5 °C", TelemetryMath.formatCelsius(28.5f))
        assertEquals("-5.0 °C", TelemetryMath.formatCelsius(-5.0f))
    }

    @Test
    fun formatVoltage_validAndNull() {
        assertEquals("N/A", TelemetryMath.formatVoltage(null))
        assertEquals("4,150 mV (4.15 V)", TelemetryMath.formatVoltage(4150))
        assertEquals("3,800 mV (3.80 V)", TelemetryMath.formatVoltage(3800))
    }

    @Test
    fun normalizeBatteryLevel_standardAndEdgeCases() {
        assertEquals(85, TelemetryMath.normalizeBatteryLevel(85, 100))
        assertEquals(84, TelemetryMath.normalizeBatteryLevel(42, 50))
        assertEquals(100, TelemetryMath.normalizeBatteryLevel(100, 100))
        assertEquals(0, TelemetryMath.normalizeBatteryLevel(0, 100))
        assertEquals(0, TelemetryMath.normalizeBatteryLevel(-1, 100))
        assertEquals(0, TelemetryMath.normalizeBatteryLevel(50, 0))
        assertEquals(0, TelemetryMath.normalizeBatteryLevel(50, -10))
        assertEquals(100, TelemetryMath.normalizeBatteryLevel(120, 100))
    }
}
