package io.livekit.android.example.voiceassistant.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import timber.log.Timber

/**
 * BiometricAuthManager - Handles biometric authentication for dangerous operations
 *
 * Protects high-risk actions like:
 * - Sending messages
 * - Making calls
 * - Opening apps
 * - Deleting data
 * - Changing security settings
 */
class BiometricAuthManager(private val activity: FragmentActivity) {

    private val biometricManager = BiometricManager.from(activity)

    /**
     * Check if biometric authentication is available
     */
    fun isBiometricAvailable(): BiometricStatus {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricStatus.SECURITY_UPDATE_REQUIRED
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricStatus.UNSUPPORTED
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricStatus.UNKNOWN
            else -> BiometricStatus.UNKNOWN
        }
    }

    /**
     * Authenticate for dangerous action
     */
    fun authenticateForAction(
        action: DangerousAction,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        val status = isBiometricAvailable()
        if (status != BiometricStatus.AVAILABLE) {
            onError("Biometric authentication not available: ${status.message}")
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("🔒 ${action.title}")
            .setSubtitle(action.description)
            .setDescription("Jarvis requires your confirmation for this dangerous operation")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Timber.i("✅ Biometric auth successful for ${action.name}")
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        Timber.w("Biometric auth cancelled by user")
                        onCancel()
                    } else {
                        Timber.e("❌ Biometric auth error: $errString")
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Timber.w("Biometric auth failed - fingerprint not recognized")
                    // Don't call onError here - let user retry
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Dangerous actions that require authentication
     */
    enum class DangerousAction(val title: String, val description: String) {
        SEND_MESSAGE(
            "Send Message",
            "Jarvis wants to send a message on your behalf"
        ),
        MAKE_CALL(
            "Make Phone Call",
            "Jarvis wants to make a phone call"
        ),
        SEND_EMAIL(
            "Send Email",
            "Jarvis wants to send an email"
        ),
        OPEN_APP(
            "Open Application",
            "Jarvis wants to open an app with elevated permissions"
        ),
        DELETE_DATA(
            "Delete Data",
            "Jarvis wants to delete data"
        ),
        CHANGE_SETTINGS(
            "Change System Settings",
            "Jarvis wants to modify system settings"
        ),
        ACCESSIBILITY_CONTROL(
            "Device Control",
            "Jarvis wants to control your device via accessibility"
        ),
        SCREEN_CAPTURE(
            "Capture Screen",
            "Jarvis wants to capture your screen content"
        ),
        CAMERA_ACCESS(
            "Access Camera",
            "Jarvis wants to access your camera"
        ),
        READ_CONTACTS(
            "Read Contacts",
            "Jarvis wants to read your contacts"
        ),
        MODIFY_SECURITY(
            "Modify Security Settings",
            "Change critical security configuration"
        )
    }

    /**
     * Biometric status
     */
    enum class BiometricStatus(val message: String) {
        AVAILABLE("Biometric authentication available"),
        NO_HARDWARE("No biometric hardware found"),
        UNAVAILABLE("Biometric hardware unavailable"),
        NOT_ENROLLED("No biometrics enrolled - please add fingerprint/face in Settings"),
        SECURITY_UPDATE_REQUIRED("Security update required"),
        UNSUPPORTED("Biometric authentication unsupported"),
        UNKNOWN("Unknown biometric status")
    }

    companion object {
        /**
         * Quick check if action requires authentication based on security settings
         */
        fun requiresAuth(
            securityPrefs: SecurityPreferences,
            action: DangerousAction
        ): Boolean {
            val settings = securityPrefs.securitySettings.value
            return settings.requireBiometricAuth
        }
    }
}
