package io.livekit.android.example.voiceassistant.security

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import timber.log.Timber

/**
 * PermissionGuard - Security gates for dangerous operations
 *
 * Enforces user consent before executing high-risk actions.
 * Integrates with SecurityPreferences and BiometricAuthManager.
 */
class PermissionGuard(private val context: Context) {

    private val securityPrefs = SecurityPreferences.getInstance(context)

    /**
     * Check if screen capture is allowed
     * @return true if allowed, false if user must grant permission
     */
    fun canCaptureScreen(): Boolean {
        val settings = securityPrefs.securitySettings.value

        // If local-only mode is on, screen capture should always ask
        if (settings.localOnlyMode) {
            Timber.w("Screen capture blocked by local-only mode")
            return false
        }

        // If screen capture always ask is enabled, return false
        if (settings.screenCaptureAlwaysAsk) {
            Timber.i("Screen capture requires user confirmation (always ask mode)")
            return false
        }

        return true
    }

    /**
     * Check if camera access is allowed
     * @return true if allowed, false if user must grant permission
     */
    fun canAccessCamera(): Boolean {
        val settings = securityPrefs.securitySettings.value

        // Check runtime permission first
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.w("Camera permission not granted")
            return false
        }

        // If local-only mode is on, camera should always ask
        if (settings.localOnlyMode) {
            Timber.w("Camera access blocked by local-only mode")
            return false
        }

        // If camera always ask is enabled, return false
        if (settings.cameraAlwaysAsk) {
            Timber.i("Camera access requires user confirmation (always ask mode)")
            return false
        }

        return true
    }

    /**
     * Check if accessibility service can perform an action based on current level
     */
    fun canPerformAccessibilityAction(requiredLevel: SecurityPreferences.AccessibilityLevel): Boolean {
        val currentLevel = securityPrefs.securitySettings.value.accessibilityLevel

        val canPerform = currentLevel.ordinal >= requiredLevel.ordinal
        if (!canPerform) {
            Timber.w("Accessibility action blocked. Current: $currentLevel, Required: $requiredLevel")
        }

        return canPerform
    }

    /**
     * Check if wake word service should be running
     */
    fun isWakeWordEnabled(): Boolean {
        return securityPrefs.securitySettings.value.wakeWordEnabled
    }

    /**
     * Check if boot start is enabled
     */
    fun isBootStartEnabled(): Boolean {
        return securityPrefs.securitySettings.value.bootStartEnabled
    }

    /**
     * Check if biometric auth is required for an action
     */
    fun requiresBiometricAuth(): Boolean {
        return securityPrefs.securitySettings.value.requireBiometricAuth
    }

    /**
     * Check if local-only mode is active
     */
    fun isLocalOnlyMode(): Boolean {
        return securityPrefs.securitySettings.value.localOnlyMode
    }

    /**
     * Log a security event
     */
    fun logSecurityEvent(event: String) {
        Timber.tag("SECURITY_GUARD").i(event)
    }

    /**
     * Validate if a dangerous action is allowed
     * Returns error message if blocked, null if allowed
     */
    fun validateDangerousAction(action: BiometricAuthManager.DangerousAction): String? {
        val settings = securityPrefs.securitySettings.value

        return when (action) {
            BiometricAuthManager.DangerousAction.SEND_MESSAGE,
            BiometricAuthManager.DangerousAction.MAKE_CALL,
            BiometricAuthManager.DangerousAction.SEND_EMAIL -> {
                if (settings.requireBiometricAuth) {
                    null // Biometric will be checked separately
                } else {
                    logSecurityEvent("${action.name} allowed without biometric (user disabled it)")
                    null
                }
            }

            BiometricAuthManager.DangerousAction.ACCESSIBILITY_CONTROL -> {
                if (settings.accessibilityLevel == SecurityPreferences.AccessibilityLevel.OFF) {
                    "Accessibility service is disabled. Enable it in Security Settings."
                } else null
            }

            BiometricAuthManager.DangerousAction.SCREEN_CAPTURE -> {
                if (settings.screenCaptureAlwaysAsk) {
                    null // Will be prompted
                } else {
                    "Screen capture is blocked. Disable 'Always Ask' in Security Settings."
                }
            }

            BiometricAuthManager.DangerousAction.CAMERA_ACCESS -> {
                if (settings.cameraAlwaysAsk) {
                    null // Will be prompted
                } else {
                    "Camera access is blocked. Disable 'Always Ask' in Security Settings."
                }
            }

            else -> null
        }
    }

    companion object {
        @Volatile
        private var instance: PermissionGuard? = null

        fun getInstance(context: Context): PermissionGuard {
            return instance ?: synchronized(this) {
                instance ?: PermissionGuard(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
