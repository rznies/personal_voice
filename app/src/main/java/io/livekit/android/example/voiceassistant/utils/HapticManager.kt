package io.livekit.android.example.voiceassistant.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import timber.log.Timber

/**
 * HapticManager - Haptic feedback for Iron Man vibes
 * "I felt that in my bones" - Tony Stark after repulsor blast
 */
class HapticManager(private val context: Context) {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * Repulsor blast haptic pattern
     * Quick burst → pause → medium pulse
     */
    fun repulsorBlast() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 50, 100, 150, 100, 50)
            val amplitudes = intArrayOf(0, 255, 0, 200, 0, 100)

            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 50, 100, 150), -1)
        }
        Timber.d("Repulsor blast haptic fired, Sir.")
    }

    /**
     * Arc reactor pulse
     * Gentle rhythmic pulse like a heartbeat
     */
    fun arcReactorPulse() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 100, 300, 100)
            val amplitudes = intArrayOf(0, 80, 0, 80)

            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 100, 300, 100), -1)
        }
    }

    /**
     * Wake word detected - subtle confirmation
     */
    fun wakeWordDetected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(30, 100)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }

    /**
     * Button click - light tap
     */
    fun buttonClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(10, 50)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10)
        }
    }

    /**
     * Error - double buzz
     */
    fun error() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 50, 50, 50)
            val amplitudes = intArrayOf(0, 150, 0, 150)

            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 50, 50, 50), -1)
        }
    }

    /**
     * Success - quick ascending pattern
     */
    fun success() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 30, 30, 40, 30, 50)
            val amplitudes = intArrayOf(0, 100, 0, 150, 0, 200)

            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 30, 30, 40, 30, 50), -1)
        }
    }

    /**
     * Suit activation - dramatic build-up
     */
    fun suitActivation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 100, 50, 150, 50, 200, 50, 250)
            val amplitudes = intArrayOf(0, 50, 0, 100, 0, 150, 0, 255)

            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 100, 50, 150, 50, 200, 50, 250), -1)
        }
    }

    /**
     * Custom vibration pattern
     */
    fun customPattern(timings: LongArray, amplitudes: IntArray, repeat: Int = -1) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(timings, amplitudes, repeat)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings, repeat)
        }
    }

    /**
     * Cancel any ongoing vibration
     */
    fun cancel() {
        vibrator.cancel()
    }

    companion object {
        @Volatile
        private var instance: HapticManager? = null

        fun getInstance(context: Context): HapticManager {
            return instance ?: synchronized(this) {
                instance ?: HapticManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
