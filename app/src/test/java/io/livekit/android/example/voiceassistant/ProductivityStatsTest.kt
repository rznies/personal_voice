package io.livekit.android.example.voiceassistant

import io.livekit.android.example.voiceassistant.data.ProductivityStats
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ProductivityStats
 * "Testing productivity metrics, Sir. Let's measure that genius output."
 */
class ProductivityStatsTest {

    @Test
    fun `test default stats initialization`() {
        val stats = ProductivityStats()

        assertEquals(0, stats.linesOfCodeToday)
        assertEquals(0, stats.goalsCompletedToday)
        assertEquals(0, stats.coffeeCountToday)
        assertEquals(0, stats.lateNightSessions)
        assertFalse(stats.isSuitModeActive)
    }

    @Test
    fun `test daily completion percentage with default target`() {
        val stats = ProductivityStats(goalsCompletedToday = 1)
        val percentage = stats.getDailyCompletionPercentage()

        assertEquals(33, percentage) // 1/3 = 33%
    }

    @Test
    fun `test daily completion percentage with custom target`() {
        val stats = ProductivityStats(goalsCompletedToday = 5)
        val percentage = stats.getDailyCompletionPercentage(targetGoals = 10)

        assertEquals(50, percentage) // 5/10 = 50%
    }

    @Test
    fun `test daily completion percentage max 100`() {
        val stats = ProductivityStats(goalsCompletedToday = 10)
        val percentage = stats.getDailyCompletionPercentage(targetGoals = 3)

        assertEquals(100, percentage) // Capped at 100%
    }

    @Test
    fun `test daily completion percentage zero target`() {
        val stats = ProductivityStats(goalsCompletedToday = 5)
        val percentage = stats.getDailyCompletionPercentage(targetGoals = 0)

        assertEquals(0, percentage) // No target = 0%
    }

    @Test
    fun `test work hours formatting`() {
        val stats = ProductivityStats(totalWorkMinutesToday = 125)
        val hours = stats.getWorkHoursToday()

        assertEquals("2h 5m", hours)
    }

    @Test
    fun `test work hours zero minutes`() {
        val stats = ProductivityStats(totalWorkMinutesToday = 0)
        val hours = stats.getWorkHoursToday()

        assertEquals("0h 0m", hours)
    }

    @Test
    fun `test work hours exactly one hour`() {
        val stats = ProductivityStats(totalWorkMinutesToday = 60)
        val hours = stats.getWorkHoursToday()

        assertEquals("1h 0m", hours)
    }

    @Test
    fun `test coffee roast threshold`() {
        val normalCoffee = ProductivityStats(coffeeCountToday = 4)
        val excessiveCoffee = ProductivityStats(coffeeCountToday = 5)

        assertFalse(normalCoffee.needsCoffeeRoast())
        assertTrue(excessiveCoffee.needsCoffeeRoast())
    }

    @Test
    fun `test coffee roast at exactly 5 cups`() {
        val stats = ProductivityStats(coffeeCountToday = 5)

        assertTrue(stats.needsCoffeeRoast())
    }

    @Test
    fun `test all-nighter detection`() {
        val normalSession = ProductivityStats(lateNightSessions = 0)
        val allNighter = ProductivityStats(lateNightSessions = 1)

        assertFalse(normalSession.isAllNighter())
        assertTrue(allNighter.isAllNighter())
    }

    @Test
    fun `test multiple late night sessions`() {
        val stats = ProductivityStats(lateNightSessions = 5)

        assertTrue(stats.isAllNighter())
        assertEquals(5, stats.lateNightSessions)
    }

    @Test
    fun `test comprehensive productivity stats`() {
        val stats = ProductivityStats(
            linesOfCodeToday = 500,
            linesOfCodeTotal = 10000,
            workSessionsToday = 3,
            totalWorkMinutesToday = 240,
            goalsCompletedToday = 3,
            goalsTotal = 50,
            coffeeCountToday = 4,
            lateNightSessions = 1,
            procrastinationWarnings = 2,
            isSuitModeActive = true,
            currentSessionStartTime = System.currentTimeMillis(),
            totalSessionsAllTime = 100
        )

        assertEquals(500, stats.linesOfCodeToday)
        assertEquals(10000, stats.linesOfCodeTotal)
        assertEquals(3, stats.workSessionsToday)
        assertEquals("4h 0m", stats.getWorkHoursToday())
        assertEquals(100, stats.getDailyCompletionPercentage()) // 3/3 goals
        assertFalse(stats.needsCoffeeRoast()) // Only 4 coffees
        assertTrue(stats.isAllNighter())
        assertTrue(stats.isSuitModeActive)
        assertEquals(100, stats.totalSessionsAllTime)
    }

    @Test
    fun `test zero completion percentage`() {
        val stats = ProductivityStats(goalsCompletedToday = 0)
        val percentage = stats.getDailyCompletionPercentage()

        assertEquals(0, percentage)
    }

    @Test
    fun `test high lines of code tracking`() {
        val stats = ProductivityStats(
            linesOfCodeToday = 1000,
            linesOfCodeTotal = 50000
        )

        assertEquals(1000, stats.linesOfCodeToday)
        assertEquals(50000, stats.linesOfCodeTotal)
    }

    @Test
    fun `test session start time null handling`() {
        val stats = ProductivityStats(currentSessionStartTime = null)

        assertNull(stats.currentSessionStartTime)
        assertFalse(stats.isSuitModeActive)
    }

    @Test
    fun `test productivity stats immutability`() {
        val stats1 = ProductivityStats(linesOfCodeToday = 100)
        val stats2 = stats1.copy(linesOfCodeToday = 200)

        assertEquals(100, stats1.linesOfCodeToday)
        assertEquals(200, stats2.linesOfCodeToday)
    }

    @Test
    fun `test extreme coffee consumption`() {
        val stats = ProductivityStats(coffeeCountToday = 20)

        assertTrue(stats.needsCoffeeRoast())
        assertEquals(20, stats.coffeeCountToday)
    }

    @Test
    fun `test work hours with large minutes`() {
        val stats = ProductivityStats(totalWorkMinutesToday = 500) // 8h 20m
        val hours = stats.getWorkHoursToday()

        assertEquals("8h 20m", hours)
    }
}
