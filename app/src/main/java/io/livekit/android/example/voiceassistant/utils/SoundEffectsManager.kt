package io.livekit.android.example.voiceassistant.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import timber.log.Timber

/**
 * SoundEffectsManager - Iron Man sound effects
 * "I love the sound of repulsors in the morning" - Tony Stark
 *
 * Note: Sound files should be placed in res/raw/
 * For MVP, we'll use TTS for audio cues instead of actual sound files
 */
class SoundEffectsManager(private val context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .build()
        )
        .build()

    private var mediaPlayer: MediaPlayer? = null

    // Sound IDs (will be loaded from res/raw/ if files exist)
    private var repulsorBlastSoundId: Int? = null
    private var arcReactorHumSoundId: Int? = null
    private var jarvisBeepSoundId: Int? = null
    private var suitUpSoundId: Int? = null

    init {
        loadSounds()
    }

    /**
     * Load sound effects from resources
     * For now, this is a placeholder - sound files can be added later
     */
    private fun loadSounds() {
        try {
            // TODO: Add actual sound files to res/raw/
            // repulsorBlastSoundId = soundPool.load(context, R.raw.repulsor_blast, 1)
            // arcReactorHumSoundId = soundPool.load(context, R.raw.arc_reactor_hum, 1)
            // jarvisBeepSoundId = soundPool.load(context, R.raw.jarvis_beep, 1)
            // suitUpSoundId = soundPool.load(context, R.raw.suit_up, 1)

            Timber.d("SoundEffectsManager initialized (sound files not yet added)")
        } catch (e: Exception) {
            Timber.e(e, "Failed to load sound effects")
        }
    }

    /**
     * Play repulsor blast sound
     */
    fun playRepulsorBlast() {
        repulsorBlastSoundId?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            Timber.d("Playing repulsor blast sound")
        } ?: run {
            Timber.d("Repulsor blast sound not loaded (add file to res/raw/)")
        }
    }

    /**
     * Play arc reactor hum (looping background sound)
     */
    fun playArcReactorHum(loop: Boolean = true) {
        arcReactorHumSoundId?.let { soundId ->
            soundPool.play(soundId, 0.3f, 0.3f, 1, if (loop) -1 else 0, 1f)
            Timber.d("Playing arc reactor hum")
        }
    }

    /**
     * Stop arc reactor hum
     */
    fun stopArcReactorHum() {
        arcReactorHumSoundId?.let { soundId ->
            soundPool.stop(soundId)
        }
    }

    /**
     * Play Jarvis notification beep
     */
    fun playJarvisBeep() {
        jarvisBeepSoundId?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            Timber.d("Playing Jarvis beep")
        }
    }

    /**
     * Play suit activation sound
     */
    fun playSuitUp() {
        suitUpSoundId?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            Timber.d("Playing suit up sound")
        }
    }

    /**
     * Play custom sound from res/raw
     */
    fun playCustomSound(resourceId: Int, volume: Float = 1f, loop: Boolean = false) {
        try {
            val soundId = soundPool.load(context, resourceId, 1)
            soundPool.play(soundId, volume, volume, 1, if (loop) -1 else 0, 1f)
        } catch (e: Exception) {
            Timber.e(e, "Failed to play custom sound")
        }
    }

    /**
     * Play long sound file (like theme music) using MediaPlayer
     */
    fun playLongSound(resourceId: Int, loop: Boolean = false) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, resourceId).apply {
                isLooping = loop
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                start()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to play long sound")
        }
    }

    /**
     * Stop long sound playback
     */
    fun stopLongSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /**
     * Set volume for all sounds (0.0 to 1.0)
     */
    fun setVolume(volume: Float) {
        // SoundPool volume is set per-play
        // MediaPlayer volume can be set
        mediaPlayer?.setVolume(volume, volume)
    }

    /**
     * Release all resources
     */
    fun release() {
        soundPool.release()
        mediaPlayer?.release()
        mediaPlayer = null
        Timber.d("SoundEffectsManager released")
    }

    companion object {
        @Volatile
        private var instance: SoundEffectsManager? = null

        fun getInstance(context: Context): SoundEffectsManager {
            return instance ?: synchronized(this) {
                instance ?: SoundEffectsManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * Helper class for sound file information
 * Use this to document which sound files to add to res/raw/
 */
object IronManSounds {
    const val REPULSOR_BLAST = "repulsor_blast.mp3"
    const val ARC_REACTOR_HUM = "arc_reactor_hum.mp3"
    const val JARVIS_BEEP = "jarvis_beep.mp3"
    const val SUIT_UP = "suit_up.mp3"
    const val IRON_MAN_THEME = "iron_man_theme.mp3"
    const val JARVIS_YES_SIR = "jarvis_yes_sir.mp3"
    const val FRIDAY_GREETING = "friday_greeting.mp3"

    /**
     * Instructions for adding sound files:
     * 1. Create res/raw/ folder if it doesn't exist
     * 2. Add .mp3 or .ogg files with names matching constants above
     * 3. Uncomment sound loading in SoundEffectsManager.loadSounds()
     * 4. Reference sounds using R.raw.repulsor_blast, etc.
     *
     * Sound sources:
     * - YouTube: Search "Iron Man sound effects" (use royalty-free)
     * - Freesound.org: Search "sci-fi", "laser", "beep"
     * - Create custom: Use Audacity to generate beeps and effects
     */
}
