package io.livekit.android.example.voiceassistant.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

/**
 * BatteryOptimizationManager - Smart power management for Jarvis
 * "Sir, I've taken the liberty of optimizing power consumption. The arc reactor should last longer now."
 *
 * Features:
 * - Battery level monitoring
 * - Power save mode detection
 * - Adaptive service behavior based on battery state
 * - Wake lock management
 */
class BatteryOptimizationManager(private val context: Context) {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    private val _batteryOptimizationState = MutableStateFlow(BatteryOptimizationState())
    val batteryOptimizationState: StateFlow<BatteryOptimizationState> = _batteryOptimizationState

    /**
     * Power mode configuration
     */
    enum class PowerMode {
        FULL_POWER,      // Battery > 50% or charging - all features enabled
        BALANCED,        // Battery 20-50% - reduce polling frequency
        POWER_SAVER,     // Battery 10-20% - minimal features only
        CRITICAL         // Battery < 10% - emergency mode
    }

    init {
        updateBatteryState()
    }

    /**
     * Get current battery level (0-100)
     */
    fun getBatteryLevel(): Int {
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        return if (level != -1 && scale != -1) {
            ((level.toFloat() / scale.toFloat()) * 100).toInt()
        } else {
            100 // Assume full if we can't read
        }
    }

    /**
     * Check if device is charging
     */
    fun isCharging(): Boolean {
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    /**
     * Check if power save mode is enabled
     */
    fun isPowerSaveMode(): Boolean {
        return powerManager.isPowerSaveMode
    }

    /**
     * Determine current power mode based on battery state
     */
    fun getCurrentPowerMode(): PowerMode {
        val batteryLevel = getBatteryLevel()
        val isCharging = isCharging()
        val isPowerSave = isPowerSaveMode()

        return when {
            isCharging -> PowerMode.FULL_POWER
            isPowerSave -> PowerMode.POWER_SAVER
            batteryLevel > 50 -> PowerMode.FULL_POWER
            batteryLevel > 20 -> PowerMode.BALANCED
            batteryLevel > 10 -> PowerMode.POWER_SAVER
            else -> PowerMode.CRITICAL
        }
    }

    /**
     * Update battery optimization state
     */
    fun updateBatteryState() {
        val powerMode = getCurrentPowerMode()
        val batteryLevel = getBatteryLevel()
        val isCharging = isCharging()

        _batteryOptimizationState.value = BatteryOptimizationState(
            powerMode = powerMode,
            batteryLevel = batteryLevel,
            isCharging = isCharging,
            shouldReduceWakeWordSensitivity = powerMode in listOf(PowerMode.POWER_SAVER, PowerMode.CRITICAL),
            shouldDisableVisionMode = powerMode == PowerMode.CRITICAL,
            shouldDisableScreenAwareness = powerMode in listOf(PowerMode.POWER_SAVER, PowerMode.CRITICAL),
            shouldReduceHaptics = powerMode != PowerMode.FULL_POWER,
            wakeWordCheckIntervalMs = when (powerMode) {
                PowerMode.FULL_POWER -> 100L
                PowerMode.BALANCED -> 200L
                PowerMode.POWER_SAVER -> 500L
                PowerMode.CRITICAL -> 1000L
            }
        )

        Timber.i("Battery optimization updated: $powerMode, ${batteryLevel}%, charging: $isCharging")
    }

    /**
     * Get recommendation message for user
     */
    fun getOptimizationRecommendation(): String? {
        val state = _batteryOptimizationState.value
        return when (state.powerMode) {
            PowerMode.CRITICAL -> "Sir, battery is at ${state.batteryLevel}%. I recommend charging immediately or I'll need to hibernate non-essential systems."
            PowerMode.POWER_SAVER -> "Battery at ${state.batteryLevel}%, Sir. Running in power-save mode. Some features are limited."
            PowerMode.BALANCED -> "Battery at ${state.batteryLevel}%. Balancing performance and efficiency, Boss."
            PowerMode.FULL_POWER -> if (state.isCharging) {
                "Arc reactor charging, Sir. All systems operating at full capacity."
            } else null
        }
    }

    /**
     * Should allow wake word service to run
     */
    fun shouldAllowWakeWord(): Boolean {
        val state = _batteryOptimizationState.value
        // Only disable in absolute critical mode (<5%)
        return state.batteryLevel > 5 || state.isCharging
    }

    /**
     * Should allow vision mode
     */
    fun shouldAllowVisionMode(): Boolean {
        val state = _batteryOptimizationState.value
        return !state.shouldDisableVisionMode || state.isCharging
    }

    /**
     * Should allow screen awareness
     */
    fun shouldAllowScreenAwareness(): Boolean {
        val state = _batteryOptimizationState.value
        return !state.shouldDisableScreenAwareness || state.isCharging
    }

    /**
     * Get sensor delay based on power mode
     */
    fun getSensorDelay(): Int {
        return when (getCurrentPowerMode()) {
            PowerMode.FULL_POWER -> android.hardware.SensorManager.SENSOR_DELAY_NORMAL
            PowerMode.BALANCED -> android.hardware.SensorManager.SENSOR_DELAY_UI
            PowerMode.POWER_SAVER -> android.hardware.SensorManager.SENSOR_DELAY_GAME
            PowerMode.CRITICAL -> android.hardware.SensorManager.SENSOR_DELAY_FASTEST // Actually means slowest for our use
        }
    }

    companion object {
        @Volatile
        private var instance: BatteryOptimizationManager? = null

        fun getInstance(context: Context): BatteryOptimizationManager {
            return instance ?: synchronized(this) {
                instance ?: BatteryOptimizationManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * Battery optimization state
 */
data class BatteryOptimizationState(
    val powerMode: BatteryOptimizationManager.PowerMode = BatteryOptimizationManager.PowerMode.FULL_POWER,
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val shouldReduceWakeWordSensitivity: Boolean = false,
    val shouldDisableVisionMode: Boolean = false,
    val shouldDisableScreenAwareness: Boolean = false,
    val shouldReduceHaptics: Boolean = false,
    val wakeWordCheckIntervalMs: Long = 100L
)
