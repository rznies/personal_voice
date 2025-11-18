# StarkJarvis - Your Personal AI Empire Builder 🦾⚡

> *"Sometimes you gotta run before you can walk."* - Tony Stark

**Welcome, Sir.** This is StarkJarvis - your personal AI assistant with the exact humor, ego, and brilliance of Tony Stark's J.A.R.V.I.S. Built to help you ship products faster, crush entrepreneurial goals, and live that Iron Man life.

---

## 🎯 What is StarkJarvis?

StarkJarvis is a comprehensive Android AI assistant app that combines:
- **Real-time voice conversation** powered by Google Gemini 2.5 Flash
- **Tony Stark personality** - sarcastic, witty, loyal, genius-level roasts
- **Always-listening wake word** detection ("Jarvis", "Boss", "Sir")
- **Vision Mode** - camera-based scene analysis
- **Screen Awareness** - reads and analyzes your screen content
- **Full device control** via Accessibility Service
- **AR Holographic HUD** (Iron Man helmet style)
- **Entrepreneurship tools** - code review, idea validation, market research
- **RDJ-style voice** via ElevenLabs or Cartesia TTS

All with the aesthetic of an arc reactor-powered, OLED-optimized, pure genius interface.

---

## 🚀 Features Implemented

### Core Features (v1.0 - MVP Ready)

#### ✅ **1. Tony Stark Personality System**
- Custom Gemini system prompt that makes Jarvis behave like movie J.A.R.V.I.S.
- Sarcastic, witty responses in every interaction
- Always addresses you as "Sir", "Boss", or "the genius billionaire playboy philanthropist"
- Contextual roasts about your code, coffee addiction, or late nights
- Located in: `ai/StarkPersonality.kt`

#### ✅ **2. Gemini AI Integration**
- Google Gemini 2.5 Flash for text/reasoning
- Function calling support for device actions
- Vision mode for image analysis
- Streaming responses for real-time conversation
- Chat history management with context preservation
- Located in: `ai/GeminiClient.kt`

#### ✅ **3. Wake Word Detection Service**
- Always-listening foreground service
- Detects: "Jarvis", "Hey Jarvis", "Boss", "Sir", "Stark"
- Battery-optimized with intelligent restart logic
- On-device recognition for privacy
- Broadcasts wake word events to trigger actions
- Located in: `service/WakeWordService.kt`

#### ✅ **4. TTS Manager (Jarvis Voice)**
- **ElevenLabs** - Premium RDJ-style voice (British accent)
- **Cartesia Sonic** - Ultra-low latency alternative
- **Android TTS** - Free fallback with British accent config
- Automatic backend selection based on API keys
- Located in: `ai/TTSManager.kt`

#### ✅ **5. Iron Man Themed UI**
- Arc Reactor Blue primary color (`#00D4FF`)
- Stark Gold accents (`#FFB300`)
- Pure black backgrounds for OLED perfection
- Custom color palette in `ui/theme/Color.kt`
- StarkJarvisTheme with dark mode optimizations
- Located in: `ui/theme/`

#### ✅ **6. Comprehensive Permissions & Services**
- All Android permissions declared in `AndroidManifest.xml`
- Accessibility Service configuration
- Foreground service setup
- ARCore metadata
- Boot receiver for auto-start

---

### Advanced Features (In Progress / Roadmap)

#### 🔄 **Vision Mode**
- Camera2 API integration
- Real-time scene analysis with Gemini Vision
- Object detection and text recognition
- Sarcastic commentary on what Jarvis sees
- "Jarvis, what do you see?" trigger

#### 🔄 **Screen Awareness**
- MediaProjection API for screen capture
- Gemini-powered content analysis
- Code review from screenshots
- Email/document reading with commentary
- "Jarvis, look at my screen" trigger

#### 🔄 **Accessibility Service (Full Device Control)**
- Open apps (VS Code, Figma, Slack, etc.)
- Send WhatsApp/SMS/emails
- Control media playback (Spotify, YouTube Music)
- Set reminders and alarms
- Toggle WiFi/Bluetooth/DND
- External API integrations (Swiggy, Ola, GPay, Yahoo Finance)

#### 🔄 **ARCore Holographic HUD**
- Iron Man helmet-style AR interface
- Floating 3D UI elements:
  - Arc reactor core animation
  - Product deadline calendar
  - Stock ticker for your portfolio
  - Goal progress bars
- Jarvis face as glowing holographic orb
- Particle effects and neon glows

#### 🔄 **Suit Mode**
- Track coding sessions and lines shipped
- Monitor work hours (roasts if it's 3 AM)
- Goal completion percentage
- Habit tracking for multi-millionaire lifestyle
- Real-time productivity roasts

#### 🔄 **Repulsor Mode**
- Shake phone twice to activate
- Repulsor blast sound + haptic feedback
- "Target acquired. Multi-millionaire mode engaged, Sir."
- Motivational Tony Stark quotes

#### 🔄 **Advanced Entrepreneurship Tools**
- **Code Review**: Paste code → get roasted + optimizations
- **Idea Validation**: Market analysis, TAM, competitors
- **Goal Crusher**: Daily millionaire habit tracker
- **Multi-Millionaire Mode**: Affirmations with sarcasm

---

## 🛠️ Tech Stack (2025 Latest, All Free Tier)

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **LLM Brain** | Google Gemini 2.5 Flash | Text, reasoning, function calling |
| **Voice** | Gemini Live API (planned) | Real-time duplex audio conversation |
| **TTS** | ElevenLabs / Cartesia | RDJ-style Jarvis voice synthesis |
| **Wake Word** | Android SpeechRecognizer | Always-listening trigger |
| **Vision** | Gemini 2.5 Flash Image | Image/scene analysis |
| **Real-time Comms** | LiveKit Android SDK | Voice/video streaming |
| **AR** | ARCore | Holographic Iron Man HUD |
| **UI** | Jetpack Compose + Material3 | Modern Android UI |
| **Architecture** | MVVM + Coroutines | Clean, reactive code |
| **Database** | Room | Local data persistence |

---

## 📦 Installation & Setup

### Prerequisites

1. **Android Studio** (latest version)
2. **Android device/emulator** (API 24+, recommended API 31+)
3. **API Keys** (instructions below)

### Step 1: Clone & Open Project

```bash
git clone <your-repo-url>
cd personal_voice
# Open in Android Studio
```

### Step 2: Configure API Keys

Create a `.env` file in the project root (or use `local.properties`):

```env
# Google Gemini AI (REQUIRED)
GEMINI_API_KEY=your_gemini_api_key_here

# ElevenLabs TTS (Optional - fallback to Android TTS if not provided)
ELEVENLABS_API_KEY=your_elevenlabs_api_key_here

# Cartesia TTS (Optional alternative to ElevenLabs)
CARTESIA_API_KEY=your_cartesia_api_key_here

# LiveKit (if using existing voice assistant features)
LIVEKIT_SANDBOX_ID=your_livekit_sandbox_id
```

#### Where to Get API Keys:

1. **Gemini API Key** (FREE - 1,500 requests/day)
   - Visit: https://aistudio.google.com/app/apikey
   - Click "Create API Key"
   - Copy and paste into `.env`

2. **ElevenLabs API Key** (FREE tier: 10,000 chars/month)
   - Visit: https://elevenlabs.io/
   - Sign up and go to Profile → API Keys
   - Copy and paste into `.env`
   - **Optional**: Clone RDJ voice or use British professional voices

3. **Cartesia API Key** (FREE tier available)
   - Visit: https://cartesia.ai/
   - Sign up and generate API key
   - Copy and paste into `.env`

### Step 3: Add API Keys to Gradle

Since Android doesn't natively support `.env`, add keys to `gradle.properties`:

```properties
# Add these lines to gradle.properties (gitignored)
GEMINI_API_KEY=your_actual_key
ELEVENLABS_API_KEY=your_actual_key
CARTESIA_API_KEY=your_actual_key
```

Or use `local.properties` (recommended for local development):

```properties
# local.properties (auto-gitignored)
GEMINI_API_KEY=your_actual_key
ELEVENLABS_API_KEY=your_actual_key
CARTESIA_API_KEY=your_actual_key
```

Then update `app/build.gradle.kts` to read from `local.properties` (already configured).

### Step 4: Sync & Build

```bash
# In Android Studio:
# File → Sync Project with Gradle Files
# Build → Make Project
```

### Step 5: Run on Device

1. Connect Android device or start emulator
2. Click "Run" (▶️) in Android Studio
3. Grant required permissions:
   - Microphone (for wake word + voice)
   - Camera (for Vision Mode - optional)
   - Accessibility (for device control - optional)
   - Notifications (for foreground service)

---

## 🎮 Usage Guide

### Basic Interaction

1. **Launch App**: Tap StarkJarvis icon
2. **Activate Wake Word Service**: Grant mic permission, tap "Enable Wake Word"
3. **Say Wake Word**: "Jarvis", "Hey Jarvis", "Boss", or "Sir"
4. **Get Roasted**: Jarvis responds with sarcasm and wit
5. **Ask Anything**:
   - "Jarvis, validate this SaaS idea"
   - "Jarvis, review my React code"
   - "Boss, what's my progress today?"

### Example Conversations

```
You: "Jarvis, validate this idea: An AI that writes code for you"

Jarvis: "On it, Sir. Though your last idea was more 'genius' than 'billionaire.'
Let me analyze the market... The TAM is $50B+ (GitHub Copilot, Tabnine, Cursor).
Competition is fierce. Your edge? None yet. Verdict: Pivot or add unique value.
Shall I help you brainstorm differentiation, or would you prefer to code another
todo app?"
```

```
You: "Jarvis, what do you see?" (in Vision Mode with camera on)

Jarvis: "That's a messy desk, Sir. I see a laptop, 3 coffee cups (classic),
scattered papers, and what appears to be... a half-eaten protein bar from 2023.
Want me to AR-organize it, or shall we pretend chaos fuels genius?"
```

```
You: "Jarvis, remind me to ship at 2 AM"

Jarvis: "Reminder set for 2 AM, Sir. Your sleep schedule concerns me, Boss.
But who am I to stop a genius billionaire playboy philanthropist from
conquering the world? Try not to pass out on your keyboard."
```

---

## 🏗️ Project Structure

```
app/src/main/java/io/livekit/android/example/voiceassistant/
│
├── ai/                          # AI & Intelligence
│   ├── GeminiClient.kt          # Gemini 2.5 Flash integration
│   ├── StarkPersonality.kt      # Tony Stark personality prompts
│   └── TTSManager.kt            # Text-to-Speech (ElevenLabs/Cartesia/Android)
│
├── service/                     # Background Services
│   ├── WakeWordService.kt       # Always-listening wake word detection
│   ├── JarvisAccessibilityService.kt # Device control (TODO)
│   └── VisionService.kt         # Camera-based vision (TODO)
│
├── receiver/                    # Broadcast Receivers
│   └── BootReceiver.kt          # Auto-start on boot (TODO)
│
├── ui/                          # UI Components
│   ├── theme/                   # Iron Man theme
│   │   ├── Color.kt             # Arc reactor colors
│   │   ├── Theme.kt             # StarkJarvisTheme
│   │   └── Type.kt              # Typography
│   ├── screen/                  # Compose screens
│   └── components/              # Reusable components
│
├── data/                        # Data Layer
│   ├── local/                   # Room database (TODO)
│   └── remote/                  # API clients (TODO)
│
├── utils/                       # Utilities
│   └── PermissionHelper.kt      # Permission management (TODO)
│
└── MainActivity.kt              # Main entry point
```

---

## 🎨 Design Philosophy

**"Iron Man Minimalism with Maximum Impact"**

- **Colors**: Arc reactor blue + Stark gold on pure black
- **Typography**: Clean, sci-fi inspired, readable
- **Animations**: Smooth, spring-based, reactor-like
- **Sounds**: Repulsor blasts, arc reactor hum
- **Haptics**: Tactile feedback for every interaction
- **Philosophy**: Less is more, but make it glow

---

## 🔧 Development Roadmap

### ✅ Phase 1: Core Brain (COMPLETED)
- [x] Gemini AI integration with Tony Stark personality
- [x] Wake word detection service
- [x] TTS Manager (ElevenLabs/Cartesia/Android)
- [x] Iron Man themed UI colors
- [x] Project dependencies and configuration

### 🔄 Phase 2: Voice & Vision (IN PROGRESS)
- [ ] Gemini Live API integration for duplex audio
- [ ] Vision Mode with Camera2 API
- [ ] Screen Awareness with MediaProjection
- [ ] Real-time conversation UI

### 📋 Phase 3: Device Domination (PLANNED)
- [ ] Accessibility Service for full device control
- [ ] App launching and task automation
- [ ] Message sending (WhatsApp/SMS)
- [ ] Media control and settings toggles

### 📋 Phase 4: AR & Advanced Features (PLANNED)
- [ ] ARCore holographic HUD
- [ ] Suit Mode productivity tracking
- [ ] Repulsor Mode with shake detection
- [ ] Advanced entrepreneurship tools

### 📋 Phase 5: Polish & Production (PLANNED)
- [ ] Battery optimization
- [ ] Offline fallback modes
- [ ] Unit tests for core features
- [ ] Sound effects and haptic feedback
- [ ] Custom arc reactor app icon
- [ ] Splash screen with arc reactor animation

---

## 🤝 Contributing

This is YOUR personal Jarvis, Sir. But if you want to make it even better:

1. Fork the repo
2. Create a feature branch: `git checkout -b feature/genius-idea`
3. Commit with style: `git commit -m "Add arc reactor core animation"`
4. Push to your fork: `git push origin feature/genius-idea`
5. Open a PR with an epic description

---

## 📝 License

This project is licensed under MIT License - use it to build your empire.

**However**, remember: With great power comes great responsibility. Don't use Jarvis for evil. Tony wouldn't approve.

---

## 🎯 Troubleshooting

### "Gemini API key not found"
- Check `.env` or `local.properties` has `GEMINI_API_KEY` set
- Rebuild project: `Build → Clean Project` then `Build → Rebuild`

### "Wake word not detecting"
- Grant microphone permission in Settings → Apps → StarkJarvis → Permissions
- Check if SpeechRecognizer is supported on your device
- Try "Hey Jarvis" instead of just "Jarvis"

### "TTS not working"
- If ElevenLabs fails, app auto-falls back to Android TTS
- Check internet connection for cloud TTS
- Verify API keys are correct

### "No voice response"
- Ensure speakers/headphones are connected
- Check device volume is not muted
- Review Logcat for TTS errors

---

## 💬 Jarvis Says...

> *"Sir, you've built something remarkable. Now go use it to build your empire. And maybe get some sleep? Just a suggestion from your favorite AI."*
>
> *- J.A.R.V.I.S., Stark Industries Personal AI Assistant*

---

## 🔗 Resources

- [Google Gemini AI](https://ai.google.dev/)
- [ElevenLabs Voice Cloning](https://elevenlabs.io/)
- [Cartesia Sonic TTS](https://cartesia.ai/)
- [LiveKit Docs](https://docs.livekit.io/)
- [ARCore Developer Guide](https://developers.google.com/ar)

---

**Built with ⚡ by a genius billionaire playboy philanthropist wannabe**

*"I am Iron Man." - You, after shipping this.*
