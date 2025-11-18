package io.livekit.android.example.voiceassistant.utils

import android.content.Context
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

/**
 * ErrorHandler - Production error handling and crash recovery
 * "Sir, I've detected a malfunction. Running diagnostics and implementing countermeasures."
 *
 * Features:
 * - Global exception handler
 * - Crash recovery strategies
 * - User-friendly error messages (with Tony Stark flair)
 * - Error logging and analytics integration points
 */
class ErrorHandler private constructor(private val context: Context) {

    /**
     * Error severity levels
     */
    enum class ErrorSeverity {
        LOW,        // Minor issues, app continues normally
        MEDIUM,     // Feature degradation, but core functions work
        HIGH,       // Major feature broken, user experience affected
        CRITICAL    // App crash or data loss risk
    }

    /**
     * Error categories
     */
    enum class ErrorCategory {
        NETWORK,            // API calls, connectivity
        PERMISSIONS,        // Missing permissions
        HARDWARE,           // Camera, mic, sensors
        AI_SERVICE,         // Gemini API errors
        TTS_SERVICE,        // Text-to-speech errors
        STORAGE,            // DataStore, file operations
        UNKNOWN             // Uncategorized errors
    }

    /**
     * Initialize global exception handler
     */
    fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleCrash(thread, throwable)

            // Call default handler to ensure proper app termination
            defaultHandler?.uncaughtException(thread, throwable)
        }

        Timber.i("Global exception handler initialized. Jarvis is watching for errors, Sir.")
    }

    /**
     * Handle app crash
     */
    private fun handleCrash(thread: Thread, throwable: Throwable) {
        try {
            val crashReport = generateCrashReport(thread, throwable)
            saveCrashReport(crashReport)

            Timber.e(throwable, "CRITICAL CRASH on thread: ${thread.name}")

            // TODO: Send to analytics service (Firebase Crashlytics, Sentry, etc.)
            // sendCrashToAnalytics(crashReport)

        } catch (e: Exception) {
            Timber.e(e, "Error while handling crash - inception level failure")
        }
    }

    /**
     * Generate comprehensive crash report
     */
    private fun generateCrashReport(thread: Thread, throwable: Throwable): CrashReport {
        val stackTrace = StringWriter().apply {
            throwable.printStackTrace(PrintWriter(this))
        }.toString()

        return CrashReport(
            timestamp = System.currentTimeMillis(),
            threadName = thread.name,
            exceptionType = throwable::class.java.simpleName,
            message = throwable.message ?: "No message",
            stackTrace = stackTrace,
            category = categorizeError(throwable),
            severity = ErrorSeverity.CRITICAL
        )
    }

    /**
     * Save crash report to local storage
     */
    private fun saveCrashReport(report: CrashReport) {
        try {
            // Save to app's cache directory
            val crashFile = context.cacheDir.resolve("crash_${report.timestamp}.txt")
            crashFile.writeText(report.toString())
            Timber.i("Crash report saved: ${crashFile.absolutePath}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save crash report")
        }
    }

    /**
     * Handle recoverable errors
     */
    fun handleError(
        throwable: Throwable,
        category: ErrorCategory = ErrorCategory.UNKNOWN,
        severity: ErrorSeverity = ErrorSeverity.MEDIUM,
        context: String = ""
    ): ErrorResult {
        Timber.e(throwable, "Error [$category/$severity]: $context")

        val userMessage = generateUserFriendlyMessage(throwable, category, severity)
        val shouldRetry = isRetryable(throwable, category)
        val recoveryAction = suggestRecoveryAction(throwable, category)

        return ErrorResult(
            userMessage = userMessage,
            shouldRetry = shouldRetry,
            recoveryAction = recoveryAction,
            technicalDetails = throwable.message ?: "Unknown error",
            category = category,
            severity = severity
        )
    }

    /**
     * Categorize error by exception type
     */
    private fun categorizeError(throwable: Throwable): ErrorCategory {
        return when {
            throwable is java.net.UnknownHostException ||
            throwable is java.net.SocketTimeoutException ||
            throwable is java.io.IOException -> ErrorCategory.NETWORK

            throwable is SecurityException -> ErrorCategory.PERMISSIONS

            throwable.message?.contains("camera", ignoreCase = true) == true ||
            throwable.message?.contains("microphone", ignoreCase = true) == true ||
            throwable.message?.contains("sensor", ignoreCase = true) == true -> ErrorCategory.HARDWARE

            throwable.message?.contains("gemini", ignoreCase = true) == true ||
            throwable.message?.contains("api", ignoreCase = true) == true -> ErrorCategory.AI_SERVICE

            throwable.message?.contains("tts", ignoreCase = true) == true ||
            throwable.message?.contains("speech", ignoreCase = true) == true -> ErrorCategory.TTS_SERVICE

            throwable is java.io.FileNotFoundException ||
            throwable.message?.contains("datastore", ignoreCase = true) == true -> ErrorCategory.STORAGE

            else -> ErrorCategory.UNKNOWN
        }
    }

    /**
     * Generate user-friendly error message with Tony Stark personality
     */
    private fun generateUserFriendlyMessage(
        throwable: Throwable,
        category: ErrorCategory,
        severity: ErrorSeverity
    ): String {
        return when (category) {
            ErrorCategory.NETWORK -> when (severity) {
                ErrorSeverity.CRITICAL -> "Sir, we've lost connection to all networks. Even Tony needs WiFi sometimes."
                ErrorSeverity.HIGH -> "Network issues detected, Boss. Check your connection or I'm flying blind."
                else -> "Minor network hiccup, Sir. Retrying with Stark-level persistence."
            }

            ErrorCategory.PERMISSIONS -> "Sir, I need additional permissions to access that. Even Jarvis follows protocol."

            ErrorCategory.HARDWARE -> when {
                throwable.message?.contains("camera", ignoreCase = true) == true ->
                    "Camera malfunction, Sir. Check if another app is hogging it, or if you covered the lens. Again."
                throwable.message?.contains("microphone", ignoreCase = true) == true ->
                    "Microphone issues, Boss. I can't hear you. Ironic, I know."
                else -> "Hardware sensor error, Sir. Did you drop the phone? Be honest."
            }

            ErrorCategory.AI_SERVICE -> when (severity) {
                ErrorSeverity.CRITICAL -> "Sir, the AI service is down. Even Jarvis needs his cloud."
                ErrorSeverity.HIGH -> "Gemini API having issues, Boss. Running offline fallback protocols."
                else -> "Minor AI hiccup. Processing with reduced genius levels."
            }

            ErrorCategory.TTS_SERVICE -> "Text-to-speech malfunction, Sir. You'll have to read my responses. The horror."

            ErrorCategory.STORAGE -> "Storage error detected, Boss. Check available space or permissions."

            ErrorCategory.UNKNOWN -> when (severity) {
                ErrorSeverity.CRITICAL -> "Critical error, Sir. Something went very wrong. Even I'm confused."
                ErrorSeverity.HIGH -> "Unexpected error, Boss. Running diagnostics."
                else -> "Minor glitch in the matrix, Sir. Handled."
            }
        }
    }

    /**
     * Check if error is retryable
     */
    private fun isRetryable(throwable: Throwable, category: ErrorCategory): Boolean {
        return when (category) {
            ErrorCategory.NETWORK -> true // Network errors are usually temporary
            ErrorCategory.AI_SERVICE -> true // API errors might be temporary
            ErrorCategory.PERMISSIONS -> false // Need user action
            ErrorCategory.HARDWARE -> false // Hardware issues need manual fix
            ErrorCategory.TTS_SERVICE -> true // Can try different backend
            ErrorCategory.STORAGE -> false // Usually needs manual intervention
            ErrorCategory.UNKNOWN -> false // Don't retry unknown errors
        }
    }

    /**
     * Suggest recovery action
     */
    private fun suggestRecoveryAction(throwable: Throwable, category: ErrorCategory): String? {
        return when (category) {
            ErrorCategory.NETWORK -> "Check your internet connection, Sir."
            ErrorCategory.PERMISSIONS -> "Grant required permissions in Settings → Apps → StarkJarvis → Permissions"
            ErrorCategory.HARDWARE -> "Close other apps using the hardware and try again, Boss."
            ErrorCategory.AI_SERVICE -> "Check your API key configuration or try again later."
            ErrorCategory.TTS_SERVICE -> "Try switching TTS backend in settings."
            ErrorCategory.STORAGE -> "Free up storage space or check app permissions."
            else -> null
        }
    }

    /**
     * Check if crash reports exist (for recovery on next launch)
     */
    fun hasPreviousCrashes(): Boolean {
        val crashFiles = context.cacheDir.listFiles { file ->
            file.name.startsWith("crash_") && file.name.endsWith(".txt")
        }
        return !crashFiles.isNullOrEmpty()
    }

    /**
     * Get crash recovery message
     */
    fun getCrashRecoveryMessage(): String {
        return "Sir, I detected a previous crash. Diagnostics complete. Systems restored. Try not to break me again."
    }

    /**
     * Clear old crash reports
     */
    fun clearCrashReports() {
        try {
            context.cacheDir.listFiles { file ->
                file.name.startsWith("crash_") && file.name.endsWith(".txt")
            }?.forEach { it.delete() }

            Timber.i("Crash reports cleared")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear crash reports")
        }
    }

    companion object {
        @Volatile
        private var instance: ErrorHandler? = null

        fun getInstance(context: Context): ErrorHandler {
            return instance ?: synchronized(this) {
                instance ?: ErrorHandler(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * Crash report data class
 */
data class CrashReport(
    val timestamp: Long,
    val threadName: String,
    val exceptionType: String,
    val message: String,
    val stackTrace: String,
    val category: ErrorHandler.ErrorCategory,
    val severity: ErrorHandler.ErrorSeverity
) {
    override fun toString(): String {
        return """
            === JARVIS CRASH REPORT ===
            Timestamp: $timestamp
            Thread: $threadName
            Exception: $exceptionType
            Message: $message
            Category: $category
            Severity: $severity

            Stack Trace:
            $stackTrace

            === END CRASH REPORT ===
        """.trimIndent()
    }
}

/**
 * Error result for recoverable errors
 */
data class ErrorResult(
    val userMessage: String,
    val shouldRetry: Boolean,
    val recoveryAction: String?,
    val technicalDetails: String,
    val category: ErrorHandler.ErrorCategory,
    val severity: ErrorHandler.ErrorSeverity
)
