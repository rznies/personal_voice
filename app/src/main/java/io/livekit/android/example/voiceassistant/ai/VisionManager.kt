package io.livekit.android.example.voiceassistant.ai

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * VisionManager - Jarvis's eyes
 * Camera2 API integration for real-time vision analysis
 * "Jarvis, what do you see?"
 */
class VisionManager(private val context: Context) {

    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)

    private var isVisionModeActive = false
    private var currentCameraId: String? = null

    /**
     * Check if camera permission is granted
     */
    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Open camera and start vision mode
     * @param useFrontCamera true for selfie cam, false for back cam
     */
    suspend fun startVisionMode(useFrontCamera: Boolean = false): Result<Unit> {
        if (!hasCameraPermission()) {
            return Result.failure(SecurityException("Camera permission not granted"))
        }

        return try {
            val cameraId = getCameraId(useFrontCamera)
            currentCameraId = cameraId

            openCamera(cameraId)
            isVisionModeActive = true

            Timber.d("Vision Mode activated, Sir. Jarvis can see now.")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to start Vision Mode")
            Result.failure(e)
        }
    }

    /**
     * Capture a single frame and analyze with Gemini Vision
     */
    suspend fun captureAndAnalyze(
        geminiClient: GeminiClient,
        prompt: String = "Describe what you see with precision and wit, Sir."
    ): String {
        if (!isVisionModeActive) {
            return "Vision Mode is not active, Sir. Call startVisionMode() first."
        }

        return try {
            val bitmap = captureFrame()
            geminiClient.analyzeImage(bitmap, prompt)
        } catch (e: Exception) {
            Timber.e(e, "Vision analysis failed")
            "Vision sensors malfunctioning, Boss. Error: ${e.message}"
        }
    }

    /**
     * Capture a single frame from camera
     */
    private suspend fun captureFrame(): Bitmap = suspendCancellableCoroutine { continuation ->
        try {
            // Set up image reader for single capture
            val captureReader = ImageReader.newInstance(
                1920, 1080, // HD resolution
                ImageFormat.JPEG,
                1
            )

            captureReader.setOnImageAvailableListener({ reader ->
                try {
                    val image = reader.acquireLatestImage()
                    if (image != null) {
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)
                        image.close()

                        // Convert to Bitmap
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(
                            bytes, 0, bytes.size
                        )

                        // Rotate bitmap if needed (front camera mirror)
                        val rotatedBitmap = rotateBitmap(bitmap, getCameraRotation())

                        continuation.resume(rotatedBitmap)
                    } else {
                        continuation.resumeWithException(Exception("Failed to capture image"))
                    }
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                } finally {
                    reader.close()
                }
            }, cameraHandler)

            // Create capture session
            cameraDevice?.createCaptureSession(
                listOf(captureReader.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        try {
                            val captureRequest = cameraDevice?.createCaptureRequest(
                                CameraDevice.TEMPLATE_STILL_CAPTURE
                            )?.apply {
                                addTarget(captureReader.surface)
                                set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
                            }?.build()

                            if (captureRequest != null) {
                                session.capture(captureRequest, null, cameraHandler)
                            } else {
                                continuation.resumeWithException(Exception("Failed to build capture request"))
                            }
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        continuation.resumeWithException(Exception("Capture session configuration failed"))
                    }
                },
                cameraHandler
            )

        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    /**
     * Stop vision mode and release camera
     */
    fun stopVisionMode() {
        isVisionModeActive = false
        cameraCaptureSession?.close()
        cameraCaptureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null

        Timber.d("Vision Mode deactivated, Sir.")
    }

    /**
     * Open camera device
     */
    private suspend fun openCamera(cameraId: String): Unit = suspendCancellableCoroutine { continuation ->
        try {
            if (!hasCameraPermission()) {
                continuation.resumeWithException(SecurityException("Camera permission not granted"))
                return@suspendCancellableCoroutine
            }

            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    Timber.d("Camera opened: $cameraId")
                    continuation.resume(Unit)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                    Timber.w("Camera disconnected")
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                    val errorMsg = "Camera error: $error"
                    Timber.e(errorMsg)
                    continuation.resumeWithException(Exception(errorMsg))
                }
            }, cameraHandler)

        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    /**
     * Get camera ID for front or back camera
     */
    private fun getCameraId(useFrontCamera: Boolean): String {
        val cameraIdList = cameraManager.cameraIdList

        for (id in cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)

            if (useFrontCamera && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                return id
            } else if (!useFrontCamera && facing == CameraCharacteristics.LENS_FACING_BACK) {
                return id
            }
        }

        // Fallback to first available camera
        return cameraIdList[0]
    }

    /**
     * Get camera rotation for proper image orientation
     */
    private fun getCameraRotation(): Int {
        currentCameraId?.let { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)

            return when (facing) {
                CameraCharacteristics.LENS_FACING_FRONT -> (360 - sensorOrientation) % 360
                else -> sensorOrientation
            }
        }
        return 0
    }

    /**
     * Rotate bitmap based on camera orientation
     */
    private fun rotateBitmap(bitmap: Bitmap, rotation: Int): Bitmap {
        if (rotation == 0) return bitmap

        val matrix = Matrix().apply {
            postRotate(rotation.toFloat())
        }

        return Bitmap.createBitmap(
            bitmap, 0, 0,
            bitmap.width, bitmap.height,
            matrix, true
        )
    }

    /**
     * Release all resources
     */
    fun release() {
        stopVisionMode()
        cameraThread.quitSafely()
    }

    companion object {
        @Volatile
        private var instance: VisionManager? = null

        fun getInstance(context: Context): VisionManager {
            return instance ?: synchronized(this) {
                instance ?: VisionManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
