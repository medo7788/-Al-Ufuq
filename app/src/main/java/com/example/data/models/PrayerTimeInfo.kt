package com.example.data.models

data class PrayerTimeInfo(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val timeFormatted: String, // e.g. "04:37 م"
    val timestampMillis: Long,
    val isNext: Boolean = false,
    val isCompleted: Boolean = false,
    val notificationEnabled: Boolean = true
)

data class PrayerDayState(
    val currentPrayerName: String,
    val nextPrayer: PrayerTimeInfo,
    val remainingTimeString: String, // e.g. "01:45:22"
    val intervalProgressPercentage: Float, // e.g. 0.45f (45%)
    val prayers: List<PrayerTimeInfo>,
    val hijriDateString: String, // e.g. "23 صفار 1448 هـ"
    val gregorianDateString: String // e.g. "الثلاثاء 11 أغسطس 2026"
)
