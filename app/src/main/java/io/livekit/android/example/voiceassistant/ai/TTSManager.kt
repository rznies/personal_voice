package io.livekit.android.example.voiceassistant.ai

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import io.livekit.android.example.voiceassistant.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * TTSManager - Jarvis's voice
 * Supports multiple TTS backends:
 * 1. ElevenLabs (premium RDJ-style voice)
 * 2. Cartesia (alternative premium voice)
 * 3. Android TTS (fallback, free)
 */
class TTSManager(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var isTTSReady = false
    private val httpClient = OkHttpClient()
    private var mediaPlayer: MediaPlayer? = null

    private val preferredBackend: TTSBackend
        get() = when {
            BuildConfig.ELEVENLABS_API_KEY.isNotEmpty() -> TTSBackend.ELEVENLABS
            BuildConfig.CARTESIA_API_KEY.isNotEmpty() -> TTSBackend.CARTESIA
            else -> TTSBackend.ANDROID
        }

    init {
        initializeAndroidTTS()
    }

    private fun initializeAndroidTTS() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.UK // British accent for Jarvis
                textToSpeech?.setPitch(0.9f) // Slightly lower pitch
                textToSpeech?.setSpeechRate(1.1f) // Slightly faster, confident pace
                isTTSReady = true
                Timber.d("Android TTS initialized successfully")
            } else {
                Timber.e("Android TTS initialization failed")
            }
        }
    }

    /**
     * Speak text using the preferred TTS backend
     */
    suspend fun speak(text: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext when (preferredBackend) {
            TTSBackend.ELEVENLABS -> speakWithElevenLabs(text)
            TTSBackend.CARTESIA -> speakWithCartesia(text)
            TTSBackend.ANDROID -> speakWithAndroidTTS(text)
        }
    }

    /**
     * ElevenLabs TTS - Premium RDJ-style Jarvis voice
     * Voice ID: "21m00Tcm4TlvDq8ikWAM" (Rachel - professional British female)
     * For actual RDJ voice, you'd need to clone it in ElevenLabs dashboard
     */
    private suspend fun speakWithElevenLabs(text: String): Result<Unit> {
        return try {
            val apiKey = BuildConfig.ELEVENLABS_API_KEY
            if (apiKey.isEmpty()) {
                return Result.failure(Exception("ElevenLabs API key not configured"))
            }

            // Use a sophisticated British voice (closest to Jarvis)
            val voiceId = "21m00Tcm4TlvDq8ikWAM" // Rachel voice
            val url = "https://api.elevenlabs.io/v1/text-to-speech/$voiceId"

            val jsonBody = JSONObject().apply {
                put("text", text)
                put("model_id", "eleven_multilingual_v2")
                put("voice_settings", JSONObject().apply {
                    put("stability", 0.5)
                    put("similarity_boost", 0.75)
                    put("style", 0.5)
                    put("use_speaker_boost", true)
                })
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .addHeader("xi-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val audioBytes = response.body?.bytes()
                if (audioBytes != null) {
                    playAudioBytes(audioBytes)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Empty audio response"))
                }
            } else {
                val error = response.body?.string() ?: "Unknown error"
                Timber.e("ElevenLabs error: $error")
                // Fallback to Android TTS
                speakWithAndroidTTS(text)
            }
        } catch (e: Exception) {
            Timber.e(e, "ElevenLabs TTS failed, falling back to Android TTS")
            speakWithAndroidTTS(text)
        }
    }

    /**
     * Cartesia TTS - Alternative premium voice
     * Cartesia Sonic - Ultra-low latency voice synthesis
     */
    private suspend fun speakWithCartesia(text: String): Result<Unit> {
        return try {
            val apiKey = BuildConfig.CARTESIA_API_KEY
            if (apiKey.isEmpty()) {
                return Result.failure(Exception("Cartesia API key not configured"))
            }

            // Cartesia Sonic API endpoint
            val url = "https://api.cartesia.ai/tts/bytes"

            val jsonBody = JSONObject().apply {
                put("model_id", "sonic-english")
                put("transcript", text)
                put("voice", JSONObject().apply {
                    put("mode", "id")
                    put("id", "a0e99841-438c-4a64-b679-ae501e7d6091") // British male voice
                })
                put("output_format", JSONObject().apply {
                    put("container", "raw")
                    put("encoding", "pcm_s16le")
                    put("sample_rate", 24000)
                })
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .addHeader("X-API-Key", apiKey)
                .addHeader("Cartesia-Version", "2024-06-10")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val audioBytes = response.body?.bytes()
                if (audioBytes != null) {
                    playAudioBytes(audioBytes)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Empty audio response"))
                }
            } else {
                val error = response.body?.string() ?: "Unknown error"
                Timber.e("Cartesia error: $error")
                // Fallback to Android TTS
                speakWithAndroidTTS(text)
            }
        } catch (e: Exception) {
            Timber.e(e, "Cartesia TTS failed, falling back to Android TTS")
            speakWithAndroidTTS(text)
        }
    }

    /**
     * Android TTS - Free fallback option
     * Configured with British accent to mimic Jarvis
     */
    private suspend fun speakWithAndroidTTS(text: String): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            if (!isTTSReady) {
                continuation.resumeWithException(Exception("Android TTS not ready"))
                return@suspendCancellableCoroutine
            }

            val utteranceId = "jarvis_${System.currentTimeMillis()}"

            textToSpeech?.setOnUtteranceProgressListener(
                object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Timber.d("TTS started: $utteranceId")
                    }

                    override fun onDone(utteranceId: String?) {
                        Timber.d("TTS completed: $utteranceId")
                        continuation.resume(Result.success(Unit))
                    }

                    override fun onError(utteranceId: String?) {
                        Timber.e("TTS error: $utteranceId")
                        continuation.resumeWithException(Exception("TTS playback error"))
                    }
                }
            )

            val result = textToSpeech?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId
            )

            if (result != TextToSpeech.SUCCESS) {
                continuation.resumeWithException(Exception("TTS speak failed"))
            }
        }
    }

    /**
     * Play audio bytes from premium TTS services
     */
    private suspend fun playAudioBytes(audioBytes: ByteArray) {
        withContext(Dispatchers.Main) {
            try {
                // Save to temp file
                val tempFile = File.createTempFile("jarvis_tts_", ".mp3", context.cacheDir)
                tempFile.writeBytes(audioBytes)

                // Play with MediaPlayer
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_ASSISTANT)
                            .build()
                    )
                    setDataSource(tempFile.absolutePath)
                    prepare()
                    start()

                    setOnCompletionListener {
                        tempFile.delete()
                        Timber.d("TTS audio playback completed")
                    }

                    setOnErrorListener { _, what, extra ->
                        Timber.e("MediaPlayer error: what=$what, extra=$extra")
                        tempFile.delete()
                        true
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to play audio bytes")
            }
        }
    }

    /**
     * Stop any currently playing speech
     */
    fun stop() {
        textToSpeech?.stop()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /**
     * Check if currently speaking
     */
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking == true || mediaPlayer?.isPlaying == true
    }

    /**
     * Release resources
     */
    fun shutdown() {
        stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        httpClient.dispatcher.executorService.shutdown()
    }

    enum class TTSBackend {
        ELEVENLABS,
        CARTESIA,
        ANDROID
    }

    companion object {
        @Volatile
        private var instance: TTSManager? = null

        fun getInstance(context: Context): TTSManager {
            return instance ?: synchronized(this) {
                instance ?: TTSManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
