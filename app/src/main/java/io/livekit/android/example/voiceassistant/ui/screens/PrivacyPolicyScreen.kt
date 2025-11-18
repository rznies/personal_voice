package io.livekit.android.example.voiceassistant.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.livekit.android.example.voiceassistant.security.SecurityPreferences
import io.livekit.android.example.voiceassistant.ui.theme.*

/**
 * PrivacyPolicyScreen - First launch privacy agreement
 * "With great power comes great responsibility" - Uncle Ben (but Tony would say it cooler)
 */
@Composable
fun PrivacyPolicyScreen(
    onAccept: () -> Unit
) {
    val context = LocalContext.current
    val securityPrefs = remember { SecurityPreferences.getInstance(context) }
    val scrollState = rememberScrollState()

    // Animated Arc Reactor glow
    val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        StarkBlack,
                        StarkDarkGray,
                        StarkBlack
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Arc Reactor Logo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .drawBehind {
                        drawCircle(
                            color = ArcReactorBlue.copy(alpha = glowAlpha * 0.3f),
                            radius = size.width / 2
                        )
                    }
                    .border(
                        width = 3.dp,
                        color = ArcReactorBlue.copy(alpha = glowAlpha),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security",
                    tint = ArcReactorBlue.copy(alpha = glowAlpha),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "STARK JARVIS",
                style = MaterialTheme.typography.headlineLarge,
                color = ArcReactorBlue,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Privacy & Security Agreement",
                style = MaterialTheme.typography.titleMedium,
                color = StarkGold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Privacy sections
            PrivacySection(
                icon = Icons.Default.Shield,
                title = "Your Safety First",
                description = "All dangerous features are OFF by default. You control every permission."
            )

            PrivacySection(
                icon = Icons.Default.Lock,
                title = "Encrypted Storage",
                description = "API keys and sensitive data are encrypted with AES-256-GCM. Never stored in plain text."
            )

            PrivacySection(
                icon = Icons.Default.NoAccounts,
                title = "No Tracking",
                description = "We don't collect, store, or sell your data. Ever. Your conversations stay on your device."
            )

            PrivacySection(
                icon = Icons.Default.PrivacyTip,
                title = "Local-Only Mode",
                description = "Process everything on-device by default. Cloud APIs only when you enable them."
            )

            PrivacySection(
                icon = Icons.Default.Fingerprint,
                title = "Biometric Protection",
                description = "High-risk actions like sending messages require your fingerprint or face."
            )

            PrivacySection(
                icon = Icons.Default.Visibility,
                title = "Full Transparency",
                description = "All security events are logged. You can audit everything Jarvis does."
            )

            PrivacySection(
                icon = Icons.Default.Warning,
                title = "God-Mode Disabled",
                description = "Accessibility controls are OFF. You must explicitly grant device control permissions."
            )

            PrivacySection(
                icon = Icons.Default.Key,
                title = "Your API Keys",
                description = "You must enter your own API keys. We never ship keys in the app."
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Warning card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, IronRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = IronRed.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = IronRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "IMPORTANT",
                            style = MaterialTheme.typography.titleSmall,
                            color = IronRed,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This AI assistant can perform powerful actions on your device. Only enable features you understand and trust. With great power comes great responsibility.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarkWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Accept button
            Button(
                onClick = {
                    securityPrefs.acceptPrivacyPolicy()
                    securityPrefs.completeFirstLaunch()
                    onAccept()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArcReactorBlue
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CheckCircle, "Accept")
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "I Understand & Accept",
                    style = MaterialTheme.typography.titleMedium,
                    color = StarkBlack,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "\"Sometimes you gotta run before you can walk.\"\n- Tony Stark\n\nBut let's keep it safe, Sir.",
                style = MaterialTheme.typography.bodySmall,
                color = StarkGold.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PrivacySection(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = StarkCharcoal
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .drawBehind {
                    drawLine(
                        color = ArcReactorBlue.copy(alpha = 0.3f),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 3.dp.toPx()
                    )
                }
                .padding(start = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ArcReactorBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = StarkWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarkLightGray,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
