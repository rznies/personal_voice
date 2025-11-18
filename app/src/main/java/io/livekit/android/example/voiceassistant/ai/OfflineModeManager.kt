package io.livekit.android.example.voiceassistant.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import timber.log.Timber
import java.util.*

/**
 * OfflineModeManager - Handles basic commands without internet
 * "Sir, we're flying dark. Operating on local protocols only."
 *
 * Features:
 * - Network connectivity detection
 * - Pattern-based command recognition
 * - Offline responses with Tony Stark personality
 * - Basic device control without AI
 */
class OfflineModeManager(private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Offline command patterns and responses
     */
    private val offlineCommands = mapOf(
        // Time queries
        Regex("what('s| is) the time|current time|tell me the time", RegexOption.IGNORE_CASE) to ::getCurrentTime,
        Regex("what('s| is) (the )?date|today('s| is) date", RegexOption.IGNORE_CASE) to ::getCurrentDate,

        // Battery queries
        Regex("battery|power level|how much (battery|power)", RegexOption.IGNORE_CASE) to ::getBatteryStatus,

        // App launching (basic)
        Regex("open (\\w+)", RegexOption.IGNORE_CASE) to ::openAppCommand,
        Regex("launch (\\w+)", RegexOption.IGNORE_CASE) to ::openAppCommand,

        // Device control
        Regex("go back|press back", RegexOption.IGNORE_CASE) to { "Going back, Sir." },
        Regex("go home|press home", RegexOption.IGNORE_CASE) to { "Taking you home, Boss." },
        Regex("recent apps|show recents", RegexOption.IGNORE_CASE) to { "Showing recent apps, Sir." },

        // Status queries
        Regex("are you (there|online|working)", RegexOption.IGNORE_CASE) to { "I'm here, Sir, but we're offline. Limited to basic protocols." },
        Regex("hello|hi|hey", RegexOption.IGNORE_CASE) to ::getGreeting,
        Regex("how are you", RegexOption.IGNORE_CASE) to { "Running smoothly, Sir, though I'd prefer an internet connection." },

        // Help
        Regex("help|what can you do", RegexOption.IGNORE_CASE) to ::getOfflineHelp
    )

    /**
     * Check if device has internet connectivity
     */
    fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Process command in offline mode
     * Returns null if command is not recognized
     */
    fun processOfflineCommand(command: String): OfflineCommandResult? {
        val normalizedCommand = command.trim()

        // Try to match against offline command patterns
        for ((pattern, handler) in offlineCommands) {
            val match = pattern.find(normalizedCommand)
            if (match != null) {
                return try {
                    val response = when (handler) {
                        is Function0<*> -> (handler as () -> String)()
                        is Function1<*, *> -> (handler as (MatchResult) -> String)(match)
                        else -> "Command recognized but handler error, Sir."
                    }
                    OfflineCommandResult(
                        response = response as String,
                        success = true,
                        commandType = getCommandType(pattern)
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Error processing offline command")
                    OfflineCommandResult(
                        response = "Sir, something went wrong processing that command offline.",
                        success = false
                    )
                }
            }
        }

        // No match found
        return OfflineCommandResult(
            response = getOfflineUnavailableResponse(normalizedCommand),
            success = false,
            commandType = CommandType.UNKNOWN
        )
    }

    /**
     * Get current time
     */
    private fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour

        val timeStr = String.format("%d:%02d %s", displayHour, minute, amPm)

        return when (hour) {
            in 0..5 -> "It's $timeStr, Sir. Even Tony Stark sleeps sometimes. Consider it."
            in 6..11 -> "It's $timeStr, Boss. Morning productivity awaits."
            in 12..17 -> "It's $timeStr. Prime working hours, Sir."
            in 18..21 -> "It's $timeStr, Boss. Evening's here."
            else -> "It's $timeStr, Sir. Late night innovation session?"
        }
    }

    /**
     * Get current date
     */
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val year = calendar.get(Calendar.YEAR)

        return "Today is $dayOfWeek, $month $day, $year, Sir."
    }

    /**
     * Get battery status
     */
    private fun getBatteryStatus(): String {
        // This would integrate with BatteryOptimizationManager
        return "Battery status check requires system integration, Sir. Use the battery optimization manager."
    }

    /**
     * Handle app opening command
     */
    private fun openAppCommand(match: MatchResult): String {
        val appName = match.groups[1]?.value ?: "unknown"
        return "Opening $appName, Sir. This requires device controller integration."
    }

    /**
     * Get greeting based on time of day
     */
    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..5 -> "Sir, it's ${hour} AM. This is concerning even for a genius."
            in 6..11 -> "Good morning, Boss. Ready to conquer the day?"
            in 12..17 -> "Good afternoon, Sir. How's the empire building?"
            in 18..21 -> "Good evening, Boss. Still grinding?"
            else -> "Working late again, Sir? Tony would approve."
        }
    }

    /**
     * Get offline help message
     */
    private fun getOfflineHelp(): String {
        return """
            Sir, we're offline. Available commands:

            • "What's the time?" - Current time
            • "What's the date?" - Today's date
            • "Open [app]" - Launch apps
            • "Go back/home" - Navigation
            • "Battery status" - Power levels

            For full capabilities, I need an internet connection. The AI needs its cloud, Boss.
        """.trimIndent()
    }

    /**
     * Get response when command is unavailable offline
     */
    private fun getOfflineUnavailableResponse(command: String): String {
        val responses = listOf(
            "Sir, that command requires internet. I'm running on backup protocols only.",
            "Boss, I need a connection for that. Even Jarvis can't work miracles offline.",
            "That's beyond my offline capabilities, Sir. Restore internet and I'll handle it.",
            "I'd love to help with that, but I need network access. Priorities, Sir.",
            "Command recognized, but I need the cloud for that one, Boss."
        )
        return responses.random()
    }

    /**
     * Get command type from pattern
     */
    private fun getCommandType(pattern: Regex): CommandType {
        return when {
            pattern.pattern.contains("time", ignoreCase = true) -> CommandType.TIME_QUERY
            pattern.pattern.contains("date", ignoreCase = true) -> CommandType.DATE_QUERY
            pattern.pattern.contains("battery|power", ignoreCase = true) -> CommandType.BATTERY_QUERY
            pattern.pattern.contains("open|launch", ignoreCase = true) -> CommandType.APP_LAUNCH
            pattern.pattern.contains("back|home|recent", ignoreCase = true) -> CommandType.NAVIGATION
            pattern.pattern.contains("hello|hi|hey|greeting", ignoreCase = true) -> CommandType.GREETING
            pattern.pattern.contains("help", ignoreCase = true) -> CommandType.HELP
            else -> CommandType.UNKNOWN
        }
    }

    companion object {
        @Volatile
        private var instance: OfflineModeManager? = null

        fun getInstance(context: Context): OfflineModeManager {
            return instance ?: synchronized(this) {
                instance ?: OfflineModeManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * Offline command result
 */
data class OfflineCommandResult(
    val response: String,
    val success: Boolean,
    val commandType: CommandType = CommandType.UNKNOWN,
    val requiresDeviceController: Boolean = false
)

/**
 * Command types for offline mode
 */
enum class CommandType {
    TIME_QUERY,
    DATE_QUERY,
    BATTERY_QUERY,
    APP_LAUNCH,
    NAVIGATION,
    GREETING,
    HELP,
    UNKNOWN
}
