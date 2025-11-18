package io.livekit.android.example.voiceassistant.ai

import android.content.Context
import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import io.livekit.android.example.voiceassistant.BuildConfig
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

/**
 * GeminiClient - The brain of StarkJarvis
 * Integrates with Google Gemini 2.5 Flash for LLM capabilities
 * Infused with Tony Stark's personality via StarkPersonality system prompt
 */
class GeminiClient(
    private val context: Context,
    private val apiKey: String = BuildConfig.GEMINI_API_KEY
) {

    private val textModel: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash-exp",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.9f  // High creativity for witty responses
                topK = 40
                topP = 0.95f
                maxOutputTokens = 2048
            },
            systemInstruction = content { text(StarkPersonality.SYSTEM_PROMPT) },
            tools = listOf(createJarvisFunctionTools())
        )
    }

    private val visionModel: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash-exp",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 1024
            },
            systemInstruction = content {
                text(StarkPersonality.SYSTEM_PROMPT + "\n\n" + StarkPersonality.Modes.VISION_MODE)
            }
        )
    }

    private val chatHistory = mutableListOf<Content>()

    /**
     * Initialize Gemini client
     * Call this before using any methods
     */
    fun initialize() {
        Timber.d("StarkJarvis: Gemini AI initialized. Jarvis is online, Sir.")
    }

    /**
     * Send a text message to Jarvis and get a response
     * Maintains conversation history for context
     */
    suspend fun sendMessage(
        message: String,
        mode: JarvisMode = JarvisMode.DEFAULT
    ): String {
        return try {
            // Add mode-specific context if needed
            val enhancedPrompt = when (mode) {
                JarvisMode.CODE_REVIEW -> "${StarkPersonality.Modes.CODE_REVIEW}\n\n$message"
                JarvisMode.IDEA_VALIDATION -> "${StarkPersonality.Modes.IDEA_VALIDATION}\n\n$message"
                JarvisMode.SUIT_MODE -> "${StarkPersonality.Modes.SUIT_MODE}\n\n$message"
                JarvisMode.SCREEN_AWARENESS -> "${StarkPersonality.Modes.SCREEN_AWARENESS}\n\n$message"
                else -> message
            }

            // Add user message to history
            chatHistory.add(content("user") { text(enhancedPrompt) })

            // Start a chat session with history
            val chat = textModel.startChat(chatHistory)
            val response = chat.sendMessage(enhancedPrompt)

            val responseText = response.text ?: "Sir, I seem to have lost my voice. How embarrassing."

            // Add Jarvis response to history
            chatHistory.add(content("model") { text(responseText) })

            // Trim history if too long (keep last 20 messages)
            if (chatHistory.size > 20) {
                chatHistory.subList(0, chatHistory.size - 20).clear()
            }

            Timber.d("Jarvis: $responseText")
            responseText

        } catch (e: Exception) {
            Timber.e(e, "Gemini error")
            "Apologies, Sir. My circuits are experiencing turbulence. Error: ${e.message}"
        }
    }

    /**
     * Send a message with streaming response
     * Perfect for real-time conversation
     */
    fun sendMessageStream(
        message: String,
        mode: JarvisMode = JarvisMode.DEFAULT
    ): Flow<GenerateContentResponse> {
        val enhancedPrompt = when (mode) {
            JarvisMode.CODE_REVIEW -> "${StarkPersonality.Modes.CODE_REVIEW}\n\n$message"
            JarvisMode.IDEA_VALIDATION -> "${StarkPersonality.Modes.IDEA_VALIDATION}\n\n$message"
            JarvisMode.SUIT_MODE -> "${StarkPersonality.Modes.SUIT_MODE}\n\n$message"
            else -> message
        }

        chatHistory.add(content("user") { text(enhancedPrompt) })
        val chat = textModel.startChat(chatHistory)
        return chat.sendMessageStream(enhancedPrompt)
    }

    /**
     * Analyze an image with Vision Mode
     * "Jarvis, what do you see?"
     */
    suspend fun analyzeImage(
        bitmap: Bitmap,
        prompt: String = "Describe what you see in this image with precision and wit, Sir."
    ): String {
        return try {
            val response = visionModel.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )

            response.text ?: "Sir, the image appears to be... invisible. Or my optical sensors need recalibration."

        } catch (e: Exception) {
            Timber.e(e, "Vision mode error")
            "Vision Mode malfunction, Boss. Error: ${e.message}"
        }
    }

    /**
     * Analyze screen content
     * MediaProjection bitmap + text overlay
     */
    suspend fun analyzeScreen(
        screenshot: Bitmap,
        additionalContext: String = ""
    ): String {
        val prompt = """
$additionalContext

Analyze this screenshot and provide:
1. What Sir is looking at (app, website, code, etc.)
2. Key information visible
3. Suggestions or actions I can take
4. Your signature sarcastic commentary
""".trim()

        return analyzeImage(screenshot, prompt)
    }

    /**
     * Clear conversation history
     * Useful for starting fresh contexts
     */
    fun clearHistory() {
        chatHistory.clear()
        Timber.d("Jarvis: Conversation history cleared, Sir. Fresh start.")
    }

    /**
     * Get conversation history for persistence
     */
    fun getHistory(): List<Content> = chatHistory.toList()

    /**
     * Restore conversation history
     */
    fun restoreHistory(history: List<Content>) {
        chatHistory.clear()
        chatHistory.addAll(history)
    }

    /**
     * Create Jarvis function calling tools
     * Allows Gemini to trigger device actions
     */
    private fun createJarvisFunctionTools(): Tool {
        return Tool(
            functionDeclarations = listOf(
                // Device Control
                FunctionDeclaration(
                    name = "open_app",
                    description = "Open an application on the device",
                    parameters = Schema.obj(
                        "package_name" to Schema.string("Package name or app name to open")
                    )
                ),
                FunctionDeclaration(
                    name = "send_message",
                    description = "Send SMS or WhatsApp message",
                    parameters = Schema.obj(
                        "recipient" to Schema.string("Contact name or phone number"),
                        "message" to Schema.string("Message content"),
                        "platform" to Schema.string("sms or whatsapp")
                    )
                ),
                FunctionDeclaration(
                    name = "set_reminder",
                    description = "Create a reminder or alarm",
                    parameters = Schema.obj(
                        "title" to Schema.string("Reminder title"),
                        "time" to Schema.string("Time in HH:mm format or relative like '2 hours'")
                    )
                ),
                FunctionDeclaration(
                    name = "web_search",
                    description = "Search the web for information",
                    parameters = Schema.obj(
                        "query" to Schema.string("Search query")
                    )
                ),
                FunctionDeclaration(
                    name = "control_media",
                    description = "Control music/media playback",
                    parameters = Schema.obj(
                        "action" to Schema.string("play, pause, next, previous"),
                        "query" to Schema.string("Song or artist name (optional)")
                    )
                ),
                // Entrepreneurship Tools
                FunctionDeclaration(
                    name = "validate_idea",
                    description = "Validate a startup/product idea with market research",
                    parameters = Schema.obj(
                        "idea" to Schema.string("The startup idea to validate"),
                        "target_market" to Schema.string("Target market or audience")
                    )
                ),
                FunctionDeclaration(
                    name = "analyze_code",
                    description = "Perform code review and analysis",
                    parameters = Schema.obj(
                        "code" to Schema.string("Code snippet to analyze"),
                        "language" to Schema.string("Programming language")
                    )
                ),
                FunctionDeclaration(
                    name = "track_goal",
                    description = "Track entrepreneurial goals and habits",
                    parameters = Schema.obj(
                        "goal_type" to Schema.string("coding, shipping, habits, revenue"),
                        "value" to Schema.string("Progress value or metric")
                    )
                )
            )
        )
    }

    /**
     * Jarvis operation modes
     */
    enum class JarvisMode {
        DEFAULT,
        CODE_REVIEW,
        IDEA_VALIDATION,
        SUIT_MODE,
        SCREEN_AWARENESS,
        VISION_MODE,
        REPULSOR_MODE
    }

    companion object {
        @Volatile
        private var instance: GeminiClient? = null

        fun getInstance(context: Context): GeminiClient {
            return instance ?: synchronized(this) {
                instance ?: GeminiClient(context.applicationContext).also {
                    instance = it
                    it.initialize()
                }
            }
        }
    }
}
