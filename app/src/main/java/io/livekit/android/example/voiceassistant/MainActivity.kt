package io.livekit.android.example.voiceassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.livekit.android.LiveKit
import io.livekit.android.example.voiceassistant.screen.ConnectRoute
import io.livekit.android.example.voiceassistant.screen.ConnectScreen
import io.livekit.android.example.voiceassistant.screen.VoiceAssistantRoute
import io.livekit.android.example.voiceassistant.screen.VoiceAssistantScreen
import io.livekit.android.example.voiceassistant.security.SecurityPreferences
import io.livekit.android.example.voiceassistant.ui.screens.PrivacyPolicyScreen
import io.livekit.android.example.voiceassistant.ui.screens.SettingsScreen
import io.livekit.android.example.voiceassistant.ui.theme.LiveKitVoiceAssistantExampleTheme
import io.livekit.android.example.voiceassistant.viewmodel.VoiceAssistantViewModel
import io.livekit.android.util.LoggingLevel
import kotlinx.serialization.Serializable

// 🔒 Security Navigation Routes
@Serializable
object PrivacyPolicyRoute

@Serializable
object SettingsRoute

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LiveKit.loggingLevel = LoggingLevel.DEBUG

        setContent {
            val navController = rememberNavController()
            val securityPrefs = SecurityPreferences.getInstance(this)
            val settings by securityPrefs.securitySettings.collectAsState()

            // 🔒 SECURITY: Show Privacy Policy on first launch
            val startDestination = if (settings.isFirstLaunch) {
                PrivacyPolicyRoute
            } else {
                ConnectRoute
            }

            LiveKitVoiceAssistantExampleTheme(dynamicColor = false) {
                Scaffold { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {

                        // Set up NavHost for the app
                        NavHost(navController, startDestination = startDestination) {
                            // 🔒 Privacy Policy Screen (first launch)
                            composable<PrivacyPolicyRoute> {
                                PrivacyPolicyScreen {
                                    runOnUiThread {
                                        navController.navigate(ConnectRoute) {
                                            popUpTo<PrivacyPolicyRoute> { inclusive = true }
                                        }
                                    }
                                }
                            }

                            // 🔒 Settings Screen
                            composable<SettingsRoute> {
                                SettingsScreen {
                                    runOnUiThread { navController.navigateUp() }
                                }
                            }

                            // Connect Screen
                            composable<ConnectRoute> {
                                ConnectScreen { url, token ->
                                    runOnUiThread {
                                        navController.navigate(VoiceAssistantRoute(url, token))
                                    }
                                }
                            }

                            // Voice Assistant Screen
                            composable<VoiceAssistantRoute> {
                                val viewModel = viewModel<VoiceAssistantViewModel>()
                                VoiceAssistantScreen(
                                    viewModel = viewModel,
                                    onEndCall = {
                                        runOnUiThread { navController.navigateUp() }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
