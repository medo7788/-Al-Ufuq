package com.example.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.local.AdhanPreferences
import com.example.data.models.PrayerSchedule
import com.example.receivers.AdhanReceiver
import java.util.Calendar

enum class AdhanScheduleStatus {
    SCHEDULED,
    CANCELLED,
    SKIPPED,
    FAILED,
    PERMISSION_MISSING
}

data class AdhanLogEntry(
    val prayerId: String,
    val prayerName: String,
    val status: AdhanScheduleStatus,
    val targetTimeMillis: Long = 0L,
    val message: String = ""
)

data class AdhanScheduleReport(
    val isGlobalEnabled: Boolean,
    val hasNotificationPermission: Boolean,
    val hasExactAlarmPermission: Boolean,
    val logs: List<AdhanLogEntry>
)

object AdhanScheduler {

    private const val TAG = "AdhanScheduler"
    const val ACTION_TRIGGER_ADHAN = "com.example.ALARM_TRIGGER_ADHAN"
    const val EXTRA_PRAYER_ID = "extra_prayer_id"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_CITY_NAME = "extra_city_name"
    const val EXTRA_AUDIO_URL = "extra_audio_url"

    fun getPrayerRequestCode(prayerId: String): Int {
        return when (prayerId.lowercase()) {
            "fajr" -> 0
            "sunrise" -> 1
            "dhuhr" -> 2
            "asr" -> 3
            "maghrib" -> 4
            "isha" -> 5
            else -> 99
        }
    }

    /**
     * Pure function to determine alarm plan for unit testing.
     */
    fun planAlarms(
        schedule: PrayerSchedule,
        isGlobalEnabled: Boolean,
        isPrayerEnabled: (String) -> Boolean,
        hasNotificationPermission: Boolean,
        hasExactAlarmPermission: Boolean,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): List<AdhanLogEntry> {
        val logs = mutableListOf<AdhanLogEntry>()

        if (!hasNotificationPermission) {
            return listOf(
                AdhanLogEntry(
                    prayerId = "all",
                    prayerName = "الكل",
                    status = AdhanScheduleStatus.PERMISSION_MISSING,
                    message = "إذن الإشعارات غير ممنوح (POST_NOTIFICATIONS)"
                )
            )
        }

        if (!isGlobalEnabled) {
            val prayers = listOf("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha")
            return prayers.map { id ->
                AdhanLogEntry(
                    prayerId = id,
                    prayerName = getArabicPrayerName(id),
                    status = AdhanScheduleStatus.CANCELLED,
                    message = "الأذان معطل عامة"
                )
            }
        }

        val timesList = listOf(
            "fajr" to schedule.fajr,
            "sunrise" to schedule.sunrise,
            "dhuhr" to schedule.dhuhr,
            "asr" to schedule.asr,
            "maghrib" to schedule.maghrib,
            "isha" to schedule.isha
        )

        for ((id, timeStr) in timesList) {
            val nameArabic = getArabicPrayerName(id)
            if (!isPrayerEnabled(id)) {
                logs.add(
                    AdhanLogEntry(
                        prayerId = id,
                        prayerName = nameArabic,
                        status = AdhanScheduleStatus.SKIPPED,
                        message = "تم إيقاف تفعيل الصلاة في الإعدادات"
                    )
                )
                continue
            }

            val targetMillis = getTargetTimeMillis(timeStr, currentTimeMillis)
            if (targetMillis <= 0L) {
                logs.add(
                    AdhanLogEntry(
                        prayerId = id,
                        prayerName = nameArabic,
                        status = AdhanScheduleStatus.FAILED,
                        message = "صيغة توقيت الصلاة غير صالحة ($timeStr)"
                    )
                )
                continue
            }

            logs.add(
                AdhanLogEntry(
                    prayerId = id,
                    prayerName = nameArabic,
                    status = AdhanScheduleStatus.SCHEDULED,
                    targetTimeMillis = targetMillis,
                    message = if (hasExactAlarmPermission) "تم الجدولة بدقة" else "تم الجدولة بنمط تقريبي (إذن Exact Alarm غير ممنوح)"
                )
            )
        }

        return logs
    }

    fun scheduleAdhanForDay(
        context: Context,
        schedule: PrayerSchedule
    ): AdhanScheduleReport {
        val prefs = AdhanPreferences(context)
        val hasNotifPerm = hasNotificationPermission(context)
        val hasExactPerm = hasExactAlarmPermission(context)

        val plan = planAlarms(
            schedule = schedule,
            isGlobalEnabled = prefs.isAdhanEnabledGlobal,
            isPrayerEnabled = { prefs.isPrayerEnabled(it) },
            hasNotificationPermission = hasNotifPerm,
            hasExactAlarmPermission = hasExactPerm
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager not available")
            return AdhanScheduleReport(
                isGlobalEnabled = prefs.isAdhanEnabledGlobal,
                hasNotificationPermission = hasNotifPerm,
                hasExactAlarmPermission = hasExactPerm,
                logs = plan.map { it.copy(status = AdhanScheduleStatus.FAILED, message = "AlarmManager غير متوفر") }
            )
        }

        val finalLogs = mutableListOf<AdhanLogEntry>()

        for (entry in plan) {
            when (entry.status) {
                AdhanScheduleStatus.CANCELLED, AdhanScheduleStatus.SKIPPED -> {
                    cancelSingleAlarm(context, alarmManager, entry.prayerId)
                    finalLogs.add(entry)
                }
                AdhanScheduleStatus.SCHEDULED -> {
                    val success = setSingleAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        prayerId = entry.prayerId,
                        prayerName = entry.prayerName,
                        cityName = schedule.cityName,
                        audioUrl = prefs.selectedMuezzinUrl,
                        targetTimeMillis = entry.targetTimeMillis,
                        hasExactAlarmPermission = hasExactPerm
                    )
                    if (success) {
                        finalLogs.add(entry)
                    } else {
                        finalLogs.add(entry.copy(status = AdhanScheduleStatus.FAILED, message = "فشل ضبط المنبه في النظام"))
                    }
                }
                else -> {
                    finalLogs.add(entry)
                }
            }
        }

        return AdhanScheduleReport(
            isGlobalEnabled = prefs.isAdhanEnabledGlobal,
            hasNotificationPermission = hasNotifPerm,
            hasExactAlarmPermission = hasExactPerm,
            logs = finalLogs
        )
    }

    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val prayers = listOf("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha")
        for (id in prayers) {
            cancelSingleAlarm(context, alarmManager, id)
        }
    }

    private fun cancelSingleAlarm(context: Context, alarmManager: AlarmManager, prayerId: String) {
        val requestCode = getPrayerRequestCode(prayerId)
        val intent = Intent(context, AdhanReceiver::class.java).apply {
            action = ACTION_TRIGGER_ADHAN
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun setSingleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        prayerId: String,
        prayerName: String,
        cityName: String,
        audioUrl: String,
        targetTimeMillis: Long,
        hasExactAlarmPermission: Boolean
    ): Boolean {
        return try {
            cancelSingleAlarm(context, alarmManager, prayerId)

            val requestCode = getPrayerRequestCode(prayerId)
            val intent = Intent(context, AdhanReceiver::class.java).apply {
                action = ACTION_TRIGGER_ADHAN
                putExtra(EXTRA_PRAYER_ID, prayerId)
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_CITY_NAME, cityName)
                putExtra(EXTRA_AUDIO_URL, audioUrl)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && hasExactAlarmPermission) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTimeMillis, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTimeMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, targetTimeMillis, pendingIntent)
            }
            Log.d(TAG, "Scheduled alarm for $prayerId ($prayerName) at $targetTimeMillis")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for $prayerId: ${e.message}")
            false
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun hasExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true
        }
    }

    private fun getTargetTimeMillis(time24: String, currentTimeMillis: Long): Long {
        val clean = PrayerSchedule.cleanTime(time24)
        val parts = clean.split(":")
        if (parts.size < 2) return 0L
        val hour = parts[0].toIntOrNull() ?: return 0L
        val minute = parts[1].toIntOrNull() ?: return 0L

        val cal = Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If prayer time today has already passed, schedule for tomorrow
        if (cal.timeInMillis <= currentTimeMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return cal.timeInMillis
    }

    private fun getArabicPrayerName(prayerId: String): String {
        return when (prayerId.lowercase()) {
            "fajr" -> "الفجر"
            "sunrise" -> "الشروق"
            "dhuhr" -> "الظهر"
            "asr" -> "العصر"
            "maghrib" -> "المغرب"
            "isha" -> "العشاء"
            else -> prayerId
        }
    }
}
