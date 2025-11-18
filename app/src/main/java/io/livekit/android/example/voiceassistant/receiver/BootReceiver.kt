package io.livekit.android.example.voiceassistant.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.livekit.android.example.voiceassistant.service.WakeWordService
import timber.log.Timber

/**
 * BootReceiver - Auto-start Jarvis on device boot
 * "Jarvis is always ready, Sir."
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Timber.d("Device boot completed. Starting Jarvis wake word service...")

            context?.let {
                // Auto-start wake word service on boot
                // User can disable this in app settings if desired
                WakeWordService.start(it)

                Timber.i("Jarvis wake word service started on boot.")
            }
        }
    }
}
