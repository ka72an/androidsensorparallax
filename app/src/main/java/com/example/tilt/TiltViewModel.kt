package com.example.tilt

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TiltViewModel : ViewModel(), SensorEventListener {
    private var sensorManager: SensorManager? = null

    var pitch by mutableStateOf(0f)
    var roll by mutableStateOf(0f)

    fun startListening(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)

        sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GRAVITY) {
            val gravX = event.values[0]
            val gravY = event.values[1]- 4.5f

            val targetRoll = (gravX / 9.8f).coerceIn(-1f, 1f)
            val targetPitch = (gravY / 9.8f).coerceIn(-1f, 1f)

            roll = (roll * 0.85f) + (targetRoll * 0.15f)
            pitch = (pitch * 0.85f) + (targetPitch * 0.15f)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }
}
