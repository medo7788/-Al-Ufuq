package com.example.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log
import com.example.data.local.AdhanPreferences
import com.example.data.local.AlUfuqDatabase
import com.example.data.models.PrayerSchedule
import com.example.utils.AdhanNotificationHelper
import com.example.utils.AdhanScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdhanReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AdhanReceiver"
        private var activePlayer: MediaPlayer? = null

        fun stopActiveAudio() {
            try {
                activePlayer?.let {
                    if (it.isPlaying) {
                        it.stop()
                    }
                    it.reset()
                    it.release()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping active player: ${e.message}")
            } finally {
                activePlayer = null
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "AdhanReceiver onReceive action=$action")

        if (action == AdhanNotificationHelper.ACTION_STOP_ADHAN) {
            stopActiveAudio()
            return
        }

        if (action == AdhanScheduler.ACTION_TRIGGER_ADHAN) {
            val prayerId = intent.getStringExtra(AdhanScheduler.EXTRA_PRAYER_ID) ?: "fajr"
            val prayerName = intent.getStringExtra(AdhanScheduler.EXTRA_PRAYER_NAME) ?: "الفجر"
            val cityName = intent.getStringExtra(AdhanScheduler.EXTRA_CITY_NAME) ?: "القاهرة"
            val audioUrl = intent.getStringExtra(AdhanScheduler.EXTRA_AUDIO_URL) ?: ""

            val prefs = AdhanPreferences(context)
            if (!prefs.isAdhanEnabledGlobal || !prefs.isPrayerEnabled(prayerId)) {
                Log.d(TAG, "Adhan disabled for $prayerId, skipping notification and audio")
                return
            }

            // 1. Show notification
            if (AdhanScheduler.hasNotificationPermission(context)) {
                AdhanNotificationHelper.showAdhanNotification(
                    context = context,
                    prayerId = prayerId,
                    prayerName = prayerName,
                    cityName = cityName,
                    muezzinName = prefs.selectedMuezzinName
                )
            } else {
                Log.w(TAG, "Notification permission missing when Adhan triggered")
            }

            // 2. Play Adhan audio
            playAdhanAudio(context, audioUrl)

            // 3. Reschedule alarms for upcoming prayers using local Room database schedule
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AlUfuqDatabase.getDatabase(context)
                    val dao = db.alUfuqDao()
                    val entity = dao.getLatestPrayerTimes()
                    val schedule = if (entity != null) {
                        PrayerSchedule.fromEntity(entity)
                    } else {
                        PrayerSchedule.fallback(cityName = cityName)
                    }
                    AdhanScheduler.scheduleAdhanForDay(context, schedule)
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling adhan after trigger: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private fun playAdhanAudio(context: Context, audioUrl: String) {
        stopActiveAudio()
        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
            }

            activePlayer = player

            if (audioUrl.isNotBlank() && (audioUrl.startsWith("http://") || audioUrl.startsWith("https://"))) {
                player.setDataSource(audioUrl)
                player.setOnPreparedListener { mp ->
                    try {
                        mp.start()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to start online player: ${e.message}")
                        playFallbackRingtone(context)
                    }
                }
                player.setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "Online player error: what=$what, extra=$extra. Playing fallback ringtone.")
                    playFallbackRingtone(context)
                    true
                }
                player.prepareAsync()
            } else {
                playFallbackRingtone(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during adhan playback initialization: ${e.message}")
            playFallbackRingtone(context)
        }
    }

    private fun playFallbackRingtone(context: Context) {
        stopActiveAudio()
        try {
            val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(context, defaultUri)
                setOnPreparedListener { mp -> mp.start() }
                prepareAsync()
            }
            activePlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play fallback ringtone: ${e.message}")
        }
    }
}
