package com.example.devicelab.platform.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.example.devicelab.domain.model.BatteryHealth
import com.example.devicelab.domain.model.BatterySnapshot
import com.example.devicelab.domain.model.BatterySpecs
import com.example.devicelab.domain.model.ChargingStatus
import com.example.devicelab.domain.model.PluggedType
import com.example.devicelab.domain.model.ThermalSnapshot
import com.example.devicelab.domain.model.ThermalSpecs
import com.example.devicelab.domain.model.ThermalStatus
import com.example.devicelab.domain.repository.BatteryThermalDataSource
import com.example.devicelab.domain.util.TelemetryMath
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.Executor

class AndroidBatteryThermalDataSource(
    private val context: Context
) : BatteryThermalDataSource {

    override fun getCurrentBatteryAndThermal(): Pair<BatterySpecs, ThermalSpecs> {
        val battery = parseBatteryIntent(getStickyBatteryIntent())
        val thermal = queryCurrentThermalSpecs()
        return battery to thermal
    }

    override fun getBatterySnapshot(): BatterySnapshot {
        val intent = getStickyBatteryIntent()
        val specs = parseBatteryIntent(intent)
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

        val currentMicroamps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && batteryManager != null) {
            val cur = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            if (cur != Int.MIN_VALUE) cur else null
        } else {
            null
        }

        val technology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)

        return BatterySnapshot(
            percentage = specs.percentage,
            chargingStatus = specs.chargingStatus,
            pluggedType = specs.pluggedType,
            health = specs.health,
            temperatureCelsius = specs.temperatureCelsius,
            voltageMillivolts = specs.voltageMillivolts,
            currentMicroamps = currentMicroamps,
            technology = technology,
            capacityMah = null
        )
    }

    override fun getThermalSnapshot(): ThermalSnapshot {
        val thermalSpecs = queryCurrentThermalSpecs()
        val isThrottling = thermalSpecs.status in listOf(
            ThermalStatus.SEVERE,
            ThermalStatus.CRITICAL,
            ThermalStatus.EMERGENCY,
            ThermalStatus.SHUTDOWN
        )
        val desc = when (thermalSpecs.status) {
            ThermalStatus.NONE -> "Silicon within nominal operating limits"
            ThermalStatus.LIGHT -> "Slight thermal elevation, no throttling"
            ThermalStatus.MODERATE -> "Moderate temperature, approaching threshold"
            ThermalStatus.SEVERE -> "Active thermal throttling, clock rates capped"
            ThermalStatus.CRITICAL -> "Critical thermal condition, heavy throttling"
            ThermalStatus.EMERGENCY -> "Emergency state, hardware protection engaged"
            ThermalStatus.SHUTDOWN -> "Device shutdown imminent due to heat"
            ThermalStatus.NOT_SUPPORTED -> "Thermal status API not supported on this device"
        }
        return ThermalSnapshot(
            status = thermalSpecs.status,
            statusDescription = desc,
            isThrottling = isThrottling
        )
    }

    override fun observeBatteryAndThermal(): Flow<Pair<BatterySpecs, ThermalSpecs>> = callbackFlow {
        var currentBatterySpecs = parseBatteryIntent(getStickyBatteryIntent())
        var currentThermalSpecs = queryCurrentThermalSpecs()

        trySend(currentBatterySpecs to currentThermalSpecs)

        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                currentBatterySpecs = parseBatteryIntent(intent)
                trySend(currentBatterySpecs to currentThermalSpecs)
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)

        var thermalListener: Any? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager != null) {
                val listener = PowerManager.OnThermalStatusChangedListener { status ->
                    currentThermalSpecs = ThermalSpecs(mapThermalStatus(status))
                    trySend(currentBatterySpecs to currentThermalSpecs)
                }
                val executor = Executor { command -> command.run() }
                powerManager.addThermalStatusListener(executor, listener)
                thermalListener = listener
            }
        }

        awaitClose {
            try {
                context.unregisterReceiver(batteryReceiver)
            } catch (_: Exception) {}

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && thermalListener != null) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                powerManager?.removeThermalStatusListener(thermalListener as PowerManager.OnThermalStatusChangedListener)
            }
        }
    }

    private fun getStickyBatteryIntent(): Intent? {
        return context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    fun parseBatteryIntent(intent: Intent?): BatterySpecs {
        if (intent == null) {
            return BatterySpecs(
                percentage = 0,
                chargingStatus = ChargingStatus.UNKNOWN,
                pluggedType = PluggedType.UNKNOWN,
                health = BatteryHealth.UNKNOWN,
                temperatureCelsius = null,
                voltageMillivolts = null
            )
        }

        val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val rawScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percentage = TelemetryMath.normalizeBatteryLevel(rawLevel, rawScale)

        val rawStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val chargingStatus = mapChargingStatus(rawStatus)

        val rawPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val pluggedType = mapPluggedType(rawPlugged)

        val rawHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val health = mapBatteryHealth(rawHealth)

        val rawTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val temperatureCelsius = parseBatteryTemperature(rawTemperature)

        val rawVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val voltageMillivolts = parseBatteryVoltage(rawVoltage)

        return BatterySpecs(
            percentage = percentage,
            chargingStatus = chargingStatus,
            pluggedType = pluggedType,
            health = health,
            temperatureCelsius = temperatureCelsius,
            voltageMillivolts = voltageMillivolts
        )
    }

    private fun queryCurrentThermalSpecs(): ThermalSpecs {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager != null) {
                return ThermalSpecs(mapThermalStatus(powerManager.currentThermalStatus))
            }
        }
        return ThermalSpecs(ThermalStatus.NOT_SUPPORTED)
    }

    companion object {
        fun mapChargingStatus(rawStatus: Int): ChargingStatus = when (rawStatus) {
            BatteryManager.BATTERY_STATUS_CHARGING -> ChargingStatus.CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> ChargingStatus.DISCHARGING
            BatteryManager.BATTERY_STATUS_FULL -> ChargingStatus.FULL
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> ChargingStatus.NOT_CHARGING
            else -> ChargingStatus.UNKNOWN
        }

        fun mapPluggedType(rawPlugged: Int): PluggedType = when (rawPlugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> PluggedType.AC
            BatteryManager.BATTERY_PLUGGED_USB -> PluggedType.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> PluggedType.WIRELESS
            0 -> PluggedType.UNPLUGGED
            else -> PluggedType.UNKNOWN
        }

        fun mapBatteryHealth(rawHealth: Int): BatteryHealth = when (rawHealth) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealth.UNSPECIFIED_FAILURE
            BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
            else -> BatteryHealth.UNKNOWN
        }

        fun parseBatteryTemperature(rawTemperature: Int): Float? =
            if (rawTemperature > -1000) rawTemperature / 10.0f else null

        fun parseBatteryVoltage(rawVoltage: Int): Int? =
            if (rawVoltage > 0) rawVoltage else null

        fun mapThermalStatus(rawStatus: Int, sdkInt: Int = Build.VERSION.SDK_INT): ThermalStatus {
            if (sdkInt < Build.VERSION_CODES.Q) return ThermalStatus.NOT_SUPPORTED
            return when (rawStatus) {
                PowerManager.THERMAL_STATUS_NONE -> ThermalStatus.NONE
                PowerManager.THERMAL_STATUS_LIGHT -> ThermalStatus.LIGHT
                PowerManager.THERMAL_STATUS_MODERATE -> ThermalStatus.MODERATE
                PowerManager.THERMAL_STATUS_SEVERE -> ThermalStatus.SEVERE
                PowerManager.THERMAL_STATUS_CRITICAL -> ThermalStatus.CRITICAL
                PowerManager.THERMAL_STATUS_EMERGENCY -> ThermalStatus.EMERGENCY
                PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalStatus.SHUTDOWN
                else -> ThermalStatus.NONE
            }
        }
    }
}
