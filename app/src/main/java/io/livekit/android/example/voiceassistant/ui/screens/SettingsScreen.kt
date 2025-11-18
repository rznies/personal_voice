package io.livekit.android.example.voiceassistant.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import io.livekit.android.example.voiceassistant.security.BiometricAuthManager
import io.livekit.android.example.voiceassistant.security.ComponentEnabler
import io.livekit.android.example.voiceassistant.security.ComponentEnabler
import io.livekit.android.example.voiceassistant.security.SecureKeyManager
import io.livekit.android.example.voiceassistant.security.SecurityPreferences
import io.livekit.android.example.voiceassistant.ui.theme.*
import timber.log.Timber

/**
 * SettingsScreen - Iron Man HUD-style security controls
 * "Sometimes you gotta run before you can walk" - but let's keep it safe, Sir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val securityPrefs = remember { SecurityPreferences.getInstance(context) }
    val keyManager = remember { SecureKeyManager.getInstance(context) }
    val componentEnabler = remember { ComponentEnabler.getInstance(context) }
    val biometricManager = remember {
        if (context is FragmentActivity) {
            BiometricAuthManager(context)
        } else null
    }

    val settings by securityPrefs.securitySettings.collectAsState()
    val scrollState = rememberScrollState()

    // Animated glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Security Controls",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "JARVIS Protection Protocol",
                            style = MaterialTheme.typography.bodySmall,
                            color = ArcReactorBlue.copy(alpha = glowAlpha)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StarkBlack,
                    titleContentColor = StarkWhite,
                    navigationIconContentColor = ArcReactorBlue
                )
            )
        },
        containerColor = StarkBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header warning
            HUDWarningCard(
                title = "IRON-CLAD PROTECTION",
                message = "All dangerous features are OFF by default. Enable only what you trust.",
                icon = Icons.Default.Security
            )

            Spacer(modifier = Modifier.height(16.dp))

            // API Keys Section
            HUDSection(title = "API KEYS", icon = Icons.Default.Key) {
                APIKeysSection(keyManager = keyManager, biometricManager = biometricManager)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wake Word Section
            HUDSection(title = "WAKE WORD DETECTION", icon = Icons.Default.RecordVoiceOver) {
                HUDToggle(
                    title = "Always Listening",
                    description = "Jarvis listens for 'Jarvis', 'Boss', or 'Sir'",
                    checked = settings.wakeWordEnabled,
                    onCheckedChange = { enabled ->
                        securityPrefs.setWakeWordEnabled(enabled)
                        componentEnabler.setWakeWordServiceEnabled(enabled)
                    },
                    dangerous = false
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permissions Section
            HUDSection(title = "PERMISSION CONTROLS", icon = Icons.Default.AdminPanelSettings) {
                HUDToggle(
                    title = "Screen Capture - Ask Every Time",
                    description = "Require permission before reading screen",
                    checked = settings.screenCaptureAlwaysAsk,
                    onCheckedChange = { securityPrefs.setScreenCaptureAlwaysAsk(it) },
                    dangerous = false
                )

                Spacer(modifier = Modifier.height(12.dp))

                HUDToggle(
                    title = "Camera - Ask Every Time",
                    description = "Require permission before using camera",
                    checked = settings.cameraAlwaysAsk,
                    onCheckedChange = { securityPrefs.setCameraAlwaysAsk(it) },
                    dangerous = false
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Accessibility Level - CRITICAL
            HUDSection(
                title = "ACCESSIBILITY LEVEL",
                icon = Icons.Default.Warning,
                dangerous = true
            ) {
                AccessibilityLevelSelector(
                    currentLevel = settings.accessibilityLevel,
                    onLevelChange = { securityPrefs.setAccessibilityLevel(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Boot Start Section
            HUDSection(title = "AUTO-START", icon = Icons.Default.PowerSettingsNew) {
                HUDToggle(
                    title = "Start on Boot",
                    description = "Launch Jarvis when device starts",
                    checked = settings.bootStartEnabled,
                    onCheckedChange = { enabled ->
                        securityPrefs.setBootStartEnabled(enabled)
                        componentEnabler.setBootReceiverEnabled(enabled)
                    },
                    dangerous = false
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Biometric Auth Section
            HUDSection(title = "BIOMETRIC PROTECTION", icon = Icons.Default.Fingerprint) {
                val biometricStatus = biometricManager?.isBiometricAvailable()
                    ?: BiometricAuthManager.BiometricStatus.UNAVAILABLE

                if (biometricStatus == BiometricAuthManager.BiometricStatus.AVAILABLE) {
                    HUDToggle(
                        title = "Require Biometric for Dangerous Actions",
                        description = "Fingerprint/Face for send message, make call, delete",
                        checked = settings.requireBiometricAuth,
                        onCheckedChange = { securityPrefs.setRequireBiometricAuth(it) },
                        dangerous = false
                    )
                } else {
                    Text(
                        text = "⚠️ ${biometricStatus.message}",
                        color = IronRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cloud Mode Section
            HUDSection(title = "CLOUD SERVICES", icon = Icons.Default.Cloud) {
                HUDToggle(
                    title = "Local-Only Mode",
                    description = "Process everything on-device. No cloud APIs except Gemini.",
                    checked = settings.localOnlyMode,
                    onCheckedChange = { securityPrefs.setLocalOnlyMode(it) },
                    dangerous = false
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audit Log Section
            HUDSection(title = "AUDIT TRAIL", icon = Icons.Default.History) {
                AuditLogSection(securityPrefs = securityPrefs)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HUDSection(
    title: String,
    icon: ImageVector,
    dangerous: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = if (dangerous) IronRed else ArcReactorBlue

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = borderColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderColor.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = borderColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = borderColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        content()
    }
}

@Composable
fun HUDToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    dangerous: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = StarkWhite,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = StarkLightGray
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (dangerous) IronRed else ArcReactorBlue,
                checkedTrackColor = if (dangerous) IronRedDark else ArcReactorBlueDark,
                uncheckedThumbColor = StarkMediumGray,
                uncheckedTrackColor = StarkSlate
            )
        )
    }
}

@Composable
fun AccessibilityLevelSelector(
    currentLevel: SecurityPreferences.AccessibilityLevel,
    onLevelChange: (SecurityPreferences.AccessibilityLevel) -> Unit
) {
    Column {
        Text(
            text = "⚠️ FULL mode grants complete device control. Use with extreme caution.",
            style = MaterialTheme.typography.bodySmall,
            color = IronRed,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        SecurityPreferences.AccessibilityLevel.values().forEach { level ->
            val selected = level == currentLevel
            val (color, description) = when (level) {
                SecurityPreferences.AccessibilityLevel.OFF -> ArcReactorBlue to "No accessibility access (safest)"
                SecurityPreferences.AccessibilityLevel.READ -> StarkGold to "Read screen content only"
                SecurityPreferences.AccessibilityLevel.BASIC -> Color(0xFFFF9800) to "Read + basic interactions"
                SecurityPreferences.AccessibilityLevel.FULL -> IronRed to "⚠️ Complete device control (DANGEROUS)"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) color else StarkSlate,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(
                        if (selected) color.copy(alpha = 0.1f) else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected,
                    onClick = { onLevelChange(level) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = color,
                        unselectedColor = StarkMediumGray
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = level.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selected) color else StarkWhite,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = StarkLightGray
                    )
                }
            }
        }
    }
}

@Composable
fun APIKeysSection(
    keyManager: SecureKeyManager,
    biometricManager: BiometricAuthManager?
) {
    var showKeyEntry by remember { mutableStateOf(false) }
    val keyStatus = remember { keyManager.getKeyStatus() }

    Column {
        Text(
            text = "⚠️ Never share your API keys. They are encrypted at rest.",
            style = MaterialTheme.typography.bodySmall,
            color = StarkGold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        SecureKeyManager.KeyType.values().forEach { keyType ->
            val status = keyStatus[keyType] ?: SecureKeyManager.KeyStatus.MISSING
            val statusColor = when (status) {
                SecureKeyManager.KeyStatus.VALID -> Color(0xFF4CAF50)
                SecureKeyManager.KeyStatus.MISSING -> IronRed
                SecureKeyManager.KeyStatus.INVALID -> Color(0xFFFF9800)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = keyType.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = StarkWhite
                    )
                    Text(
                        text = if (keyType.required) "Required" else "Optional",
                        style = MaterialTheme.typography.bodySmall,
                        color = StarkLightGray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = status.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Icon(
                        imageVector = when (status) {
                            SecureKeyManager.KeyStatus.VALID -> Icons.Default.CheckCircle
                            SecureKeyManager.KeyStatus.MISSING -> Icons.Default.Cancel
                            SecureKeyManager.KeyStatus.INVALID -> Icons.Default.Warning
                        },
                        contentDescription = status.name,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { showKeyEntry = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = ArcReactorBlue,
                contentColor = StarkBlack
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.VpnKey, "Manage Keys")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Manage API Keys")
        }
    }

    if (showKeyEntry) {
        APIKeyEntryDialog(
            keyManager = keyManager,
            onDismiss = { showKeyEntry = false }
        )
    }
}

@Composable
fun APIKeyEntryDialog(
    keyManager: SecureKeyManager,
    onDismiss: () -> Unit
) {
    var selectedKeyType by remember { mutableStateOf(SecureKeyManager.KeyType.GEMINI) }
    var keyValue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Enter API Key",
                color = ArcReactorBlue,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Select key type and enter value. Keys are encrypted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = StarkLightGray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Key type selector
                SecureKeyManager.KeyType.values().forEach { keyType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = keyType == selectedKeyType,
                            onClick = {
                                selectedKeyType = keyType
                                keyValue = keyManager.getKey(keyType) ?: ""
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = ArcReactorBlue)
                        )
                        Text(
                            text = keyType.displayName,
                            color = StarkWhite,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = keyValue,
                    onValueChange = { keyValue = it },
                    label = { Text("API Key") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcReactorBlue,
                        unfocusedBorderColor = StarkSlate,
                        focusedLabelColor = ArcReactorBlue,
                        cursorColor = ArcReactorBlue,
                        focusedTextColor = StarkWhite,
                        unfocusedTextColor = StarkWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    keyManager.storeKey(selectedKeyType, keyValue)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ArcReactorBlue)
            ) {
                Text("Save", color = StarkBlack)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = StarkLightGray)
            }
        },
        containerColor = StarkDarkGray,
        tonalElevation = 8.dp
    )
}

@Composable
fun AuditLogSection(securityPrefs: SecurityPreferences) {
    var showFullLog by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "All security events are logged for your protection.",
            style = MaterialTheme.typography.bodySmall,
            color = StarkLightGray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(
            onClick = { showFullLog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = StarkSlate,
                contentColor = ArcReactorBlue
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Article, "View Log")
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Audit Trail")
        }
    }

    if (showFullLog) {
        AlertDialog(
            onDismissRequest = { showFullLog = false },
            title = {
                Text("Security Audit Trail", color = ArcReactorBlue)
            },
            text = {
                val log = securityPrefs.getAuditTrail()
                Text(
                    text = log,
                    style = MaterialTheme.typography.bodySmall,
                    color = StarkWhite,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = { showFullLog = false }) {
                    Text("Close", color = ArcReactorBlue)
                }
            },
            containerColor = StarkDarkGray
        )
    }
}

@Composable
fun HUDWarningCard(
    title: String,
    message: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = ArcReactorBlue,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 4.dp.toPx()
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = StarkCharcoal
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ArcReactorBlue,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ArcReactorBlue,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarkWhite
                )
            }
        }
    }
}
