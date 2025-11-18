# StarkJarvis Sound Effects Guide

## "I love the sound of repulsors in the morning" - Tony Stark

This guide helps you add authentic Iron Man / Stark Industries sound effects to enhance the Jarvis experience.

---

## Required Sound Files

All sound files should be placed in: `app/src/main/res/raw/`

### Core Sounds (Priority 1)

| Filename | Description | When It Plays | Duration |
|----------|-------------|---------------|----------|
| `jarvis_beep.mp3` | Notification beep (like computer alert) | Wake word detected, message received | 0.5-1s |
| `repulsor_blast.mp3` | Repulsor blast sound (energy weapon) | Shake detection (Repulsor Mode) | 1-2s |
| `suit_up.mp3` | Mechanical suit assembly sound | Suit Mode activated | 2-3s |
| `arc_reactor_hum.mp3` | Low ambient hum (looping) | Background while processing | Loop |

### Enhanced Sounds (Priority 2)

| Filename | Description | When It Plays | Duration |
|----------|-------------|---------------|----------|
| `jarvis_yes_sir.mp3` | "Yes, Sir" audio clip | Command acknowledged | 1-2s |
| `friday_greeting.mp3` | "Welcome back, Boss" greeting | App opened after crash recovery | 2s |
| `iron_man_theme.mp3` | Iron Man theme music (looping) | Boot sequence, special achievements | Loop |
| `error_sound.mp3` | Error alert (mechanical failure sound) | Critical errors | 1s |
| `success_chime.mp3` | Success confirmation (energy surge) | Goals completed, tasks done | 1-2s |

### Ambient Sounds (Optional)

| Filename | Description | When It Plays | Duration |
|----------|-------------|---------------|----------|
| `workshop_ambient.mp3` | Tony's workshop ambient noise | Background music option | Loop |
| `startup_sequence.mp3` | Bootup sequence sound | App launch, splash screen | 3-5s |
| `shutdown_sequence.mp3` | System shutdown | App closing | 2s |

---

## How to Source Sound Effects

### Option 1: Royalty-Free Sound Libraries (Recommended)

**Free Sources:**
1. **Freesound.org** (freesound.org)
   - Search: "sci-fi beep", "laser blast", "energy hum", "robot voice"
   - Filter: Creative Commons licenses
   - Download as MP3 or OGG

2. **Zapsplat** (zapsplat.com)
   - Free with attribution
   - Search: "sci-fi UI", "tech sound", "robot", "mechanical"

3. **YouTube Audio Library** (studio.youtube.com/channel/UC.../music)
   - Free, no attribution required
   - Filter by genre: Cinematic, Sci-Fi

4. **BBC Sound Effects** (sound-effects.bbcrewind.co.uk)
   - Free for personal use
   - Search: "computer", "beep", "mechanical"

**Paid Sources:**
- **Epidemic Sound** ($15/month) - High quality, commercial use
- **Artlist** ($16/month) - Premium sound effects
- **Soundsnap** (soundsnap.com) - Professional SFX library

### Option 2: Create Your Own (Free)

**Tools:**
1. **Audacity** (Free, audacityteam.org)
   - Generate tones and beeps
   - Apply effects (reverb, echo, distortion)
   - Export as MP3

2. **LMMS** (Free, lmms.io)
   - Digital audio workstation
   - Create electronic/synthetic sounds

**Quick Tutorial - Create Jarvis Beep:**
```
1. Open Audacity
2. Generate → Tone
   - Frequency: 800 Hz
   - Amplitude: 0.8
   - Duration: 0.5 seconds
3. Effect → Echo (Delay: 0.1s, Decay: 0.5)
4. Effect → Reverb (small room preset)
5. File → Export → Export as MP3
6. Name: jarvis_beep.mp3
```

### Option 3: AI Sound Generation

**Tools:**
- **ElevenLabs Sound Effects** (elevenlabs.io) - AI-generated sounds (Beta)
- **Soundraw** (soundraw.io) - AI music generation
- **Riffusion** - AI sound synthesis

**Prompt examples:**
- "Electronic beep notification sound, futuristic, clean"
- "Energy weapon blast, sci-fi, powerful"
- "Mechanical suit assembly, metallic clinks and whirs"

### Option 4: Extract from Movies (Personal Use Only)

**Warning:** Only for personal/educational use, NOT for distribution or commercial apps.

**Tools:**
- **4K Video Downloader** - Download YouTube clips
- **Audacity** - Extract audio from video files
- **MP3 Cutter** - Isolate specific sound moments

**YouTube Searches:**
- "Iron Man Jarvis sound effects"
- "Arc reactor sound"
- "Repulsor blast sound effect"
- "Iron Man suit up sound"

---

## File Format Requirements

### Recommended Format
- **Format:** MP3 (best compatibility)
- **Bitrate:** 128 kbps (good quality, small file size)
- **Sample Rate:** 44.1 kHz
- **Channels:** Mono (for sound effects), Stereo (for music)

### Alternative Format
- **Format:** OGG Vorbis (better compression)
- **Quality:** Q5 (~160 kbps equivalent)

### Conversion
If you have WAV or other formats, convert using:
1. **Audacity:** File → Export → Export as MP3
2. **FFmpeg:**
   ```bash
   ffmpeg -i input.wav -codec:a libmp3lame -b:a 128k output.mp3
   ```
3. **Online:** cloudconvert.com

---

## Implementation

### Step 1: Add Files to Project

1. Create `res/raw/` folder if it doesn't exist:
   ```bash
   mkdir -p app/src/main/res/raw
   ```

2. Copy your sound files:
   ```bash
   cp jarvis_beep.mp3 app/src/main/res/raw/
   cp repulsor_blast.mp3 app/src/main/res/raw/
   # ... etc
   ```

3. File names MUST be:
   - All lowercase
   - No spaces (use underscores)
   - No special characters
   - Alphanumeric and underscore only

### Step 2: Enable Sounds in Code

Open: `app/src/main/java/io/livekit/android/example/voiceassistant/utils/SoundEffectsManager.kt`

Uncomment these lines in `loadSounds()`:

```kotlin
private fun loadSounds() {
    try {
        // UNCOMMENT THESE AFTER ADDING SOUND FILES:
        repulsorBlastSoundId = soundPool.load(context, R.raw.repulsor_blast, 1)
        arcReactorHumSoundId = soundPool.load(context, R.raw.arc_reactor_hum, 1)
        jarvisBeepSoundId = soundPool.load(context, R.raw.jarvis_beep, 1)
        suitUpSoundId = soundPool.load(context, R.raw.suit_up, 1)

        Timber.d("SoundEffectsManager loaded all Iron Man sound effects")
    } catch (e: Exception) {
        Timber.e(e, "Failed to load sound effects")
    }
}
```

### Step 3: Test Sounds

Trigger sounds from JarvisViewModel:

```kotlin
// Play Jarvis beep
soundEffectsManager.playJarvisBeep()

// Play repulsor blast
soundEffectsManager.playRepulsorBlast()

// Play suit activation
soundEffectsManager.playSuitUp()

// Play arc reactor hum (looping)
soundEffectsManager.playArcReactorHum(loop = true)
```

---

## Sound Design Tips

### Jarvis Personality
- **Clean and precise** - No distortion or noise
- **Futuristic** - Electronic, not mechanical
- **Professional** - Stark Industries quality
- **Subtle** - Not overpowering or annoying

### Volume Levels
- **Notifications:** 70-80% volume
- **Confirmation beeps:** 50-60% volume
- **Ambient hum:** 20-30% volume (very subtle)
- **Dramatic effects (repulsor):** 90-100% volume

### Timing
- **Quick beeps:** 0.3-0.5 seconds
- **Acknowledgments:** 1-2 seconds
- **Dramatic sounds:** 2-3 seconds
- **Ambient loops:** 10-30 seconds (seamless loop)

---

## Quick Start - Minimal Setup (5 minutes)

If you just want to get started quickly:

1. **Download 3 essential sounds from Freesound.org:**
   - Search "notification beep" → Save as `jarvis_beep.mp3`
   - Search "energy blast" → Save as `repulsor_blast.mp3`
   - Search "mechanical startup" → Save as `suit_up.mp3`

2. **Add to project:**
   ```bash
   mkdir -p app/src/main/res/raw
   cp *.mp3 app/src/main/res/raw/
   ```

3. **Uncomment code in SoundEffectsManager.kt** (see Step 2 above)

4. **Build and test!**

---

## Advanced: TTS for Jarvis Voice

Instead of pre-recorded sounds, you can use **ElevenLabs** to generate Jarvis voice responses:

**Already implemented in TTSManager.kt:**
- British accent (Jarvis voice)
- Real-time synthesis
- Personality injection via text

**Example voice responses:**
- "Yes, Sir. How may I assist?"
- "Repulsor blast ready, Boss."
- "Suit Mode activated. Let's build an empire."

**Upgrade to voice clips:**
1. Generate voice with ElevenLabs API
2. Save common responses as MP3
3. Play cached audio instead of API call
4. Fallback to API for dynamic responses

---

## Stark-Approved Sound Checklist

- [ ] All core sounds added (jarvis_beep, repulsor_blast, suit_up)
- [ ] Files in correct format (MP3, 128kbps)
- [ ] Files in `res/raw/` folder
- [ ] Code uncommented in SoundEffectsManager.kt
- [ ] Tested on physical device (sounds play correctly)
- [ ] Volume levels balanced (not too loud/quiet)
- [ ] Sounds match Jarvis personality (clean, futuristic)

---

**"Friday, remind me to add those sound effects. Actually, just add them yourself."** - Tony Stark

**"I'm afraid I cannot do that, Boss. You'll need to follow the guide above."** - F.R.I.D.A.Y.
