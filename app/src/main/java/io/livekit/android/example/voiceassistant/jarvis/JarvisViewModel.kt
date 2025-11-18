package io.livekit.android.example.voiceassistant.jarvis

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.livekit.android.example.voiceassistant.ai.*
import io.livekit.android.example.voiceassistant.data.ProductivityStats
import io.livekit.android.example.voiceassistant.data.SuitModeTracker
import io.livekit.android.example.voiceassistant.service.WakeWordService
import io.livekit.android.example.voiceassistant.utils.HapticManager
import io.livekit.android.example.voiceassistant.utils.ShakeDetector
import io.livekit.android.example.voiceassistant.utils.SoundEffectsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * JarvisViewModel - The orchestrator of StarkJarvis
 * Coordinates all AI features: voice, vision, screen awareness, TTS
 * "I am Jarvis. I run the house."
 */
class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // AI Components
    private val geminiClient = GeminiClient.getInstance(context)
    private val ttsManager = TTSManager.getInstance(context)
    private val visionManager = VisionManager.getInstance(context)
    private val screenAwarenessManager = ScreenAwarenessManager.getInstance(context)

    // Phase 3 Components
    private val hapticManager = HapticManager.getInstance(context)
    private val soundEffectsManager = SoundEffectsManager.getInstance(context)
    private val suitModeTracker = SuitModeTracker.getInstance(context)
    private var shakeDetector: ShakeDetector? = null

    // UI State
    private val _uiState = MutableStateFlow(JarvisUiState())
    val uiState: StateFlow<JarvisUiState> = _uiState.asStateFlow()

    // Conversation history (UI display)
    private val _conversationHistory = MutableStateFlow<List<Message>>(emptyList())
    val conversationHistory: StateFlow<List<Message>> = _conversationHistory.asStateFlow()

    // Productivity stats (Suit Mode)
    val productivityStats: StateFlow<ProductivityStats> = suitModeTracker.productivityStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductivityStats())

    // Wake word receiver
    private val wakeWordReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val detectedPhrase = intent?.getStringExtra(WakeWordService.EXTRA_DETECTED_PHRASE)
            Timber.i("Wake word detected in ViewModel: $detectedPhrase")
            handleWakeWord(detectedPhrase ?: "")
        }
    }

    init {
        // Register wake word receiver
        val filter = IntentFilter(WakeWordService.ACTION_WAKE_WORD_DETECTED)
        context.registerReceiver(wakeWordReceiver, filter, Context.RECEIVER_NOT_EXPORTED)

        // Add greeting message
        addMessage(
            Message(
                text = StarkPersonality.getGreeting(),
                isFromJarvis = true,
                type = MessageType.TEXT
            )
        )
    }

    /**
     * Handle wake word detection
     */
    private fun handleWakeWord(phrase: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isListening = true,
                statusMessage = "At your service, Sir."
            )

            // Speak confirmation
            ttsManager.speak("Yes, Sir?")

            // Start listening for command (this will be handled by existing LiveKit voice pipeline)
            // For now, just update UI state
        }
    }

    /**
     * Send text message to Jarvis
     */
    fun sendMessage(
        message: String,
        mode: GeminiClient.JarvisMode = GeminiClient.JarvisMode.DEFAULT
    ) {
        viewModelScope.launch {
            // Add user message to history
            addMessage(Message(text = message, isFromJarvis = false, type = MessageType.TEXT))

            // Show thinking state
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                statusMessage = "Jarvis is thinking..."
            )

            try {
                // Get response from Gemini
                val response = geminiClient.sendMessage(message, mode)

                // Add Jarvis response to history
                addMessage(Message(text = response, isFromJarvis = true, type = MessageType.TEXT))

                // Speak response
                ttsManager.speak(response)

            } catch (e: Exception) {
                Timber.e(e, "Error getting Jarvis response")
                val errorMsg = "Apologies, Sir. My circuits are experiencing turbulence."
                addMessage(Message(text = errorMsg, isFromJarvis = true, type = MessageType.TEXT))
                ttsManager.speak(errorMsg)
            } finally {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    statusMessage = "Ready, Sir."
                )
            }
        }
    }

    /**
     * Activate Vision Mode and analyze what Jarvis sees
     */
    fun activateVisionMode(useFrontCamera: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                statusMessage = "Activating Vision Mode..."
            )

            try {
                // Start vision mode
                val result = visionManager.startVisionMode(useFrontCamera)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isVisionModeActive = true,
                        statusMessage = "Vision Mode active. Say 'Jarvis, what do you see?'"
                    )

                    ttsManager.speak("Vision Mode activated, Sir.")
                } else {
                    throw result.exceptionOrNull() ?: Exception("Unknown error")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to activate Vision Mode")
                val errorMsg = "Vision Mode malfunction, Boss. ${e.message}"
                addMessage(Message(text = errorMsg, isFromJarvis = true, type = MessageType.TEXT))
                ttsManager.speak(errorMsg)
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }

    /**
     * Capture and analyze current camera view
     */
    fun analyzeVision(customPrompt: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                statusMessage = "Analyzing visual input..."
            )

            try {
                val prompt = customPrompt
                    ?: "Describe what you see with precision and wit, Sir."

                val analysis = visionManager.captureAndAnalyze(geminiClient, prompt)

                addMessage(
                    Message(
                        text = analysis,
                        isFromJarvis = true,
                        type = MessageType.VISION
                    )
                )

                ttsManager.speak(analysis)

            } catch (e: Exception) {
                Timber.e(e, "Vision analysis failed")
                val errorMsg = "Visual sensors malfunctioning, Boss."
                addMessage(Message(text = errorMsg, isFromJarvis = true, type = MessageType.TEXT))
                ttsManager.speak(errorMsg)
            } finally {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    statusMessage = "Vision Mode active."
                )
            }
        }
    }

    /**
     * Deactivate Vision Mode
     */
    fun deactivateVisionMode() {
        visionManager.stopVisionMode()
        _uiState.value = _uiState.value.copy(
            isVisionModeActive = false,
            statusMessage = "Vision Mode deactivated."
        )
        ttsManager.speak("Vision Mode off, Sir.")
    }

    /**
     * Activate Screen Awareness Mode
     * Note: This requires calling startScreenAwareness with MediaProjection result
     */
    fun requestScreenCapturePermission(): Intent {
        return screenAwarenessManager.createScreenCaptureIntent()
    }

    /**
     * Start screen awareness with permission result
     */
    fun startScreenAwareness(resultCode: Int, data: Intent) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                statusMessage = "Activating Screen Awareness..."
            )

            try {
                val result = screenAwarenessManager.startScreenAwareness(resultCode, data)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isScreenAwarenessActive = true,
                        statusMessage = "Screen Awareness active. Say 'Jarvis, look at my screen.'"
                    )

                    ttsManager.speak("Screen Awareness activated, Sir. I can read your screen now.")
                } else {
                    throw result.exceptionOrNull() ?: Exception("Unknown error")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to activate Screen Awareness")
                val errorMsg = "Screen reading malfunction, Boss. ${e.message}"
                addMessage(Message(text = errorMsg, isFromJarvis = true, type = MessageType.TEXT))
                ttsManager.speak(errorMsg)
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }

    /**
     * Analyze current screen content
     */
    fun analyzeScreen(mode: ScreenAnalysisMode = ScreenAnalysisMode.QUICK) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                statusMessage = "Analyzing your screen..."
            )

            try {
                val analysis = when (mode) {
                    ScreenAnalysisMode.QUICK -> screenAwarenessManager.quickScreenAnalysis(geminiClient)
                    ScreenAnalysisMode.CODE_REVIEW -> screenAwarenessManager.reviewCodeOnScreen(geminiClient)
                    ScreenAnalysisMode.READ_TEXT -> screenAwarenessManager.readScreenText(geminiClient)
                }

                addMessage(
                    Message(
                        text = analysis,
                        isFromJarvis = true,
                        type = MessageType.SCREEN_ANALYSIS
                    )
                )

                ttsManager.speak(analysis)

            } catch (e: Exception) {
                Timber.e(e, "Screen analysis failed")
                val errorMsg = "Screen reading failed, Boss. ${e.message}"
                addMessage(Message(text = errorMsg, isFromJarvis = true, type = MessageType.TEXT))
                ttsManager.speak(errorMsg)
            } finally {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    statusMessage = "Screen Awareness active."
                )
            }
        }
    }

    /**
     * Deactivate Screen Awareness
     */
    fun deactivateScreenAwareness() {
        screenAwarenessManager.stopScreenAwareness()
        _uiState.value = _uiState.value.copy(
            isScreenAwarenessActive = false,
            statusMessage = "Screen Awareness deactivated."
        )
        ttsManager.speak("Screen Awareness off, Sir.")
    }

    /**
     * Start wake word listening service
     */
    fun startWakeWordService() {
        WakeWordService.start(context)
        _uiState.value = _uiState.value.copy(
            isWakeWordActive = true,
            statusMessage = "Listening for wake word..."
        )
    }

    /**
     * Stop wake word listening service
     */
    fun stopWakeWordService() {
        WakeWordService.stop(context)
        _uiState.value = _uiState.value.copy(
            isWakeWordActive = false,
            statusMessage = "Wake word detection stopped."
        )
    }

    /**
     * Stop Jarvis from speaking
     */
    fun stopSpeaking() {
        ttsManager.stop()
    }

    /**
     * Clear conversation history
     */
    fun clearHistory() {
        _conversationHistory.value = emptyList()
        geminiClient.clearHistory()
    }

    /**
     * Add message to conversation history
     */
    private fun addMessage(message: Message) {
        _conversationHistory.value = _conversationHistory.value + message
    }

    // ========================================
    // PHASE 3: REPULSOR MODE & SUIT MODE
    // ========================================

    /**
     * Activate Repulsor Mode with shake detection
     */
    fun activateRepulsorMode() {
        if (shakeDetector == null) {
            shakeDetector = ShakeDetector(context) {
                onRepulsorTriggered()
            }
        }

        shakeDetector?.startListening()
        _uiState.value = _uiState.value.copy(
            isRepulsorModeActive = true,
            statusMessage = "Repulsor Mode active. Shake to fire, Sir."
        )

        ttsManager.speak("Repulsor Mode activated. Shake your device to fire, Sir.")
        hapticManager.suitActivation()
    }

    /**
     * Deactivate Repulsor Mode
     */
    fun deactivateRepulsorMode() {
        shakeDetector?.stopListening()
        _uiState.value = _uiState.value.copy(
            isRepulsorModeActive = false,
            statusMessage = "Repulsor Mode deactivated."
        )

        ttsManager.speak("Repulsor Mode off, Sir.")
    }

    /**
     * Handle repulsor blast trigger
     */
    private fun onRepulsorTriggered() {
        viewModelScope.launch {
            Timber.i("REPULSOR BLAST FIRED!")

            // Haptic feedback
            hapticManager.repulsorBlast()

            // Sound effect
            soundEffectsManager.playRepulsorBlast()

            // Visual feedback (update UI state)
            _uiState.value = _uiState.value.copy(
                statusMessage = "REPULSOR BLAST! 💥"
            )

            // Jarvis commentary
            val message = "Target acquired. Multi-millionaire mode engaged, Sir."
            addMessage(Message(text = message, isFromJarvis = true, type = MessageType.TEXT))
            ttsManager.speak(message)

            // Reset status after delay
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(
                statusMessage = "Repulsor Mode active. Ready to fire again."
            )
        }
    }

    /**
     * Activate Suit Mode for productivity tracking
     */
    fun activateSuitMode() {
        viewModelScope.launch {
            suitModeTracker.activateSuitMode()

            _uiState.value = _uiState.value.copy(
                isSuitModeActive = true,
                statusMessage = "Suit Mode activated. Let's build an empire, Sir."
            )

            val message = "Suit Mode activated. I'm tracking your productivity now, Boss. No pressure."
            addMessage(Message(text = message, isFromJarvis = true, type = MessageType.TEXT))
            ttsManager.speak(message)

            hapticManager.suitActivation()
            soundEffectsManager.playSuitUp()
        }
    }

    /**
     * Deactivate Suit Mode
     */
    fun deactivateSuitMode() {
        viewModelScope.launch {
            suitModeTracker.deactivateSuitMode()

            _uiState.value = _uiState.value.copy(
                isSuitModeActive = false,
                statusMessage = "Suit Mode deactivated."
            )

            ttsManager.speak("Suit Mode off, Sir. Rest well.")
        }
    }

    /**
     * Get productivity summary from Suit Mode
     */
    fun getProductivitySummary() {
        viewModelScope.launch {
            val summary = suitModeTracker.getProductivitySummary()

            addMessage(
                Message(
                    text = summary,
                    isFromJarvis = true,
                    type = MessageType.TEXT
                )
            )

            // Speak a condensed version
            val stats = productivityStats.value
            val spoken = "You shipped ${stats.linesOfCodeToday} lines today, Sir. " +
                    "That's ${stats.getDailyCompletionPercentage()}% daily progress. " +
                    if (stats.needsCoffeeRoast()) "Also, that's a lot of coffee, Boss." else "Keep going."

            ttsManager.speak(spoken)
        }
    }

    /**
     * Log coding activity (lines of code)
     */
    fun logCodingActivity(lines: Int) {
        viewModelScope.launch {
            suitModeTracker.logLinesOfCode(lines)
            Timber.d("Logged $lines lines of code")
        }
    }

    /**
     * Complete a goal
     */
    fun completeGoal() {
        viewModelScope.launch {
            suitModeTracker.completeGoal()
            hapticManager.success()

            val message = "Goal completed! One step closer to Stark Tower, Sir."
            addMessage(Message(text = message, isFromJarvis = true, type = MessageType.TEXT))
            ttsManager.speak(message)
        }
    }

    /**
     * Log coffee (for roasting)
     */
    fun logCoffee() {
        viewModelScope.launch {
            suitModeTracker.logCoffee()

            val count = productivityStats.value.coffeeCountToday
            if (count >= 5) {
                val roast = StarkPersonality.getRandomRoast(StarkPersonality.Roasts.COFFEE_ADDICTION)
                addMessage(Message(text = roast, isFromJarvis = true, type = MessageType.TEXT))
                ttsManager.speak(roast)
            }
        }
    }

    /**
     * Trigger haptic feedback for UI interactions
     */
    fun triggerHaptic(type: HapticType = HapticType.BUTTON_CLICK) {
        when (type) {
            HapticType.BUTTON_CLICK -> hapticManager.buttonClick()
            HapticType.SUCCESS -> hapticManager.success()
            HapticType.ERROR -> hapticManager.error()
            HapticType.WAKE_WORD -> hapticManager.wakeWordDetected()
            HapticType.ARC_REACTOR -> hapticManager.arcReactorPulse()
        }
    }

    override fun onCleared() {
        super.onCleared()
        context.unregisterReceiver(wakeWordReceiver)
        visionManager.release()
        screenAwarenessManager.release()
        ttsManager.shutdown()
        shakeDetector?.stopListening()
        soundEffectsManager.release()
    }
}

/**
 * UI State for Jarvis
 */
data class JarvisUiState(
    val isProcessing: Boolean = false,
    val isListening: Boolean = false,
    val isWakeWordActive: Boolean = false,
    val isVisionModeActive: Boolean = false,
    val isScreenAwarenessActive: Boolean = false,
    val isRepulsorModeActive: Boolean = false,
    val isSuitModeActive: Boolean = false,
    val statusMessage: String = "Ready, Sir.",
    val currentMode: GeminiClient.JarvisMode = GeminiClient.JarvisMode.DEFAULT
)

/**
 * Message in conversation
 */
data class Message(
    val text: String,
    val isFromJarvis: Boolean,
    val type: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageType {
    TEXT,
    VISION,
    SCREEN_ANALYSIS,
    VOICE
}

enum class ScreenAnalysisMode {
    QUICK,
    CODE_REVIEW,
    READ_TEXT
}

enum class HapticType {
    BUTTON_CLICK,
    SUCCESS,
    ERROR,
    WAKE_WORD,
    ARC_REACTOR
}
