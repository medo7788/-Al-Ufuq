package com.example.data.models

import com.example.data.api.AladhanData
import com.example.data.local.PrayerTimeEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class PrayerScheduleSource {
    LIVE_API,
    ROOM_CACHE,
    FALLBACK
}

data class PrayerSchedule(
    val dateStr: String, // "dd-MM-yyyy"
    val cityName: String,
    val hijriDateFormatted: String,
    val gregorianDateFormatted: String,
    val fajr: String,    // 24-hour "HH:mm"
    val sunrise: String, // 24-hour "HH:mm"
    val dhuhr: String,   // 24-hour "HH:mm"
    val asr: String,     // 24-hour "HH:mm"
    val maghrib: String, // 24-hour "HH:mm"
    val isha: String,    // 24-hour "HH:mm"
    val source: PrayerScheduleSource = PrayerScheduleSource.LIVE_API
) {
    /**
     * Evaluates the active state (next prayer, remaining countdown, completed flags, etc.)
     * for a given timestamp.
     */
    fun evaluateDayState(currentTimeMillis: Long = System.currentTimeMillis()): PrayerDayState {
        val cal = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        val curHour = cal.get(Calendar.HOUR_OF_DAY)
        val curMin = cal.get(Calendar.MINUTE)
        val curSec = cal.get(Calendar.SECOND)
        val curTotalSecs = curHour * 3600 + curMin * 60 + curSec

        val fajrSec = parseHHmmToSeconds(fajr)
        val sunriseSec = parseHHmmToSeconds(sunrise)
        val dhuhrSec = parseHHmmToSeconds(dhuhr)
        val asrSec = parseHHmmToSeconds(asr)
        val maghribSec = parseHHmmToSeconds(maghrib)
        val ishaSec = parseHHmmToSeconds(isha)

        val timesList = listOf(
            Triple("fajr", "الفجر" to "Fajr", fajr to fajrSec),
            Triple("sunrise", "الشروق" to "Sunrise", sunrise to sunriseSec),
            Triple("dhuhr", "الظهر" to "Dhuhr", dhuhr to dhuhrSec),
            Triple("asr", "العصر" to "Asr", asr to asrSec),
            Triple("maghrib", "المغرب" to "Maghrib", maghrib to maghribSec),
            Triple("isha", "العشاء" to "Isha", isha to ishaSec)
        )

        // Determine next prayer index (0 to 5)
        val nextIndex = when {
            curTotalSecs < fajrSec -> 0
            curTotalSecs < sunriseSec -> 1
            curTotalSecs < dhuhrSec -> 2
            curTotalSecs < asrSec -> 3
            curTotalSecs < maghribSec -> 4
            curTotalSecs < ishaSec -> 5
            else -> 0 // Rollover to tomorrow's Fajr
        }

        val isPastIsha = curTotalSecs >= ishaSec

        // Build list of PrayerTimeInfo items
        val prayerItems = timesList.mapIndexed { idx, item ->
            val isNext = (idx == nextIndex)
            val isCompleted = if (isPastIsha) true else idx < nextIndex
            val timeMillis = getTodayMillisForHHmm(item.third.first, currentTimeMillis)

            PrayerTimeInfo(
                id = item.first,
                nameArabic = item.second.first,
                nameEnglish = item.second.second,
                timeFormatted = formatAmPm(item.third.first),
                timestampMillis = timeMillis,
                isNext = isNext,
                isCompleted = isCompleted
            )
        }

        // Current prayer name calculation
        val currentPrayerName = when {
            curTotalSecs < fajrSec -> "العشاء"
            curTotalSecs < sunriseSec -> "الفجر"
            curTotalSecs < dhuhrSec -> "الشروق"
            curTotalSecs < asrSec -> "الظهر"
            curTotalSecs < maghribSec -> "العصر"
            curTotalSecs < ishaSec -> "المغرب"
            else -> "العشاء"
        }

        // Remaining countdown calculation
        val targetSec = if (isPastIsha) {
            fajrSec + 86400
        } else {
            timesList[nextIndex].third.second
        }

        val diffSec = (targetSec - curTotalSecs).coerceAtLeast(0)
        val remH = diffSec / 3600
        val remM = (diffSec % 3600) / 60
        val remS = diffSec % 60
        val remainingFormatted = String.format(Locale.US, "%02d:%02d:%02d", remH, remM, remS)

        // Progress interval percentage calculation
        val prevSec = when {
            nextIndex == 0 -> if (isPastIsha) ishaSec else 0
            else -> timesList[nextIndex - 1].third.second
        }
        val totalIntervalSec = (targetSec - prevSec).coerceAtLeast(1)
        val elapsedSec = (curTotalSecs - prevSec).coerceAtLeast(0)
        val progress = (elapsedSec.toFloat() / totalIntervalSec.toFloat()).coerceIn(0f, 1f)

        return PrayerDayState(
            currentPrayerName = currentPrayerName,
            nextPrayer = prayerItems[nextIndex],
            remainingTimeString = remainingFormatted,
            intervalProgressPercentage = progress,
            prayers = prayerItems,
            hijriDateString = hijriDateFormatted,
            gregorianDateString = gregorianDateFormatted
        )
    }

    companion object {
        fun cleanTime(raw: String?): String {
            if (raw.isNullOrBlank()) return "00:00"
            return raw.trim().split(" ")[0] // remove timezone text like (EET)
        }

        fun parseHHmmToSeconds(timeStr: String): Int {
            val parts = cleanTime(timeStr).split(":")
            if (parts.size < 2) return 0
            val h = parts[0].toIntOrNull() ?: 0
            val m = parts[1].toIntOrNull() ?: 0
            return h * 3600 + m * 60
        }

        fun formatAmPm(time24: String): String {
            val clean = cleanTime(time24)
            val parts = clean.split(":")
            if (parts.size < 2) return time24
            val h = parts[0].toIntOrNull() ?: 0
            val m = parts[1].toIntOrNull() ?: 0
            val ampm = if (h >= 12) "م" else "ص"
            val h12 = if (h % 12 == 0) 12 else h % 12
            return String.format(Locale.US, "%02d:%02d %s", h12, m, ampm)
        }

        private fun getTodayMillisForHHmm(time24: String, currentTimeMillis: Long): Long {
            val parts = cleanTime(time24).split(":")
            if (parts.size < 2) return currentTimeMillis
            val h = parts[0].toIntOrNull() ?: 0
            val m = parts[1].toIntOrNull() ?: 0
            val cal = Calendar.getInstance().apply {
                timeInMillis = currentTimeMillis
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

        fun fromApiData(
            data: AladhanData,
            cityName: String,
            fallbackDateStr: String = ""
        ): PrayerSchedule {
            val timings = data.timings
            val date = data.date
            val hijri = date?.hijri
            val hijriMonthAr = hijri?.month?.ar ?: ""
            val hijriStr = if (hijri != null) "${hijri.day} $hijriMonthAr ${hijri.year} هـ" else "١٦ صفر ١٤٤٨ هـ"
            val gregorianStr = if (date?.gregorian != null) {
                "${date.gregorian.weekday?.en ?: ""} ${date.readable ?: ""}".trim()
            } else ""

            return PrayerSchedule(
                dateStr = date?.gregorian?.date ?: fallbackDateStr,
                cityName = cityName,
                hijriDateFormatted = hijriStr,
                gregorianDateFormatted = gregorianStr,
                fajr = cleanTime(timings.Fajr),
                sunrise = cleanTime(timings.Sunrise),
                dhuhr = cleanTime(timings.Dhuhr),
                asr = cleanTime(timings.Asr),
                maghrib = cleanTime(timings.Maghrib),
                isha = cleanTime(timings.Isha),
                source = PrayerScheduleSource.LIVE_API
            )
        }

        fun fromEntity(
            entity: PrayerTimeEntity
        ): PrayerSchedule {
            return PrayerSchedule(
                dateStr = entity.dateStr,
                cityName = entity.cityName,
                hijriDateFormatted = entity.hijriDateFormatted,
                gregorianDateFormatted = entity.dateStr,
                fajr = cleanTime(entity.fajr),
                sunrise = cleanTime(entity.sunrise),
                dhuhr = cleanTime(entity.dhuhr),
                asr = cleanTime(entity.asr),
                maghrib = cleanTime(entity.maghrib),
                isha = cleanTime(entity.isha),
                source = PrayerScheduleSource.ROOM_CACHE
            )
        }

        fun fallback(
            cityName: String = "القاهرة",
            dateStr: String = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        ): PrayerSchedule {
            val currentGregorian = SimpleDateFormat("EEEE d MMMM yyyy", Locale("ar")).format(Date())
            return PrayerSchedule(
                dateStr = dateStr,
                cityName = cityName,
                hijriDateFormatted = "١٦ صفر ١٤٤٨ هـ",
                gregorianDateFormatted = currentGregorian,
                fajr = "04:52",
                sunrise = "06:18",
                dhuhr = "12:31",
                asr = "15:58",
                maghrib = "18:14",
                isha = "19:44",
                source = PrayerScheduleSource.FALLBACK
            )
        }
    }
}
