package io.livekit.android.example.voiceassistant.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * SecurityPreferences - Iron-clad security settings with encrypted storage
 *
 * Controls all dangerous permissions and features with user consent.
 * Everything is OFF by default for maximum safety.
 */
class SecurityPreferences private constructor(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "jarvis_security_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // State flows for reactive UI
    private val _securitySettings = MutableStateFlow(loadSettings())
    val securitySettings: StateFlow<SecuritySettings> = _securitySettings.asStateFlow()

    /**
     * Security Settings Data Class
     */
    data class SecuritySettings(
        // Wake word listening (OFF by default)
        val wakeWordEnabled: Boolean = false,

        // Screen capture permission (ask every time)
        val screenCaptureAlwaysAsk: Boolean = true,

        // Camera access (ask every time)
        val cameraAlwaysAsk: Boolean = true,

        // Accessibility level
        val accessibilityLevel: AccessibilityLevel = AccessibilityLevel.OFF,

        // Boot start (OFF by default)
        val bootStartEnabled: Boolean = false,

        // Biometric confirmation for dangerous actions (ON by default)
        val requireBiometricAuth: Boolean = true,

        // Local-only mode (ON by default - no cloud unless enabled)
        val localOnlyMode: Boolean = true,

        // Privacy policy accepted
        val privacyPolicyAccepted: Boolean = false,

        // First launch
        val isFirstLaunch: Boolean = true,

        // Audit log enabled
        val auditLogEnabled: Boolean = true
    )

    /**
     * Accessibility Levels
     * - OFF: No accessibility access (safest)
     * - READ: Read screen content only
     * - BASIC: Read + basic interactions (tap, swipe)
     * - FULL: Complete device control (DANGEROUS)
     */
    enum class AccessibilityLevel {
        OFF,
        READ,
        BASIC,
        FULL
    }

    /**
     * Load settings from encrypted storage
     */
    private fun loadSettings(): SecuritySettings {
        return try {
            SecuritySettings(
                wakeWordEnabled = encryptedPrefs.getBoolean(KEY_WAKE_WORD, false),
                screenCaptureAlwaysAsk = encryptedPrefs.getBoolean(KEY_SCREEN_CAPTURE_ASK, true),
                cameraAlwaysAsk = encryptedPrefs.getBoolean(KEY_CAMERA_ASK, true),
                accessibilityLevel = AccessibilityLevel.valueOf(
                    encryptedPrefs.getString(KEY_ACCESSIBILITY_LEVEL, AccessibilityLevel.OFF.name)
                        ?: AccessibilityLevel.OFF.name
                ),
                bootStartEnabled = encryptedPrefs.getBoolean(KEY_BOOT_START, false),
                requireBiometricAuth = encryptedPrefs.getBoolean(KEY_REQUIRE_BIOMETRIC, true),
                localOnlyMode = encryptedPrefs.getBoolean(KEY_LOCAL_ONLY, true),
                privacyPolicyAccepted = encryptedPrefs.getBoolean(KEY_PRIVACY_ACCEPTED, false),
                isFirstLaunch = encryptedPrefs.getBoolean(KEY_FIRST_LAUNCH, true),
                auditLogEnabled = encryptedPrefs.getBoolean(KEY_AUDIT_LOG, true)
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to load security settings, using defaults")
            SecuritySettings()
        }
    }

    /**
     * Update wake word setting
     */
    fun setWakeWordEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_WAKE_WORD, enabled).apply()
        _securitySettings.value = _securitySettings.value.copy(wakeWordEnabled = enabled)
        logSecurityEvent("Wake word ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Update screen capture setting
     */
    fun setScreenCaptureAlwaysAsk(alwaysAsk: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_SCREEN_CAPTURE_ASK, alwaysAsk).apply()
        _securitySettings.value = _securitySettings.value.copy(screenCaptureAlwaysAsk = alwaysAsk)
        logSecurityEvent("Screen capture ask mode: $alwaysAsk")
    }

    /**
     * Update camera setting
     */
    fun setCameraAlwaysAsk(alwaysAsk: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_CAMERA_ASK, alwaysAsk).apply()
        _securitySettings.value = _securitySettings.value.copy(cameraAlwaysAsk = alwaysAsk)
        logSecurityEvent("Camera ask mode: $alwaysAsk")
    }

    /**
     * Update accessibility level (CRITICAL SECURITY SETTING)
     */
    fun setAccessibilityLevel(level: AccessibilityLevel) {
        encryptedPrefs.edit().putString(KEY_ACCESSIBILITY_LEVEL, level.name).apply()
        _securitySettings.value = _securitySettings.value.copy(accessibilityLevel = level)
        logSecurityEvent("Accessibility level changed to: $level")
        Timber.w("⚠️ SECURITY: Accessibility level changed to $level")
    }

    /**
     * Update boot start setting
     */
    fun setBootStartEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_BOOT_START, enabled).apply()
        _securitySettings.value = _securitySettings.value.copy(bootStartEnabled = enabled)
        logSecurityEvent("Boot start ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Update biometric auth requirement
     */
    fun setRequireBiometricAuth(required: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_REQUIRE_BIOMETRIC, required).apply()
        _securitySettings.value = _securitySettings.value.copy(requireBiometricAuth = required)
        logSecurityEvent("Biometric auth ${if (required) "required" else "optional"}")
    }

    /**
     * Update local-only mode
     */
    fun setLocalOnlyMode(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_LOCAL_ONLY, enabled).apply()
        _securitySettings.value = _securitySettings.value.copy(localOnlyMode = enabled)
        logSecurityEvent("Local-only mode ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Mark privacy policy as accepted
     */
    fun acceptPrivacyPolicy() {
        encryptedPrefs.edit().putBoolean(KEY_PRIVACY_ACCEPTED, true).apply()
        _securitySettings.value = _securitySettings.value.copy(privacyPolicyAccepted = true)
        logSecurityEvent("Privacy policy accepted")
    }

    /**
     * Complete first launch
     */
    fun completeFirstLaunch() {
        encryptedPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        _securitySettings.value = _securitySettings.value.copy(isFirstLaunch = false)
    }

    /**
     * Log security event to audit trail
     */
    private fun logSecurityEvent(event: String) {
        if (_securitySettings.value.auditLogEnabled) {
            val timestamp = System.currentTimeMillis()
            Timber.tag("SECURITY_AUDIT").i("[$timestamp] $event")

            // Store in encrypted prefs for audit trail
            val currentLog = encryptedPrefs.getString(KEY_AUDIT_TRAIL, "") ?: ""
            val newEntry = "[$timestamp] $event\n"
            val updatedLog = (newEntry + currentLog).take(10000) // Keep last 10KB
            encryptedPrefs.edit().putString(KEY_AUDIT_TRAIL, updatedLog).apply()
        }
    }

    /**
     * Get audit trail (last 50 events)
     */
    fun getAuditTrail(): String {
        return encryptedPrefs.getString(KEY_AUDIT_TRAIL, "No audit events") ?: "No audit events"
    }

    /**
     * Clear audit trail
     */
    fun clearAuditTrail() {
        encryptedPrefs.edit().putString(KEY_AUDIT_TRAIL, "").apply()
        logSecurityEvent("Audit trail cleared")
    }

    /**
     * Reset all security settings to defaults (DANGEROUS)
     */
    fun resetToDefaults() {
        encryptedPrefs.edit().clear().apply()
        _securitySettings.value = SecuritySettings()
        logSecurityEvent("⚠️ All security settings reset to defaults")
        Timber.w("⚠️ SECURITY RESET: All settings restored to safe defaults")
    }

    companion object {
        // Preference keys
        private const val KEY_WAKE_WORD = "wake_word_enabled"
        private const val KEY_SCREEN_CAPTURE_ASK = "screen_capture_ask"
        private const val KEY_CAMERA_ASK = "camera_ask"
        private const val KEY_ACCESSIBILITY_LEVEL = "accessibility_level"
        private const val KEY_BOOT_START = "boot_start_enabled"
        private const val KEY_REQUIRE_BIOMETRIC = "require_biometric"
        private const val KEY_LOCAL_ONLY = "local_only_mode"
        private const val KEY_PRIVACY_ACCEPTED = "privacy_policy_accepted"
        private const val KEY_FIRST_LAUNCH = "is_first_launch"
        private const val KEY_AUDIT_LOG = "audit_log_enabled"
        private const val KEY_AUDIT_TRAIL = "audit_trail"

        @Volatile
        private var instance: SecurityPreferences? = null

        fun getInstance(context: Context): SecurityPreferences {
            return instance ?: synchronized(this) {
                instance ?: SecurityPreferences(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
