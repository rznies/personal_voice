package io.livekit.android.example.voiceassistant.security

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import io.livekit.android.example.voiceassistant.receiver.BootReceiver
import io.livekit.android.example.voiceassistant.service.WakeWordService
import timber.log.Timber

/**
 * ComponentEnabler - Dynamically enable/disable services and receivers
 *
 * Controls WakeWordService and BootReceiver based on user security settings.
 * Components are disabled by default in the manifest for safety.
 */
class ComponentEnabler(private val context: Context) {

    /**
     * Enable or disable the boot receiver
     */
    fun setBootReceiverEnabled(enabled: Boolean) {
        val component = ComponentName(context, BootReceiver::class.java)
        val newState = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        context.packageManager.setComponentEnabledSetting(
            component,
            newState,
            PackageManager.DONT_KILL_APP
        )

        Timber.i("BootReceiver ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Check if boot receiver is enabled
     */
    fun isBootReceiverEnabled(): Boolean {
        val component = ComponentName(context, BootReceiver::class.java)
        val state = context.packageManager.getComponentEnabledSetting(component)
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }

    /**
     * Enable or disable the wake word service component
     * Note: This doesn't start/stop the service, just controls if it CAN start
     */
    fun setWakeWordServiceEnabled(enabled: Boolean) {
        val component = ComponentName(context, WakeWordService::class.java)
        val newState = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        context.packageManager.setComponentEnabledSetting(
            component,
            newState,
            PackageManager.DONT_KILL_APP
        )

        Timber.i("WakeWordService ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Check if wake word service is enabled
     */
    fun isWakeWordServiceEnabled(): Boolean {
        val component = ComponentName(context, WakeWordService::class.java)
        val state = context.packageManager.getComponentEnabledSetting(component)
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }

    /**
     * Apply security settings to components
     * Called when user changes security settings
     */
    fun applySecuritySettings(settings: SecurityPreferences.SecuritySettings) {
        // Enable/disable boot receiver
        setBootReceiverEnabled(settings.bootStartEnabled)

        // Enable/disable wake word service
        setWakeWordServiceEnabled(settings.wakeWordEnabled)

        Timber.i("Security settings applied to components")
    }

    companion object {
        @Volatile
        private var instance: ComponentEnabler? = null

        fun getInstance(context: Context): ComponentEnabler {
            return instance ?: synchronized(this) {
                instance ?: ComponentEnabler(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
