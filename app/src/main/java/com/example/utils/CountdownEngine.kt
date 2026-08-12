package com.example.utils

import com.example.data.models.PrayerDayState
import com.example.data.models.PrayerSchedule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dedicated real-time CountdownEngine for AL-UFUQ.
 * Evaluates countdowns dynamically using the canonical PrayerSchedule and current device time.
 * Does NOT re-fetch or re-calculate PrayerSchedules every second.
 */
object CountdownEngine {

    /**
     * Evaluates the active PrayerDayState for a given canonical PrayerSchedule and timestamp.
     */
    fun evaluate(
        schedule: PrayerSchedule,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): PrayerDayState {
        return schedule.evaluateDayState(currentTimeMillis)
    }

    /**
     * Determines whether the given schedule's date string is outdated compared to current device date.
     */
    fun isScheduleDateOutdated(
        schedule: PrayerSchedule,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (schedule.dateStr.isBlank()) return false
        val currentDateStr = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(currentTimeMillis))
        return schedule.dateStr != currentDateStr
    }

    /**
     * Ticker flow that emits updated PrayerDayState every second based on device time.
     */
    fun startTicker(
        getSchedule: () -> PrayerSchedule,
        onDayChanged: (() -> Unit)? = null,
        intervalMillis: Long = 1000L
    ): Flow<PrayerDayState> = flow {
        var lastCheckedDate = ""
        while (true) {
            val now = System.currentTimeMillis()
            val currentSchedule = getSchedule()

            val currentDateStr = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date(now))
            if (lastCheckedDate.isNotEmpty() && currentDateStr != lastCheckedDate) {
                onDayChanged?.invoke()
            }
            lastCheckedDate = currentDateStr

            emit(evaluate(currentSchedule, now))
            delay(intervalMillis)
        }
    }
}
