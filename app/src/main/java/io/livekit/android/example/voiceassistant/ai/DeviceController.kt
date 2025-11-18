package io.livekit.android.example.voiceassistant.ai

import android.content.Context
import android.provider.Settings
import io.livekit.android.example.voiceassistant.service.JarvisAccessibilityService
import timber.log.Timber

/**
 * DeviceController - Jarvis's hands
 * Executes device actions via Accessibility Service
 * Integrated with Gemini function calling
 */
class DeviceController(private val context: Context) {

    private val accessibilityService: JarvisAccessibilityService?
        get() = JarvisAccessibilityService.getInstance()

    /**
     * Check if accessibility service is enabled
     */
    fun isAccessibilityServiceEnabled(): Boolean {
        val service = JarvisAccessibilityService::class.java.canonicalName
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(service ?: "") == true
    }

    /**
     * Execute a device action based on Gemini function call
     * This is called when Gemini triggers a function
     */
    fun executeAction(functionName: String, parameters: Map<String, Any>): ActionResult {
        if (!isAccessibilityServiceEnabled()) {
            return ActionResult(
                success = false,
                message = "Accessibility service not enabled, Sir. Enable it in Settings for full device control."
            )
        }

        val service = accessibilityService ?: return ActionResult(
            success = false,
            message = "Accessibility service not connected, Boss."
        )

        return when (functionName) {
            "open_app" -> {
                val packageName = parameters["package_name"] as? String ?: ""
                val success = service.openApp(packageName)
                ActionResult(
                    success = success,
                    message = if (success) {
                        "Opening $packageName, Sir."
                    } else {
                        "Failed to open $packageName. App not found, Boss."
                    }
                )
            }

            "send_message" -> {
                val recipient = parameters["recipient"] as? String ?: ""
                val message = parameters["message"] as? String ?: ""
                val platform = parameters["platform"] as? String ?: "sms"

                val success = when (platform.lowercase()) {
                    "whatsapp" -> service.openWhatsAppChat(recipient)
                    "sms" -> service.sendSMS(recipient, message)
                    else -> false
                }

                ActionResult(
                    success = success,
                    message = if (success) {
                        "Sending $platform message to $recipient, Sir."
                    } else {
                        "Failed to send message, Boss. Check the recipient details."
                    }
                )
            }

            "set_reminder" -> {
                // Open clock app to set reminder
                // In future, integrate with AlarmManager directly
                val success = service.openApp("com.google.android.deskclock")
                ActionResult(
                    success = success,
                    message = "Opening Clock app for reminder, Sir. Set it manually for now."
                )
            }

            "web_search" -> {
                val query = parameters["query"] as? String ?: ""
                val url = "https://www.google.com/search?q=${query.replace(" ", "+")}"
                val success = service.openUrl(url)

                ActionResult(
                    success = success,
                    message = if (success) {
                        "Searching Google for '$query', Sir."
                    } else {
                        "Search failed, Boss."
                    }
                )
            }

            "control_media" -> {
                val action = parameters["action"] as? String ?: ""
                // Media control requires different approach
                // For now, open Spotify/YouTube Music
                val success = service.openApp("com.spotify.music")

                ActionResult(
                    success = success,
                    message = "Opening music app, Sir. $action manually for now."
                )
            }

            else -> ActionResult(
                success = false,
                message = "Unknown action: $functionName, Boss."
            )
        }
    }

    /**
     * Quick actions - commonly used device controls
     */
    fun goBack(): ActionResult {
        val service = accessibilityService ?: return ActionResult(false, "Service not available")
        val success = service.navigateBack()
        return ActionResult(success, if (success) "Going back, Sir." else "Failed to go back.")
    }

    fun goHome(): ActionResult {
        val service = accessibilityService ?: return ActionResult(false, "Service not available")
        val success = service.navigateHome()
        return ActionResult(success, if (success) "Going home, Sir." else "Failed to go home.")
    }

    fun openRecents(): ActionResult {
        val service = accessibilityService ?: return ActionResult(false, "Service not available")
        val success = service.openRecents()
        return ActionResult(success, if (success) "Opening recents, Sir." else "Failed.")
    }

    fun openNotifications(): ActionResult {
        val service = accessibilityService ?: return ActionResult(false, "Service not available")
        val success = service.openNotifications()
        return ActionResult(success, if (success) "Opening notifications, Sir." else "Failed.")
    }

    fun openQuickSettings(): ActionResult {
        val service = accessibilityService ?: return ActionResult(false, "Service not available")
        val success = service.openQuickSettings()
        return ActionResult(success, if (success) "Opening quick settings, Sir." else "Failed.")
    }

    fun openSettings(): ActionResult {
        val service = accessibilityService ?: return ActionResult(false, "Service not available")
        val success = service.openSettings()
        return ActionResult(success, if (success) "Opening settings, Sir." else "Failed.")
    }

    /**
     * App shortcuts
     */
    fun openChrome(): ActionResult {
        val service = accessibilityService ?: return ActionResult(false, "Service not available")
        val success = service.openApp("chrome")
        return ActionResult(success, if (success) "Opening Chrome, Sir." else "Chrome not found.")
    }

    fun openGmail(): ActionResult {
        val service = accessibilityService ?: return ActionResult(false, "Service not available")
        val success = service.openApp("gmail")
        return ActionResult(success, if (success) "Opening Gmail, Sir." else "Gmail not found.")
    }

    fun openWhatsApp(): ActionResult {
        val service = accessibilityService ?: return ActionResult(false, "Service not available")
        val success = service.openApp("whatsapp")
        return ActionResult(success, if (success) "Opening WhatsApp, Sir." else "WhatsApp not found.")
    }

    fun openSpotify(): ActionResult {
        val service = accessibilityService ?: return ActionResult(false, "Service not available")
        val success = service.openApp("spotify")
        return ActionResult(success, if (success) "Opening Spotify, Sir." else "Spotify not found.")
    }

    fun openYouTube(): ActionResult {
        val service = accessibilityService ?: return ActionResult(false, "Service not available")
        val success = service.openApp("youtube")
        return ActionResult(success, if (success) "Opening YouTube, Sir." else "YouTube not found.")
    }

    fun openCamera(): ActionResult {
        val service = accessibilityService ?: return ActionResult(false, "Service not available")
        val success = service.openApp("camera")
        return ActionResult(success, if (success) "Opening Camera, Sir." else "Camera not found.")
    }

    /**
     * Read current screen content
     */
    fun readCurrentScreen(): ActionResult {
        val service = accessibilityService ?: return ActionResult(
            false,
            "Accessibility service not available, Sir."
        )

        val content = service.readScreen()
        return ActionResult(true, content)
    }

    companion object {
        @Volatile
        private var instance: DeviceController? = null

        fun getInstance(context: Context): DeviceController {
            return instance ?: synchronized(this) {
                instance ?: DeviceController(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * Result of a device action
 */
data class ActionResult(
    val success: Boolean,
    val message: String
)
