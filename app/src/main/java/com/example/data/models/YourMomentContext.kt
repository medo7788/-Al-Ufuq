package com.example.data.models

data class YourMomentContext(
    val titleArabic: String,
    val subtitleArabic: String,
    val actionLabelArabic: String,
    val targetDestination: MomentTarget,
    val badgeTagArabic: String = "لحظتك"
)

enum class MomentTarget {
    PRAYER,
    QURAN_CONTINUE,
    MORNING_ADHKAR,
    EVENING_ADHKAR,
    TASBEEH,
    QIBLA
}
