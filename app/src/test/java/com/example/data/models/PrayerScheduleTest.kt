package com.example.data.models

import com.example.data.api.AladhanData
import com.example.data.api.AladhanDate
import com.example.data.api.AladhanTimings
import com.example.data.api.GregorianDateData
import com.example.data.api.GregorianWeekday
import com.example.data.api.HijriDateData
import com.example.data.api.HijriMonth
import com.example.data.local.PrayerTimeEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class PrayerScheduleTest {

    @Test
    fun scheduleCreation_fromApi_hasCorrectTimesAndSource() {
        val mockData = AladhanData(
            timings = AladhanTimings(
                Fajr = "04:30 (EET)",
                Sunrise = "06:00 (EET)",
                Dhuhr = "12:15 (EET)",
                Asr = "15:45 (EET)",
                Maghrib = "18:30 (EET)",
                Isha = "20:00 (EET)"
            ),
            date = AladhanDate(
                readable = "11 Aug 2026",
                hijri = HijriDateData(
                    day = "23",
                    month = HijriMonth(2, "Safar", "صفر"),
                    year = "1448"
                ),
                gregorian = GregorianDateData(
                    date = "11-08-2026",
                    weekday = GregorianWeekday("Tuesday")
                )
            )
        )

        val schedule = PrayerSchedule.fromApiData(mockData, "القاهرة")

        assertEquals(PrayerScheduleSource.LIVE_API, schedule.source)
        assertEquals("04:30", schedule.fajr)
        assertEquals("06:00", schedule.sunrise)
        assertEquals("12:15", schedule.dhuhr)
        assertEquals("15:45", schedule.asr)
        assertEquals("18:30", schedule.maghrib)
        assertEquals("20:00", schedule.isha)
        assertEquals("القاهرة", schedule.cityName)
        assertEquals("23 صفر 1448 هـ", schedule.hijriDateFormatted)
    }

    @Test
    fun scheduleCreation_fromEntity_hasCorrectTimesAndSource() {
        val entity = PrayerTimeEntity(
            dateStr = "11-08-2026",
            cityName = "الإسكندرية",
            hijriDateFormatted = "23 صفر 1448 هـ",
            fajr = "04:35",
            sunrise = "06:05",
            dhuhr = "12:20",
            asr = "15:50",
            maghrib = "18:35",
            isha = "20:05"
        )

        val schedule = PrayerSchedule.fromEntity(entity)

        assertEquals(PrayerScheduleSource.ROOM_CACHE, schedule.source)
        assertEquals("04:35", schedule.fajr)
        assertEquals("الإسكندرية", schedule.cityName)
        assertEquals("11-08-2026", schedule.dateStr)
    }

    @Test
    fun scheduleCreation_fallback_isMarkedAsFallback() {
        val schedule = PrayerSchedule.fallback("القاهرة")

        assertEquals(PrayerScheduleSource.FALLBACK, schedule.source)
        assertEquals("04:52", schedule.fajr)
        assertEquals("19:44", schedule.isha)
    }

    @Test
    fun nextPrayerSelection_beforeFajr_selectsFajrToday() {
        val schedule = PrayerSchedule.fallback("القاهرة")
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val dayState = schedule.evaluateDayState(cal.timeInMillis)

        assertEquals("الفجر", dayState.nextPrayer.nameArabic)
        assertEquals("العشاء", dayState.currentPrayerName)
        assertFalse(dayState.nextPrayer.isCompleted)
    }

    @Test
    fun nextPrayerSelection_betweenFajrAndSunrise_selectsSunrise() {
        val schedule = PrayerSchedule.fallback("القاهرة")
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 5)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
        }

        val dayState = schedule.evaluateDayState(cal.timeInMillis)

        assertEquals("الشروق", dayState.nextPrayer.nameArabic)
        assertEquals("الفجر", dayState.currentPrayerName)
        assertTrue(dayState.prayers[0].isCompleted) // Fajr completed
        assertFalse(dayState.prayers[1].isCompleted) // Sunrise not completed
    }

    @Test
    fun nextPrayerSelection_betweenDhuhrAndAsr_selectsAsr() {
        val schedule = PrayerSchedule.fallback("القاهرة")
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val dayState = schedule.evaluateDayState(cal.timeInMillis)

        assertEquals("العصر", dayState.nextPrayer.nameArabic)
        assertEquals("الظهر", dayState.currentPrayerName)
        assertTrue(dayState.prayers[2].isCompleted) // Dhuhr completed
    }

    @Test
    fun nextPrayerSelection_afterIsha_selectsFajrNextDayBoundary() {
        val schedule = PrayerSchedule.fallback("القاهرة")
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val dayState = schedule.evaluateDayState(cal.timeInMillis)

        assertEquals("الفجر", dayState.nextPrayer.nameArabic)
        assertEquals("العشاء", dayState.currentPrayerName)
        assertTrue(dayState.prayers.all { it.isCompleted }) // All 6 today completed
    }

    @Test
    fun missingOrInvalidApiData_fallsBackGracefully() {
        val dirtyData = AladhanData(
            timings = AladhanTimings(
                Fajr = "",
                Sunrise = "06:00",
                Dhuhr = "invalid",
                Asr = "15:30",
                Maghrib = "18:00",
                Isha = "19:30"
            ),
            date = AladhanDate(
                readable = "",
                hijri = HijriDateData(month = HijriMonth()),
                gregorian = GregorianDateData()
            )
        )

        val schedule = PrayerSchedule.fromApiData(dirtyData, "القاهرة")
        val state = schedule.evaluateDayState()

        assertEquals("00:00", schedule.fajr)
        assertEquals(PrayerScheduleSource.LIVE_API, schedule.source)
        assertEquals(6, state.prayers.size)
    }
}
