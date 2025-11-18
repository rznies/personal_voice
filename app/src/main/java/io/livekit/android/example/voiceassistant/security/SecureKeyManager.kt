package io.livekit.android.example.voiceassistant.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber

/**
 * SecureKeyManager - Encrypted storage for API keys
 *
 * NEVER ship API keys in the APK. Users must enter them manually.
 * All keys are encrypted at rest using AES-256-GCM.
 */
class SecureKeyManager private constructor(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "jarvis_api_keys",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * API Key types
     */
    enum class KeyType(val displayName: String, val required: Boolean) {
        GEMINI("Google Gemini API", required = true),
        ELEVENLABS("ElevenLabs TTS", required = false),
        CARTESIA("Cartesia TTS", required = false)
    }

    /**
     * Store API key (encrypted)
     */
    fun storeKey(keyType: KeyType, apiKey: String) {
        if (apiKey.isBlank()) {
            Timber.w("Attempted to store blank API key for ${keyType.displayName}")
            return
        }

        encryptedPrefs.edit()
            .putString(keyType.name, apiKey)
            .apply()

        Timber.i("✅ ${keyType.displayName} key stored securely (encrypted)")
    }

    /**
     * Get API key (decrypted)
     */
    fun getKey(keyType: KeyType): String? {
        val key = encryptedPrefs.getString(keyType.name, null)
        if (key == null) {
            Timber.w("⚠️ ${keyType.displayName} key not found")
        }
        return key
    }

    /**
     * Check if key exists
     */
    fun hasKey(keyType: KeyType): Boolean {
        return encryptedPrefs.contains(keyType.name)
    }

    /**
     * Delete API key
     */
    fun deleteKey(keyType: KeyType) {
        encryptedPrefs.edit()
            .remove(keyType.name)
            .apply()
        Timber.w("🗑️ ${keyType.displayName} key deleted")
    }

    /**
     * Check if all required keys are present
     */
    fun hasAllRequiredKeys(): Boolean {
        return KeyType.values()
            .filter { it.required }
            .all { hasKey(it) }
    }

    /**
     * Get validation status for all keys
     */
    fun getKeyStatus(): Map<KeyType, KeyStatus> {
        return KeyType.values().associateWith { keyType ->
            when {
                !hasKey(keyType) -> KeyStatus.MISSING
                else -> {
                    val key = getKey(keyType)
                    when {
                        key.isNullOrBlank() -> KeyStatus.INVALID
                        key.length < 10 -> KeyStatus.INVALID
                        else -> KeyStatus.VALID
                    }
                }
            }
        }
    }

    /**
     * Key status
     */
    enum class KeyStatus {
        VALID,
        MISSING,
        INVALID
    }

    /**
     * Validate API key format (basic check)
     */
    fun isValidKeyFormat(keyType: KeyType, key: String): Boolean {
        return when (keyType) {
            KeyType.GEMINI -> key.startsWith("AIza") && key.length >= 39
            KeyType.ELEVENLABS -> key.length >= 32
            KeyType.CARTESIA -> key.length >= 32
        }
    }

    /**
     * Clear all API keys (DANGEROUS - requires confirmation)
     */
    fun clearAllKeys() {
        encryptedPrefs.edit().clear().apply()
        Timber.w("⚠️ ALL API KEYS CLEARED")
    }

    /**
     * Export key summary (for debugging - DOES NOT EXPOSE ACTUAL KEYS)
     */
    fun getKeysSummary(): String {
        val status = getKeyStatus()
        return buildString {
            appendLine("🔐 API Keys Status:")
            appendLine()
            status.forEach { (keyType, keyStatus) ->
                val icon = when (keyStatus) {
                    KeyStatus.VALID -> "✅"
                    KeyStatus.MISSING -> "❌"
                    KeyStatus.INVALID -> "⚠️"
                }
                val required = if (keyType.required) "(Required)" else "(Optional)"
                appendLine("$icon ${keyType.displayName} $required: $keyStatus")

                // Show masked preview if valid
                if (keyStatus == KeyStatus.VALID) {
                    val key = getKey(keyType)
                    if (key != null && key.length > 8) {
                        val masked = key.take(4) + "****" + key.takeLast(4)
                        appendLine("   Preview: $masked")
                    }
                }
            }
        }
    }

    companion object {
        @Volatile
        private var instance: SecureKeyManager? = null

        fun getInstance(context: Context): SecureKeyManager {
            return instance ?: synchronized(this) {
                instance ?: SecureKeyManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
