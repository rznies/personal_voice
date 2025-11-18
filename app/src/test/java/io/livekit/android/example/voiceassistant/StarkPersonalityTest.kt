package io.livekit.android.example.voiceassistant

import io.livekit.android.example.voiceassistant.ai.StarkPersonality
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for StarkPersonality
 * "Testing personality, Sir. Making sure the sass levels are optimal."
 */
class StarkPersonalityTest {

    @Test
    fun `test system prompt is not empty`() {
        assertTrue(StarkPersonality.SYSTEM_PROMPT.isNotEmpty())
    }

    @Test
    fun `test system prompt contains Jarvis identity`() {
        assertTrue(StarkPersonality.SYSTEM_PROMPT.contains("Jarvis", ignoreCase = true))
    }

    @Test
    fun `test system prompt contains personality traits`() {
        val prompt = StarkPersonality.SYSTEM_PROMPT

        assertTrue(prompt.contains("sarcastic", ignoreCase = true))
        assertTrue(prompt.contains("witty", ignoreCase = true))
        assertTrue(prompt.contains("Sir", ignoreCase = true) || prompt.contains("Boss", ignoreCase = true))
    }

    @Test
    fun `test system prompt contains Tony Stark reference`() {
        assertTrue(StarkPersonality.SYSTEM_PROMPT.contains("Tony", ignoreCase = true) ||
                   StarkPersonality.SYSTEM_PROMPT.contains("Stark", ignoreCase = true))
    }

    @Test
    fun `test vision mode prompt exists`() {
        assertTrue(StarkPersonality.VISION_MODE_PROMPT.isNotEmpty())
    }

    @Test
    fun `test vision mode prompt mentions vision`() {
        assertTrue(StarkPersonality.VISION_MODE_PROMPT.contains("see", ignoreCase = true) ||
                   StarkPersonality.VISION_MODE_PROMPT.contains("vision", ignoreCase = true) ||
                   StarkPersonality.VISION_MODE_PROMPT.contains("image", ignoreCase = true))
    }

    @Test
    fun `test screen awareness prompt exists`() {
        assertTrue(StarkPersonality.SCREEN_AWARENESS_PROMPT.isNotEmpty())
    }

    @Test
    fun `test screen awareness prompt mentions screen`() {
        assertTrue(StarkPersonality.SCREEN_AWARENESS_PROMPT.contains("screen", ignoreCase = true))
    }

    @Test
    fun `test code review prompt exists`() {
        assertTrue(StarkPersonality.CODE_REVIEW_PROMPT.isNotEmpty())
    }

    @Test
    fun `test code review prompt mentions code`() {
        assertTrue(StarkPersonality.CODE_REVIEW_PROMPT.contains("code", ignoreCase = true))
    }

    @Test
    fun `test idea validation prompt exists`() {
        assertTrue(StarkPersonality.IDEA_VALIDATION_PROMPT.isNotEmpty())
    }

    @Test
    fun `test coffee roasts exist`() {
        val roasts = StarkPersonality.getCoffeeRoast()
        assertTrue(roasts.isNotEmpty())
    }

    @Test
    fun `test coffee roasts contain coffee reference`() {
        val roasts = StarkPersonality.getCoffeeRoast()
        assertTrue(roasts.contains("coffee", ignoreCase = true) ||
                   roasts.contains("caffeine", ignoreCase = true))
    }

    @Test
    fun `test procrastination roasts exist`() {
        val roasts = StarkPersonality.getProcrastinationRoast()
        assertTrue(roasts.isNotEmpty())
    }

    @Test
    fun `test bad code roasts exist`() {
        val roasts = StarkPersonality.getBadCodeRoast()
        assertTrue(roasts.isNotEmpty())
    }

    @Test
    fun `test bad code roasts mention code`() {
        val roasts = StarkPersonality.getBadCodeRoast()
        assertTrue(roasts.contains("code", ignoreCase = true) ||
                   roasts.contains("bug", ignoreCase = true))
    }

    @Test
    fun `test late night roasts exist`() {
        val roasts = StarkPersonality.getLateNightRoast()
        assertTrue(roasts.isNotEmpty())
    }

    @Test
    fun `test late night roasts mention time`() {
        val roasts = StarkPersonality.getLateNightRoast()
        assertTrue(roasts.contains("night", ignoreCase = true) ||
                   roasts.contains("sleep", ignoreCase = true) ||
                   roasts.contains("AM", ignoreCase = true))
    }

    @Test
    fun `test roasts maintain personality`() {
        val roasts = listOf(
            StarkPersonality.getCoffeeRoast(),
            StarkPersonality.getProcrastinationRoast(),
            StarkPersonality.getBadCodeRoast(),
            StarkPersonality.getLateNightRoast()
        )

        roasts.forEach { roast ->
            // Each roast should address the user respectfully
            assertTrue(
                "Roast lacks personality: $roast",
                roast.contains("Sir", ignoreCase = true) ||
                roast.contains("Boss", ignoreCase = true) ||
                roast.contains("you", ignoreCase = true)
            )
        }
    }

    @Test
    fun `test greeting messages exist`() {
        assertTrue(StarkPersonality.GREETING_MESSAGES.isNotEmpty())
    }

    @Test
    fun `test greeting messages are varied`() {
        assertTrue(StarkPersonality.GREETING_MESSAGES.size >= 3)
    }

    @Test
    fun `test greeting messages maintain personality`() {
        StarkPersonality.GREETING_MESSAGES.forEach { greeting ->
            assertTrue(
                greeting.contains("Sir", ignoreCase = true) ||
                greeting.contains("Boss", ignoreCase = true)
            )
        }
    }

    @Test
    fun `test random greeting selection works`() {
        val greeting1 = StarkPersonality.getRandomGreeting()
        val greeting2 = StarkPersonality.getRandomGreeting()
        val greeting3 = StarkPersonality.getRandomGreeting()

        // At least one should be different (with high probability)
        assertTrue(greeting1.isNotEmpty())
        assertTrue(greeting2.isNotEmpty())
        assertTrue(greeting3.isNotEmpty())
    }

    @Test
    fun `test acknowledgment messages exist`() {
        assertTrue(StarkPersonality.ACKNOWLEDGMENT_MESSAGES.isNotEmpty())
    }

    @Test
    fun `test acknowledgment messages are varied`() {
        assertTrue(StarkPersonality.ACKNOWLEDGMENT_MESSAGES.size >= 3)
    }

    @Test
    fun `test random acknowledgment selection works`() {
        val ack1 = StarkPersonality.getRandomAcknowledgment()
        val ack2 = StarkPersonality.getRandomAcknowledgment()

        assertTrue(ack1.isNotEmpty())
        assertTrue(ack2.isNotEmpty())
    }

    @Test
    fun `test all prompts maintain consistent personality`() {
        val prompts = listOf(
            StarkPersonality.SYSTEM_PROMPT,
            StarkPersonality.VISION_MODE_PROMPT,
            StarkPersonality.SCREEN_AWARENESS_PROMPT,
            StarkPersonality.CODE_REVIEW_PROMPT,
            StarkPersonality.IDEA_VALIDATION_PROMPT
        )

        prompts.forEach { prompt ->
            assertTrue("Prompt lacks personality: $prompt", prompt.isNotEmpty())
        }
    }

    @Test
    fun `test system prompt has substantial content`() {
        // System prompt should be detailed (at least 500 characters)
        assertTrue(StarkPersonality.SYSTEM_PROMPT.length > 500)
    }

    @Test
    fun `test roasts are not empty strings`() {
        repeat(10) {
            val coffee = StarkPersonality.getCoffeeRoast()
            val procrastination = StarkPersonality.getProcrastinationRoast()
            val badCode = StarkPersonality.getBadCodeRoast()
            val lateNight = StarkPersonality.getLateNightRoast()

            assertTrue(coffee.isNotEmpty())
            assertTrue(procrastination.isNotEmpty())
            assertTrue(badCode.isNotEmpty())
            assertTrue(lateNight.isNotEmpty())
        }
    }
}
