package io.livekit.android.example.voiceassistant

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import io.livekit.android.example.voiceassistant.ai.CommandType
import io.livekit.android.example.voiceassistant.ai.OfflineModeManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Assert.*

/**
 * Unit tests for OfflineModeManager
 * "Testing offline capabilities, Sir. Because even Jarvis can lose signal."
 */
@RunWith(MockitoJUnitRunner::class)
class OfflineModeManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockConnectivityManager: ConnectivityManager

    private lateinit var offlineModeManager: OfflineModeManager

    @Before
    fun setup() {
        `when`(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE))
            .thenReturn(mockConnectivityManager)
        offlineModeManager = OfflineModeManager(mockContext)
    }

    @Test
    fun `test time query recognition`() {
        val result = offlineModeManager.processOfflineCommand("what's the time")

        assertNotNull(result)
        assertTrue(result!!.success)
        assertEquals(CommandType.TIME_QUERY, result.commandType)
        assertTrue(result.response.contains("AM") || result.response.contains("PM"))
    }

    @Test
    fun `test time query variations`() {
        val commands = listOf(
            "what's the time",
            "what is the time",
            "current time",
            "tell me the time"
        )

        commands.forEach { command ->
            val result = offlineModeManager.processOfflineCommand(command)
            assertNotNull("Failed for command: $command", result)
            assertTrue("Failed for command: $command", result!!.success)
            assertEquals(CommandType.TIME_QUERY, result.commandType)
        }
    }

    @Test
    fun `test date query recognition`() {
        val result = offlineModeManager.processOfflineCommand("what's the date")

        assertNotNull(result)
        assertTrue(result!!.success)
        assertEquals(CommandType.DATE_QUERY, result.commandType)
        assertTrue(result.response.contains("Today is"))
    }

    @Test
    fun `test greeting recognition`() {
        val greetings = listOf("hello", "hi", "hey")

        greetings.forEach { greeting ->
            val result = offlineModeManager.processOfflineCommand(greeting)
            assertNotNull(result)
            assertTrue(result!!.success)
            assertEquals(CommandType.GREETING, result.commandType)
        }
    }

    @Test
    fun `test help command`() {
        val result = offlineModeManager.processOfflineCommand("help")

        assertNotNull(result)
        assertTrue(result!!.success)
        assertEquals(CommandType.HELP, result.commandType)
        assertTrue(result.response.contains("offline"))
    }

    @Test
    fun `test app launch command recognition`() {
        val result = offlineModeManager.processOfflineCommand("open chrome")

        assertNotNull(result)
        assertTrue(result!!.success)
        assertEquals(CommandType.APP_LAUNCH, result.commandType)
        assertTrue(result.response.contains("chrome", ignoreCase = true))
    }

    @Test
    fun `test navigation commands`() {
        val commands = mapOf(
            "go back" to "back",
            "go home" to "home",
            "recent apps" to "recent"
        )

        commands.forEach { (command, expected) ->
            val result = offlineModeManager.processOfflineCommand(command)
            assertNotNull(result)
            assertTrue(result!!.success)
            assertEquals(CommandType.NAVIGATION, result.commandType)
            assertTrue(result.response.contains(expected, ignoreCase = true))
        }
    }

    @Test
    fun `test unknown command returns error`() {
        val result = offlineModeManager.processOfflineCommand("solve world hunger")

        assertNotNull(result)
        assertFalse(result!!.success)
        assertEquals(CommandType.UNKNOWN, result.commandType)
        assertTrue(result.response.contains("offline", ignoreCase = true))
    }

    @Test
    fun `test case insensitive command matching`() {
        val commands = listOf(
            "WHAT'S THE TIME",
            "What's The Time",
            "what's the time"
        )

        commands.forEach { command ->
            val result = offlineModeManager.processOfflineCommand(command)
            assertNotNull(result)
            assertTrue(result!!.success)
            assertEquals(CommandType.TIME_QUERY, result.commandType)
        }
    }

    @Test
    fun `test empty command handling`() {
        val result = offlineModeManager.processOfflineCommand("")

        assertNotNull(result)
        assertFalse(result!!.success)
    }

    @Test
    fun `test whitespace command handling`() {
        val result = offlineModeManager.processOfflineCommand("   ")

        assertNotNull(result)
        assertFalse(result!!.success)
    }

    @Test
    fun `test Stark personality in responses`() {
        val result = offlineModeManager.processOfflineCommand("hello")

        assertNotNull(result)
        assertTrue(
            result!!.response.contains("Sir", ignoreCase = true) ||
            result.response.contains("Boss", ignoreCase = true)
        )
    }
}
