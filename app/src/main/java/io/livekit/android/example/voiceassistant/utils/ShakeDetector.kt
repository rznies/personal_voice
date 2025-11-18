package io.livekit.android.example.voiceassistant.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import timber.log.Timber
import kotlin.math.sqrt

/**
 * ShakeDetector - Detects device shake for Repulsor Mode
 * "Sometimes you gotta shake it before you can break it" - Tony Stark (probably)
 */
class ShakeDetector(
    context: Context,
    private val onShakeDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeTime = 0L
    private var shakeCount = 0

    companion object {
        private const val SHAKE_THRESHOLD = 15.0f // Acceleration threshold
        private const val SHAKE_INTERVAL = 500L // Time between shakes (ms)
        private const val SHAKE_COUNT_RESET_TIME = 1000L // Reset shake count after 1s
        private const val REQUIRED_SHAKES = 2 // Number of shakes to trigger
    }

    /**
     * Start listening for shake gestures
     */
    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            Timber.d("ShakeDetector: Listening for repulsor activation, Sir.")
        } ?: run {
            Timber.e("ShakeDetector: No accelerometer found on device")
        }
    }

    /**
     * Stop listening for shake gestures
     */
    fun stopListening() {
        sensorManager.unregisterListener(this)
        Timber.d("ShakeDetector: Repulsor detection paused.")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate total acceleration
            val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            // Remove gravity
            val accelerationWithoutGravity = acceleration - SensorManager.GRAVITY_EARTH

            val currentTime = System.currentTimeMillis()

            // Detect shake
            if (accelerationWithoutGravity > SHAKE_THRESHOLD) {
                // Check if enough time has passed since last shake
                if (currentTime - lastShakeTime > SHAKE_INTERVAL) {
                    shakeCount++
                    lastShakeTime = currentTime

                    Timber.d("Shake detected! Count: $shakeCount")

                    // Trigger repulsor if required shakes achieved
                    if (shakeCount >= REQUIRED_SHAKES) {
                        Timber.i("Repulsor Mode activated!")
                        onShakeDetected()
                        shakeCount = 0 // Reset
                    }
                }
            }

            // Reset shake count if too much time has passed
            if (currentTime - lastShakeTime > SHAKE_COUNT_RESET_TIME) {
                if (shakeCount > 0) {
                    shakeCount = 0
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for shake detection
    }
}
