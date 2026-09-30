package com.example.devicelab.platform.system

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import com.example.devicelab.domain.model.SensorCategory
import com.example.devicelab.domain.model.SensorDescriptor
import com.example.devicelab.domain.model.SensorReading
import com.example.devicelab.domain.repository.SensorDataSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidSensorDataSource(
    private val context: Context
) : SensorDataSource {

    private val sensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }

    override fun getSensorInventory(): List<SensorDescriptor> {
        val manager = sensorManager ?: return emptyList()
        val allSensors = manager.getSensorList(Sensor.TYPE_ALL)

        return allSensors.map { sensor ->
            val category = classifySensor(sensor.type)
            val typeString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                sensor.stringType ?: "android.sensor.${sensor.type}"
            } else {
                "android.sensor.${sensor.type}"
            }
            val reportingMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                when (sensor.reportingMode) {
                    Sensor.REPORTING_MODE_CONTINUOUS -> "Continuous"
                    Sensor.REPORTING_MODE_ON_CHANGE -> "On Change"
                    Sensor.REPORTING_MODE_ONE_SHOT -> "One Shot"
                    Sensor.REPORTING_MODE_SPECIAL_TRIGGER -> "Special Trigger"
                    else -> "Standard"
                }
            } else {
                "Standard"
            }

            SensorDescriptor(
                type = sensor.type,
                name = sensor.name ?: "Unknown Sensor",
                vendor = sensor.vendor ?: "Generic Vendor",
                typeString = typeString,
                category = category,
                version = sensor.version,
                resolution = sensor.resolution,
                maxRange = sensor.maximumRange,
                powerMa = sensor.power,
                reportingMode = reportingMode
            )
        }.sortedWith(compareBy({ it.category.name }, { it.name }))
    }

    override fun observeSensorReadings(sensorType: Int): Flow<SensorReading> = callbackFlow {
        val manager = sensorManager
        if (manager == null) {
            close()
            return@callbackFlow
        }

        val sensor = manager.getDefaultSensor(sensorType)
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val reading = SensorReading(
                    sensorType = event.sensor.type,
                    sensorName = event.sensor.name ?: "Sensor $sensorType",
                    values = event.values.toList(),
                    accuracy = event.accuracy,
                    timestampNanos = event.timestamp
                )
                trySend(reading)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Accuracy updates handled implicitly in onSensorChanged
            }
        }

        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            manager.unregisterListener(listener)
        }
    }

    private fun classifySensor(type: Int): SensorCategory {
        return when (type) {
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_ACCELEROMETER_UNCALIBRATED,
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> SensorCategory.MOTION

            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED,
            Sensor.TYPE_ORIENTATION,
            Sensor.TYPE_ROTATION_VECTOR,
            Sensor.TYPE_GAME_ROTATION_VECTOR,
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> SensorCategory.POSITION

            Sensor.TYPE_LIGHT,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_AMBIENT_TEMPERATURE,
            Sensor.TYPE_RELATIVE_HUMIDITY,
            Sensor.TYPE_PROXIMITY -> SensorCategory.ENVIRONMENT

            Sensor.TYPE_HEART_RATE,
            Sensor.TYPE_STEP_COUNTER,
            Sensor.TYPE_STEP_DETECTOR -> SensorCategory.BIOMETRIC

            else -> SensorCategory.OTHER
        }
    }
}
