package io.livekit.android.example.voiceassistant.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.net.Uri
import android.provider.ContactsContract
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import timber.log.Timber

/**
 * JarvisAccessibilityService - Full device control for Jarvis
 * "Sometimes you gotta run before you can walk" - Tony Stark
 *
 * Capabilities:
 * - Open apps by package name or app name
 * - Click buttons and UI elements
 * - Read screen content
 * - Perform gestures (tap, swipe, etc.)
 * - Navigate back/home/recents
 * - Control system settings
 */
class JarvisAccessibilityService : AccessibilityService() {

    companion object {
        private var instance: JarvisAccessibilityService? = null

        fun getInstance(): JarvisAccessibilityService? = instance

        // Action commands
        const val ACTION_OPEN_APP = "open_app"
        const val ACTION_CLICK = "click"
        const val ACTION_BACK = "back"
        const val ACTION_HOME = "home"
        const val ACTION_RECENTS = "recents"
        const val ACTION_NOTIFICATIONS = "notifications"
        const val ACTION_QUICK_SETTINGS = "quick_settings"
        const val ACTION_SWIPE = "swipe"
        const val ACTION_READ_SCREEN = "read_screen"

        // Common app package names
        val APP_PACKAGES = mapOf(
            "chrome" to "com.android.chrome",
            "gmail" to "com.google.android.gm",
            "maps" to "com.google.android.apps.maps",
            "youtube" to "com.google.android.youtube",
            "whatsapp" to "com.whatsapp",
            "telegram" to "org.telegram.messenger",
            "slack" to "com.slack",
            "spotify" to "com.spotify.music",
            "settings" to "com.android.settings",
            "camera" to "com.google.android.GoogleCamera",
            "photos" to "com.google.android.apps.photos",
            "play store" to "com.android.vending",
            "calendar" to "com.google.android.calendar",
            "clock" to "com.google.android.deskclock",
            "messages" to "com.google.android.apps.messaging",
            "phone" to "com.google.android.dialer",
            "contacts" to "com.google.android.contacts"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Timber.i("Jarvis Accessibility Service connected. Full device control enabled, Sir.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Monitor accessibility events
        // Can be used to track screen changes, detect specific UI elements, etc.
        event?.let {
            when (it.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    val packageName = it.packageName?.toString()
                    Timber.d("Window changed: $packageName")
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    // Screen content updated
                }
            }
        }
    }

    override fun onInterrupt() {
        Timber.w("Jarvis Accessibility Service interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        Timber.i("Jarvis Accessibility Service disconnected")
        return super.onUnbind(intent)
    }

    /**
     * Open an app by package name or common app name
     */
    fun openApp(appIdentifier: String): Boolean {
        return try {
            val packageName = APP_PACKAGES[appIdentifier.lowercase()] ?: appIdentifier

            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Timber.i("Opened app: $packageName")
                true
            } else {
                Timber.w("App not found: $packageName")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to open app: $appIdentifier")
            false
        }
    }

    /**
     * Navigate back (system back button)
     */
    fun navigateBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    /**
     * Navigate home
     */
    fun navigateHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    /**
     * Open recent apps
     */
    fun openRecents(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    /**
     * Open notifications
     */
    fun openNotifications(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }

    /**
     * Open quick settings
     */
    fun openQuickSettings(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
    }

    /**
     * Click on a UI element by text content
     */
    fun clickByText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false

        val targetNode = findNodeByText(rootNode, text)
        return if (targetNode != null) {
            targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            targetNode.recycle()
            Timber.i("Clicked on: $text")
            true
        } else {
            Timber.w("Element not found: $text")
            false
        }
    }

    /**
     * Find node by text content
     */
    private fun findNodeByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.toString()?.contains(text, ignoreCase = true) == true) {
            return node
        }

        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i) ?: continue
            val result = findNodeByText(childNode, text)
            if (result != null) {
                return result
            }
            childNode.recycle()
        }

        return null
    }

    /**
     * Perform tap gesture at coordinates
     */
    fun tapAt(x: Float, y: Float): Boolean {
        val path = Path().apply {
            moveTo(x, y)
        }

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 100))

        return dispatchGesture(gestureBuilder.build(), null, null)
    }

    /**
     * Perform swipe gesture
     */
    fun swipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long = 300): Boolean {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))

        return dispatchGesture(gestureBuilder.build(), null, null)
    }

    /**
     * Read all text content on current screen
     */
    fun readScreen(): String {
        val rootNode = rootInActiveWindow ?: return "Unable to read screen, Sir."

        val textContent = StringBuilder()
        extractText(rootNode, textContent)
        rootNode.recycle()

        return if (textContent.isNotEmpty()) {
            textContent.toString()
        } else {
            "No text content found on screen, Sir."
        }
    }

    /**
     * Extract text from accessibility node tree
     */
    private fun extractText(node: AccessibilityNodeInfo, textBuilder: StringBuilder) {
        node.text?.let {
            if (it.isNotBlank()) {
                textBuilder.append(it).append("\n")
            }
        }

        node.contentDescription?.let {
            if (it.isNotBlank()) {
                textBuilder.append(it).append("\n")
            }
        }

        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            if (childNode != null) {
                extractText(childNode, textBuilder)
                childNode.recycle()
            }
        }
    }

    /**
     * Make a phone call
     */
    fun makeCall(phoneNumber: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            Timber.i("Initiated call to: $phoneNumber")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to make call")
            false
        }
    }

    /**
     * Send SMS (opens messaging app)
     */
    fun sendSMS(phoneNumber: String, message: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:$phoneNumber")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            Timber.i("Opened SMS to: $phoneNumber")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to send SMS")
            false
        }
    }

    /**
     * Open WhatsApp chat (requires WhatsApp installed)
     */
    fun openWhatsAppChat(phoneNumber: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            Timber.i("Opened WhatsApp chat: $phoneNumber")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to open WhatsApp")
            false
        }
    }

    /**
     * Open URL in browser
     */
    fun openUrl(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            Timber.i("Opened URL: $url")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to open URL")
            false
        }
    }

    /**
     * Set system volume
     */
    fun setVolume(percentage: Int): Boolean {
        // This requires additional audio manager integration
        // For now, open quick settings for manual adjustment
        return openQuickSettings()
    }

    /**
     * Toggle WiFi (requires system permissions)
     */
    fun toggleWiFi(): Boolean {
        // Modern Android requires user interaction for WiFi toggle
        // Open quick settings as workaround
        return openQuickSettings()
    }

    /**
     * Toggle Bluetooth (requires system permissions)
     */
    fun toggleBluetooth(): Boolean {
        // Modern Android requires user interaction for Bluetooth toggle
        // Open quick settings as workaround
        return openQuickSettings()
    }

    /**
     * Open Settings page
     */
    fun openSettings(settingsAction: String = android.provider.Settings.ACTION_SETTINGS): Boolean {
        return try {
            val intent = Intent(settingsAction).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            Timber.i("Opened settings: $settingsAction")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to open settings")
            false
        }
    }
}
