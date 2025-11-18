package io.livekit.android.example.voiceassistant.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import io.livekit.android.example.voiceassistant.MainActivity
import io.livekit.android.example.voiceassistant.ui.theme.ArcReactorBlue
import io.livekit.android.example.voiceassistant.ui.theme.StarkBlack
import io.livekit.android.example.voiceassistant.ui.theme.StarkJarvisTheme
import io.livekit.android.example.voiceassistant.utils.ErrorHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * SplashActivity - Arc Reactor startup animation
 * "Initiating Jarvis Protocol..."
 *
 * Features:
 * - Arc reactor pulsing animation
 * - Circular energy rings
 * - Stark Industries branding
 * - Crash recovery check
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for crash recovery
        val errorHandler = ErrorHandler.getInstance(this)
        if (errorHandler.hasPreviousCrashes()) {
            Timber.w("Previous crash detected on splash screen")
            // Show recovery message in MainActivity
        }

        setContent {
            StarkJarvisTheme {
                SplashScreen(
                    onTimeout = {
                        navigateToMain()
                    }
                )
            }
        }
    }

    private fun navigateToMain() {
        lifecycleScope.launch {
            // Minimum splash duration for smooth experience
            delay(2500)

            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()

            // Override transition for smooth fade
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor_pulse")

    // Arc reactor pulsing scale
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Rotating angle for energy rings
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Opacity pulse
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Trigger navigation after initial animation
    LaunchedEffect(Unit) {
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StarkBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Arc Reactor Animation
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                ArcReactorSplashAnimation(
                    scale = scale,
                    rotation = rotation,
                    alpha = alpha
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Jarvis branding
            Text(
                text = "JARVIS",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = ArcReactorBlue,
                letterSpacing = 8.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "STARK INDUSTRIES",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = ArcReactorBlue.copy(alpha = 0.6f),
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loading message
            Text(
                text = "Initiating AI protocol...",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Text(
                text = "Powered by Stark Tech",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
fun ArcReactorSplashAnimation(
    scale: Float,
    rotation: Float,
    alpha: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = size.minDimension / 3f

        // Outer rotating ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ArcReactorBlue.copy(alpha = alpha * 0.5f),
                    Color.Transparent
                ),
                center = center,
                radius = baseRadius * scale * 1.5f
            ),
            radius = baseRadius * scale * 1.5f,
            center = center
        )

        // Middle pulsing ring
        drawCircle(
            color = ArcReactorBlue.copy(alpha = alpha * 0.7f),
            radius = baseRadius * scale,
            center = center,
            style = Stroke(width = 3f)
        )

        // Inner core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha),
                    ArcReactorBlue.copy(alpha = alpha * 0.8f),
                    ArcReactorBlue.copy(alpha = alpha * 0.3f)
                ),
                center = center,
                radius = baseRadius * scale * 0.5f
            ),
            radius = baseRadius * scale * 0.5f,
            center = center
        )

        // Rotating energy segments (8 segments)
        for (i in 0..7) {
            val angle = Math.toRadians((rotation + i * 45).toDouble())
            val startOffset = Offset(
                x = center.x + (baseRadius * scale * 0.6f * kotlin.math.cos(angle)).toFloat(),
                y = center.y + (baseRadius * scale * 0.6f * kotlin.math.sin(angle)).toFloat()
            )
            val endOffset = Offset(
                x = center.x + (baseRadius * scale * 1.2f * kotlin.math.cos(angle)).toFloat(),
                y = center.y + (baseRadius * scale * 1.2f * kotlin.math.sin(angle)).toFloat()
            )

            drawLine(
                color = ArcReactorBlue.copy(alpha = alpha * 0.6f),
                start = startOffset,
                end = endOffset,
                strokeWidth = 2f
            )
        }

        // Center dot
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = 4f,
            center = center
        )
    }
}
