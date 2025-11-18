package io.livekit.android.example.voiceassistant.ai

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * ScreenAwarenessManager - Jarvis's screen reading ability
 * MediaProjection API for capturing and analyzing screen content
 * "Jarvis, look at my screen"
 */
class ScreenAwarenessManager(private val context: Context) {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE)
            as MediaProjectionManager

    private val handlerThread = HandlerThread("ScreenCaptureThread").apply { start() }
    private val handler = Handler(handlerThread.looper)

    private var screenWidth = 0
    private var screenHeight = 0
    private var screenDensity = 0

    private var isScreenAwarenessActive = false

    companion object {
        const val REQUEST_CODE_SCREEN_CAPTURE = 1001

        @Volatile
        private var instance: ScreenAwarenessManager? = null

        fun getInstance(context: Context): ScreenAwarenessManager {
            return instance ?: synchronized(this) {
                instance ?: ScreenAwarenessManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    init {
        initializeScreenMetrics()
    }

    /**
     * Initialize screen dimensions
     */
    private fun initializeScreenMetrics() {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)

        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        screenDensity = metrics.densityDpi

        Timber.d("Screen metrics: ${screenWidth}x$screenHeight @ ${screenDensity}dpi")
    }

    /**
     * Request screen capture permission
     * Call this from an Activity, then handle result in onActivityResult
     */
    fun createScreenCaptureIntent(): Intent {
        return mediaProjectionManager.createScreenCaptureIntent()
    }

    /**
     * Start screen awareness with MediaProjection result
     * Call this from onActivityResult after user grants permission
     */
    fun startScreenAwareness(resultCode: Int, data: Intent): Result<Unit> {
        return try {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)

            if (mediaProjection == null) {
                return Result.failure(Exception("Failed to get MediaProjection"))
            }

            // Set up ImageReader for screen capture
            imageReader = ImageReader.newInstance(
                screenWidth,
                screenHeight,
                PixelFormat.RGBA_8888,
                2 // Buffer size
            )

            // Create virtual display
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "JarvisScreenCapture",
                screenWidth,
                screenHeight,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                handler
            )

            isScreenAwarenessActive = true
            Timber.d("Screen Awareness activated, Sir. Jarvis can read your screen now.")

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to start Screen Awareness")
            Result.failure(e)
        }
    }

    /**
     * Capture current screen and analyze with Gemini
     */
    suspend fun captureAndAnalyzeScreen(
        geminiClient: GeminiClient,
        additionalContext: String = ""
    ): String {
        if (!isScreenAwarenessActive) {
            return "Screen Awareness is not active, Sir. Grant screen capture permission first."
        }

        return try {
            val screenshot = captureScreen()
            geminiClient.analyzeScreen(screenshot, additionalContext)
        } catch (e: Exception) {
            Timber.e(e, "Screen analysis failed")
            "Screen reading sensors malfunctioning, Boss. Error: ${e.message}"
        }
    }

    /**
     * Capture current screen as Bitmap
     */
    private suspend fun captureScreen(): Bitmap = suspendCancellableCoroutine { continuation ->
        try {
            imageReader?.setOnImageAvailableListener({ reader ->
                var image: Image? = null
                try {
                    image = reader.acquireLatestImage()

                    if (image != null) {
                        val planes = image.planes
                        val buffer = planes[0].buffer
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * screenWidth

                        // Create bitmap from buffer
                        val bitmap = Bitmap.createBitmap(
                            screenWidth + rowPadding / pixelStride,
                            screenHeight,
                            Bitmap.Config.ARGB_8888
                        )
                        bitmap.copyPixelsFromBuffer(buffer)

                        // Crop to actual screen size (remove padding)
                        val croppedBitmap = Bitmap.createBitmap(
                            bitmap,
                            0,
                            0,
                            screenWidth,
                            screenHeight
                        )

                        continuation.resume(croppedBitmap)
                    } else {
                        continuation.resumeWithException(Exception("Failed to acquire image"))
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error capturing screen")
                    continuation.resumeWithException(e)
                } finally {
                    image?.close()
                }
            }, handler)

            // Trigger capture by posting to handler
            handler.post {
                // ImageReader will automatically capture next frame
            }

        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    /**
     * Quick screen analysis - what's on screen right now?
     */
    suspend fun quickScreenAnalysis(geminiClient: GeminiClient): String {
        val context = """
Quickly identify what Sir is looking at right now:
1. What app or screen is visible?
2. Key information or action being taken
3. Brief sarcastic observation

Keep it concise, Boss is busy.
"""
        return captureAndAnalyzeScreen(geminiClient, context)
    }

    /**
     * Code review from screen
     * Useful when Sir is looking at code in IDE or browser
     */
    suspend fun reviewCodeOnScreen(geminiClient: GeminiClient): String {
        val context = """
${StarkPersonality.Modes.CODE_REVIEW}

Analyze the code visible on this screen:
1. Identify the language and what the code does
2. Point out bugs, anti-patterns, or issues
3. Suggest improvements
4. Deliver with signature Jarvis sarcasm

If no code is visible, roast Sir for procrastinating.
"""
        return captureAndAnalyzeScreen(geminiClient, context)
    }

    /**
     * Read text on screen (emails, documents, browser, etc.)
     */
    suspend fun readScreenText(geminiClient: GeminiClient): String {
        val context = """
Read and summarize the text content on this screen:
1. What type of content is it? (email, article, doc, etc.)
2. Key points or information
3. Any action items or important details
4. Witty commentary if appropriate

If it's nonsense, feel free to roast Sir.
"""
        return captureAndAnalyzeScreen(geminiClient, context)
    }

    /**
     * Stop screen awareness and release resources
     */
    fun stopScreenAwareness() {
        isScreenAwarenessActive = false
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
        mediaProjection?.stop()
        mediaProjection = null

        Timber.d("Screen Awareness deactivated, Sir.")
    }

    /**
     * Check if screen awareness is currently active
     */
    fun isActive(): Boolean = isScreenAwarenessActive

    /**
     * Release all resources
     */
    fun release() {
        stopScreenAwareness()
        handlerThread.quitSafely()
    }
}
