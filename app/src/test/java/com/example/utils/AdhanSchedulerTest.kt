package com.example.utils

import com.example.data.models.PrayerSchedule
import com.example.data.models.PrayerScheduleSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AdhanSchedulerTest {

    private val sampleSchedule = PrayerSchedule(
        dateStr = "11-08-2026",
        cityName = "القاهرة",
        hijriDateFormatted = "23 صفر 1448 هـ",
        gregorianDateFormatted = "الثلاثاء 11 أغسطس 2026",
        fajr = "04:30",
        sunrise = "06:00",
        dhuhr = "12:15",
        asr = "15:45",
        maghrib = "18:30",
        isha = "20:00",
        source = PrayerScheduleSource.LIVE_API
    )

    private fun getTimeMillis(hour: Int, minute: Int, second: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Test
    fun planAlarms_whenGlobalDisabled_returnsAllCancelledLogs() {
        val nowMillis = getTimeMillis(10, 0, 0)

        val plan = AdhanScheduler.planAlarms(
            schedule = sampleSchedule,
            isGlobalEnabled = false,
            isPrayerEnabled = { true },
            hasNotificationPermission = true,
            hasExactAlarmPermission = true,
            currentTimeMillis = nowMillis
        )

        assertEquals(6, plan.size)
        assertTrue(plan.all { it.status == AdhanScheduleStatus.CANCELLED })
    }

    @Test
    fun planAlarms_whenNotificationPermissionMissing_returnsPermissionMissingLog() {
        val nowMillis = getTimeMillis(10, 0, 0)

        val plan = AdhanScheduler.planAlarms(
            schedule = sampleSchedule,
            isGlobalEnabled = true,
            isPrayerEnabled = { true },
            hasNotificationPermission = false,
            hasExactAlarmPermission = true,
            currentTimeMillis = nowMillis
        )

        assertEquals(1, plan.size)
        assertEquals(AdhanScheduleStatus.PERMISSION_MISSING, plan[0].status)
    }

    @Test
    fun planAlarms_whenSinglePrayerDisabled_skipsThatPrayer() {
        val nowMillis = getTimeMillis(10, 0, 0)

        val plan = AdhanScheduler.planAlarms(
            schedule = sampleSchedule,
            isGlobalEnabled = true,
            isPrayerEnabled = { prayerId -> prayerId != "asr" },
            hasNotificationPermission = true,
            hasExactAlarmPermission = true,
            currentTimeMillis = nowMillis
        )

        assertEquals(6, plan.size)
        val asrLog = plan.find { it.prayerId == "asr" }
        assertEquals(AdhanScheduleStatus.SKIPPED, asrLog?.status)

        val dhuhrLog = plan.find { it.prayerId == "dhuhr" }
        assertEquals(AdhanScheduleStatus.SCHEDULED, dhuhrLog?.status)
    }

    @Test
    fun planAlarms_whenExactAlarmPermissionMissing_schedulesInApproximateMode() {
        val nowMillis = getTimeMillis(10, 0, 0)

        val plan = AdhanScheduler.planAlarms(
            schedule = sampleSchedule,
            isGlobalEnabled = true,
            isPrayerEnabled = { true },
            hasNotificationPermission = true,
            hasExactAlarmPermission = false,
            currentTimeMillis = nowMillis
        )

        assertEquals(6, plan.size)
        assertTrue(plan.all { it.status == AdhanScheduleStatus.SCHEDULED })
        assertTrue(plan[0].message.contains("تقريبي"))
    }

    @Test
    fun planAlarms_whenPrayerTimeInPastToday_schedulesForTomorrow() {
        // Current time is 13:00 (after Dhuhr 12:15)
        val nowMillis = getTimeMillis(13, 0, 0)

        val plan = AdhanScheduler.planAlarms(
            schedule = sampleSchedule,
            isGlobalEnabled = true,
            isPrayerEnabled = { true },
            hasNotificationPermission = true,
            hasExactAlarmPermission = true,
            currentTimeMillis = nowMillis
        )

        val dhuhrLog = plan.find { it.prayerId == "dhuhr" }!!
        assertEquals(AdhanScheduleStatus.SCHEDULED, dhuhrLog.status)
        // Verify target time is in the future (> nowMillis)
        assertTrue(dhuhrLog.targetTimeMillis > nowMillis)
    }

    @Test
    fun planAlarms_malformedSchedule_logsFailedStatusSafely() {
        val malformed = PrayerSchedule(
            dateStr = "",
            cityName = "القاهرة",
            hijriDateFormatted = "",
            gregorianDateFormatted = "",
            fajr = "invalid",
            sunrise = "",
            dhuhr = "12:15",
            asr = "15:45",
            maghrib = "18:30",
            isha = "20:00",
            source = PrayerScheduleSource.FALLBACK
        )

        val nowMillis = getTimeMillis(10, 0, 0)

        val plan = AdhanScheduler.planAlarms(
            schedule = malformed,
            isGlobalEnabled = true,
            isPrayerEnabled = { true },
            hasNotificationPermission = true,
            hasExactAlarmPermission = true,
            currentTimeMillis = nowMillis
        )

        val fajrLog = plan.find { it.prayerId == "fajr" }
        assertEquals(AdhanScheduleStatus.FAILED, fajrLog?.status)

        val dhuhrLog = plan.find { it.prayerId == "dhuhr" }
        assertEquals(AdhanScheduleStatus.SCHEDULED, dhuhrLog?.status)
    }
}
