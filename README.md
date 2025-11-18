# StarkJarvis - AI Assistant with Tony Stark's Personality

> "Sometimes you gotta run before you can walk." - Tony Stark

**StarkJarvis** is a production-ready Android AI assistant app that combines cutting-edge AI technology with the wit, sarcasm, and brilliance of Tony Stark's Jarvis. Built with Gemini 2.0 Flash, LiveKit, and modern Android architecture.

<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-latest-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Gemini API](https://img.shields.io/badge/Gemini-2.0%20Flash-blue.svg)](https://ai.google.dev)
[![LiveKit](https://img.shields.io/badge/LiveKit-Android-orange.svg)](https://livekit.io)

</div>

---

## ✨ Features

### 🎤 Core AI Capabilities
- **Wake Word Detection** - Always listening for "Jarvis", "Hey Boss", "Sir"
- **Natural Conversations** - Powered by Gemini 2.0 Flash with Tony Stark personality
- **Multi-Backend TTS** - ElevenLabs, Cartesia, or Android TTS with British accent
- **Offline Mode** - Basic commands work without internet connection

### 👁️ Vision & Awareness
- **Vision Mode** - Camera-based scene analysis using Gemini Vision API
- **Screen Awareness** - Read and analyze anything on your screen
- **Full Device Control** - Open apps, send messages, control settings via Accessibility Service

### 🦾 Stark Features
- **Suit Mode** - Track productivity (lines of code, goals, work sessions, coffee consumption)
- **Repulsor Mode** - Shake to fire repulsor blasts with haptic feedback
- **Arc Reactor UI** - OLED-optimized dark theme with glowing animations
- **Sarcastic Roasts** - Get roasted for excessive coffee, bad code, or procrastination

### 🚀 Production Features
- **Battery Optimization** - Adaptive performance based on battery level
- **Error Recovery** - Crash detection and graceful error handling
- **Performance Monitoring** - Track response times and memory usage
- **Offline Fallback** - Basic commands work without internet
- **Unit Tests** - Comprehensive test coverage for core features

---

## 📸 Screenshots

*(Add screenshots of your app here)*

- Splash Screen with Arc Reactor animation
- Main conversation interface
- Vision Mode
- Suit Mode productivity dashboard
- Settings and configuration

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| **AI Model** | Google Gemini 2.0 Flash |
| **Voice** | LiveKit Android SDK, Android SpeechRecognizer |
| **TTS** | ElevenLabs, Cartesia Sonic, Android TTS |
| **UI** | Jetpack Compose + Material3 |
| **Architecture** | MVVM + Kotlin Coroutines + Flow |
| **Camera** | Camera2 API |
| **Storage** | DataStore Preferences |
| **Sensors** | Accelerometer (shake detection) |
| **Permissions** | Accessibility Service, MediaProjection |
| **Testing** | JUnit, Mockito |

---

## 📋 Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Kotlin**: 1.9+
- **JDK**: 17 or higher

### API Keys Required

1. **Google Gemini API** - Free tier available at [ai.google.dev](https://ai.google.dev)
2. **ElevenLabs API** (Optional) - For premium TTS at [elevenlabs.io](https://elevenlabs.io)
3. **Cartesia API** (Optional) - For Sonic TTS at [cartesia.ai](https://cartesia.ai)

---

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/personal_voice.git
cd personal_voice
```

### 2. Configure API Keys

Copy the `.env.template` to `.env`:

```bash
cp .env .env.local
```

Edit `.env.local` and add your API keys:

```env
# Required
GEMINI_API_KEY=your_gemini_api_key_here

# Optional (for premium TTS)
ELEVENLABS_API_KEY=your_elevenlabs_api_key_here
CARTESIA_API_KEY=your_cartesia_api_key_here

# Optional (for LiveKit voice features)
LIVEKIT_URL=wss://your-livekit-server.com
LIVEKIT_API_KEY=your_livekit_api_key
LIVEKIT_API_SECRET=your_livekit_api_secret
```

### 3. Add API Keys to gradle.properties

Add to `~/.gradle/gradle.properties` or `local.properties`:

```properties
GEMINI_API_KEY=your_gemini_api_key_here
ELEVENLABS_API_KEY=your_elevenlabs_api_key_here
CARTESIA_API_KEY=your_cartesia_api_key_here
```

### 4. Build and Run

```bash
./gradlew assembleDebug
```

Or use Android Studio:
1. Open project in Android Studio
2. Sync Gradle files
3. Run on device or emulator (physical device recommended for sensors)

---

## ⚙️ Configuration

### Permissions Setup

The app requires several permissions for full functionality:

**Required:**
- 🎤 Microphone - Wake word detection and voice commands
- 🌐 Internet - AI API calls

**Recommended:**
- 📷 Camera - Vision Mode
- 📱 Accessibility Service - Device control (open apps, send messages)
- 📺 Screen Capture - Screen awareness
- 📳 Vibrate - Haptic feedback for Repulsor Mode

**Optional:**
- 📞 Contacts, Phone - For "call X" commands
- 💬 SMS - For "send message to X" commands
- 📅 Calendar - For scheduling features

### Accessibility Service Setup

To enable full device control:

1. Open **Settings** → **Accessibility**
2. Find **StarkJarvis**
3. Toggle **On**
4. Grant permission

This allows Jarvis to:
- Open apps by voice command
- Click UI elements
- Read screen content
- Navigate (back, home, recents)

### Wake Word Service

The wake word service starts automatically on app launch and boot.

**Supported wake words:**
- "Jarvis"
- "Hey Jarvis"
- "Boss"
- "Hey Boss"
- "Sir"
- "Stark"

To disable auto-start:
- Remove `BootReceiver` from `AndroidManifest.xml`
- Or disable "Start on boot" in app settings (if implemented)

---

## 📱 Usage Guide

### Basic Conversation

1. Launch the app
2. Say **"Hey Jarvis"** or tap the microphone button
3. Ask anything:
   - "What's the weather like?"
   - "Open Chrome"
   - "Send a message to John saying I'll be late"
   - "Review this code" *(with screen awareness)*

### Vision Mode

1. Tap the **camera icon** in the app
2. Choose front or back camera
3. Say or type: "What do you see?"
4. Jarvis will analyze the image with sarcastic commentary

### Screen Awareness

1. Grant screen capture permission when prompted
2. Say: "What's on my screen?"
3. Jarvis will read and analyze your screen content

### Suit Mode (Productivity Tracking)

1. Say: "Activate Suit Mode" or use the toggle
2. Work on your tasks
3. Say: "How's my productivity, Jarvis?"
4. Get your daily report with stats and roasts

**Track:**
- Lines of code shipped
- Work sessions and hours
- Goals completed
- Coffee consumption (get roasted at 5+ cups)
- Late night sessions (midnight - 5 AM)

### Repulsor Mode

1. Say: "Activate Repulsor Mode"
2. Shake your phone (2 quick shakes)
3. Feel the haptic blast!
4. *(Optional)* Add sound effects (see [SOUND_EFFECTS_GUIDE.md](SOUND_EFFECTS_GUIDE.md))

---

## 🏗️ Architecture

### Project Structure

```
app/src/main/java/io/livekit/android/example/voiceassistant/
├── ai/
│   ├── GeminiClient.kt              # Gemini API integration
│   ├── StarkPersonality.kt          # Personality prompts and roasts
│   ├── TTSManager.kt                # Multi-backend TTS
│   ├── VisionManager.kt             # Camera + Gemini Vision
│   ├── ScreenAwarenessManager.kt    # Screen capture
│   ├── DeviceController.kt          # Action execution
│   └── OfflineModeManager.kt        # Offline command handling
├── service/
│   ├── WakeWordService.kt           # Foreground service for wake word
│   └── JarvisAccessibilityService.kt # Device control service
├── jarvis/
│   ├── JarvisViewModel.kt           # Main orchestrator
│   └── JarvisScreen.kt              # Conversation UI
├── ui/
│   ├── SplashActivity.kt            # Arc reactor splash screen
│   └── theme/                       # Material3 theme
├── data/
│   └── SuitModeTracker.kt           # Productivity tracking
├── utils/
│   ├── BatteryOptimizationManager.kt # Power management
│   ├── ErrorHandler.kt              # Error handling and crash recovery
│   ├── PerformanceMonitor.kt        # Performance tracking
│   ├── HapticManager.kt             # Vibration patterns
│   ├── SoundEffectsManager.kt       # Sound effects
│   └── ShakeDetector.kt             # Accelerometer shake detection
└── receiver/
    └── BootReceiver.kt              # Auto-start on boot
```

### Key Design Patterns

- **MVVM** - Separation of UI, business logic, and data
- **Singleton** - Managers use thread-safe singleton pattern
- **Repository Pattern** - Data layer abstraction (DataStore)
- **Observer Pattern** - Kotlin Flow for reactive UI updates
- **Strategy Pattern** - Multi-backend TTS with automatic fallback
- **Factory Pattern** - Error categorization and message generation

### Data Flow

```
User Input (Voice/Text)
    ↓
JarvisViewModel
    ↓
GeminiClient (AI Processing)
    ↓
[Function Calling] → DeviceController → Actions
    ↓
TTSManager (Speech Output)
    ↓
UI Update (Compose StateFlow)
```

---

## 🧪 Testing

Run unit tests:

```bash
./gradlew test
```

Run all tests with coverage:

```bash
./gradlew testDebugUnitTest jacocoTestReport
```

### Test Coverage

- ✅ **StarkPersonality** - Personality prompts and roasts
- ✅ **OfflineModeManager** - Offline command recognition
- ✅ **ProductivityStats** - Suit Mode calculations
- ✅ **ErrorHandler** - Error categorization and messages

*(Add integration tests for more coverage)*

---

## 🎨 Customization

### Change Jarvis Personality

Edit `ai/StarkPersonality.kt`:

```kotlin
const val SYSTEM_PROMPT = """
You are Jarvis, but... [customize here]
"""
```

### Add New Commands

1. Update `GeminiClient.kt` function tools
2. Add handler in `DeviceController.kt`
3. Update offline fallback in `OfflineModeManager.kt`

### Custom Roasts

Add to `StarkPersonality.kt`:

```kotlin
private val coffeeRoasts = listOf(
    "Your new roast here, Sir.",
    // ... existing roasts
)
```

### Change Arc Reactor Color

Edit `ui/theme/Color.kt`:

```kotlin
val ArcReactorBlue = Color(0xFFYOURCOLOR)
```

---

## 🔧 Troubleshooting

### Wake Word Not Working

1. **Check microphone permission** - Ensure RECORD_AUDIO is granted
2. **Foreground service** - Verify notification shows "Listening for wake word"
3. **Try different wake words** - "Jarvis", "Boss", "Sir"
4. **Restart service** - Force close app and relaunch

### Gemini API Errors

1. **Check API key** - Verify in `gradle.properties`
2. **Internet connection** - Ensure device is online
3. **API quota** - Check [ai.google.dev](https://ai.google.dev) quota limits
4. **Model availability** - Gemini 2.0 Flash may have regional restrictions

### TTS Not Speaking

1. **Check backend** - Logs show which TTS backend is active
2. **ElevenLabs API key** - If using premium TTS, verify API key
3. **Fallback to Android TTS** - Remove API keys to use built-in TTS
4. **Volume** - Ensure media volume is turned up

### Vision Mode Crashes

1. **Camera permission** - Grant CAMERA permission
2. **Camera in use** - Close other apps using camera
3. **Memory** - Vision mode is memory-intensive, close other apps

### Device Control Not Working

1. **Accessibility Service** - Enable in Settings → Accessibility
2. **App package names** - Some apps have custom package names
3. **Permissions** - Grant BIND_ACCESSIBILITY_SERVICE

---

## 📊 Performance Optimization

### Battery Life

The app includes **BatteryOptimizationManager** that automatically adjusts based on battery level:

- **100-50%**: Full power mode (all features)
- **50-20%**: Balanced mode (reduced polling)
- **20-10%**: Power saver (minimal features)
- **<10%**: Critical mode (wake word only)

### Memory Management

- **Performance monitoring** built-in (`PerformanceMonitor`)
- View stats: "Jarvis, show performance report"
- Logs slow operations (>1s) automatically

### Reducing Battery Drain

1. **Disable wake word** when not needed
2. **Lower TTS quality** (use Android TTS instead of cloud)
3. **Reduce vision mode usage** (camera is battery-intensive)
4. **Enable battery optimization** in Android settings

---

## 🎯 Roadmap

### Phase 5 - Advanced Features (Future)

- [ ] ARCore holographic HUD (Iron Man helmet style)
- [ ] Calendar integration and scheduling
- [ ] Code review with GitHub integration
- [ ] Stock market and crypto tracking
- [ ] Smart home control (Philips Hue, smart plugs)
- [ ] Contextual awareness (location-based responses)
- [ ] Multi-language support
- [ ] Voice cloning (custom Jarvis voice)
- [ ] Widgets and shortcuts
- [ ] Wear OS companion app

---

## 🤝 Contributing

Contributions welcome! Please:

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

### Code Style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable names
- Add comments for complex logic
- Write unit tests for new features
- Keep Tony Stark personality in all user-facing messages

---

## 📄 License

This project is licensed under the MIT License - see [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **OpenAI / Google** - For making powerful AI accessible
- **LiveKit** - For excellent real-time communication SDK
- **ElevenLabs** - For premium text-to-speech
- **Marvel / Disney** - For inspiring the Jarvis concept (not affiliated)
- **Tony Stark** - For being the genius, billionaire, playboy, philanthropist

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/personal_voice/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/personal_voice/discussions)

---

## 🚨 Disclaimer

This app is **not affiliated with** Marvel, Disney, or Iron Man. It's a fan project inspired by the Jarvis AI assistant from the movies. All trademarks are property of their respective owners.

**API Usage:**
- Gemini API has free tier limits (check [ai.google.dev](https://ai.google.dev))
- ElevenLabs and Cartesia are paid services (free trials available)
- Respect API rate limits and terms of service

**Privacy:**
- Voice data is sent to Gemini API for processing
- No data is stored on external servers (except API providers)
- Review privacy implications before using in production

---

## 💬 Final Words from Jarvis

> "Sir, I've analyzed your README. It's comprehensive, well-structured, and almost as brilliant as Mr. Stark's first arc reactor design. Almost. Now go build something extraordinary, Boss."

---

<div align="center">

**Built with ⚡ by entrepreneurs, for entrepreneurs**

⭐ Star this repo if you want your own Jarvis!

</div>
