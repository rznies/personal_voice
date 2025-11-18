# 🔒 StarkJarvis Security & Privacy

## Overview

StarkJarvis now features **IRON-CLAD** security and privacy controls. All dangerous features are **OFF by default**, and you have complete control over every permission and capability.

## 🛡️ Security Features

### 1. **Encrypted Storage**
- All API keys encrypted with **AES-256-GCM** using Android Jetpack Security
- Sensitive data stored in `EncryptedSharedPreferences`
- Keys never shipped in APK - users must enter manually
- Zero plaintext secrets in the codebase

### 2. **Privacy Policy on First Launch**
- Users must accept privacy policy before using the app
- Clear explanation of all permissions and capabilities
- Opt-in model for all dangerous features

### 3. **Granular Permission Controls**
All controllable via Settings screen:

#### Wake Word Listening
- **Default**: OFF
- Manually enable "Always Listening" for voice wake words
- Service disabled in manifest by default

#### Screen Capture
- **Default**: Ask every time
- Never captures screen without explicit user consent
- Integrates with local-only mode

#### Camera Access
- **Default**: Ask every time
- Requires runtime permission + user confirmation
- Blocked in local-only mode

#### Accessibility Levels
- **OFF** (default): No accessibility access
- **READ**: Read screen content only
- **BASIC**: Read + basic interactions (tap, swipe)
- **FULL**: Complete device control ⚠️ DANGEROUS

#### Boot Start
- **Default**: OFF
- BootReceiver disabled in manifest
- Dynamically enabled only when user permits

### 4. **Biometric Authentication**
- Fingerprint/Face ID required for dangerous actions:
  - Send message
  - Make phone call
  - Send email
  - Delete data
  - Open apps with elevated permissions
  - Change security settings

### 5. **Local-Only Mode**
- **Default**: ON
- Process everything on-device
- No cloud APIs except when explicitly enabled
- Blocks screen capture and camera in local mode

### 6. **Audit Trail**
- All security events logged
- Timestamped audit log accessible in Settings
- User can review all dangerous actions

## 🔐 API Keys

### Required Keys
- **Google Gemini API**: Required for AI functionality

### Optional Keys
- **ElevenLabs**: Premium TTS (optional)
- **Cartesia**: Ultra-low latency TTS (optional)

### How to Add Keys
1. Open StarkJarvis
2. Navigate to **Settings** → **API Keys**
3. Select key type (Gemini, ElevenLabs, or Cartesia)
4. Enter API key
5. Keys are encrypted and stored securely

### Key Validation
- Format validation for each key type
- Status indicators: ✅ Valid, ❌ Missing, ⚠️ Invalid
- Masked preview (shows first/last 4 characters only)

## 🏗️ Architecture

### Security Components

```
┌──────────────────────────────────────────┐
│         SecurityPreferences              │
│  (Encrypted settings storage)            │
│  - Wake word enabled                     │
│  - Accessibility level                   │
│  - Biometric auth required               │
│  - Local-only mode                       │
└──────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────┐
│         SecureKeyManager                 │
│  (Encrypted API key storage)             │
│  - Gemini API key                        │
│  - ElevenLabs API key                    │
│  - Cartesia API key                      │
└──────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────┐
│         BiometricAuthManager             │
│  (Fingerprint/Face authentication)       │
│  - Validates user identity               │
│  - Guards dangerous actions              │
└──────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────┐
│         PermissionGuard                  │
│  (Runtime permission validation)         │
│  - Screen capture checks                 │
│  - Camera access checks                  │
│  - Accessibility level enforcement       │
└──────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────┐
│         ComponentEnabler                 │
│  (Dynamic service control)               │
│  - Enable/disable WakeWordService        │
│  - Enable/disable BootReceiver           │
└──────────────────────────────────────────┘
```

## 📱 User Experience

### First Launch Flow
1. **Privacy Policy Screen** → User reads and accepts
2. **API Key Entry** → User enters Gemini API key (required)
3. **Settings Configuration** → User customizes security preferences
4. **Ready to Use** → All dangerous features OFF by default

### Settings Screen
Beautiful Iron Man HUD-style interface with:
- Real-time security status indicators
- Color-coded danger levels (Blue = safe, Gold = caution, Red = danger)
- Animated Arc Reactor glow effects
- Clear descriptions for every setting
- Audit trail viewer

## 🚨 Dangerous Actions

The following actions require biometric authentication (if enabled):

1. **SEND_MESSAGE**: Sending SMS or messages
2. **MAKE_CALL**: Making phone calls
3. **SEND_EMAIL**: Sending emails
4. **OPEN_APP**: Opening apps with elevated permissions
5. **DELETE_DATA**: Deleting user data
6. **CHANGE_SETTINGS**: Modifying system settings
7. **ACCESSIBILITY_CONTROL**: Using accessibility service for device control
8. **SCREEN_CAPTURE**: Capturing screen content
9. **CAMERA_ACCESS**: Accessing camera
10. **READ_CONTACTS**: Reading contacts
11. **MODIFY_SECURITY**: Changing security configuration

## 🔧 Developer Notes

### Manifest Changes
- `WakeWordService`: `android:enabled="false"` by default
- `BootReceiver`: `android:enabled="false"` by default
- Services only enabled when user explicitly grants permission

### Build Configuration
- No API keys in `build.gradle.kts`
- All secrets managed at runtime
- ProGuard/R8 rules for security components

### Testing Security
```kotlin
// Check if security settings are properly initialized
val securityPrefs = SecurityPreferences.getInstance(context)
val settings = securityPrefs.securitySettings.value

// Verify default values
assert(settings.wakeWordEnabled == false)
assert(settings.bootStartEnabled == false)
assert(settings.accessibilityLevel == AccessibilityLevel.OFF)
assert(settings.localOnlyMode == true)
assert(settings.requireBiometricAuth == true)
```

## 📊 Security Checklist

- [x] Encrypted API key storage
- [x] No hardcoded secrets in APK
- [x] Privacy policy on first launch
- [x] All dangerous features OFF by default
- [x] Biometric authentication for high-risk actions
- [x] Granular accessibility controls (OFF/READ/BASIC/FULL)
- [x] Local-only mode enabled by default
- [x] Screen capture requires permission
- [x] Camera access requires permission
- [x] Boot start disabled by default
- [x] Wake word disabled by default
- [x] Audit logging for all security events
- [x] Component-level service control
- [x] Runtime permission validation
- [x] Beautiful HUD-style Settings UI

## 🎯 Comparison: Before vs After

| Feature | Before (DANGEROUS) | After (SECURE) |
|---------|-------------------|----------------|
| API Keys | Hardcoded in APK ❌ | Encrypted, user-entered ✅ |
| Wake Word | Always on ❌ | OFF by default ✅ |
| Boot Start | Auto-start ❌ | OFF by default ✅ |
| Accessibility | Full access ❌ | OFF by default ✅ |
| Screen Capture | Unrestricted ❌ | Ask every time ✅ |
| Camera | Unrestricted ❌ | Ask every time ✅ |
| Dangerous Actions | No confirmation ❌ | Biometric required ✅ |
| First Launch | No warning ❌ | Privacy policy ✅ |
| Audit Log | None ❌ | Full logging ✅ |

## 🏆 Result

StarkJarvis is now the **safest AI assistant on Android**. Every feature is gated, every action is logged, and the user is in complete control.

**"With great power comes great responsibility" - but now you're protected from your own AI.**

---

**Made with ❤️ and 🔒 by the StarkJarvis Security Team**
