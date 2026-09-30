package com.example.devicelab.platform.system

import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.example.devicelab.domain.model.BatteryHealth
import com.example.devicelab.domain.model.ChargingStatus
import com.example.devicelab.domain.model.PluggedType
import com.example.devicelab.domain.model.ThermalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BatteryThermalMappingTest {

    @Test
    fun mapChargingStatus_allConstants_mappedCorrectly() {
        assertEquals(ChargingStatus.CHARGING, AndroidBatteryThermalDataSource.mapChargingStatus(BatteryManager.BATTERY_STATUS_CHARGING))
        assertEquals(ChargingStatus.DISCHARGING, AndroidBatteryThermalDataSource.mapChargingStatus(BatteryManager.BATTERY_STATUS_DISCHARGING))
        assertEquals(ChargingStatus.FULL, AndroidBatteryThermalDataSource.mapChargingStatus(BatteryManager.BATTERY_STATUS_FULL))
        assertEquals(ChargingStatus.NOT_CHARGING, AndroidBatteryThermalDataSource.mapChargingStatus(BatteryManager.BATTERY_STATUS_NOT_CHARGING))
        assertEquals(ChargingStatus.UNKNOWN, AndroidBatteryThermalDataSource.mapChargingStatus(BatteryManager.BATTERY_STATUS_UNKNOWN))
        assertEquals(ChargingStatus.UNKNOWN, AndroidBatteryThermalDataSource.mapChargingStatus(-99))
    }

    @Test
    fun mapPluggedType_allConstants_mappedCorrectly() {
        assertEquals(PluggedType.AC, AndroidBatteryThermalDataSource.mapPluggedType(BatteryManager.BATTERY_PLUGGED_AC))
        assertEquals(PluggedType.USB, AndroidBatteryThermalDataSource.mapPluggedType(BatteryManager.BATTERY_PLUGGED_USB))
        assertEquals(PluggedType.WIRELESS, AndroidBatteryThermalDataSource.mapPluggedType(BatteryManager.BATTERY_PLUGGED_WIRELESS))
        assertEquals(PluggedType.UNPLUGGED, AndroidBatteryThermalDataSource.mapPluggedType(0))
        assertEquals(PluggedType.UNKNOWN, AndroidBatteryThermalDataSource.mapPluggedType(-1))
    }

    @Test
    fun mapBatteryHealth_allConstants_mappedCorrectly() {
        assertEquals(BatteryHealth.GOOD, AndroidBatteryThermalDataSource.mapBatteryHealth(BatteryManager.BATTERY_HEALTH_GOOD))
        assertEquals(BatteryHealth.OVERHEAT, AndroidBatteryThermalDataSource.mapBatteryHealth(BatteryManager.BATTERY_HEALTH_OVERHEAT))
        assertEquals(BatteryHealth.DEAD, AndroidBatteryThermalDataSource.mapBatteryHealth(BatteryManager.BATTERY_HEALTH_DEAD))
        assertEquals(BatteryHealth.OVER_VOLTAGE, AndroidBatteryThermalDataSource.mapBatteryHealth(BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE))
        assertEquals(BatteryHealth.UNSPECIFIED_FAILURE, AndroidBatteryThermalDataSource.mapBatteryHealth(BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE))
        assertEquals(BatteryHealth.COLD, AndroidBatteryThermalDataSource.mapBatteryHealth(BatteryManager.BATTERY_HEALTH_COLD))
        assertEquals(BatteryHealth.UNKNOWN, AndroidBatteryThermalDataSource.mapBatteryHealth(BatteryManager.BATTERY_HEALTH_UNKNOWN))
        assertEquals(BatteryHealth.UNKNOWN, AndroidBatteryThermalDataSource.mapBatteryHealth(-1))
    }

    @Test
    fun parseBatteryTemperature_rawTenthsCelsius_convertedCorrectly() {
        assertEquals(29.5f, AndroidBatteryThermalDataSource.parseBatteryTemperature(295) ?: 0f, 0.001f)
        assertEquals(-2.0f, AndroidBatteryThermalDataSource.parseBatteryTemperature(-20) ?: 0f, 0.001f)
        assertNull(AndroidBatteryThermalDataSource.parseBatteryTemperature(-10000))
    }

    @Test
    fun parseBatteryVoltage_millivolts_parsedCorrectly() {
        assertEquals(4150, AndroidBatteryThermalDataSource.parseBatteryVoltage(4150))
        assertNull(AndroidBatteryThermalDataSource.parseBatteryVoltage(0))
        assertNull(AndroidBatteryThermalDataSource.parseBatteryVoltage(-1))
    }

    @Test
    fun mapThermalStatus_apiQAndAbove_mapsCorrectly() {
        val qSdk = Build.VERSION_CODES.Q
        assertEquals(ThermalStatus.NONE, AndroidBatteryThermalDataSource.mapThermalStatus(PowerManager.THERMAL_STATUS_NONE, qSdk))
        assertEquals(ThermalStatus.LIGHT, AndroidBatteryThermalDataSource.mapThermalStatus(PowerManager.THERMAL_STATUS_LIGHT, qSdk))
        assertEquals(ThermalStatus.MODERATE, AndroidBatteryThermalDataSource.mapThermalStatus(PowerManager.THERMAL_STATUS_MODERATE, qSdk))
        assertEquals(ThermalStatus.SEVERE, AndroidBatteryThermalDataSource.mapThermalStatus(PowerManager.THERMAL_STATUS_SEVERE, qSdk))
        assertEquals(ThermalStatus.CRITICAL, AndroidBatteryThermalDataSource.mapThermalStatus(PowerManager.THERMAL_STATUS_CRITICAL, qSdk))
        assertEquals(ThermalStatus.EMERGENCY, AndroidBatteryThermalDataSource.mapThermalStatus(PowerManager.THERMAL_STATUS_EMERGENCY, qSdk))
        assertEquals(ThermalStatus.SHUTDOWN, AndroidBatteryThermalDataSource.mapThermalStatus(PowerManager.THERMAL_STATUS_SHUTDOWN, qSdk))
        assertEquals(ThermalStatus.NONE, AndroidBatteryThermalDataSource.mapThermalStatus(999, qSdk))
    }

    @Test
    fun mapThermalStatus_belowApiQ_returnsNotSupported() {
        val preQSdk = Build.VERSION_CODES.P
        assertEquals(ThermalStatus.NOT_SUPPORTED, AndroidBatteryThermalDataSource.mapThermalStatus(PowerManager.THERMAL_STATUS_NONE, preQSdk))
        assertEquals(ThermalStatus.NOT_SUPPORTED, AndroidBatteryThermalDataSource.mapThermalStatus(PowerManager.THERMAL_STATUS_SEVERE, preQSdk))
    }
}
