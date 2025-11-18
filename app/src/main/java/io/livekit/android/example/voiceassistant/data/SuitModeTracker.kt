package io.livekit.android.example.voiceassistant.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * SuitModeTracker - Tracks entrepreneur productivity metrics
 * "Jarvis, how's my empire building today?"
 *
 * Tracks:
 * - Lines of code shipped
 * - Work sessions and duration
 * - Goals completed
 * - Coffee consumed (for roasting purposes)
 * - Late night sessions (3 AM+ work)
 * - Procrastination warnings
 */

// DataStore extension
private val Context.suitModeDataStore: DataStore<Preferences> by preferencesDataStore(name = "suit_mode_prefs")

class SuitModeTracker(private val context: Context) {

    private val dataStore = context.suitModeDataStore

    // Preference keys
    private object Keys {
        val LINES_OF_CODE_TODAY = intPreferencesKey("lines_of_code_today")
        val LINES_OF_CODE_TOTAL = intPreferencesKey("lines_of_code_total")
        val WORK_SESSIONS_TODAY = intPreferencesKey("work_sessions_today")
        val TOTAL_WORK_MINUTES_TODAY = intPreferencesKey("total_work_minutes_today")
        val GOALS_COMPLETED_TODAY = intPreferencesKey("goals_completed_today")
        val GOALS_TOTAL = intPreferencesKey("goals_total")
        val COFFEE_COUNT_TODAY = intPreferencesKey("coffee_count_today")
        val LATE_NIGHT_SESSIONS = intPreferencesKey("late_night_sessions")
        val PROCRASTINATION_WARNINGS = intPreferencesKey("procrastination_warnings")
        val CURRENT_SESSION_START = longPreferencesKey("current_session_start")
        val LAST_RESET_DATE = stringPreferencesKey("last_reset_date")
        val SUIT_MODE_ACTIVE = booleanPreferencesKey("suit_mode_active")
        val TOTAL_SESSIONS_ALL_TIME = intPreferencesKey("total_sessions_all_time")
    }

    /**
     * Get current productivity stats
     */
    val productivityStats: Flow<ProductivityStats> = dataStore.data.map { prefs ->
        ProductivityStats(
            linesOfCodeToday = prefs[Keys.LINES_OF_CODE_TODAY] ?: 0,
            linesOfCodeTotal = prefs[Keys.LINES_OF_CODE_TOTAL] ?: 0,
            workSessionsToday = prefs[Keys.WORK_SESSIONS_TODAY] ?: 0,
            totalWorkMinutesToday = prefs[Keys.TOTAL_WORK_MINUTES_TODAY] ?: 0,
            goalsCompletedToday = prefs[Keys.GOALS_COMPLETED_TODAY] ?: 0,
            goalsTotal = prefs[Keys.GOALS_TOTAL] ?: 0,
            coffeeCountToday = prefs[Keys.COFFEE_COUNT_TODAY] ?: 0,
            lateNightSessions = prefs[Keys.LATE_NIGHT_SESSIONS] ?: 0,
            procrastinationWarnings = prefs[Keys.PROCRASTINATION_WARNINGS] ?: 0,
            isSuitModeActive = prefs[Keys.SUIT_MODE_ACTIVE] ?: false,
            currentSessionStartTime = prefs[Keys.CURRENT_SESSION_START],
            totalSessionsAllTime = prefs[Keys.TOTAL_SESSIONS_ALL_TIME] ?: 0
        )
    }

    /**
     * Activate Suit Mode
     */
    suspend fun activateSuitMode() {
        checkAndResetDailyStats()

        dataStore.edit { prefs ->
            prefs[Keys.SUIT_MODE_ACTIVE] = true
            prefs[Keys.CURRENT_SESSION_START] = System.currentTimeMillis()
            prefs[Keys.WORK_SESSIONS_TODAY] = (prefs[Keys.WORK_SESSIONS_TODAY] ?: 0) + 1
            prefs[Keys.TOTAL_SESSIONS_ALL_TIME] = (prefs[Keys.TOTAL_SESSIONS_ALL_TIME] ?: 0) + 1

            // Check if it's late night (midnight to 5 AM)
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (hour in 0..4) {
                prefs[Keys.LATE_NIGHT_SESSIONS] = (prefs[Keys.LATE_NIGHT_SESSIONS] ?: 0) + 1
            }
        }

        Timber.i("Suit Mode activated. Let's build an empire, Sir.")
    }

    /**
     * Deactivate Suit Mode
     */
    suspend fun deactivateSuitMode() {
        val startTime = dataStore.data.first()[Keys.CURRENT_SESSION_START]
        if (startTime != null) {
            val duration = (System.currentTimeMillis() - startTime) / 60000 // Minutes
            dataStore.edit { prefs ->
                prefs[Keys.SUIT_MODE_ACTIVE] = false
                prefs[Keys.TOTAL_WORK_MINUTES_TODAY] = (prefs[Keys.TOTAL_WORK_MINUTES_TODAY] ?: 0) + duration.toInt()
                prefs[Keys.CURRENT_SESSION_START] = 0L
            }
            Timber.i("Suit Mode deactivated. Session duration: $duration minutes")
        }
    }

    /**
     * Log lines of code shipped
     */
    suspend fun logLinesOfCode(lines: Int) {
        checkAndResetDailyStats()

        dataStore.edit { prefs ->
            prefs[Keys.LINES_OF_CODE_TODAY] = (prefs[Keys.LINES_OF_CODE_TODAY] ?: 0) + lines
            prefs[Keys.LINES_OF_CODE_TOTAL] = (prefs[Keys.LINES_OF_CODE_TOTAL] ?: 0) + lines
        }

        Timber.d("Logged $lines lines of code. Total today: ${dataStore.data.first()[Keys.LINES_OF_CODE_TODAY]}")
    }

    /**
     * Mark goal as completed
     */
    suspend fun completeGoal() {
        checkAndResetDailyStats()

        dataStore.edit { prefs ->
            prefs[Keys.GOALS_COMPLETED_TODAY] = (prefs[Keys.GOALS_COMPLETED_TODAY] ?: 0) + 1
            prefs[Keys.GOALS_TOTAL] = (prefs[Keys.GOALS_TOTAL] ?: 0) + 1
        }

        Timber.i("Goal completed! Total today: ${dataStore.data.first()[Keys.GOALS_COMPLETED_TODAY]}")
    }

    /**
     * Log coffee consumption (for roasting)
     */
    suspend fun logCoffee() {
        checkAndResetDailyStats()

        dataStore.edit { prefs ->
            val newCount = (prefs[Keys.COFFEE_COUNT_TODAY] ?: 0) + 1
            prefs[Keys.COFFEE_COUNT_TODAY] = newCount

            // Roast if excessive coffee
            if (newCount >= 5) {
                Timber.w("Coffee count: $newCount - Sir, that's excessive even for a genius.")
            }
        }
    }

    /**
     * Log procrastination warning
     */
    suspend fun logProcrastination() {
        dataStore.edit { prefs ->
            prefs[Keys.PROCRASTINATION_WARNINGS] = (prefs[Keys.PROCRASTINATION_WARNINGS] ?: 0) + 1
        }
        Timber.w("Procrastination detected. Get back to work, Boss.")
    }

    /**
     * Get productivity summary with Jarvis commentary
     */
    suspend fun getProductivitySummary(): String {
        val stats = productivityStats.first()

        return buildString {
            appendLine("🦾 **Suit Mode Report - ${getCurrentDate()}**")
            appendLine()
            appendLine("**Productivity Metrics:**")
            appendLine("• Lines shipped: ${stats.linesOfCodeToday} (Total: ${stats.linesOfCodeTotal})")
            appendLine("• Work sessions: ${stats.workSessionsToday}")
            appendLine("• Time invested: ${stats.totalWorkMinutesToday} minutes (${stats.totalWorkMinutesToday / 60}h ${stats.totalWorkMinutesToday % 60}m)")
            appendLine("• Goals crushed: ${stats.goalsCompletedToday} (Total: ${stats.goalsTotal})")
            appendLine()
            appendLine("**Lifestyle Metrics:**")
            appendLine("• Coffee consumed: ${stats.coffeeCountToday} cups")
            appendLine("• Late night sessions: ${stats.lateNightSessions}")
            appendLine("• Procrastination warnings: ${stats.procrastinationWarnings}")
            appendLine()

            // Jarvis commentary
            appendLine("**Jarvis's Verdict:**")
            val commentary = when {
                stats.linesOfCodeToday >= 500 -> "Impressive output, Sir. At this rate, Stark Tower will build itself."
                stats.linesOfCodeToday >= 200 -> "Solid progress, Boss. You're ${(stats.linesOfCodeToday / 500.0 * 100).toInt()}% to genius levels."
                stats.linesOfCodeToday >= 50 -> "Decent effort, Sir. But Tony wouldn't call it quits yet."
                else -> "Sir, with ${stats.linesOfCodeToday} lines, you're barely past 'hello world.' Ship or sleep?"
            }
            appendLine(commentary)

            // Coffee roast
            if (stats.coffeeCountToday >= 5) {
                appendLine("\n⚠️ That's your ${stats.coffeeCountToday}th coffee, Sir. Your heart rate concerns me.")
            }

            // Late night roast
            if (stats.lateNightSessions > 0) {
                appendLine("\n🌙 ${stats.lateNightSessions} late night sessions. Sleep is for the weak. Or the smart. Your call, Boss.")
            }

            // Completion percentage
            val completionRate = if (stats.goalsTotal > 0) {
                (stats.goalsCompletedToday.toFloat() / 3 * 100).toInt() // Assume 3 goals/day
            } else 0

            appendLine("\n📊 Daily goal completion: $completionRate%")
            when {
                completionRate >= 100 -> appendLine("Multi-millionaire mode: ENGAGED 🚀")
                completionRate >= 66 -> appendLine("On track for Stark Industries levels, Sir.")
                completionRate >= 33 -> appendLine("You're halfway there. Don't stop now, Boss.")
                else -> appendLine("Sir, your empire won't build itself. Let's go.")
            }
        }
    }

    /**
     * Check if we need to reset daily stats (new day)
     */
    private suspend fun checkAndResetDailyStats() {
        val today = getCurrentDate()
        val lastReset = dataStore.data.first()[Keys.LAST_RESET_DATE]

        if (lastReset != today) {
            dataStore.edit { prefs ->
                prefs[Keys.LINES_OF_CODE_TODAY] = 0
                prefs[Keys.WORK_SESSIONS_TODAY] = 0
                prefs[Keys.TOTAL_WORK_MINUTES_TODAY] = 0
                prefs[Keys.GOALS_COMPLETED_TODAY] = 0
                prefs[Keys.COFFEE_COUNT_TODAY] = 0
                prefs[Keys.PROCRASTINATION_WARNINGS] = 0
                prefs[Keys.LAST_RESET_DATE] = today
            }
            Timber.i("Daily stats reset for new day: $today")
        }
    }

    /**
     * Get current date as string (YYYY-MM-DD)
     */
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    /**
     * Reset all stats (use with caution)
     */
    suspend fun resetAllStats() {
        dataStore.edit { it.clear() }
        Timber.w("All Suit Mode stats reset")
    }

    companion object {
        @Volatile
        private var instance: SuitModeTracker? = null

        fun getInstance(context: Context): SuitModeTracker {
            return instance ?: synchronized(this) {
                instance ?: SuitModeTracker(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * Productivity statistics data class
 */
data class ProductivityStats(
    val linesOfCodeToday: Int = 0,
    val linesOfCodeTotal: Int = 0,
    val workSessionsToday: Int = 0,
    val totalWorkMinutesToday: Int = 0,
    val goalsCompletedToday: Int = 0,
    val goalsTotal: Int = 0,
    val coffeeCountToday: Int = 0,
    val lateNightSessions: Int = 0,
    val procrastinationWarnings: Int = 0,
    val isSuitModeActive: Boolean = false,
    val currentSessionStartTime: Long? = null,
    val totalSessionsAllTime: Int = 0
) {
    /**
     * Get daily completion percentage (0-100)
     */
    fun getDailyCompletionPercentage(targetGoals: Int = 3): Int {
        return if (targetGoals > 0) {
            (goalsCompletedToday.toFloat() / targetGoals * 100).toInt().coerceIn(0, 100)
        } else 0
    }

    /**
     * Get work hours today
     */
    fun getWorkHoursToday(): String {
        val hours = totalWorkMinutesToday / 60
        val minutes = totalWorkMinutesToday % 60
        return "${hours}h ${minutes}m"
    }

    /**
     * Check if user needs coffee roast
     */
    fun needsCoffeeRoast(): Boolean = coffeeCountToday >= 5

    /**
     * Check if user is pulling an all-nighter
     */
    fun isAllNighter(): Boolean = lateNightSessions > 0
}
