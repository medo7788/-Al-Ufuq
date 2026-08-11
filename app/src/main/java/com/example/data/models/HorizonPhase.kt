package com.example.data.models

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class HorizonPhase(
    val titleArabic: String,
    val subtitleArabic: String,
    val startColor: Color,
    val endColor: Color,
    val accentColor: Color
) {
    FAJR(
        titleArabic = "الفجر",
        subtitleArabic = "سكون الفجر ونور البدايات",
        startColor = FajrVioletStart,
        endColor = FajrVioletEnd,
        accentColor = SoftGold
    ),
    SUNRISE(
        titleArabic = "الشروق",
        subtitleArabic = "إشراقة يوم جديد برحمة الله",
        startColor = SunriseGoldStart,
        endColor = SunriseGoldEnd,
        accentColor = SacredGold
    ),
    DHUHR(
        titleArabic = "الظهر",
        subtitleArabic = "سكون الظهيرة وتجديد النية",
        startColor = DhuhrAzureStart,
        endColor = DhuhrAzureEnd,
        accentColor = SacredGold
    ),
    ASR(
        titleArabic = "العصر",
        subtitleArabic = "دفء العصر والاقتراب من الختام",
        startColor = AsrWarmStart,
        endColor = AsrWarmEnd,
        accentColor = TerracottaSunset
    ),
    MAGHRIB(
        titleArabic = "المغرب",
        subtitleArabic = "حمرة الشفق ولحظات السكينة",
        startColor = MaghribCrimsonStart,
        endColor = MaghribCrimsonEnd,
        accentColor = TerracottaSunset
    ),
    ISHA(
        titleArabic = "العشاء",
        subtitleArabic = "هدوء الليل واستحضار العبادة",
        startColor = IshaDeepNavyStart,
        endColor = IshaDeepNavyEnd,
        accentColor = SacredGold
    );

    companion object {
        fun getCurrentPhase(hour: Int): HorizonPhase {
            return when (hour) {
                in 4..5 -> FAJR
                in 6..11 -> SUNRISE
                in 12..14 -> DHUHR
                in 15..17 -> ASR
                in 18..19 -> MAGHRIB
                else -> ISHA
            }
        }
    }
}
