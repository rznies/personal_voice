package io.livekit.android.example.voiceassistant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import io.livekit.android.example.voiceassistant.MainActivity
import io.livekit.android.example.voiceassistant.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale

/**
 * WakeWordService - Always listening for "Jarvis", "Boss", or "Sir"
 * Foreground service for continuous wake word detection
 * Battery optimized with intelligent listening cycles
 */
class WakeWordService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var isListening = false
    private var restartAttempts = 0

    private val wakeWords = listOf(
        "jarvis",
        "hey jarvis",
        "boss",
        "hey boss",
        "sir",
        "hey sir",
        "stark"
    )

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "wake_word_channel"
        private const val MAX_RESTART_ATTEMPTS = 3
        private const val RESTART_DELAY_MS = 2000L

        fun start(context: Context) {
            val intent = Intent(context, WakeWordService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, WakeWordService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("WakeWordService: Initializing Jarvis ears...")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Listening for wake word..."))
        initializeSpeechRecognizer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startListening()
        return START_STICKY // Restart if killed
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("WakeWordService: Shutting down Jarvis ears...")
        stopListening()
        speechRecognizer?.destroy()
        serviceJob.cancel()
    }

    private fun initializeSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Timber.e("Speech recognition not available on this device")
            stopSelf()
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    restartAttempts = 0
                    Timber.d("Jarvis: Ready to hear you, Sir.")
                }

                override fun onBeginningOfSpeech() {
                    Timber.d("Jarvis: Speech detected...")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Audio level monitoring (optional, for visualizations)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    isListening = false
                    Timber.d("Jarvis: Processing audio...")
                }

                override fun onError(error: Int) {
                    isListening = false
                    val errorMsg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "No mic permission"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No match (silence)"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                        else -> "Unknown error: $error"
                    }

                    Timber.d("Speech recognition error: $errorMsg")

                    // Auto-restart on errors (except permission issues)
                    if (error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                        scheduleRestart()
                    } else {
                        Timber.e("Mic permission denied. Stopping wake word service.")
                        stopSelf()
                    }
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.forEach { result ->
                        Timber.d("Heard: $result")
                        if (containsWakeWord(result)) {
                            onWakeWordDetected(result)
                            return // Don't restart immediately
                        }
                    }
                    // No wake word detected, restart listening
                    scheduleRestart()
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    // Real-time results (optional for faster detection)
                    val matches = partialResults?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )
                    matches?.forEach { result ->
                        if (containsWakeWord(result)) {
                            // Cancel and trigger immediately
                            speechRecognizer?.cancel()
                            onWakeWordDetected(result)
                        }
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    private fun startListening() {
        if (isListening) {
            Timber.d("Already listening, skipping...")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            // Prefer on-device recognition for privacy and speed
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }

        try {
            speechRecognizer?.startListening(intent)
            updateNotification("Listening for \"Jarvis\"...")
            Timber.d("Jarvis: Ears active, Sir.")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start listening")
            scheduleRestart()
        }
    }

    private fun stopListening() {
        if (!isListening) return
        speechRecognizer?.stopListening()
        isListening = false
        Timber.d("Jarvis: Ears paused.")
    }

    private fun scheduleRestart() {
        if (restartAttempts >= MAX_RESTART_ATTEMPTS) {
            Timber.w("Max restart attempts reached. Waiting longer before retry...")
            restartAttempts = 0
            serviceScope.launch {
                delay(5000L) // Wait 5 seconds
                if (isActive) startListening()
            }
        } else {
            restartAttempts++
            serviceScope.launch {
                delay(RESTART_DELAY_MS)
                if (isActive) startListening()
            }
        }
    }

    private fun containsWakeWord(text: String): Boolean {
        val lowerText = text.lowercase(Locale.getDefault())
        return wakeWords.any { lowerText.contains(it) }
    }

    private fun onWakeWordDetected(detectedPhrase: String) {
        Timber.i("Wake word detected: $detectedPhrase")
        updateNotification("At your service, Sir!")

        // Broadcast wake word detection
        val intent = Intent(ACTION_WAKE_WORD_DETECTED).apply {
            putExtra(EXTRA_DETECTED_PHRASE, detectedPhrase)
        }
        sendBroadcast(intent)

        // Trigger haptic feedback
        triggerHapticFeedback()

        // Restart listening after a short delay
        serviceScope.launch {
            delay(1000L)
            if (isActive) startListening()
        }
    }

    private fun triggerHapticFeedback() {
        // Haptic feedback implementation
        // Will be implemented with full device control
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Jarvis Wake Word Detection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Jarvis is listening for wake words in the background"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(message: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("StarkJarvis")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher) // TODO: Create arc reactor icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(message: String) {
        val notification = createNotification(message)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val ACTION_WAKE_WORD_DETECTED = "io.livekit.android.example.voiceassistant.WAKE_WORD_DETECTED"
        const val EXTRA_DETECTED_PHRASE = "detected_phrase"
    }
}
