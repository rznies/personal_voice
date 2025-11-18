package io.livekit.android.example.voiceassistant.jarvis

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.livekit.android.example.voiceassistant.ui.theme.*
import kotlinx.coroutines.launch

/**
 * JarvisScreen - Main conversation interface
 * Tony Stark's personal AI assistant UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisScreen(
    viewModel: JarvisViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val conversationHistory by viewModel.conversationHistory.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var messageText by remember { mutableStateOf("") }

    // Screen capture permission launcher
    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.startScreenAwareness(result.resultCode, result.data!!)
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(conversationHistory.size) {
        if (conversationHistory.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(conversationHistory.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            JarvisTopBar(
                uiState = uiState,
                onVisionToggle = {
                    if (uiState.isVisionModeActive) {
                        viewModel.deactivateVisionMode()
                    } else {
                        viewModel.activateVisionMode()
                    }
                },
                onScreenAwarenessToggle = {
                    if (uiState.isScreenAwarenessActive) {
                        viewModel.deactivateScreenAwareness()
                    } else {
                        val intent = viewModel.requestScreenCapturePermission()
                        screenCaptureLauncher.launch(intent)
                    }
                },
                onWakeWordToggle = {
                    if (uiState.isWakeWordActive) {
                        viewModel.stopWakeWordService()
                    } else {
                        viewModel.startWakeWordService()
                    }
                }
            )
        },
        bottomBar = {
            JarvisBottomBar(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSendMessage = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                },
                isProcessing = uiState.isProcessing,
                onVisionCapture = {
                    if (uiState.isVisionModeActive) {
                        viewModel.analyzeVision()
                    }
                },
                onScreenCapture = {
                    if (uiState.isScreenAwarenessActive) {
                        viewModel.analyzeScreen(ScreenAnalysisMode.QUICK)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(StarkBlack, StarkDarkGray)
                    )
                )
                .padding(paddingValues)
        ) {
            if (conversationHistory.isEmpty()) {
                // Empty state - show welcome
                JarvisWelcomeScreen()
            } else {
                // Conversation history
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(conversationHistory) { message ->
                        MessageBubble(message = message)
                    }
                }
            }

            // Processing indicator
            AnimatedVisibility(
                visible = uiState.isProcessing,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                JarvisProcessingIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisTopBar(
    uiState: JarvisUiState,
    onVisionToggle: () -> Unit,
    onScreenAwarenessToggle: () -> Unit,
    onWakeWordToggle: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "JARVIS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ArcReactorBlue
                )
                Text(
                    text = uiState.statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = StarkLightGray
                )
            }
        },
        actions = {
            // Vision Mode toggle
            IconButton(onClick = onVisionToggle) {
                Icon(
                    imageVector = if (uiState.isVisionModeActive) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = "Vision Mode",
                    tint = if (uiState.isVisionModeActive) ArcReactorBlue else StarkMediumGray
                )
            }

            // Screen Awareness toggle
            IconButton(onClick = onScreenAwarenessToggle) {
                Icon(
                    imageVector = if (uiState.isScreenAwarenessActive) Icons.Filled.ScreenShare else Icons.Filled.StopScreenShare,
                    contentDescription = "Screen Awareness",
                    tint = if (uiState.isScreenAwarenessActive) ArcReactorBlue else StarkMediumGray
                )
            }

            // Wake Word toggle
            IconButton(onClick = onWakeWordToggle) {
                Icon(
                    imageVector = if (uiState.isWakeWordActive) Icons.Filled.Mic else Icons.Filled.MicOff,
                    contentDescription = "Wake Word",
                    tint = if (uiState.isWakeWordActive) ArcReactorBlue else StarkMediumGray
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = StarkDarkGray,
            titleContentColor = StarkWhite
        )
    )
}

@Composable
fun JarvisBottomBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isProcessing: Boolean,
    onVisionCapture: () -> Unit,
    onScreenCapture: () -> Unit
) {
    Surface(
        color = StarkDarkGray,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Vision capture button
            IconButton(
                onClick = onVisionCapture,
                modifier = Modifier
                    .size(48.dp)
                    .background(StarkCharcoal, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "Capture Vision",
                    tint = ArcReactorBlue
                )
            }

            // Screen capture button
            IconButton(
                onClick = onScreenCapture,
                modifier = Modifier
                    .size(48.dp)
                    .background(StarkCharcoal, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Screenshot,
                    contentDescription = "Capture Screen",
                    tint = ArcReactorBlue
                )
            }

            // Text input
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Ask Jarvis anything...",
                        color = StarkMediumGray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArcReactorBlue,
                    unfocusedBorderColor = StarkSlate,
                    focusedTextColor = StarkWhite,
                    unfocusedTextColor = StarkWhite,
                    cursorColor = ArcReactorBlue
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                enabled = !isProcessing
            )

            // Send button
            IconButton(
                onClick = onSendMessage,
                enabled = messageText.isNotBlank() && !isProcessing,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (messageText.isNotBlank() && !isProcessing) ArcReactorBlue else StarkSlate,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send",
                    tint = if (messageText.isNotBlank() && !isProcessing) StarkBlack else StarkMediumGray
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromJarvis) Arrangement.Start else Arrangement.End
    ) {
        Surface(
            color = if (message.isFromJarvis) StarkCharcoal else ArcReactorBlueDark,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Message type indicator
                if (message.type != MessageType.TEXT) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (message.type) {
                                MessageType.VISION -> Icons.Filled.RemoveRedEye
                                MessageType.SCREEN_ANALYSIS -> Icons.Filled.Monitor
                                else -> Icons.Filled.Chat
                            },
                            contentDescription = null,
                            tint = ArcReactorBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = when (message.type) {
                                MessageType.VISION -> "Vision Mode"
                                MessageType.SCREEN_ANALYSIS -> "Screen Analysis"
                                else -> "Message"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = ArcReactorBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarkWhite
                )
            }
        }
    }
}

@Composable
fun JarvisWelcomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Arc Reactor Animation
        ArcReactorAnimation()

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "JARVIS",
            style = MaterialTheme.typography.displayLarge,
            color = ArcReactorBlue,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Stark Industries",
            style = MaterialTheme.typography.titleMedium,
            color = StarkGold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "For When You Need a Billionaire's Edge",
            style = MaterialTheme.typography.bodyMedium,
            color = StarkLightGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Say \"Jarvis\" or type your command, Sir.",
            style = MaterialTheme.typography.bodySmall,
            color = StarkMediumGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ArcReactorAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    ArcReactorGlow.copy(alpha = alpha),
                    CircleShape
                )
        )

        // Core
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(ArcReactorBlue, CircleShape)
        )

        // Center
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(StarkWhite, CircleShape)
        )
    }
}

@Composable
fun JarvisProcessingIndicator() {
    Surface(
        color = StarkCharcoal.copy(alpha = 0.9f),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = ArcReactorBlue,
                strokeWidth = 2.dp
            )

            Text(
                text = "Processing your genius...",
                style = MaterialTheme.typography.bodyMedium,
                color = StarkWhite
            )
        }
    }
}
