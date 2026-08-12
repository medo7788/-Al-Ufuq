package com.example.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.AlUfuqDatabase
import com.example.data.models.PrayerSchedule
import com.example.utils.AdhanScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "BootReceiver triggered with action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AlUfuqDatabase.getDatabase(context)
                    val dao = db.alUfuqDao()
                    val entity = dao.getLatestPrayerTimes()
                    val schedule = if (entity != null) {
                        PrayerSchedule.fromEntity(entity)
                    } else {
                        PrayerSchedule.fallback()
                    }
                    val report = AdhanScheduler.scheduleAdhanForDay(context, schedule)
                    Log.i(TAG, "Restored Adhan alarms after $action. Report: $report")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to restore Adhan alarms on boot/timechange: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
