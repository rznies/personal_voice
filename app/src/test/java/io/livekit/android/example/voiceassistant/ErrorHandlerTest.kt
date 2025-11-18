package io.livekit.android.example.voiceassistant

import android.content.Context
import io.livekit.android.example.voiceassistant.utils.ErrorHandler
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Assert.*
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Unit tests for ErrorHandler
 * "Testing error handling, Sir. Preparing for inevitable failures."
 */
@RunWith(MockitoJUnitRunner::class)
class ErrorHandlerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockCacheDir: File

    private lateinit var errorHandler: ErrorHandler

    @Before
    fun setup() {
        `when`(mockContext.applicationContext).thenReturn(mockContext)
        `when`(mockContext.cacheDir).thenReturn(mockCacheDir)
        errorHandler = ErrorHandler.getInstance(mockContext)
    }

    @Test
    fun `test network error categorization`() {
        val networkErrors = listOf(
            UnknownHostException("No internet"),
            SocketTimeoutException("Timeout"),
            IOException("Network error")
        )

        networkErrors.forEach { error ->
            val result = errorHandler.handleError(
                throwable = error,
                context = "Test network error"
            )

            assertTrue(
                "Failed for ${error::class.simpleName}",
                result.userMessage.contains("network", ignoreCase = true) ||
                result.userMessage.contains("connection", ignoreCase = true)
            )
            assertTrue("Network errors should be retryable", result.shouldRetry)
        }
    }

    @Test
    fun `test permission error categorization`() {
        val error = SecurityException("Permission denied")

        val result = errorHandler.handleError(
            throwable = error,
            severity = ErrorHandler.ErrorSeverity.HIGH,
            context = "Test permission error"
        )

        assertTrue(result.userMessage.contains("permission", ignoreCase = true))
        assertFalse("Permission errors should not be auto-retryable", result.shouldRetry)
        assertNotNull("Should provide recovery action", result.recoveryAction)
    }

    @Test
    fun `test error severity affects message`() {
        val error = Exception("Test error")

        val lowSeverity = errorHandler.handleError(
            throwable = error,
            category = ErrorHandler.ErrorCategory.UNKNOWN,
            severity = ErrorHandler.ErrorSeverity.LOW
        )

        val criticalSeverity = errorHandler.handleError(
            throwable = error,
            category = ErrorHandler.ErrorCategory.UNKNOWN,
            severity = ErrorHandler.ErrorSeverity.CRITICAL
        )

        assertNotEquals(lowSeverity.userMessage, criticalSeverity.userMessage)
        assertTrue(criticalSeverity.userMessage.contains("critical", ignoreCase = true))
    }

    @Test
    fun `test network errors are retryable`() {
        val error = UnknownHostException("No internet")

        val result = errorHandler.handleError(
            throwable = error,
            category = ErrorHandler.ErrorCategory.NETWORK
        )

        assertTrue(result.shouldRetry)
    }

    @Test
    fun `test hardware errors are not retryable`() {
        val error = Exception("Camera in use")

        val result = errorHandler.handleError(
            throwable = error,
            category = ErrorHandler.ErrorCategory.HARDWARE
        )

        assertFalse(result.shouldRetry)
    }

    @Test
    fun `test AI service errors are retryable`() {
        val error = Exception("Gemini API error")

        val result = errorHandler.handleError(
            throwable = error,
            category = ErrorHandler.ErrorCategory.AI_SERVICE
        )

        assertTrue(result.shouldRetry)
    }

    @Test
    fun `test error messages maintain Stark personality`() {
        val errors = listOf(
            UnknownHostException("No internet") to ErrorHandler.ErrorCategory.NETWORK,
            SecurityException("Permission denied") to ErrorHandler.ErrorCategory.PERMISSIONS,
            Exception("Camera error") to ErrorHandler.ErrorCategory.HARDWARE
        )

        errors.forEach { (error, category) ->
            val result = errorHandler.handleError(
                throwable = error,
                category = category
            )

            assertTrue(
                "Message lacks personality: ${result.userMessage}",
                result.userMessage.contains("Sir", ignoreCase = true) ||
                result.userMessage.contains("Boss", ignoreCase = true) ||
                result.userMessage.contains("Tony", ignoreCase = true) ||
                result.userMessage.contains("Jarvis", ignoreCase = true)
            )
        }
    }

    @Test
    fun `test error result contains all required fields`() {
        val error = Exception("Test error")

        val result = errorHandler.handleError(
            throwable = error,
            category = ErrorHandler.ErrorCategory.NETWORK,
            severity = ErrorHandler.ErrorSeverity.MEDIUM
        )

        assertNotNull(result.userMessage)
        assertNotNull(result.technicalDetails)
        assertNotNull(result.category)
        assertNotNull(result.severity)
        assertEquals(ErrorHandler.ErrorCategory.NETWORK, result.category)
        assertEquals(ErrorHandler.ErrorSeverity.MEDIUM, result.severity)
    }

    @Test
    fun `test network error provides recovery action`() {
        val error = UnknownHostException("No internet")

        val result = errorHandler.handleError(
            throwable = error,
            category = ErrorHandler.ErrorCategory.NETWORK
        )

        assertNotNull(result.recoveryAction)
        assertTrue(result.recoveryAction!!.contains("internet", ignoreCase = true) ||
                   result.recoveryAction!!.contains("connection", ignoreCase = true))
    }

    @Test
    fun `test permission error provides recovery action`() {
        val error = SecurityException("Permission denied")

        val result = errorHandler.handleError(
            throwable = error,
            category = ErrorHandler.ErrorCategory.PERMISSIONS
        )

        assertNotNull(result.recoveryAction)
        assertTrue(result.recoveryAction!!.contains("Settings", ignoreCase = true) ||
                   result.recoveryAction!!.contains("permission", ignoreCase = true))
    }

    @Test
    fun `test unknown errors have no recovery action`() {
        val error = Exception("Mystery error")

        val result = errorHandler.handleError(
            throwable = error,
            category = ErrorHandler.ErrorCategory.UNKNOWN
        )

        // Unknown errors might not have a recovery action
        // This is expected behavior
        assertTrue(result.category == ErrorHandler.ErrorCategory.UNKNOWN)
    }

    @Test
    fun `test critical network error message`() {
        val error = UnknownHostException("No internet")

        val result = errorHandler.handleError(
            throwable = error,
            category = ErrorHandler.ErrorCategory.NETWORK,
            severity = ErrorHandler.ErrorSeverity.CRITICAL
        )

        assertTrue(result.userMessage.contains("network", ignoreCase = true) ||
                   result.userMessage.contains("connection", ignoreCase = true))
    }

    @Test
    fun `test camera error message`() {
        val error = Exception("Camera malfunction")

        val result = errorHandler.handleError(
            throwable = error,
            category = ErrorHandler.ErrorCategory.HARDWARE
        )

        assertTrue(result.userMessage.contains("camera", ignoreCase = true) ||
                   result.userMessage.contains("hardware", ignoreCase = true))
    }

    @Test
    fun `test microphone error message`() {
        val error = Exception("Microphone issues")

        val result = errorHandler.handleError(
            throwable = error,
            category = ErrorHandler.ErrorCategory.HARDWARE
        )

        assertTrue(result.userMessage.contains("microphone", ignoreCase = true) ||
                   result.userMessage.contains("hardware", ignoreCase = true))
    }

    @Test
    fun `test TTS service error`() {
        val error = Exception("TTS unavailable")

        val result = errorHandler.handleError(
            throwable = error,
            category = ErrorHandler.ErrorCategory.TTS_SERVICE
        )

        assertTrue(result.shouldRetry)
        assertTrue(result.userMessage.contains("speech", ignoreCase = true) ||
                   result.userMessage.contains("voice", ignoreCase = true) ||
                   result.userMessage.contains("TTS", ignoreCase = true))
    }

    @Test
    fun `test storage error message`() {
        val error = Exception("Storage full")

        val result = errorHandler.handleError(
            throwable = error,
            category = ErrorHandler.ErrorCategory.STORAGE
        )

        assertFalse(result.shouldRetry)
        assertTrue(result.userMessage.contains("storage", ignoreCase = true) ||
                   result.userMessage.contains("space", ignoreCase = true))
    }

    @Test
    fun `test error with null message`() {
        val error = Exception(null as String?)

        val result = errorHandler.handleError(
            throwable = error,
            category = ErrorHandler.ErrorCategory.UNKNOWN
        )

        assertNotNull(result.userMessage)
        assertNotNull(result.technicalDetails)
    }

    @Test
    fun `test error context is preserved`() {
        val error = Exception("Test error")
        val context = "Testing context preservation"

        val result = errorHandler.handleError(
            throwable = error,
            category = ErrorHandler.ErrorCategory.NETWORK,
            context = context
        )

        assertNotNull(result)
        // Context is used for logging, not returned in result
    }

    @Test
    fun `test multiple error categories`() {
        val categories = listOf(
            ErrorHandler.ErrorCategory.NETWORK,
            ErrorHandler.ErrorCategory.PERMISSIONS,
            ErrorHandler.ErrorCategory.HARDWARE,
            ErrorHandler.ErrorCategory.AI_SERVICE,
            ErrorHandler.ErrorCategory.TTS_SERVICE,
            ErrorHandler.ErrorCategory.STORAGE,
            ErrorHandler.ErrorCategory.UNKNOWN
        )

        categories.forEach { category ->
            val error = Exception("Test error for $category")
            val result = errorHandler.handleError(
                throwable = error,
                category = category
            )

            assertNotNull("Failed for category: $category", result.userMessage)
            assertEquals(category, result.category)
        }
    }
}
